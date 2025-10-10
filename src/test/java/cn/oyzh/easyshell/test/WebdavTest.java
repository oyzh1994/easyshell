package cn.oyzh.easyshell.test;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.github.sardine.impl.SardineImpl;
import org.apache.http.Header;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 *
 * @author oyzh
 * @since 2025-10-09
 */
public class WebdavTest {

    private String username = "oyzh.1994@qq.com";
    private String password = "a62tcykbjyujgfim";
    private String url = "https://dav.jianguoyun.com/dav/test";

    @Test
    public void test1() throws IOException {
        Sardine sardine = SardineFactory.begin(username, password);
        List<DavResource> resources = sardine.list(url);
        System.out.println(resources);
    }

    @Test
    public void test2() throws IOException {
        // 创建自定义的 HttpClient
        HttpClientBuilder builder = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(30000) // 30秒连接超时
                        .setSocketTimeout(60000)  // 60秒socket超时
                        .setConnectionRequestTimeout(30000) // 30秒请求超时
                        .build());

// 使用自定义 HttpClient 创建 Sardine 实例
        SardineImpl sardine = new SardineImpl(builder, username, password);
        FileInputStream fIn = new FileInputStream("/Users/oyzh/Desktop/k2.pub");
        InputStreamEntity entity = new InputStreamEntity(fIn, -1);

        List<Header> headers = new ArrayList<>();
        String auth = "username" + ":" + "password";
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        String authHeader = "Basic " + encodedAuth;
        headers.add(new BasicHeader("Authorization", authHeader));
        sardine.put(url + "aa.text", entity, headers);
        fIn.close();
    }

    @Test
    public void test3() throws IOException {

        // 创建认证提供者
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                new UsernamePasswordCredentials(username, password));

        // 创建自定义的 HttpClient
        HttpClientBuilder builder = HttpClients.custom()
                .setDefaultCredentialsProvider(credentialsProvider)
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(30000) // 30秒连接超时
                        .setSocketTimeout(60000)  // 60秒socket超时
                        .setConnectionRequestTimeout(30000) // 30秒请求超时
                        .build());

// 使用自定义 HttpClient 创建 Sardine 实例
        SardineImpl sardine = new SardineImpl(builder, username, password);
        FileInputStream fIn = new FileInputStream("/Users/oyzh/Desktop/k2.pub");
        InputStreamEntity entity = new InputStreamEntity(fIn, -1);

        List<Header> headers = new ArrayList<>();
        String auth = username + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        String authHeader = "Basic " + encodedAuth;
        headers.add(new BasicHeader("Authorization", authHeader));

        sardine.put(url + "/aa.text", entity, headers);
        fIn.close();
    }

    @Test
    public void test4() throws IOException {
        SardineImpl sardine = new SardineImpl(username, password);
        File file = new File("/Users/oyzh/Desktop/k2.pub");
        sardine.put(url + "/k2.pub", file, null);
    }

    @Test
    public void test5() throws IOException {
        SardineImpl sardine = new SardineImpl(username, password);
        File file = new File("/Users/oyzh/Desktop/k2.pub");
        FileInputStream fIn = new FileInputStream(file);
        InputStreamEntity entity = new InputStreamEntity(fIn, file.length());

        List<Header> headers = new ArrayList<>();
        String auth = username + ":" + password;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        String authHeader = "Basic " + encodedAuth;
        headers.add(new BasicHeader("Authorization", authHeader));
        sardine.put(url + "/k2.pub", entity, headers);
        // sardine.put(url + "/k2.pub", fIn, null, false, file.length());
        fIn.close();
    }

}
