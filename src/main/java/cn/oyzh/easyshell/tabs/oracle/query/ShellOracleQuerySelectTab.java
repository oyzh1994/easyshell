package cn.oyzh.easyshell.tabs.oracle.query;

import cn.oyzh.easyshell.query.oracle.OracleExecuteResult;
import cn.oyzh.easyshell.trees.oracle.schema.ShellOracleSchemaTreeItem;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;

/**
 * Oracle查询结果标签页，用于展示查询执行结果
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellOracleQuerySelectTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "oracle/query/shellOracleQuerySelectTab.fxml";
    }

    /**
     * 初始化
     *
     * @param title 标题
     * @param result 结果
     * @param dbItem 数据库树节点
     */
    public void init(String title, OracleExecuteResult result, ShellOracleSchemaTreeItem dbItem) {
        this.setTitle(title);
        this.controller().init(result, dbItem);
    }

    @Override
    public ShellOracleQuerySelectTabController controller() {
        return (ShellOracleQuerySelectTabController) super.controller();
    }

    @Override
    public void initNode() {
        this.setClosable(false);
        super.initNode();
    }

    /**
     * 创建实例
     *
     * @param title 标题
     * @param result 结果
     * @param dbItem 数据库树节点
     * @return 实例对象
     */
    public static ShellOracleQuerySelectTab of(String title, OracleExecuteResult result, ShellOracleSchemaTreeItem dbItem) {
        ShellOracleQuerySelectTab tab = new ShellOracleQuerySelectTab();
        tab.init(title, result, dbItem);
        return tab;
    }
}
