package cn.oyzh.easyshell.local;

import cn.oyzh.easyshell.llm.LLMExecutionMode;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ShellLocalLLMToolsTest {

    @Test
    public void systemMessageContainsEnvironment() {
        try (ShellLocalLLMTools tools = new ShellLocalLLMTools()) {
            tools.setExecutor(new ShellLocalCommandExecutor());

            String message = tools.systemMessage(LLMExecutionMode.APPROVE);

            assertTrue(message.contains("本机命令工具"));
            assertTrue(message.contains(System.getProperty("os.name")));
            assertFalse(message.contains("{workingDirectory}"));
        }
    }

    @Test
    public void executeCommandReturnsStructuredResult() {
        try (ShellLocalLLMTools tools = new ShellLocalLLMTools()) {
            tools.setExecutor(new ShellLocalCommandExecutor());

            String result = tools.executeCommand("echo hello");

            assertTrue(result.contains("exitCode: 0"));
            assertTrue(result.contains("hello"));
        }
    }
}
