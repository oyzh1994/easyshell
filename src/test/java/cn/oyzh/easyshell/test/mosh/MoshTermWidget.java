package cn.oyzh.easyshell.test.mosh;

import cn.oyzh.easyshell.terminal.ShellProcessTermWidget;
import org.mosh4j.core.MoshTerminalFrontend;

import java.io.IOException;

public class MoshTermWidget extends ShellProcessTermWidget {

    public MoshTtyConnector createTtyConnector(MoshTerminalFrontend frontend) throws IOException {
        MoshTtyConnector connector = new MoshTtyConnector(frontend);
        return connector;
    }


}
