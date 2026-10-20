package cn.oyzh.easyshell.terminal.zk_v2;

import cn.oyzh.fx.terminal.command.TerminalCommand;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ZKDynamicCommandHandlerTest {

    @Test
    public void includesCommandNameForCliExecution() throws Exception {
        ZKDynamicCommandHandler handler = new ZKDynamicCommandHandler("create");
        TerminalCommand command = handler.parseCommand("create /easyshell value");

        assertEquals(List.of("/easyshell", "value"), command.argsList());
        assertEquals(List.of("create", "/easyshell", "value"), handler.commandArgs(command));
    }
}
