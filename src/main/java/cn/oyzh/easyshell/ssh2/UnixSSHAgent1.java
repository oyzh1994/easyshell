package cn.oyzh.easyshell.ssh2;

import cn.oyzh.ssh.util.SSHUtil;
import com.jcraft.jsch.AgentIdentityRepository;
import com.jcraft.jsch.AgentProxyException;
import com.jcraft.jsch.Identity;
import org.apache.sshd.agent.SshAgent;
import org.apache.sshd.agent.SshAgentKeyConstraint;
import org.apache.sshd.common.session.SessionContext;

import java.io.IOException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author oyzh
 * @since 2025-06-30
 */
public class UnixSSHAgent1 implements SshAgent {

    private AgentIdentityRepository repository;

    public UnixSSHAgent1() throws AgentProxyException {
        repository= SSHUtil.initAgentIdentityRepository();
    }

    @Override
    public Iterable<? extends Map.Entry<PublicKey, String>> getIdentities() throws IOException {
        List<Map.Entry<PublicKey,String>> pubKeys= new ArrayList<>();
        for (Identity identity : repository.getIdentities()) {
            MyPubKey myPubKey= new MyPubKey(){
                @Override
                public String getName() {
                    return identity.getName();
                }

                @Override
                public byte[] getEncoded() {
                    return identity.getPublicKeyBlob();
                }
            };
            pubKeys.add(new Map.Entry<PublicKey, String>() {
                @Override
                public PublicKey getKey() {
                    return myPubKey;
                }

                @Override
                public String getValue() {
                    return identity.getName();
                }

                @Override
                public String setValue(String value) {
                    return "";
                }
            });
        }
        return pubKeys;
    }

    @Override
    public Map.Entry<String, byte[]> sign(SessionContext session, PublicKey key, String algo, byte[] data) throws IOException {
        for (Identity identity : this.repository.getIdentities()) {
            if(Arrays.equals(identity.getPublicKeyBlob(), key.getEncoded())){
                return new Map.Entry<>() {
                    @Override
                    public String getKey() {
                        return identity.getName();
                    }

                    @Override
                    public byte[] getValue() {
                        return identity.getSignature(data);
                    }

                    @Override
                    public byte[] setValue(byte[] value) {
                        return new byte[0];
                    }
                };
            }
        }
        return null;
    }

    @Override
    public void addIdentity(KeyPair key, String comment, SshAgentKeyConstraint... constraints) throws IOException {

    }

    @Override
    public void removeIdentity(PublicKey key) throws IOException {

    }

    @Override
    public void removeAllIdentities() throws IOException {

    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public void close() throws IOException {

    }

    public class MyPubKey implements PublicKey {

        public String getName(){
            return null;
        }

        @Override
        public String getAlgorithm() {
            return "";
        }

        @Override
        public String getFormat() {
            return "";
        }

        @Override
        public byte[] getEncoded() {
            return new byte[0];
        }
    }
}
