package cn.oyzh.easyshell.mongo;

import cn.oyzh.common.security.TrustAllX509TrustManager;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellSSLConfig;
import cn.oyzh.easyshell.mongo.column.MongoColumn;
import cn.oyzh.easyshell.mongo.column.MongoColumns;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.ssh.util.PemUtil;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/7/1
 */
public class ShellMongoHelper {

    /**
     * 角色列表
     */
    public static final List<String> ROLES = List.of("read", "readWrite", "dbAdmin", "userAdmin", "dbOwner", "backup", "restore", "clusterAdmin", "clusterManager", "hostManager");

    ///**
    // * 初始化代理
    // *
    // * @param timeoutMs 超时时间
    // */
    //public static SocketFactory initProxySocketFactory(ShellProxyConfig proxyConfig, int timeoutMs, String host, int port) {
    //
    //    return new SocketFactory() {
    //        @Override
    //        public java.net.Socket createSocket(String host, int port) throws java.io.IOException {
    //            return ShellProxyUtil.createSocket(proxyConfig, host, port, timeoutMs);
    //        }
    //
    //        @Override
    //        public java.net.Socket createSocket(String host, int port, java.net.InetAddress localHost, int localPort) throws java.io.IOException {
    //            return createSocket(host, port);
    //        }
    //
    //        @Override
    //        public java.net.Socket createSocket(java.net.InetAddress host, int port) throws java.io.IOException {
    //            return createSocket(host.getHostAddress(), port);
    //        }
    //
    //        @Override
    //        public java.net.Socket createSocket(java.net.InetAddress host, int port, java.net.InetAddress localHost, int localPort) throws java.io.IOException {
    //            return createSocket(host.getHostAddress(), port);
    //        }
    //
    //        @Override
    //        public java.net.Socket createSocket() throws java.io.IOException {
    //            return createSocket(host, port);
    //        }
    //    };
    //}

    /**
     * 从 SSL 配置构建 SSLContext
     */
    public static SSLContext buildSSLContext(ShellSSLConfig sslConfig) throws Exception {
        String caPemFile = sslConfig.getCaCrt();
        String clientPemFile = sslConfig.getClientCrt();
        String privateKeyPassword = sslConfig.getClientPwd();
        // 1. 加载 CA 证书 → 构造 TrustManager（只用证书）
        TrustManagerFactory tmf = null;
        if (StringUtil.isNotBlank(caPemFile)) {
            X509Certificate[] caCerts = PemUtil.loadCertificates(caPemFile);
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            for (int i = 0; i < caCerts.length; i++) {
                trustStore.setCertificateEntry("ca-" + i, caCerts[i]);
            }
            tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);
        }

        // 2. 加载客户端证书+私钥 → 构造 KeyManager
        PemUtil.PemKeyCertData clientData = PemUtil.loadKeyAndCertificates(clientPemFile, privateKeyPassword);
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        keyStore.setKeyEntry("client",
                clientData.getPrivateKey(),
                privateKeyPassword.toCharArray(),
                clientData.getCertificates().toArray(new X509Certificate[0]));
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, privateKeyPassword.toCharArray());

        // 3. 构建 SSLContext
        SSLContext sslContext = SSLContext.getInstance("TLS");
        TrustManager[] trustManagers = tmf == null ? new TrustManager[]{TrustAllX509TrustManager.INSTANCE} : tmf.getTrustManagers();
        sslContext.init(kmf.getKeyManagers(),// 提供密钥管理器（包含客户端证书）
                trustManagers, // 提供信任管理器（包含受信任的 CA）
                new SecureRandom());
        return sslContext;
    }

    /**
     * 存储桶字段列表
     *
     * @return 结果
     */
    public static MongoColumns bucketColumns() {
        MongoColumns columns = new MongoColumns();
        MongoColumn idColumn = new MongoColumn("_id", I18nHelper.id());
        columns.add(idColumn);
        MongoColumn fileNameColumn = new MongoColumn("filename", I18nHelper.fileName());
        columns.add(fileNameColumn);
        MongoColumn lengthColumn = new MongoColumn("length", I18nHelper.length());
        columns.add(lengthColumn);
        MongoColumn chunkSizeColumn = new MongoColumn("chunkSize", I18nHelper.chunkSize());
        columns.add(chunkSizeColumn);
        MongoColumn uploadDateColumn = new MongoColumn("uploadDate", I18nHelper.uploadDate());
        columns.add(uploadDateColumn);
        MongoColumn metadataColumn = new MongoColumn("metadata", I18nHelper.metadata());
        columns.add(metadataColumn);
        return columns;
    }

    public static String getDbName(String remoteFile) {
        return remoteFile.substring(0, remoteFile.indexOf("@"));
    }

    public static String getBucketName(String remoteFile) {
        return remoteFile.substring(remoteFile.indexOf("@") + 1, remoteFile.indexOf("/"));
    }

    public static String getFileName(String remoteFile) {
        return remoteFile.substring(remoteFile.indexOf("/") + 1);
    }

}
