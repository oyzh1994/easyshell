package cn.oyzh.easyshell.terminal.zk.cli;

/**
 * @author oyzh
 * @since 2023/09/20
 */
public class ZKRmrTerminalCommandHandler extends ZKDeleteallTerminalCommandHandler {

    @Override
    public String commandName() {
        return "rmr";
    }

    @Override
    public boolean commandDeprecated() {
        return true;
    }
}
