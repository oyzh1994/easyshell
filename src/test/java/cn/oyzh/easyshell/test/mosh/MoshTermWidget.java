package cn.oyzh.easyshell.test.mosh;

import cn.oyzh.easyshell.terminal.ShellProcessTermWidget;
import org.mosh4j.core.MoshTerminalFrontend;

import java.io.IOException;

/**
 * 基于 MoshTtyConnector 的 Mosh 终端控件
 *
 * @author oyzh
 * @since 2026-07-04
 */
public class MoshTermWidget extends ShellProcessTermWidget {

    public MoshTtyConnector createTtyConnector(MoshTerminalFrontend frontend) throws IOException {
        MoshTtyConnector connector = new MoshTtyConnector(frontend);
        return connector;
    }


}
