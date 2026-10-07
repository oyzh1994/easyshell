package cn.oyzh.easyshell.event.snippet;

import cn.oyzh.event.Event;

/**
 * 执行片段事件
 *
 * @author oyzh
 * @since 2025-06-11
 */
public class ShellRunSnippetEvent extends Event<String>   {

    /**
     * 是否在所有tab执行
     */
    private boolean runAll;

    public boolean isRunAll() {
        return runAll;
    }

    public void setRunAll(boolean runAll) {
        this.runAll = runAll;
    }
}
