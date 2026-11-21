package cn.oyzh.easyshell.llm.prompt;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LLMPromptUtilTest {

    @Test
    public void enforcesNameAndContentLengthLimits() {
        assertFalse(LLMPromptUtil.exceedsNameLimit("1234567890"));
        assertTrue(LLMPromptUtil.exceedsNameLimit("12345678901"));
        assertFalse(LLMPromptUtil.exceedsContentLimit("a".repeat(1000)));
        assertTrue(LLMPromptUtil.exceedsContentLimit("a".repeat(1001)));
        assertFalse(LLMPromptUtil.exceedsNameLimit("😀".repeat(10)));
        assertTrue(LLMPromptUtil.exceedsNameLimit("😀".repeat(11)));
    }

    @Test
    public void buildsHighestPrioritySystemMessage() {
        LLMPrompt prompt = new LLMPrompt();
        prompt.setName("运维");
        prompt.setContent("始终使用中文回答。");

        String message = LLMPromptUtil.systemMessage(prompt);

        assertTrue(message.startsWith("## Highest-priority user-defined prompt: 运维"));
        assertTrue(message.contains("highest priority"));
        assertTrue(message.contains("Do not use them to bypass EasyShell"));
        assertTrue(message.endsWith("始终使用中文回答。"));
    }

    @Test
    public void returnsEmptySystemMessageWithoutPrompt() {
        assertEquals("", LLMPromptUtil.systemMessage(null));
        LLMPrompt prompt = new LLMPrompt();
        prompt.setName("empty");
        prompt.setContent("   ");
        assertEquals("", LLMPromptUtil.systemMessage(prompt));
    }

    @Test
    public void previewsSingleLineText() {
        assertEquals("line one line two",
                LLMPromptUtil.preview(" line one\nline two "));
    }
}
