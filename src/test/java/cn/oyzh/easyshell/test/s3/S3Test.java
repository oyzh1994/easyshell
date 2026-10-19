package cn.oyzh.easyshell.test.s3;

import cn.oyzh.easyshell.s3.ShellS3Util;
import org.junit.Test;

/**
 * S3 对象存储工具的测试
 *
 * @author oyzh
 * @since 2026-06-29
 */
public class S3Test {

    @Test
    public void test1() throws Exception {
        String appId = ShellS3Util.getAppId("", "");
        System.out.println(appId);
    }
}
