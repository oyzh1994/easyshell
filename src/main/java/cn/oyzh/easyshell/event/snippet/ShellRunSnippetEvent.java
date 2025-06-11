package cn.oyzh.easyshell.event.snippet;

import cn.oyzh.event.Event;

/**
 * @author oyzh
 * @since 2025/06/11
 */
public class ShellRunSnippetEvent extends Event<String>   {

    private boolean runAll;

    public boolean isRunAll() {
        return runAll;
    }

    public void setRunAll(boolean runAll) {
        this.runAll = runAll;
    }
}
