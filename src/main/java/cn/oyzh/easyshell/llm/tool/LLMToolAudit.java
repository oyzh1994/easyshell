package cn.oyzh.easyshell.llm.tool;

import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.log.JulUtil;
import cn.oyzh.easyshell.ShellConst;
import cn.oyzh.easyshell.llm.LLMExecutionMode;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.invocation.InvocationContext;
import dev.langchain4j.service.tool.ToolExecution;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.function.Consumer;

/**
 * LLM工具调用审计记录。
 *
 * <p>每条记录以一行JSON追加写入日志目录，包含调用参数、返回结果、耗时及会话上下文。
 * 参数中的密码、令牌、密钥等敏感字段会被脱敏，过长的参数和结果会被截断。</p>
 *
 * @author oyzh
 * @since 2026-11-05
 */
public final class LLMToolAudit {

    /**
     * 审计日志文件名
     */
    public static final String FILE_NAME = "llm-tool-audit.jsonl";

    /**
     * 单条参数或结果保留的最大字符数
     */
    static final int MAX_TEXT_LENGTH = 64 * 1024;

    /**
     * JSONL追加写锁
     */
    private static final ReentrantLock WRITE_LOCK = new ReentrantLock();

    /**
     * 时间格式
     */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private static final Set<String> LOCAL_COMMAND_TOOLS = Set.of(
            "local_execute_command_direct",
            "local_execute_command_in_directory_direct",
            "execute_local_command",
            "execute_local_command_in_directory",
            "skill_run_shell_command");

    /**
     * 无法按JSON解析时使用的敏感字段匹配规则
     */
    private static final Pattern SENSITIVE_TEXT_PATTERN = Pattern.compile(
            "(?i)((?:password|passwd|pwd|secret|token|api[_-]?key|private[_-]?key|access[_-]?key|secret[_-]?key|credential|cookie|authorization|sk)\\s*[:=]\\s*)(\"(?:\\\\.|[^\"])*\"|'(?:\\\\.|[^'])*'|[^,}\\s]+)");

    private final Path auditFile;

    private final Map<String, String> toolClasses;

    private final Map<String, LLMToolScope> toolScopes;

    private final String sessionId;

    private final String sessionTabId;

    private String sessionTabType;

    private String connectionHost;

    private String connectionUser;

    private LLMToolAccessMode toolAccessMode;

    private final String provider;

    private final String model;

    private final LLMExecutionMode executionMode;

    /**
     * 工具执行完成回调
     */
    private volatile Consumer<ToolExecution> executionListener;

    public LLMToolAudit(Collection<?> tools,
                        String sessionId,
                        String sessionTabId,
                        String provider,
                        String model,
                        LLMExecutionMode executionMode) {
        this(defaultAuditFile(), tools, sessionId, sessionTabId, provider, model, executionMode);
    }

    public LLMToolAudit(Path auditFile,
                        Collection<?> tools,
                        String sessionId,
                        String sessionTabId,
                        String provider,
                        String model,
                        LLMExecutionMode executionMode) {
        this.auditFile = auditFile == null ? defaultAuditFile() : auditFile;
        this.toolClasses = resolveToolClasses(tools);
        this.toolScopes = resolveToolScopes(tools);
        this.sessionId = sessionId;
        this.sessionTabId = sessionTabId;
        this.provider = provider;
        this.model = model;
        this.executionMode = executionMode;
    }

    /**
     * 注册由工具提供器（例如 MCP）动态提供的工具来源。
     *
     * @param sources 工具名到来源的映射
     */
    public void registerToolSources(Map<String, String> sources) {
        this.registerToolSources(sources, LLMToolScope.MCP);
    }

    /**
     * 注册动态工具来源及作用域。
     *
     * @param sources 工具名到来源的映射
     * @param scope   工具作用域
     */
    public void registerToolSources(Map<String, String> sources, LLMToolScope scope) {
        if (sources != null) {
            this.toolClasses.putAll(sources);
            sources.keySet().forEach(name -> this.toolScopes.put(name, scope));
        }
    }

    /**
     * Registers non-sensitive context for the bound session tab.
     *
     * @param tabType tab protocol or type
     * @param host    target host
     * @param user    target user
     */
    public void registerSessionContext(String tabType, String host, String user) {
        this.sessionTabType = tabType;
        this.connectionHost = host;
        this.connectionUser = user;
    }

    /**
     * Registers the ordinary tool access mode. MCP remains an independent scope.
     *
     * @param accessMode tool access mode
     */
    public void registerToolAccessMode(LLMToolAccessMode accessMode) {
        this.toolAccessMode = accessMode;
    }

    /**
     * 获取默认审计日志文件
     *
     * @return 审计日志文件
     */
    public static Path defaultAuditFile() {
        String logsDir = JulUtil.getLogsDir();
        if (logsDir.isBlank()) {
            logsDir = Path.of(ShellConst.getStorePath(),"logs").toString();
        }
        return Path.of(logsDir, FILE_NAME);
    }

    /**
     * 记录一次工具执行。
     *
     * @param execution 工具执行信息
     */
    public void record(ToolExecution execution) {
        if (execution == null) {
            return;
        }
        try {
            Consumer<ToolExecution> listener = this.executionListener;
            if (listener != null) {
                listener.accept(execution);
            }
        } catch (Exception ex) {
            JulLog.warn("llm tool execution listener failed", ex);
        }
        try {
            this.write(this.buildRecord(execution));
        } catch (Exception ex) {
            // 审计失败不能影响工具本身的执行
            JulLog.warn("llm tool audit failed", ex);
        }
    }

    /**
     * 设置工具执行完成回调
     *
     * @param listener 回调
     */
    public void setExecutionListener(Consumer<ToolExecution> listener) {
        this.executionListener = listener;
    }

    /**
     * 是否本地命令工具
     *
     * @param execution 工具执行信息
     * @return 结果
     */
    public boolean isLocalCommand(ToolExecution execution) {
        if (execution == null || execution.request() == null) {
            return false;
        }
        String toolName = execution.request().name();
        return LOCAL_COMMAND_TOOLS.contains(toolName)
                || this.toolScopes.get(toolName) == LLMToolScope.LOCAL_EXECUTION;
    }

    private Map<String, Object> buildRecord(ToolExecution execution) {
        ToolExecutionRequest request = execution.request();
        InvocationContext invocationContext = execution.invocationContext();
        Arguments arguments = this.redactArguments(request.arguments());
        String result = resultText(execution);
        boolean blockedByPolicy = result.startsWith(LLMToolRuntimePolicy.BLOCKED_PREFIX);

        Map<String, Object> record = new LinkedHashMap<>();
        record.put("timestamp", OffsetDateTime.now().format(TIME_FORMATTER));
        record.put("callId", UUID.randomUUID().toString());
        record.put("providerCallId", request.id());
        record.put("invocationId", invocationContext == null ? null : stringify(invocationContext.invocationId()));
        record.put("interfaceName", invocationContext == null ? null : invocationContext.interfaceName());
        record.put("methodName", invocationContext == null ? null : invocationContext.methodName());
        record.put("sessionId", firstNonBlank(this.sessionId,
                invocationContext == null ? null : stringify(invocationContext.chatMemoryId())));
        record.put("sessionTabId", this.sessionTabId);
        record.put("sessionTabType", this.sessionTabType);
        record.put("connectionHost", this.connectionHost);
        record.put("connectionUser", this.connectionUser);
        record.put("chatMemoryId", invocationContext == null ? null : stringify(invocationContext.chatMemoryId()));
        record.put("provider", this.provider);
        record.put("model", this.model);
        record.put("executionMode", this.executionMode == null ? null : this.executionMode.name());
        record.put("toolAccessMode", this.toolAccessMode == null ? null : this.toolAccessMode.name());
        record.put("toolName", request.name());
        record.put("toolClass", this.toolClasses.get(request.name()));
        record.put("toolScope", this.toolScopes.get(request.name()));
        record.put("blockedByScope", blockedByPolicy);
        if (blockedByPolicy) {
            record.put("blockReason", result.substring(
                    LLMToolRuntimePolicy.BLOCKED_PREFIX.length()).trim());
        }
        record.put("arguments", truncate(arguments.text));
        record.put("argumentsLength", arguments.text == null ? 0 : arguments.text.length());
        record.put("argumentsTruncated", arguments.text != null && arguments.text.length() > MAX_TEXT_LENGTH);
        record.put("argumentsRedacted", arguments.redacted);
        record.put("result", truncate(result));
        record.put("resultLength", result.length());
        record.put("resultTruncated", result.length() > MAX_TEXT_LENGTH);
        record.put("failed", execution.hasFailed());
        record.put("startTime", execution.startTime() == null ? null : execution.startTime().toString());
        record.put("finishTime", execution.finishTime() == null ? null : execution.finishTime().toString());
        record.put("durationMs", execution.duration() == null ? null : execution.duration().toMillis());
        return record;
    }

    /**
     * Records a tool invocation rejected by scope policy before execution.
     *
     * @param toolName tool name
     * @param reason   rejection reason
     */
    public void recordBlocked(String toolName, String reason) {
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("timestamp", OffsetDateTime.now().format(TIME_FORMATTER));
        record.put("callId", UUID.randomUUID().toString());
        record.put("sessionId", this.sessionId);
        record.put("sessionTabId", this.sessionTabId);
        record.put("sessionTabType", this.sessionTabType);
        record.put("connectionHost", this.connectionHost);
        record.put("connectionUser", this.connectionUser);
        record.put("provider", this.provider);
        record.put("model", this.model);
        record.put("executionMode", this.executionMode == null ? null : this.executionMode.name());
        record.put("toolAccessMode", this.toolAccessMode == null ? null : this.toolAccessMode.name());
        record.put("toolName", toolName);
        record.put("toolClass", this.toolClasses.get(toolName));
        record.put("toolScope", this.toolScopes.get(toolName));
        record.put("blockedByScope", true);
        record.put("blockReason", reason);
        record.put("failed", true);
        try {
            this.write(record);
        } catch (Exception ex) {
            JulLog.warn("llm blocked tool audit failed", ex);
        }
    }

    private void write(Map<String, Object> record) throws Exception {
        String line = JSONUtil.toJson(record) + "\n";
        Path parent = this.auditFile.toAbsolutePath().getParent();
        WRITE_LOCK.lock();
        try {
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(this.auditFile, line, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } finally {
            WRITE_LOCK.unlock();
        }
    }

    private Arguments redactArguments(String text) {
        if (text == null || text.isBlank()) {
            return new Arguments(text, false);
        }
        try {
            Object value = JSONUtil.parseObject(text);
            Redaction redaction = new Redaction();
            Object redacted = redactValue(value, redaction);
            return new Arguments(JSONUtil.toJson(redacted), redaction.redacted);
        } catch (Exception ignored) {
            // 非法JSON按文本处理，尽量遮蔽常见的敏感键值
            String redacted = redactSensitiveText(text);
            return new Arguments(redacted, !redacted.equals(text));
        }
    }

    private static Object redactValue(Object value, Redaction redaction) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = String.valueOf(entry.getKey());
                if (isSensitiveKey(key)) {
                    result.put(key, "***");
                    redaction.redacted = true;
                } else {
                    result.put(key, redactValue(entry.getValue(), redaction));
                }
            }
            return result;
        }
        if (value instanceof List<?> list) {
            return list.stream().map(item -> redactValue(item, redaction)).toList();
        }
        return value;
    }

    private static boolean isSensitiveKey(String key) {
        if (key == null) {
            return false;
        }
        String normalized = key.toLowerCase(Locale.ROOT)
                .replace("_", "")
                .replace("-", "")
                .replace(".", "");
        return normalized.equals("sk")
                || normalized.contains("password")
                || normalized.contains("passwd")
                || normalized.contains("pwd")
                || normalized.contains("secret")
                || normalized.contains("token")
                || normalized.contains("apikey")
                || normalized.contains("privatekey")
                || normalized.contains("accesskey")
                || normalized.contains("secretkey")
                || normalized.contains("credential")
                || normalized.contains("cookie")
                || normalized.contains("authheader")
                || normalized.contains("authorization");
    }

    private static String redactSensitiveText(String text) {
        Matcher matcher = SENSITIVE_TEXT_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String value = matcher.group(2);
            String replacement = value.startsWith("\"") || value.startsWith("'") ? "\"***\"" : "***";
            matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(1) + replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static String resultText(ToolExecution execution) {
        Object resultObject = execution.resultObject();
        if (resultObject != null && containsJdbcResource(resultObject)) {
            return "[raw JDBC result omitted]";
        }
        try {
            String text = execution.result();
            return text == null ? "" : text;
        } catch (RuntimeException ex) {
            if (resultObject == null) {
                return "";
            }
            try {
                return String.valueOf(resultObject);
            } catch (RuntimeException ignored) {
                return "[" + resultObject.getClass().getName() + "]";
            }
        }
    }

    private static boolean containsJdbcResource(Object result) {
        if (result instanceof java.sql.ResultSet
                || result instanceof java.sql.Statement
                || result instanceof java.sql.Connection) {
            return true;
        }
        if (result instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                if (item instanceof java.sql.ResultSet
                        || item instanceof java.sql.Statement
                        || item instanceof java.sql.Connection) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String truncate(String text) {
        if (text == null || text.length() <= MAX_TEXT_LENGTH) {
            return text;
        }
        return text.substring(0, MAX_TEXT_LENGTH) + "\n...[truncated]";
    }

    private static Map<String, String> resolveToolClasses(Collection<?> tools) {
        Map<String, String> result = new LinkedHashMap<>();
        if (tools == null) {
            return result;
        }
        for (Object tool : tools) {
            if (tool == null) {
                continue;
            }
            try {
                for (ToolSpecification specification : ToolSpecifications.toolSpecificationsFrom(tool)) {
                    result.put(specification.name(), tool.getClass().getName());
                }
            } catch (Exception ex) {
                JulLog.warn("resolve llm tool class failed: " + tool.getClass().getName(), ex);
            }
        }
        return result;
    }

    private static Map<String, LLMToolScope> resolveToolScopes(Collection<?> tools) {
        Map<String, LLMToolScope> result = new LinkedHashMap<>();
        if (tools == null) {
            return result;
        }
        for (Object tool : tools) {
            if (tool == null) {
                continue;
            }
            LLMToolScope scope = tool instanceof LLMTools llmTools
                    ? llmTools.scope()
                    : LLMToolScope.SESSION;
            try {
                for (ToolSpecification specification : ToolSpecifications.toolSpecificationsFrom(tool)) {
                    result.put(specification.name(), scope);
                }
            } catch (Exception ex) {
                JulLog.warn("resolve llm tool scope failed: " + tool.getClass().getName(), ex);
            }
        }
        return result;
    }

    private static String firstNonBlank(String first, String second) {
        return first != null && !first.isBlank() ? first : second;
    }

    private static String stringify(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private record Arguments(String text, boolean redacted) {
    }

    private static final class Redaction {

        private boolean redacted;
    }
}
