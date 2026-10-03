package cn.oyzh.easyshell.terminal;

import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.fx.tty.TtyStreamConnector;
import cn.oyzh.fx.tty.TtyTermWidget;
import com.jediterm.terminal.TtyConnector;

import java.io.IOException;

/**
 * @author oyzh
 * @since 2026-10-03
 */
public class ShellStreamTermWidget extends TtyTermWidget {

    /**
     * 设置
     */
    protected final ShellSetting setting = ShellSettingStore.SETTING;

    public ShellStreamTermWidget() {
        super(new ShellSettingsProvider());
        ShellTerminalUtil.applySetting(this, this.setting);
    }

    @Override
    public TtyStreamConnector getTtyConnector() {
        return (TtyStreamConnector) super.getTtyConnector();
    }

    @Override
    public TtyConnector createTtyConnector() throws IOException {
        throw new UnsupportedOperationException();
    }
}
