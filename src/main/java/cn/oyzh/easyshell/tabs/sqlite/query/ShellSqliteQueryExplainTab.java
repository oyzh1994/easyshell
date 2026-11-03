package cn.oyzh.easyshell.tabs.sqlite.query;

import cn.oyzh.easyshell.query.sqlite.SQLiteExecuteResult;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;

/**
 * SQLite 查询执行计划标签页
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteQueryExplainTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "sqlite/query/shellSqliteQueryExplainTab.fxml";
    }

    /**
     * 初始化
     *
     * @param title  标题
     * @param result 执行结果
     */
    public void init(String title, SQLiteExecuteResult result) {
        this.setTitle(title);
        this.controller().init(result);
    }

    @Override
    public ShellSqliteQueryExplainTabController controller() {
        return (ShellSqliteQueryExplainTabController) super.controller();
    }

    @Override
    public void initNode() {
        this.setClosable(false);
        super.initNode();
    }

    /**
     * 创建实例
     *
     * @param title  标题
     * @param result 执行结果
     * @return 实例对象
     */
    public static ShellSqliteQueryExplainTab of(String title, SQLiteExecuteResult result) {
        ShellSqliteQueryExplainTab tab = new ShellSqliteQueryExplainTab();
        tab.init(title, result);
        return tab;
    }
}
