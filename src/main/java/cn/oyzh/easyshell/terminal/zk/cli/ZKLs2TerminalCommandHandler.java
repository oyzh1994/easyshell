package cn.oyzh.easyshell.terminal.zk.cli;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.util.TerminalUtil;
import org.apache.zookeeper.cli.CliCommand;
import org.apache.zookeeper.cli.LsCommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author oyzh
 * @since 2023/09/20
 */
public class ZKLs2TerminalCommandHandler extends ZKPathTerminalCommandHandler<TerminalCommand> {

    private final CliCommand cliCommand = new LsCommand();

    @Override
    public CliCommand cliCommand() {
        return this.cliCommand;
    }

    @Override
    public TerminalCommand parseCommand(String line) {
        String[] args = TerminalUtil.split(line);
        TerminalCommand command = new TerminalCommand();
        List<String> list = new ArrayList<>();
        Collections.addAll(list, args);
        list.add(1, "-s");
        command.setArgs(list.toArray(new String[]{}));
        return command;
    }

    @Override
    public String commandName() {
        return "ls2";
    }

    @Override
    public String commandArg() {
        return "path";
    }

    @Override
    public String commandDesc() {
        return I18nResourceBundle.i18nString("base.iter", "base.child", "base.node");
    }

    @Override
    public boolean commandDeprecated() {
        return true;
    }
}
