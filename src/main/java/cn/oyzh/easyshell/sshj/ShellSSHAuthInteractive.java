package cn.oyzh.easyshell.sshj;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.ssh.SSHException;
import net.schmizz.sshj.common.Message;
import net.schmizz.sshj.common.SSHPacket;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.method.AuthKeyboardInteractive;
import net.schmizz.sshj.userauth.method.ChallengeResponseProvider;
import net.schmizz.sshj.userauth.password.Resource;

import java.util.List;

/**
 * 认证ui实现，支持密码和验证码双重认证
 *
 * @author oyzh
 * @since 2025/06/21
 */
public class ShellSSHAuthInteractive extends AuthKeyboardInteractive {

    public ShellSSHAuthInteractive(String password) {
        super(new MixedChallengeResponseProvider(password));
    }

    @Override
    public void handle(Message cmd, SSHPacket buf) throws UserAuthException, TransportException {
        super.handle(cmd, buf);
    }

    public static class MixedChallengeResponseProvider implements ChallengeResponseProvider {

        private final String password;

        private MixedChallengeResponseProvider(String password) {
            this.password = password;
        }

        @Override
        public List<String> getSubmethods() {
            return List.of();
        }

        @Override
        public void init(Resource resource, String name, String instruction) {

        }

        @Override
        public char[] getResponse(String prompt, boolean echo) {
            if (StringUtil.containsAnyIgnoreCase(prompt, "Password", "密码")) {
                return this.password.toCharArray();
            }
            if (StringUtil.containsAnyIgnoreCase(prompt, "Verification code", "验证码")) {
                String verificationCode = MessageBox.prompt(I18nHelper.pleaseInputVerificationCode());
                if (StringUtil.isEmpty(verificationCode)) {
                    throw new SSHException("invalid verification code");
                }
                return verificationCode.toCharArray();
            }
            return new char[0];
        }

        @Override
        public boolean shouldRetry() {
            return false;
        }
    }
}
