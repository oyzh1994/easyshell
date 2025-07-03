package cn.oyzh.easyshell.ssh2;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.ssh.SSHException;
import org.apache.sshd.client.auth.keyboard.UserInteraction;
import org.apache.sshd.client.session.ClientSession;


/**
 * ssh交互认证
 * 支持密码、验证码、密码&验证码
 * TODO: 特别注意，针对ssh和sftp，请直接把ssh的session共用于sftp，否则可能一直要求验证
 *
 * @author oyzh
 * @since 2025/07/01
 */
public class ShellSSHAuthInteractive implements UserInteraction {

    private final String password;

    public ShellSSHAuthInteractive(String password) {
        this.password = password;
    }

    @Override
    public String[] interactive(ClientSession session, String name, String instruction, String lang, String[] prompt, boolean[] echo) {
        String content = ArrayUtil.first(prompt);
        JulLog.info("interactive prompt:", content);
        if (StringUtil.containsAnyIgnoreCase(content, "Password", "密码")) {
            return new String[]{this.password};
        }
        if (StringUtil.containsAnyIgnoreCase(content, "", "Verification code", "验证码")) {
            String verificationCode = MessageBox.prompt(I18nHelper.pleaseInputVerificationCode());
            if (StringUtil.isEmpty(verificationCode)) {
                throw new SSHException("invalid verification code");
            }
            return new String[]{verificationCode};
        }
        return null;
    }

    @Override
    public String resolveAuthPasswordAttempt(ClientSession session) throws Exception {
        JulLog.info("resolveAuthPasswordAttempt");
        return this.password;
    }

    @Override
    public String getUpdatedPassword(ClientSession session, String prompt, String lang) {
        return this.password;
    }
}
