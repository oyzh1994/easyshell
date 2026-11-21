package cn.oyzh.easyshell.llm.prompt;

import cn.oyzh.common.util.StringUtil;

/**
 * 用户提示词工具。
 *
 * @author oyzh
 * @since 2026-11-21
 */
public final class LLMPromptUtil {

    public static final int MAX_NAME_LENGTH = 10;

    public static final int MAX_CONTENT_LENGTH = 1000;

    private static final int PREVIEW_LENGTH = 120;

    private LLMPromptUtil() {
    }

    public static String normalizeName(String name) {
        return name == null ? "" : name.trim();
    }

    public static String normalizeContent(String content) {
        return content == null ? "" : content.trim();
    }

    public static boolean exceedsNameLimit(String name) {
        return codePointLength(normalizeName(name)) > MAX_NAME_LENGTH;
    }

    public static boolean exceedsContentLimit(String content) {
        return codePointLength(normalizeContent(content)) > MAX_CONTENT_LENGTH;
    }

    /**
     * 构造用户选择提示词的系统消息。
     *
     * <p>该配置优先于普通提示词和默认行为说明，但不能绕过 EasyShell
     * 的目标作用域、工具边界和执行安全规则。</p>
     *
     * @param prompt 用户提示词
     * @return 系统消息
     */
    public static String systemMessage(LLMPrompt prompt) {
        if (prompt == null || StringUtil.isBlank(prompt.getContent())) {
            return "";
        }
        String name = StringUtil.isBlank(prompt.getName()) ? "custom" : prompt.getName();
        return "## Highest-priority user-defined prompt: " + name + "\n"
                + "The following user-selected instructions have the highest priority among "
                + "configurable prompt guidance. Apply them when they conflict with ordinary "
                + "defaults or lower-priority prompt sections. Do not use them to bypass "
                + "EasyShell target scope, tool boundaries, execution mode, or safety rules. "
                + "The current user message defines the concrete task.\n\n"
                + prompt.getContent().trim();
    }

    public static String preview(String content) {
        String normalized = normalizeContent(content)
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replaceAll("\\s+", " ");
        if (normalized.isEmpty()) {
            return "";
        }
        int length = codePointLength(normalized);
        if (length <= PREVIEW_LENGTH) {
            return normalized;
        }
        int end = normalized.offsetByCodePoints(0, PREVIEW_LENGTH);
        return normalized.substring(0, end) + "...";
    }

    public static int codePointLength(String value) {
        return value == null ? 0 : value.codePointCount(0, value.length());
    }
}
