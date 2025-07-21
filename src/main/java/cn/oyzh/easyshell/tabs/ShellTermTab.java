package cn.oyzh.easyshell.tabs;

import cn.oyzh.fx.gui.tabs.RichTabController;

/**
 * shell终端tab
 *
 * @author oyzh
 * @since 2025/05/17
 */
public abstract class ShellTermTab extends ShellConnectTab {

    @Override
    protected RichTabController controller() {
        return super.controller();
    }

    /**
     * 运行片段
     *
     * @param content 内容
     */
    public void runSnippet(String content) throws Exception {

    }
}
