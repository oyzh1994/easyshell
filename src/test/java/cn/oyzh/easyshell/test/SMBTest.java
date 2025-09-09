package cn.oyzh.easyshell.test;

import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.Test;

import java.io.IOException;

/**
 * @author oyzh
 * @since 2025-09-09
 */
public class SMBTest {

    @Test
    public void test() throws IOException {
        String user = "ftp3";
        String passwd = "123456";
        // String domain = "DESKTOP-A1LGBH6";
        String domain = null;
        String shareName = "ftp3";
        SmbConfig config = SmbConfig.builder()
                .withMultiProtocolNegotiate(true)
                .withSigningRequired(false)
                .build();
        SMBClient smbClient = new SMBClient(config);
        Connection conn = smbClient.connect("127.0.0.1", 445);
        Session session = conn.authenticate(new AuthenticationContext(user, passwd.toCharArray(), domain));
        DiskShare share = (DiskShare) session.connectShare(shareName);
        share.list("\\");
    }
}
