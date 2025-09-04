package cn.oyzh.easyshell.terminal.zk.basic;

import cn.oyzh.easyshell.terminal.zk.ZKTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.zk.ZKTerminalPane;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import cn.oyzh.fx.terminal.command.TerminalCommand;
import cn.oyzh.fx.terminal.execute.TerminalExecuteResult;
import org.apache.zookeeper.Version;

/**
 * @author oyzh
 * @since 2023/09/20
 */
public class ZKVersionTerminalCommandHandler extends ZKTerminalCommandHandler<TerminalCommand> {
    @Override
    protected boolean checkArgs(String[] words) {
        return words.length == 1;
    }

    @Override
    public TerminalExecuteResult execute(TerminalCommand command, ZKTerminalPane terminal) {
        TerminalExecuteResult result = new TerminalExecuteResult();
        try {
            result.setResult("ZooKeeper CLI version: " + Version.getFullVersion());
        } catch (Exception ex) {
            ex.printStackTrace();
            result.setException(ex);
        }
        return result;
    }

    @Override
    public String commandName() {
        return "version";
    }

    @Override
    public String commandDesc() {
        return I18nResourceBundle.i18nString("base.get", "base.client", "base.version");
    }

}
