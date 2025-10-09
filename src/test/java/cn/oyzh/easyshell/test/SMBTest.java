package cn.oyzh.easyshell.test;

import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.smb.ShellSMBUtil;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

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

    @Test
    public void test1() {
        try {
            System.out.println("开始AES-CMAC实现验证...");

            // 测试用例1: 空消息
            System.out.println("测试1: 空消息");
            byte[] key = TextUtil.hexStrToBytes("2b7e151628aed2a6abf7158809cf4f3c");
            byte[] msg1 = new byte[0];
            byte[] expected1 = TextUtil.hexStrToBytes("bb1d6929e95937287fa37d129b756746");

            byte[] result1 = ShellSMBUtil.calculateAesCmac(key, msg1);
            System.out.println("期望: " + TextUtil.bytesToHexStr(expected1));
            System.out.println("实际: " + TextUtil.bytesToHexStr(result1));
            if (!Arrays.equals(result1, expected1)) {
                System.err.println("测试1失败!");
                return;
            }
            System.out.println("测试1通过");

            // 测试用例2: 单块消息
            System.out.println("测试2: 单块消息");
            byte[] msg2 = TextUtil.hexStrToBytes("6bc1bee22e409f96e93d7e117393172a");
            byte[] expected2 = TextUtil.hexStrToBytes("070a16b46b4d4144f79bdd9dd04a287c");

            byte[] result2 = ShellSMBUtil.calculateAesCmac(key, msg2);
            System.out.println("期望: " + TextUtil.bytesToHexStr(expected2));
            System.out.println("实际: " + TextUtil.bytesToHexStr(result2));
            if (!Arrays.equals(result2, expected2)) {
                System.err.println("测试2失败!");
                return;
            }
            System.out.println("测试2通过");

            // 测试用例3: 多块消息
            System.out.println("测试3: 多块消息");
            byte[] msg3 = TextUtil.hexStrToBytes("6bc1bee22e409f96e93d7e117393172aae2d8a571e03ac9c9eb76fac45af8e5130c81c46a35ce411");
            byte[] expected3 = TextUtil.hexStrToBytes("dfa66747de9ae63030ca32611497c827");

            byte[] result3 = ShellSMBUtil.calculateAesCmac(key, msg3);
            System.out.println("期望: " + TextUtil.bytesToHexStr(expected3));
            System.out.println("实际: " + TextUtil.bytesToHexStr(result3));
            if (!Arrays.equals(result3, expected3)) {
                System.err.println("测试3失败!");
                return;
            }
            System.out.println("测试3通过");
            System.out.println("所有AES-CMAC测试通过！");
        } catch (Exception e) {
            System.err.println("验证过程中出现异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
