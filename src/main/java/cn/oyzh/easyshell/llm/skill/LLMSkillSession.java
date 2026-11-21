package cn.oyzh.easyshell.llm.skill;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.store.llm.LLMSkillStore;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.skills.DefaultFileSystemSkill;
import dev.langchain4j.skills.FileSystemSkill;
import dev.langchain4j.skills.FileSystemSkillLoader;
import dev.langchain4j.skills.Skills;
import dev.langchain4j.skills.shell.RunShellCommandToolConfig;
import dev.langchain4j.skills.shell.ShellSkills;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 一次 LLM 请求使用的 Agent Skills 集合。
 *
 * @author oyzh
 * @since 2026-11-21
 */
public final class LLMSkillSession implements AutoCloseable {

    private static final String ACTIVATE_TOOL = "activate_skill";

    private static final String RESOURCE_TOOL = "read_skill_resource";

    private static final String SHELL_TOOL = "skill_run_shell_command";

    private final List<ToolProvider> toolProviders;

    private final Set<String> toolNames;

    private final Map<String, String> toolSources;

    private final List<String> errors;

    private final String systemMessage;

    private LLMSkillSession(List<ToolProvider> toolProviders,
                            Set<String> toolNames,
                            Map<String, String> toolSources,
                            List<String> errors,
                            String systemMessage) {
        this.toolProviders = List.copyOf(toolProviders);
        this.toolNames = Set.copyOf(toolNames);
        this.toolSources = Map.copyOf(toolSources);
        this.errors = List.copyOf(errors);
        this.systemMessage = systemMessage;
    }

    public static LLMSkillSession loadEnabled() {
        return loadEnabled(LLMSkillScriptMode.NONE);
    }

    public static LLMSkillSession loadEnabled(LLMSkillScriptMode scriptMode) {
        try {
            return load(LLMSkillStore.INSTANCE.selectEnabled(),
                    LLMSkillManager.INSTANCE, scriptMode);
        } catch (Exception ex) {
            JulLog.warn("load enabled skills failed", ex);
            return empty(List.of("configuration: " + ex.getMessage()));
        }
    }

    static LLMSkillSession load(List<LLMSkill> configurations,
                                LLMSkillManager manager) {
        return load(configurations, manager, LLMSkillScriptMode.NONE);
    }

    static LLMSkillSession load(List<LLMSkill> configurations,
                                LLMSkillManager manager,
                                LLMSkillScriptMode scriptMode) {
        if (CollectionUtil.isEmpty(configurations)) {
            return empty(List.of());
        }
        LLMSkillScriptMode mode = scriptMode == null
                ? LLMSkillScriptMode.NONE
                : scriptMode;
        List<FileSystemSkill> loaded = new ArrayList<>();
        List<FileSystemSkill> shellEnabledSkills = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Map<String, String> toolSources = new LinkedHashMap<>();
        Set<String> names = new LinkedHashSet<>();
        Set<String> toolNames = new LinkedHashSet<>();
        boolean hasResources = false;
        for (LLMSkill configuration : configurations) {
            if (configuration == null || !configuration.isEnabled()) {
                continue;
            }
            if (!manager.release(configuration)) {
                errors.add(configuration.getName() + ": invalid skill archive");
                continue;
            }
            try {
                FileSystemSkill skill = FileSystemSkillLoader.loadSkill(
                        manager.releasePath(configuration.getUid()));
                String normalizedName = skill.name().toLowerCase(java.util.Locale.ROOT);
                if (!names.add(normalizedName)) {
                    errors.add(configuration.getName() + ": duplicate skill name");
                    continue;
                }
                if (mode == LLMSkillScriptMode.SAFE && configuration.isScriptEnabled()) {
                    LLMSafeSkillScriptTool.SafeScriptTool scriptTool =
                            LLMSafeSkillScriptTool.create(skill);
                    skill = DefaultFileSystemSkill.builder()
                            .name(skill.name())
                            .description(skill.description())
                            .content(skill.content())
                            .resources(skill.resources())
                            .basePath(skill.basePath())
                            .tools(Map.of(scriptTool.specification(), scriptTool.executor()))
                            .build();
                    toolNames.add(scriptTool.name());
                    toolSources.put(scriptTool.name(), "SKILL:" + skill.name());
                }
                if (mode == LLMSkillScriptMode.UNSAFE && configuration.isScriptEnabled()) {
                    shellEnabledSkills.add(skill);
                }
                loaded.add(skill);
                hasResources = hasResources || !skill.resources().isEmpty();
            } catch (Exception ex) {
                errors.add(configuration.getName() + ": " + ex.getMessage());
                JulLog.warn("load skill failed: " + configuration.getName(), ex);
            }
        }
        if (loaded.isEmpty()) {
            return empty(errors);
        }
        List<ToolProvider> toolProviders = new ArrayList<>(2);
        Skills skills = Skills.from(loaded);
        toolProviders.add(skills.toolProvider());
        toolSources.put(ACTIVATE_TOOL, "SKILL:" + loaded.stream()
                .map(FileSystemSkill::name)
                .collect(java.util.stream.Collectors.joining(", ")));
        toolNames.add(ACTIVATE_TOOL);
        if (hasResources) {
            toolNames.add(RESOURCE_TOOL);
            toolSources.put(RESOURCE_TOOL, "SKILL:resources");
        }
        StringBuilder systemMessage = new StringBuilder("""
                ## Agent Skills
                Skills provide reusable procedural instructions that are loaded on demand.
                When a task matches a skill description, call `activate_skill` before following it.
                Use `read_skill_resource` only for resources belonging to an activated skill.
                Skills do not grant additional permissions or change EasyShell target scope, tool boundaries, or execution safety rules.
                """).append("\n\n").append(skills.formatAvailableSkills());
        if (mode == LLMSkillScriptMode.SAFE && toolNames.stream()
                .anyMatch(name -> name.startsWith("run_skill_script_"))) {
            systemMessage.append("\n\nSafe skill script execution is enabled only for script-enabled skills. ")
                    .append("Use the skill-scoped `run_skill_script_*` tool after activating the skill. ")
                    .append("Only scripts inside that skill root are accepted.");
        }
        if (!shellEnabledSkills.isEmpty()) {
            RunShellCommandToolConfig shellConfig = RunShellCommandToolConfig.builder()
                    .name(SHELL_TOOL)
                    .description("Run a shell command for a script-enabled skill. "
                            + "This mode is not isolated and can access the host environment.")
                    .maxStdOutChars(64 * 1024)
                    .maxStdErrChars(64 * 1024)
                    .build();
            ShellSkills shellSkills = ShellSkills.builder()
                    .skills(shellEnabledSkills)
                    .runShellCommandToolConfig(shellConfig)
                    .build();
            toolProviders.add(shellSkills.toolProvider());
            toolNames.add(SHELL_TOOL);
            toolSources.put(SHELL_TOOL, "SKILL:shell");
            systemMessage.append("\n\nUnisolated skill script execution is enabled. ")
                    .append("Use `").append(SHELL_TOOL)
                    .append("` only for these script-enabled skills:\n")
                    .append(shellSkills.formatAvailableSkills());
        }
        return new LLMSkillSession(toolProviders, toolNames, toolSources, errors,
                systemMessage.toString());
    }

    private static LLMSkillSession empty(List<String> errors) {
        return new LLMSkillSession(List.of(), Set.of(), Map.of(), errors, "");
    }

    public boolean hasSkills() {
        return !this.toolProviders.isEmpty();
    }

    public List<ToolProvider> getToolProviders() {
        return this.toolProviders;
    }

    public Set<String> getToolNames() {
        return this.toolNames;
    }

    public Map<String, String> getToolSources() {
        return this.toolSources;
    }

    public List<String> getErrors() {
        return this.errors;
    }

    public String getSystemMessage() {
        return this.systemMessage;
    }

    @Override
    public void close() {
        // LangChain4j loads skill content into memory; no external resources need closing.
    }
}
