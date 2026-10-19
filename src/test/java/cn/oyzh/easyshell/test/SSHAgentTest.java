package cn.oyzh.easyshell.test;

import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.fx.plus.information.MessageBox;
import com.jcraft.jsch.AgentIdentityRepository;
import com.jcraft.jsch.AgentProxyException;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Identity;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.PageantConnector;
import com.jcraft.jsch.Session;
import org.junit.Test;

/**
 * 测试通过 Pageant 代理身份仓库建立 SSH 连接及内存占用情况的测试类
 *
 * @author oyzh
 * @since 2025-06-09
 */
public class SSHAgentTest {

    // 通过 Pageant 代理连接 SSH 并统计前后内存变化
    @Test
    public void test1() throws JSchException, AgentProxyException {
        double m1 = SystemUtil.getUsedMemory();
        PageantConnector connector = new PageantConnector();
        AgentIdentityRepository repository = new AgentIdentityRepository(connector);
        if(!connector.isAvailable()){
            MessageBox.warn("agent<UNK>");
            return;
        }
        for (Identity identity : repository.getIdentities()) {
            System.out.println(identity.getName());
        }
        JSch jsch = new JSch();
        jsch.setIdentityRepository(repository);
        Session session = jsch.getSession("root", "127.0.0.1", 22);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setConfig("PreferredAuthentications", "publickey,password");
        session.connect();
        ChannelShell shell = (ChannelShell) session.openChannel("shell");
        shell.connect();
        session.disconnect();
        double m2 = SystemUtil.getUsedMemory();
        SystemUtil.gc();
        double m3 = SystemUtil.getUsedMemory();
        System.out.println("m1:" + m1);
        System.out.println("m2:" + m2);
        System.out.println("m3:" + m3);
        System.out.println("----------------------------");
    }
}
