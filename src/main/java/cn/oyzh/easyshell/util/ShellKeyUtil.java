package cn.oyzh.easyshell.util;

import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.domain.ShellKey;
import cn.oyzh.easyshell.sftp.ShellSFTPChannel;
import cn.oyzh.easyshell.sftp.ShellSFTPClient;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import com.jcraft.jsch.SftpException;

import java.io.ByteArrayInputStream;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-04-03
 */
public class ShellKeyUtil {

//    /**
//     * 生成私钥文件
//     *
//     * @param key 密钥
//     * @return 私钥文件
//     */
//    public static File generateKeyFile(ShellKey key) {
//        try {
//            String cachePath = ShellConst.getCachePath();
//            File privateKeyFile = new File(cachePath + File.separator + key.getId());
//            if (!privateKeyFile.exists()) {
//                String priKey = key.getPrivateKey();
//
//                String[] lines = priKey.split("\n");
//                StringBuilder sb = new StringBuilder();
//                for (int i = 1; i < lines.length - 1; i++) {
//                    sb.append(lines[i]);
//                }
////                PrivateKey privateKey = getPrivateKeyFromBase64(sb.toString());
//                RSAPrivateCrtKey pkey = (RSAPrivateCrtKey) getPrivateKeyFromBase64(sb.toString());
////                RSAPrivateCrtKey pkey = (RSAPrivateCrtKey) getPrivateKeyFromBase64(priKey);
//                byte[] alg = "ssh-rsa".getBytes(), none = "none".getBytes();
//                byte[] nbyt = pkey.getModulus().toByteArray(), ebyt = pkey.getPublicExponent().toByteArray();
//                int rand = new Random().nextInt();
//
//                ByteBuffer pub = ByteBuffer.allocate(nbyt.length + 50); // always enough, but not too much over
//                for (byte[] x : new byte[][]{alg, ebyt, nbyt}) {
//                    pub.putInt(x.length);
//                    pub.put(x);
//                }
//
//                ByteBuffer prv = ByteBuffer.allocate(nbyt.length * 4 + 50); // ditto
//                prv.putInt(rand);
//                prv.putInt(rand);
//                for (byte[] x : new byte[][]{alg, nbyt, ebyt, pkey.getPrivateExponent().toByteArray(),
//                        pkey.getCrtCoefficient().toByteArray(), pkey.getPrimeP().toByteArray(), pkey.getPrimeQ().toByteArray()}) {
//                    prv.putInt(x.length);
//                    prv.put(x);
//                }
//                prv.putInt(0); // no comment
//                for (int i = 0; prv.position() % 8 != 0; ) prv.put((byte) ++i); // 8 apparently default? IDK
//
//                ByteBuffer all = ByteBuffer.allocate(100 + pub.position() + prv.position()); // ditto
//                all.put("openssh-key-v1".getBytes());
//                all.put((byte) 0);
//                all.putInt(none.length);
//                all.put(none); // cipher
//                all.putInt(none.length);
//                all.put(none); // pbkdf
//                all.putInt(0);
//                all.putInt(1); // parms, count
//                all.putInt(pub.position());
//                all.put(pub.array(), 0, pub.position());
//                all.putInt(prv.position());
//                all.put(prv.array(), 0, prv.position());
//                byte[] result = Arrays.copyOf(all.array(), all.position());
////                FileUtil.writeString(key.getPrivateKey(), privateKeyFile);
//                FileUtil.writeBytes(result, privateKeyFile);
////                PrivateKey privateKey = getPrivateKeyFromBase64(priKey);
////                writeOpenSSHPrivateKeyToFile(privateKey, privateKeyFile.getPath());
//            }
//            File publicKeyFile = new File(cachePath + File.separator + key.getId() + ".pub");
//            if (!publicKeyFile.exists()) {
////                FileUtil.writeString(key.getPublicKey(), publicKeyFile);
//            }
//            return privateKeyFile;
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return null;
//    }
//
//    /**
//     * 将 Base64 编码的私钥转换为 PrivateKey 对象
//     *
//     * @param privateKeyBase64 私钥的 Base64 编码字符串
//     * @return PrivateKey 对象
//     * @throws NoSuchAlgorithmException 若不支持 RSA 算法
//     * @throws InvalidKeySpecException  若密钥规格无效
//     */
//    public static PrivateKey getPrivateKeyFromBase64(String privateKeyBase64)
//            throws NoSuchAlgorithmException, InvalidKeySpecException {
//        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyBase64);
//        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
//        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//        return keyFactory.generatePrivate(keySpec);
//    }
//
//    /**
//     * 将私钥以 OpenSSH 格式写入文件
//     *
//     * @param privateKey 私钥
//     * @param filePath   文件路径
//     * @throws IOException 写入文件时发生错误
//     */
//    public static void writeOpenSSHPrivateKeyToFile(PrivateKey privateKey, String filePath) throws IOException {
//        try (PemWriter pemWriter = new PemWriter(new FileWriter(filePath))) {
//            // 创建 PKCS8 格式的私钥信息
//            PrivateKeyInfo privateKeyInfo = PrivateKeyInfo.getInstance(privateKey.getEncoded());
//            // 生成 OpenSSH 格式的 PEM 对象
//            PemObject pemObject = new PemObject("OPENSSH PRIVATE KEY", privateKeyInfo.getEncoded());
//            // 写入 PEM 文件
//            pemWriter.writeObject(pemObject);
//        }
//    }
//
//    /**
//     * rsa算法
//     *
//     * @param length 长度
//     * @return 密钥
//     */
//    public static String[] rsa(int length) {
//        try {
//
//            Security.addProvider(new BouncyCastleProvider());
//
//            // 生成密钥对
//            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
//            keyPairGenerator.initialize(length);
//            KeyPair keyPair = keyPairGenerator.generateKeyPair();
//            PublicKey publicKey = keyPair.getPublic();
//            PrivateKey privateKey = keyPair.getPrivate();
//
//            byte[] publicKeyBytes = publicKey.getEncoded();
//            byte[] privateKeyBytes = privateKey.getEncoded();
//            String publicKeyBase64 = org.bouncycastle.util.encoders.Base64.toBase64String(publicKeyBytes);
//            String privateKeyBase64 = org.bouncycastle.util.encoders.Base64.toBase64String(privateKeyBytes);
//            return new String[]{publicKeyBase64, privateKeyBase64};
//        } catch (Exception ex) {
//            ex.printStackTrace();
//
//        }
//        return new String[]{};
//    }
//
//    /**
//     * ed25519算法
//     *
//     * @return 密钥
//     */
//    public static String[] ed25519() {
//        return generator("ED25519", null);
//    }
//
//    /**
//     * 生成密钥
//     *
//     * @param type   类型
//     * @param length 长度
//     * @return [0]公钥 [1]密钥
//     */
//    private static String[] generator(String type, Integer length) {
//        try {
//            // 初始化密钥对生成器，指定使用 RSA 算法
//            KeyPairGenerator generator = KeyPairGenerator.getInstance(type);
//            // 指定密钥长度
//            if (length != null) {
//                generator.initialize(length);
//            }
//            // 生成密钥对
//            KeyPair keyPair = generator.generateKeyPair();
//            // 获取公钥
//            PublicKey publicKey = keyPair.getPublic();
//            // 获取私钥
//            PrivateKey privateKey = keyPair.getPrivate();
//            byte[] publicKeyBytes = publicKey.getEncoded();
//            byte[] privateKeyBytes = privateKey.getEncoded();
//            String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKeyBytes);
//            String privateKeyBase64 = Base64.getEncoder().encodeToString(privateKeyBytes);
//            return new String[]{publicKeyBase64, privateKeyBase64};
//        } catch (NoSuchAlgorithmException ex) {
//            ex.printStackTrace();
//        }
//        return null;
//    }

    /**
     * 把ssh公钥复制到远程服务
     *
     * @param keys   密钥列表
     * @param client shell客户端
     * @return 结果
     */
    public static boolean sshCopyId(List<ShellKey> keys, ShellSSHClient client) {
        try {
            ShellSFTPClient sftpClient = client.getSftpClient();
            // ssh已知公钥文件
            String sshFile;
            // windows
            if (client.isWindows()) {
                sshFile = client.getUserHome() + ".ssh" + client.getFileSeparator();
                // 检查文件夹
                if (!sftpClient.openSftp().exist(sshFile)) {
                    sftpClient.openSftp().mkdir(sshFile);
                }
                sshFile = sshFile + "authorized_keys";
            } else {
                sshFile = client.getUserHome() + ".ssh" + client.getFileSeparator() + "authorized_keys";
            }
            for (ShellKey key : keys) {
                // 远程临时公钥
                String remoteFile = client.getUserHome() + key.getId() + ".pub";
                // 上传
                ShellSFTPChannel sftp = sftpClient.newSftp();
                sftp.put(new ByteArrayInputStream(key.getPublicKeyBytes()), remoteFile);
                IOUtil.close(sftp);
                // 追加到已知公钥
                client.shellExec().append_file(remoteFile, sshFile);
                try {
                    // 删除临时公钥文件
                    sftpClient.openSftp().rm(remoteFile);
                } catch (Exception ignored) {
                }
            }
            return true;
        } catch (SftpException ex) {
            ex.printStackTrace();
        }
        return false;
    }

}
