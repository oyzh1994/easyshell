package cn.oyzh.easyshell.test;

import cn.oyzh.fx.tty.TtyTermWidget;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.settings.FXDefaultSettingsProvider;

import java.io.IOException;
import java.nio.charset.Charset;

public class ShellJSchTestTermWidget extends TtyTermWidget {

    public ShellJSchTestTermWidget() {
        super(new FXDefaultSettingsProvider());
    }

    @Override
    public TtyConnector createTtyConnector() throws IOException {
        return this.createTtyConnector(Charset.defaultCharset());
    }

    public ShellJSchTestTtyConnector createTtyConnector(Charset charset) {
        return new ShellJSchTestTtyConnector(charset);
    }
}
