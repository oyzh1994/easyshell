package cn.oyzh.easyshell.test.s3;

import cn.oyzh.easyshell.s3.ShellS3Util;
import org.junit.Test;

/**
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
