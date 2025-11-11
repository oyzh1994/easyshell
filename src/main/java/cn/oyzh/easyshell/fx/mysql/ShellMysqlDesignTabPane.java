package cn.oyzh.easyshell.fx.mysql;

import cn.oyzh.fx.plus.controls.tab.FXTabPane;

/**
 *
 * @author oyzh
 * @since 2025-10-31
 */
public class ShellMysqlDesignTabPane extends FXTabPane {

    @Override
    public void initNode() {
        super.initNode();
        // this.setupSelectCountListener();
        this.setupRefreshListener();
    }
}
