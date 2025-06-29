package cn.oyzh.easyshell.ssh;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.ssh.SSHException;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

/**
 * ssh认证用户信息
 *
 * @author oyzh
 * @since 2025/05/22
 */
public class ShellSSHAuthInteractive implements UIKeyboardInteractive, UserInfo {

    private final String password;

    public ShellSSHAuthInteractive(String password) {
        this.password = password;
    }

    @Override
    public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt, boolean[] echo) {
        String content = ArrayUtil.first(prompt);
        if (StringUtil.containsAnyIgnoreCase(content, "Password", "密码")) {
            return new String[]{this.password};
        }
        if (StringUtil.containsAnyIgnoreCase(content, "Verification code", "验证码")) {
            String verificationCode = MessageBox.prompt(I18nHelper.pleaseInputVerificationCode());
            if (StringUtil.isEmpty(verificationCode)) {
                throw new SSHException("invalid verification code");
            }
            return new String[]{verificationCode};
        }
        return null;
    }

    @Override
    public String getPassphrase() {
        return this.password;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public boolean promptPassword(String message) {
        return true;
    }

    @Override
    public boolean promptPassphrase(String message) {
        return true;
    }

    @Override
    public boolean promptYesNo(String message) {
        return true;
    }

    @Override
    public void showMessage(String message) {
        if (JulLog.isInfoEnabled()) {
            JulLog.info(message);
        }
    }
}
