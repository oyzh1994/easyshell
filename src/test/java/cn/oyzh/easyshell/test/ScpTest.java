package cn.oyzh.easyshell.test;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;
import org.junit.Test;

/**
 * @author oyzh
 * @since 2025-06-09
 */
public class ScpTest {

    @Test
    public void test1() throws TransportException, ConnectionException {

        SSHClient sshClient = new SSHClient();
        Session session = sshClient.startSession();

        Session.Shell shell = session.startShell();

    }
}
