package cn.oyzh.easyshell.test;

import cn.oyzh.common.util.HttpUtil;
import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.github.sardine.impl.SardineImpl;
import org.apache.http.Header;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicHeader;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 *
 * @author oyzh
 * @since 2025-10-09
 */
public class WebdavTest {

    private String username = "test";
    private String password = "123456";
    // private String url = "http://127.0.0.1:1880";
    private String url = "http://127.0.0.1:18082";
    // private String url = "https://dav.jianguoyun.com/dav/test";

    @Test
    public void test1() throws IOException {
        Sardine sardine = SardineFactory.begin(username, password);
        List<DavResource> resources = sardine.list(url);
        System.out.println(resources);
    }

    @Test
    public void test2() throws IOException {
        // 使用自定义 HttpClient 创建 Sardine 实例
        SardineImpl sardine = new SardineImpl(username, password);
        FileInputStream fIn = new FileInputStream("/Users/oyzh/Desktop/k2.pub");
        sardine.put(url + "/aa.text", fIn, Map.of("Authorization", HttpUtil.basic(username, password)));
        fIn.close();
    }

    @Test
    public void test3() throws IOException {
// 使用自定义 HttpClient 创建 Sardine 实例
        SardineImpl sardine = new SardineImpl(username, password);
        FileInputStream fIn = new FileInputStream("/Users/oyzh/Desktop/k2.pub");
        sardine.put(url + "/aa.text", fIn);
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
