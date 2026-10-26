package cn.oyzh.easyshell.terminal;

import org.junit.Test;

import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

/**
 * 验证交互式终端认证提示识别。
 */
public class ShellTtyAuthUtilTest {

    @Test
    public void recognizesPromptSplitAcrossChunks() throws Exception {
        ShellTtyAuthUtil util = new ShellTtyAuthUtil();
        StringWriter writer = new StringWriter();

        util.authenticate("Log".toCharArray(), 0, 3, "alice", "secret", writer, "\r\n");
        util.authenticate("in: ".toCharArray(), 0, 4, "alice", "secret", writer, "\r\n");

        assertEquals("alice\r\n", writer.toString());
    }

    @Test
    public void recognizesUsernamePromptVariants() throws Exception {
        String[] prompts = {"login:", "login as:", "login id:", "host login:", "User Name:", "Username:",
                "Account Name:", "用户：", "账号：", "请输入用户名：", "登录名：", "登录用户："};
        for (String prompt : prompts) {
            StringWriter writer = new StringWriter();
            ShellTtyAuthUtil util = new ShellTtyAuthUtil();
            util.authenticate(prompt.toCharArray(), 0, prompt.length(), "alice", "secret", writer, "\r\n");
            assertEquals(prompt, "alice\r\n", writer.toString());
        }
    }

    @Test
    public void recognizesPasswordPromptVariants() throws Exception {
        String[] prompts = {"Password: ", "passwd：", "Enter current password:",
                "Please enter your password: ", "Current UNIX password: ",
                "Password for root: ", "root@host's password:", "请输入密码：", "请输入您的密码：", "口令："};
        for (String prompt : prompts) {
            StringWriter writer = new StringWriter();
            ShellTtyAuthUtil util = new ShellTtyAuthUtil();
            util.authenticate(prompt.toCharArray(), 0, prompt.length(), "alice", "secret", writer, "\r");
            assertEquals(prompt, "secret\r", writer.toString());
        }
    }

    @Test
    public void ignoresPasswordsThatShouldNotBeAutoFilled() throws Exception {
        String[] prompts = {"New password:", "Retype new password:", "Verification code:",
                "One-time password:", "New UNIX password:", "Are you sure you want to continue connecting (yes/no)?"};
        for (String prompt : prompts) {
            StringWriter writer = new StringWriter();
            ShellTtyAuthUtil util = new ShellTtyAuthUtil();
            util.authenticate(prompt.toCharArray(), 0, prompt.length(), "alice", "secret", writer, "\r\n");
            assertEquals(prompt, "", writer.toString());
        }
    }

    @Test
    public void ignoresAnsiAndControlSequences() throws Exception {
        String prompt = "Welcome\r\n\u001B[32mPassword:\u001B[0m \u0000";
        StringWriter writer = new StringWriter();
        ShellTtyAuthUtil util = new ShellTtyAuthUtil();

        util.authenticate(prompt.toCharArray(), 0, prompt.length(), "alice", "secret", writer, "\r\n");

        assertEquals("secret\r\n", writer.toString());
    }

    @Test
    public void recognizesAnsiSequenceSplitAcrossChunks() throws Exception {
        StringWriter writer = new StringWriter();
        ShellTtyAuthUtil util = new ShellTtyAuthUtil();

        String first = "Password:\u001B[";
        util.authenticate(first.toCharArray(), 0, first.length(), "alice", "secret", writer, "\r\n");
        assertEquals("", writer.toString());
        util.authenticate("0m ".toCharArray(), 0, 3, "alice", "secret", writer, "\r\n");

        assertEquals("secret\r\n", writer.toString());
    }

    @Test
    public void ignoresLoginHistory() throws Exception {
        StringWriter writer = new StringWriter();
        ShellTtyAuthUtil util = new ShellTtyAuthUtil();
        String output = "Last login: Sun Oct 26 22:00:00 from 127.0.0.1\r\n"
                + "Welcome. Last login:\r\nLast successful login:\r\n$ ";

        util.authenticate(output.toCharArray(), 0, output.length(), "alice", "secret", writer, "\r\n");

        assertEquals("", writer.toString());
    }

    @Test
    public void doesNotCompleteOnHashInBannerBeforePassword() throws Exception {
        StringWriter writer = new StringWriter();
        ShellTtyAuthUtil util = new ShellTtyAuthUtil();

        util.authenticate("host login: ".toCharArray(), 0, 12, "alice", "secret", writer, "\r\n");
        util.authenticate("Notice #\r\n".toCharArray(), 0, 10, "alice", "secret", writer, "\r\n");
        util.authenticate("Password: ".toCharArray(), 0, 10, "alice", "secret", writer, "\r\n");

        assertEquals("alice\r\nsecret\r\n", writer.toString());
    }

    @Test
    public void retriesPasswordOnlyUpToConfiguredLimit() throws Exception {
        StringWriter writer = new StringWriter();
        ShellTtyAuthUtil util = new ShellTtyAuthUtil(2);
        for (int i = 0; i < 3; i++) {
            util.authenticate("Password: ".toCharArray(), 0, 10, "alice", "secret", writer, "\r");
        }

        assertEquals("secret\rsecret\r", writer.toString());
    }

    @Test
    public void doesNotWriteBlankCredentials() throws Exception {
        StringWriter writer = new StringWriter();
        ShellTtyAuthUtil util = new ShellTtyAuthUtil();

        util.authenticate("login: ".toCharArray(), 0, 7, " ", "secret", writer, "\r\n");
        util.authenticate("Password: ".toCharArray(), 0, 10, "alice", " ", writer, "\r\n");

        assertEquals("", writer.toString());
    }

    @Test
    public void completionStopsFurtherAuthentication() throws Exception {
        StringWriter writer = new StringWriter();
        ShellTtyAuthUtil util = new ShellTtyAuthUtil();

        util.complete();
        util.authenticate("Password: ".toCharArray(), 0, 10, "alice", "secret", writer, "\r\n");

        assertEquals("", writer.toString());
    }

    @Test
    public void completesAfterShellPrompt() throws Exception {
        StringWriter writer = new StringWriter();
        ShellTtyAuthUtil util = new ShellTtyAuthUtil();

        util.authenticate("Password: ".toCharArray(), 0, 10, "alice", "secret", writer, "\r\n");
        String prompt = "\r\nroot@host:~# ";
        util.authenticate(prompt.toCharArray(), 0, prompt.length(), "alice", "secret", writer, "\r\n");
        String currentPassword = "Current password: ";
        util.authenticate(currentPassword.toCharArray(), 0, currentPassword.length(), "alice", "secret", writer, "\r\n");

        assertEquals("secret\r\n", writer.toString());
    }

    @Test
    public void completesAfterUnicodeShellPrompt() throws Exception {
        StringWriter writer = new StringWriter();
        ShellTtyAuthUtil util = new ShellTtyAuthUtil();

        util.authenticate("Password: ".toCharArray(), 0, 10, "alice", "secret", writer, "\r\n");
        util.authenticate("~/project ❯ ".toCharArray(), 0, 12, "alice", "secret", writer, "\r\n");
        util.authenticate("Password: ".toCharArray(), 0, 10, "alice", "secret", writer, "\r\n");

        assertEquals("secret\r\n", writer.toString());
    }

    @Test
    public void doesNotTreatEarlierOutputAsShellPrompt() throws Exception {
        StringWriter writer = new StringWriter();
        ShellTtyAuthUtil util = new ShellTtyAuthUtil();

        util.authenticate("host login: ".toCharArray(), 0, 12, "alice", "secret", writer, "\r\n");
        String output = "\r\nWelcome >\r\nPassword: ";
        util.authenticate(output.toCharArray(), 0, output.length(), "alice", "secret", writer, "\r\n");

        assertEquals("alice\r\nsecret\r\n", writer.toString());
    }
}
