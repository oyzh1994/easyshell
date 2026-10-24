package cn.oyzh.easyshell.ssh;

import cn.oyzh.easyshell.domain.ShellConnect;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * OSC 工作目录上报配置测试
 */
public class ShellSSHOscWorkerDirSettingTest {

    @Test
    public void missingSettingIsDisabledByDefault() {
        assertFalse(ShellSSHUtil.isEnableOscWorkerDir(new ShellConnect()));
        assertFalse(ShellSSHUtil.isEnableOscWorkerDir(null));
    }

    @Test
    public void settingIsStoredInExtras() {
        ShellConnect connect = new ShellConnect();
        ShellSSHUtil.setEnableOscWorkerDir(connect, true);
        assertTrue(ShellSSHUtil.isEnableOscWorkerDir(connect));
        assertTrue((Boolean) connect.getExtra(ShellSSHUtil.OSC_WORKER_DIR_EXTRA));

        ShellSSHUtil.setEnableOscWorkerDir(connect, false);
        assertFalse(ShellSSHUtil.isEnableOscWorkerDir(connect));
    }

    @Test
    public void textBooleanValueIsAccepted() {
        ShellConnect connect = new ShellConnect();
        connect.putExtra(ShellSSHUtil.OSC_WORKER_DIR_EXTRA, " TRUE ");
        assertTrue(ShellSSHUtil.isEnableOscWorkerDir(connect));

        connect.putExtra(ShellSSHUtil.OSC_WORKER_DIR_EXTRA, "false");
        assertFalse(ShellSSHUtil.isEnableOscWorkerDir(connect));
    }

    @Test
    public void malformedSettingIsDisabled() {
        ShellConnect connect = new ShellConnect();
        connect.putExtra(ShellSSHUtil.OSC_WORKER_DIR_EXTRA, "unsupported");
        assertFalse(ShellSSHUtil.isEnableOscWorkerDir(connect));
    }
}
