package cn.oyzh.easyshell.test;

import org.apache.commons.net.bsd.RLoginClient;
import org.junit.Test;

import java.io.IOException;

/**
 * @author oyzh
 * @since 2025-05-27
 */
public class RloginTest {

    @Test
    public void test1() throws IOException {
        RLoginClient client = new RLoginClient();
        client.connect("192.168.3.156",513);
    }
}
