package cn.oyzh.easyshell.test;

import org.apache.commons.codec.binary.Base64;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.config.keys.writer.openssh.OpenSSHKeyPairResourceWriter;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;

public class KeyTest {

    @Test
    public void test1() throws GeneralSecurityException, IOException {
        KeyPair keyPair = KeyUtils.generateKeyPair(KeyPairProvider.SSH_ED25519, 256);

      String  publicKey = Base64.encodeBase64String(keyPair.getPublic().getEncoded());
        String  privateKey = Base64.encodeBase64String(keyPair.getPrivate().getEncoded());
        System.out.println(publicKey);
        System.out.println(privateKey);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OpenSSHKeyPairResourceWriter.INSTANCE
                .writePublicKey(keyPair.getPublic(), null, baos);

        ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        OpenSSHKeyPairResourceWriter.INSTANCE
                .writePrivateKey(keyPair, null,null, baos1);

        System.out.println(baos.toString());
        System.out.println(baos1.toString());

    }
}
