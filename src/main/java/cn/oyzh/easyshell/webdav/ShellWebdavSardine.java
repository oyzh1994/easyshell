package cn.oyzh.easyshell.webdav;

import cn.oyzh.common.network.ProxyUtil;
import cn.oyzh.common.util.ReflectUtil;
import cn.oyzh.easyshell.domain.ShellProxyConfig;
import com.github.sardine.impl.SardineImpl;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.ProxySelector;

/**
 *
 * @author oyzh
 * @since 2025-10-10
 */
public class ShellWebdavSardine extends SardineImpl implements Closeable {

    // /**
    //  * 认证信息
    //  */
    // private String authorization;
    //
    // public String getAuthorization() {
    //     return authorization;
    // }
    //
    // public void setAuthorization(String authorization) {
    //     this.authorization = authorization;
    // }

    /**
     * 超时时间
     */
    private final int timeout;

    public ShellWebdavSardine(int timeout,
                              String user,
                              String password) {

        this(timeout, user, password, null);
    }

    public ShellWebdavSardine(int timeout,
                              String user,
                              String password,
                              ShellProxyConfig proxyConfig
    ) {
        this.timeout = timeout;
        Method method = ReflectUtil.getMethod(SardineImpl.class,
                "createDefaultCredentialsProvider",
                String.class,
                String.class,
                String.class,
                String.class
        );
        CredentialsProvider credentialsProvider = (CredentialsProvider) ReflectUtil.invoke(this,
                method,
                user,
                password,
                null,
                null);
        ProxySelector selector = null;
        if (proxyConfig != null) {
            if (proxyConfig.isHttpProxy()) {
                selector = ProxyUtil.createHttpProxySelector(proxyConfig.getHost(), proxyConfig.getPort());
            } else if (proxyConfig.isSocksProxy()) {
                selector = ProxyUtil.createSocksProxySelector(proxyConfig.getHost(), proxyConfig.getPort());
            }
        }
        HttpClientBuilder builder = this.configure(selector, credentialsProvider);

        Field field = ReflectUtil.getField(ShellWebdavSardine.class, "builder", true, true);
        ReflectUtil.setFieldValue(field, builder, this);
        this.client = builder.build();
    }

    // @Override
    // public boolean exists(String url) throws IOException {
    //     if (this.authorization != null) {
    //         HttpHead head = new HttpHead(url);
    //         head.setHeader("Authorization", this.authorization);
    //         return this.execute(head, new ExistsResponseHandler());
    //     }
    //     return super.exists(url);
    // }

    @Override
    public void close() throws IOException {
        this.shutdown();
    }

    @Override
    protected HttpClientBuilder configure(ProxySelector selector, CredentialsProvider credentials) {
        HttpClientBuilder builder = super.configure(selector, credentials);
        builder.setDefaultRequestConfig(RequestConfig.custom()
                .setSocketTimeout(this.timeout)
                .setConnectTimeout(this.timeout)
                .setConnectionRequestTimeout(this.timeout)
                .build());
        return builder;
    }
}
