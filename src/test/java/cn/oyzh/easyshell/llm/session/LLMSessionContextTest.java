package cn.oyzh.easyshell.llm.session;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LLMSessionContextTest {

    @Test
    public void buildsAuthoritativeContextFromSessionTab() {
        LLMSessionTab tab = new LLMSessionTab("tab-1", "server-a");
        tab.setType("ssh");
        tab.setHost("10.0.0.12");
        tab.setPort(22);
        tab.setUser("root");

        LLMSessionContext context = LLMSessionContext.from(tab);
        String prompt = context.promptText();

        assertTrue(context.available());
        assertTrue(prompt.contains("name: server-a"));
        assertTrue(prompt.contains("host: 10.0.0.12"));
        assertTrue(prompt.contains("port: 22"));
        assertTrue(prompt.contains("user: root"));
        assertTrue(prompt.contains("defaultTarget: the currently bound ssh session"));
        assertTrue(prompt.contains("Do not infer the target from timezone"));
        assertFalse(prompt.toLowerCase().contains("password"));
    }

    @Test
    public void localSessionTargetsTheLocalMachine() {
        LLMSessionContext context = new LLMSessionContext(
                "local-1", "Local", "local", "", null, "developer");

        assertEquals("local", context.type());
        assertTrue(context.promptText().contains(
                "defaultTarget: the local machine running EasyShell"));
    }
}
