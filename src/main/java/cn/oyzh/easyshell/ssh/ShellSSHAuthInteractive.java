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

import java.util.function.Function;
import java.util.function.Supplier;

public class ShellSSHAuthInteractive implements UIKeyboardInteractive, UserInfo {

    private String password;

    private String passphrase;

    /**
     * 验证码供应器，为空时由界面提示输入。
     */
    private Supplier<String> verificationCodeSupplier;

    /**
     * 主机密钥确认回调，为空时由界面提示。
     */
    private Function<String, Boolean> hostKeyConfirmCallback;

    /**
     * 验证码已请求回调，用于标记当前 SSH 连接实际使用了 2FA。
     */
    private Runnable verificationCodeRequestedCallback;

    /**
     * JSch 密码认证失败后会循环复用同一凭证，这里只允许提交一次。
     */
    private boolean passwordRequested;

    /**
     * JSch 的 keyboard-interactive 在认证失败后会再次调用本对象。
     * 验证码只允许提交一次，避免同一 TOTP 被反复提交直至触发服务端锁定。
     */
    private volatile boolean verificationCodeRequested;

    public ShellSSHAuthInteractive() {
    }

    public ShellSSHAuthInteractive(String password) {
        this(password, null);
    }

    public ShellSSHAuthInteractive(String password, String passphrase) {
        this.password = password;
        this.passphrase = passphrase;
    }

    public void setVerificationCodeSupplier(Supplier<String> verificationCodeSupplier) {
        this.verificationCodeSupplier = verificationCodeSupplier;
    }

    public void setHostKeyConfirmCallback(Function<String, Boolean> hostKeyConfirmCallback) {
        this.hostKeyConfirmCallback = hostKeyConfirmCallback;
    }

    public void setVerificationCodeRequestedCallback(Runnable verificationCodeRequestedCallback) {
        this.verificationCodeRequestedCallback = verificationCodeRequestedCallback;
    }

    @Override
    public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt, boolean[] echo) {
        if (prompt == null || prompt.length == 0) {
            return null;
        }
        if (this.verificationCodeRequested) {
            JulLog.debug("ssh interactive verification code already submitted, stop repeated authentication");
            return null;
        }
        String[] responses = new String[prompt.length];
        for (int index = 0; index < prompt.length; index++) {
            String content = prompt[index] == null ? "" : prompt[index];
            if (this.isVerificationPrompt(content)) {
                responses[index] = this.requestVerificationCode();
            } else if (StringUtil.containsAnyIgnoreCase(content, "password", "密码")) {
                responses[index] = this.password;
                if (StringUtil.isEmpty(responses[index])) {
                    responses[index] = this.requestPassword(I18nHelper.pleaseInputPassword());
                }
            } else {
                responses[index] = MessageBox.prompt(content, StageManager.getFrontWindow());
            }
            if (StringUtil.isEmpty(responses[index])) {
                throw new ShellException("invalid interactive authentication response");
            }
        }
        return responses;
    }

    private boolean isVerificationPrompt(String prompt) {
        return StringUtil.containsAnyIgnoreCase(prompt,
                "verification", "one-time", "one time", "otp", "authenticator", "passcode", "pass code",
                "authentication code", "auth code", "security code", "two-factor", "two factor", "2fa", "mfa",
                "验证码", "动态码", "动态口令", "一次性密码", "安全码", "令牌");
    }

    private String requestVerificationCode() {
        String value;
        if (this.verificationCodeSupplier == null) {
            value = MessageBox.prompt(I18nHelper.pleaseInputVerificationCode(), StageManager.getFrontWindow());
        } else {
            try {
                value = this.verificationCodeSupplier.get();
            } catch (Exception ex) {
                JulLog.warn("failed to obtain interactive verification code", ex);
                throw new ShellException(ex);
            }
        }
        if (StringUtil.isEmpty(value)) {
            throw new ShellException("verification code is empty");
        }
        this.verificationCodeRequested = true;
        if (this.verificationCodeRequestedCallback != null) {
            this.verificationCodeRequestedCallback.run();
        }
        JulLog.debug("ssh keyboard-interactive verification code supplied");
        return value;
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
        if (this.hostKeyConfirmCallback != null) {
            try {
                return Boolean.TRUE.equals(this.hostKeyConfirmCallback.apply(message));
            } catch (Exception ex) {
                JulLog.warn("failed to confirm ssh host key", ex);
                throw new ShellException(ex);
            }
        }
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
