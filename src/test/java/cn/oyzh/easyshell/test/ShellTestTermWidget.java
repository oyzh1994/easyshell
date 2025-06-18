package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.ssh.ShellSSHTtyConnector;
import cn.oyzh.easyshell.terminal.ShellDefaultTermWidget;
import com.pty4j.PtyProcess;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

public class ShellTestTermWidget extends ShellDefaultTermWidget {

    @Override
    public ShellTestTtyConnector createTtyConnector(Charset charset) throws IOException {
        PtyProcess process = this.createProcess();
        String[] command = this.getProcessCommand();
        ShellTestTtyConnector connector = new ShellTestTtyConnector(process, charset, Arrays.asList(command));
        return connector;
    }

    @Override
    public ShellSSHTtyConnector getTtyConnector() {
        return (ShellSSHTtyConnector) super.getTtyConnector();
    }

}
