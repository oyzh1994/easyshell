package cn.oyzh.easyshell.test;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author oyzh
 * @since 2025-10-09
 */
public class WebdavTest {

    @Test
    public void test1() throws IOException {
// 1. 创建Sardine客户端并设置认证信息
        Sardine sardine = SardineFactory.begin("oyzh.1994@qq.com", "a62tcykbjyujgfim");


        List<DavResource> resources= sardine.list("https://dav.jianguoyun.com/dav/test");
        // // 2. 测试连接是否成功
        // boolean isConnected = sardine.exists("https://dav.jianguoyun.com/dav/");
        //
        //
        // System.out.println("WebDAV服务器连接" + (isConnected ? "成功" : "失败"));

        System.out.println(resources);
    }
}
