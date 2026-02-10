package cn.oyzh.easyshell.ssh2;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
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

    private String password;

    private ShellConnect connect;

    public ShellSSHAuthInteractive(ShellConnect connect) {
        this.connect = connect;
        this.password = connect.getPassword();
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
    public String resolveAuthPasswordAttempt(ClientSession session) {
        JulLog.info("resolveAuthPasswordAttempt");
//        FXUtil.runWait(()->{
//            StageAdapter adapter = ShellViewFactory.sshAuth(this.connect);
//            if (adapter != null) {
//                String password = adapter.getProp("password");
//                // 密码认证
//                if (password != null) {
//                    this.password = password;
//                } else if (adapter.hasProp("keyPairs")) { //  证书认证
//                    Iterable<KeyPair> keyPairs = adapter.getProp("keyPairs");
//                    for (KeyPair keyPair : keyPairs) {
//                        session.addPublicKeyIdentity(keyPair);
//                    }
//                }
//            }
//        });
        System.out.println("------------------------xx");
        String key = """
                -----BEGIN OPENSSH PRIVATE KEY-----
                b3BlbnNzaC1rZXktdjEAAAAACmFlczI1Ni1jdHIAAAAGYmNyeXB0AAAAGAAAABALtuIPk0
                RIAey225zEUFqUAAAAEAAAAAEAAAAzAAAAC3NzaC1lZDI1NTE5AAAAIKC5/qRcwB5Pm1qn
                zFiJV24Hxy/koGGymmb/n3zOTfodAAAAkBYWwQlTtyiWSljYeIbN+ZsRD1bYWMZQGyo3Hb
                wHqeijZHDUYWc8JGHqAEzjqou3H3c3i6LLvcr/h0xxgQQ63lYJrE/AeqEArZ1Ui+XoLAQ2
                1u5g1C54L1TL/5+Esbp7VXjO5ZHn4Wa/l3NYa6Drotsp4S1YjWlmUYFNAwX+oCkBvezh1B
                U2GEqnLXXsvRoWBg==
                -----END OPENSSH PRIVATE KEY-----
                """;

//        // 加载证书
//        Iterable<KeyPair> keyPairs = null;
//        try {
//            keyPairs = SSHKeyUtil.loadKeysForStr(key, "123456");
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        //  设置证书认证
//        for (KeyPair keyPair : keyPairs) {
//            session.addPublicKeyIdentity(keyPair);
//        }
//        return "oyzh@2026";
//        return this.password;
        return null;
    }

    @Override
    public String getUpdatedPassword(ClientSession session, String prompt, String lang) {
        JulLog.info("getUpdatedPassword");
        return this.password;
    }
}
