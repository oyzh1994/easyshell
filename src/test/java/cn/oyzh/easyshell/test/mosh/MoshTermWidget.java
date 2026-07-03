package cn.oyzh.easyshell.test.mosh;

import cn.oyzh.easyshell.terminal.ShellDefaultTermWidget;
import org.mosh4j.core.MoshTerminalFrontend;

import java.io.IOException;

public class MoshTermWidget extends ShellDefaultTermWidget {

    public MoshTtyConnector createTtyConnector(MoshTerminalFrontend frontend) throws IOException {
        MoshTtyConnector connector = new MoshTtyConnector(frontend);
        return connector;
    }


}
