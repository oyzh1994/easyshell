package cn.oyzh.easyssh.tabs;

import cn.oyzh.easyfx.controls.FXTab;
import cn.oyzh.easyfx.util.FXUtil;

/**
 * ssh基础tab
 *
 * @author oyzh
 * @since 2023/06/21
 */
public class SSHBaseTab extends FXTab {

    /**
     * 刷新图标
     */
    public void flushGraphic() {
    }

    /**
     * 加载内容
     */
    protected void loadContent() {
    }

    /**
     * 关闭当前tab
     */
    protected void closeTab() {
        if (this.isClosable()) {
            FXUtil.runWait(() -> this.getTabPane().getTabs().remove(this));
        }
    }
}
