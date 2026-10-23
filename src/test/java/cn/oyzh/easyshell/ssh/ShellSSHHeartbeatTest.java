package cn.oyzh.easyshell.ssh;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellConnState;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ShellSSHHeartbeatTest {

    @Test
    public void defaultHeartbeatIntervalIsSixtySeconds() {
        assertEquals(60, ShellSSHUtil.getHeartbeatInterval(new ShellConnect()));
    }

    @Test
    public void heartbeatIntervalIsStoredInExtras() {
        ShellConnect connect = new ShellConnect();
        ShellSSHUtil.setHeartbeatInterval(connect, 15);
        assertEquals(15, ShellSSHUtil.getHeartbeatInterval(connect));
        assertEquals(15, (Object) connect.getExtra(ShellSSHUtil.HEARTBEAT_INTERVAL_EXTRA));
    }

    @Test
    public void zeroDisablesHeartbeat() {
        ShellConnect connect = new ShellConnect();
        ShellSSHUtil.setHeartbeatInterval(connect, 0);
        assertEquals(0, ShellSSHUtil.getHeartbeatInterval(connect));
    }

    @Test
    public void invalidHeartbeatIntervalUsesDefault() {
        ShellConnect connect = new ShellConnect();
        ShellSSHUtil.setHeartbeatInterval(connect, 3601);
        assertEquals(60, ShellSSHUtil.getHeartbeatInterval(connect));
    }

    @Test
    public void sessionUsesConfiguredHeartbeatInterval() throws Exception {
        ShellConnect connect = new ShellConnect();
        connect.setAuthMethod("password");
        ShellSSHUtil.setHeartbeatInterval(connect, 15);
        Session session = new JSch().getSession("user", "127.0.0.1", 22);
        TestSSHClient client = new TestSSHClient(connect);
        client.configure(session);
        assertEquals(15_000, session.getServerAliveInterval());
    }

    private static final class TestSSHClient extends ShellBaseSSHClient {

        private final ObjectProperty<ShellConnState> state = new SimpleObjectProperty<>();

        private TestSSHClient(ShellConnect connect) {
            super(connect);
        }

        private void configure(Session session) throws Exception {
            this.session = session;
            this.initSession();
        }

        @Override
        public void start(int timeout) {
        }

        @Override
        public void close() {
        }

        @Override
        public boolean isConnected() {
            return false;
        }

        @Override
        public ObjectProperty<ShellConnState> stateProperty() {
            return this.state;
        }
    }
}
