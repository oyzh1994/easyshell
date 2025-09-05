package cn.oyzh.easyshell.terminal.zk.cli;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.terminal.zk.ZKTerminalPane;
import cn.oyzh.easyshell.terminal.zk.ZKTerminalUtil;
import cn.oyzh.easyshell.util.zk.ShellZKNodeUtil;
import cn.oyzh.fx.terminal.command.TerminalCommand;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2023/7/21
 */
public abstract class ZKPathTerminalCommandHandler<C extends TerminalCommand> extends ZKCliTerminalCommandHandler<C> {

    @Override
    public boolean completion(String line, ZKTerminalPane terminal) {
        // 获取路径
        String path = ZKTerminalUtil.getPath(line);
        if (!StringUtil.contains(path, "/")) {
            return false;
        }
        try {
            // 父节点路径
            String parentPath = ShellZKNodeUtil.getParentPath(path);
            // 获取父节点的子节点列表，并判断是否以输入路径为起始内容
            List<String> children = terminal.getClient().getChildren(parentPath);
            // 节点列表
            List<String> list = new ArrayList<>();
            for (String child : children) {
                String cPath = ShellZKNodeUtil.concatPath(parentPath, child);
                if (cPath.startsWith(path)) {
                    list.add(child);
                }
            }
            if (list.size() > 1) {
                String text = TextUtil.beautifyFormat(list, 4, 0);
                terminal.outputLine(text);
                terminal.outputPrompt();
                terminal.outputByAppend(line);
                // terminal.moveCaretEnd(50);
            } else {
                String newInput = line.substring(0, line.indexOf("/")) + ShellZKNodeUtil.concatPath(parentPath, list.get(0));
                terminal.coverInput(newInput);
            }
            terminal.moveCaretEnd();
            return true;
        } catch (Exception ex) {
            terminal.onError(ex);
        }
        return false;
    }
}
