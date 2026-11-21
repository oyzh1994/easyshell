package cn.oyzh.easyshell.llm.skill;

import cn.oyzh.common.system.OSUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonArraySchema;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonStringSchema;
import dev.langchain4j.service.tool.ToolExecutor;
import dev.langchain4j.skills.FileSystemSkill;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Skill 激活后可调用的受控脚本执行工具。
 *
 * @author oyzh
 * @since 2026-11-21
 */
final class LLMSafeSkillScriptTool {

    private static final int MAX_ARGUMENTS = 64;

    private static final int MAX_ARGUMENT_LENGTH = 4096;

    private static final int MAX_OUTPUT_CHARS = 64 * 1024;

    private static final long DEFAULT_TIMEOUT_SECONDS = 60;

    private static final long MAX_TIMEOUT_SECONDS = 300;

    private static final String SUPPORTED_EXTENSIONS = ".py, .pyw, .js, .mjs, .cjs, .ts, .tsx, "
            + ".sh, .bash, .zsh, .command, .ps1, .bat, .cmd, .rb, .pl, .php, .lua, "
            + ".R, .tcl, .groovy, .java, .kts, .fsx, .exs, .jl, .awk";

    private LLMSafeSkillScriptTool() {
    }

    static SafeScriptTool create(FileSystemSkill skill) {
        String toolName = toolName(skill.name());
        ToolSpecification specification = ToolSpecification.builder()
                .name(toolName)
                .description("Run a script from skill '" + skill.name()
                        + "'. The script path must be relative to the skill root. "
                        + "Supported extensions: " + SUPPORTED_EXTENSIONS + ".")
                .parameters(JsonObjectSchema.builder()
                        .addProperty("script_path", JsonStringSchema.builder()
                                .description("Script path relative to the skill root")
                                .build())
                        .addProperty("arguments", JsonArraySchema.builder()
                                .description("Optional script arguments passed without shell interpolation")
                                .items(JsonStringSchema.builder().build())
                                .build())
                        .addIntegerProperty("timeout_seconds",
                                "Optional timeout in seconds, default 60, maximum 300")
                        .required("script_path")
                        .additionalProperties(false)
                        .build())
                .build();
        ToolExecutor executor = new Executor(skill);
        return new SafeScriptTool(toolName, specification, executor);
    }

    private static String toolName(String skillName) {
        String normalized = skillName == null
                ? "skill"
                : skillName.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_");
        normalized = normalized.replaceAll("^_+|_+$", "");
        if (normalized.isEmpty()) {
            normalized = "skill";
        }
        if (normalized.length() > 28) {
            normalized = normalized.substring(0, 28);
        }
        String hash = Integer.toUnsignedString(
                String.valueOf(skillName).hashCode(), 36);
        return "run_skill_script_" + normalized + "_" + hash;
    }

    record SafeScriptTool(String name,
                          ToolSpecification specification,
                          ToolExecutor executor) {
    }

    private static final class Executor implements ToolExecutor {

        private final FileSystemSkill skill;

        private Executor(FileSystemSkill skill) {
            this.skill = skill;
        }

        @Override
        public String execute(ToolExecutionRequest request, Object memoryId) {
            try {
                return this.run(request.arguments());
            } catch (Exception ex) {
                throw new IllegalArgumentException(
                        "Skill script execution failed: " + ex.getMessage(), ex);
            }
        }

        private String run(String argumentJson) throws Exception {
            JSONObject arguments = JSON.parseObject(
                    argumentJson == null || argumentJson.isBlank() ? "{}" : argumentJson);
            String relative = arguments.getString("script_path");
            if (relative == null || relative.isBlank()) {
                throw new IllegalArgumentException("script_path is required");
            }
            Path skillRoot = this.skill.basePath().toRealPath();
            Path script = skillRoot.resolve(relative).normalize().toRealPath();
            if (!script.startsWith(skillRoot) || !Files.isRegularFile(script)) {
                throw new IllegalArgumentException(
                        "Script must be a file inside the skill root");
            }
            List<String> command = this.command(script);
            for (String argument : this.arguments(arguments)) {
                if (argument.length() > MAX_ARGUMENT_LENGTH) {
                    throw new IllegalArgumentException("Script argument is too long");
                }
                command.add(argument);
            }

            ProcessBuilder builder = new ProcessBuilder(command)
                    .directory(skillRoot.toFile())
                    .redirectErrorStream(false);
            builder.environment().put("EASYSHELL_SKILL_NAME", this.skill.name());
            builder.environment().put("EASYSHELL_SKILL_ROOT", skillRoot.toString());
            Process process = builder.start();
            process.getOutputStream().close();
            LimitedCollector stdout = new LimitedCollector();
            LimitedCollector stderr = new LimitedCollector();
            Thread outReader = startReader(process.getInputStream(), stdout);
            Thread errReader = startReader(process.getErrorStream(), stderr);
            boolean timedOut = false;
            try {
                long timeout = this.timeoutSeconds(arguments);
                if (!process.waitFor(timeout, TimeUnit.SECONDS)) {
                    timedOut = true;
                    destroy(process);
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                destroy(process);
                throw ex;
            } finally {
                outReader.join(2000);
                errReader.join(2000);
            }
            int exitCode = process.isAlive() ? -1 : process.exitValue();
            return "skill: " + this.skill.name()
                    + "\nscript: " + relative
                    + "\nexitCode: " + exitCode
                    + (timedOut ? "\ntimedOut: true" : "")
                    + "\nSTDOUT:\n" + stdout.text()
                    + "\nSTDERR:\n" + stderr.text();
        }

        private List<String> command(Path script) {
            String fileName = script.getFileName().toString().toLowerCase(Locale.ROOT);
            String absolute = script.toString();
            if (fileName.endsWith(".py") || fileName.endsWith(".pyw")) {
                return new ArrayList<>(List.of(OSUtil.isWindows() ? "python" : "python3", absolute));
            }
            if (fileName.endsWith(".js") || fileName.endsWith(".mjs")
                    || fileName.endsWith(".cjs")) {
                return new ArrayList<>(List.of("node", absolute));
            }
            if (fileName.endsWith(".ts") || fileName.endsWith(".tsx")) {
                return new ArrayList<>(List.of("tsx", absolute));
            }
            if (fileName.endsWith(".sh") || fileName.endsWith(".command")) {
                return new ArrayList<>(List.of(
                        OSUtil.isWindows() ? "bash" : "/bin/sh", absolute));
            }
            if (fileName.endsWith(".bash")) {
                return new ArrayList<>(List.of("bash", absolute));
            }
            if (fileName.endsWith(".zsh")) {
                return new ArrayList<>(List.of("zsh", absolute));
            }
            if (fileName.endsWith(".ps1")) {
                return new ArrayList<>(List.of(
                        OSUtil.isWindows() ? "powershell.exe" : "pwsh",
                        "-NoProfile", "-ExecutionPolicy", "Bypass", "-File", absolute));
            }
            if (fileName.endsWith(".bat") || fileName.endsWith(".cmd")) {
                return new ArrayList<>(List.of("cmd.exe", "/d", "/c", absolute));
            }
            if (fileName.endsWith(".rb")) {
                return new ArrayList<>(List.of("ruby", absolute));
            }
            if (fileName.endsWith(".pl")) {
                return new ArrayList<>(List.of("perl", absolute));
            }
            if (fileName.endsWith(".php")) {
                return new ArrayList<>(List.of("php", absolute));
            }
            if (fileName.endsWith(".lua")) {
                return new ArrayList<>(List.of("lua", absolute));
            }
            if (fileName.endsWith(".r")) {
                return new ArrayList<>(List.of("Rscript", absolute));
            }
            if (fileName.endsWith(".tcl")) {
                return new ArrayList<>(List.of("tclsh", absolute));
            }
            if (fileName.endsWith(".groovy")) {
                return new ArrayList<>(List.of("groovy", absolute));
            }
            if (fileName.endsWith(".java")) {
                return new ArrayList<>(List.of("java", absolute));
            }
            if (fileName.endsWith(".kts")) {
                return new ArrayList<>(List.of("kotlin", absolute));
            }
            if (fileName.endsWith(".fsx")) {
                return new ArrayList<>(List.of("dotnet", "fsi", absolute));
            }
            if (fileName.endsWith(".exs")) {
                return new ArrayList<>(List.of("elixir", absolute));
            }
            if (fileName.endsWith(".jl")) {
                return new ArrayList<>(List.of("julia", absolute));
            }
            if (fileName.endsWith(".awk")) {
                return new ArrayList<>(List.of("awk", "-f", absolute));
            }
            throw new IllegalArgumentException("Unsupported skill script type: " + fileName);
        }

        private List<String> arguments(JSONObject root) {
            JSONArray array = root.getJSONArray("arguments");
            if (array == null || array.isEmpty()) {
                return List.of();
            }
            if (array.size() > MAX_ARGUMENTS) {
                throw new IllegalArgumentException("Too many script arguments");
            }
            List<String> arguments = new ArrayList<>(array.size());
            for (Object value : array) {
                arguments.add(value == null ? "" : value.toString());
            }
            return arguments;
        }

        private long timeoutSeconds(JSONObject arguments) {
            Integer value = arguments.getInteger("timeout_seconds");
            if (value == null || value <= 0) {
                return DEFAULT_TIMEOUT_SECONDS;
            }
            return Math.min(value, MAX_TIMEOUT_SECONDS);
        }

        private static Thread startReader(InputStream stream, LimitedCollector collector) {
            return Thread.ofVirtual().start(() -> {
                try (Reader reader = new InputStreamReader(stream, Charset.defaultCharset())) {
                    char[] buffer = new char[4096];
                    int read;
                    while ((read = reader.read(buffer)) != -1) {
                        collector.append(buffer, read);
                    }
                } catch (IOException ignored) {
                }
            });
        }

        private static void destroy(Process process) {
            process.descendants().forEach(ProcessHandle::destroyForcibly);
            process.destroyForcibly();
        }
    }

    private static final class LimitedCollector {

        private final StringBuilder text = new StringBuilder();

        private boolean truncated;

        private synchronized void append(char[] chars, int length) {
            int remaining = MAX_OUTPUT_CHARS - this.text.length();
            if (remaining > 0) {
                this.text.append(chars, 0, Math.min(remaining, length));
            }
            if (length > remaining) {
                this.truncated = true;
            }
        }

        private synchronized String text() {
            return this.truncated
                    ? this.text + "\n...[truncated]"
                    : this.text.toString();
        }
    }
}
