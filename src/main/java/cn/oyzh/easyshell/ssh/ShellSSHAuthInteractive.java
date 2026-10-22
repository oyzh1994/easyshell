package cn.oyzh.easyshell.ssh;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

public class ShellSSHAuthInteractive implements UIKeyboardInteractive, UserInfo {

    private String password;

    private String passphrase;

    /**
     * JSch 密码认证失败后会循环复用同一凭证，这里只允许提交一次。
     */
    private boolean passwordRequested;

    public ShellSSHAuthInteractive() {
    }

    public ShellSSHAuthInteractive(String password) {
        this(password, null);
    }

    public ShellSSHAuthInteractive(String password, String passphrase) {
        this.password = password;
        this.passphrase = passphrase;
    }

    @Override
    public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt, boolean[] echo) {
        if (prompt == null || prompt.length == 0) {
            return null;
        }
        String[] responses = new String[prompt.length];
        for (int index = 0; index < prompt.length; index++) {
            String content = prompt[index] == null ? "" : prompt[index];
            if (StringUtil.containsAnyIgnoreCase(content, "password", "密码")) {
                responses[index] = this.password;
                if (StringUtil.isEmpty(responses[index])) {
                    responses[index] = this.requestPassword(I18nHelper.pleaseInputPassword());
                }
            } else {
                String message = StringUtil.containsAnyIgnoreCase(content, "verification", "验证码")
                        ? I18nHelper.pleaseInputVerificationCode()
                        : content;
                responses[index] = MessageBox.prompt(message, StageManager.getFrontWindow());
            }
            if (StringUtil.isEmpty(responses[index])) {
                throw new ShellException("invalid interactive authentication response");
            }
        }
        return responses;
    }

    private String requestPassword(String message) {
        String value = MessageBox.prompt(message, StageManager.getFrontWindow());
        if (StringUtil.isEmpty(value)) {
            throw new ShellException("invalid password");
        }
        this.password = value;
        return value;
    }

    @Override
    public String getPassphrase() {
        return this.passphrase;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public boolean promptPassword(String message) {
        if (this.passwordRequested) {
            JulLog.debug("ssh password prompt already handled, stop repeated authentication");
            return false;
        }
        this.passwordRequested = true;
        if (StringUtil.isNotEmpty(this.password)) {
            return true;
        }
        this.requestPassword(I18nHelper.pleaseInputPassword());
        return true;
    }

    @Override
    public boolean promptPassphrase(String message) {
        if (StringUtil.isNotEmpty(this.passphrase)) {
            return true;
        }
        String value = MessageBox.prompt(message, StageManager.getFrontWindow());
        if (StringUtil.isEmpty(value)) {
            return false;
        }
        this.passphrase = value;
        return true;
    }

    @Override
    public boolean promptYesNo(String message) {
        String tip = StringUtil.containsAnyIgnoreCase(message, "changed", "IDENTIFICATION HAS CHANGED")
                ? ShellI18nHelper.sshTip1()
                : ShellI18nHelper.sshTip2();
        return MessageBox.confirm(tip, StageManager.getFrontWindow());
    }

    @Override
    public void showMessage(String message) {
        JulLog.info("ssh message: {}", message);
    }
}
