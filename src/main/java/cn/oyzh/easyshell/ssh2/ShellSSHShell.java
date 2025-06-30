package cn.oyzh.easyshell.ssh2;

import cn.oyzh.common.thread.TaskManager;
import com.jcraft.jsch.ChannelShell;

/**
 * @author oyzh
 * @since 2023/8/16
 */
public class ShellSSHShell extends ShellSSHChannel {

    public ShellSSHShell(ChannelShell channel) {
        super(channel);
    }

    @Override
    public ChannelShell getChannel() {
        return (ChannelShell) super.getChannel();
    }

    public void setPtySize(int columns, int rows, int sizeW, int sizeH) {
        this.getChannel().setPtySize(columns, rows, sizeW, sizeH);
    }

    @Override
    public void close() {
        TaskManager.startTimeout(() -> {
            try {
                super.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, 100);
    }
}
