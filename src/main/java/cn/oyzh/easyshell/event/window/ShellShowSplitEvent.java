package cn.oyzh.easyshell.event.window;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.event.Event;

import java.util.List;

/**
 * @author oyzh
 * @since 2023/9/21
 */
public class ShellShowSplitEvent extends Event<String> {

    /**
     * 连接列表
     */
    private List<ShellConnect> connects;

    public List<ShellConnect> getConnects() {
        return connects;
    }

    public void setConnects(List<ShellConnect> connects) {
        this.connects = connects;
    }
}
