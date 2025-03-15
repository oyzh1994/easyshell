package cn.oyzh.easyshell.shell;

import com.jcraft.jsch.ChannelShell;

/**
 * @author oyzh
 * @since 2023/8/16
 */
public class ShellShell extends ShellChannel {

    public ShellShell(ChannelShell channel) {
        super(channel);
    }

    @Override
    public ChannelShell getChannel() {
        return (ChannelShell) super.getChannel();
    }

    public void setPtySize(int columns, int rows, int sizeW, int sizeH) {
        this.getChannel().setPtySize(columns, rows, sizeW, sizeH);
    }
}
