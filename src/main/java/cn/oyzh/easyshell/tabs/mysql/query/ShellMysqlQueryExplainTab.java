package cn.oyzh.easyshell.tabs.mysql.query;

import cn.oyzh.easyshell.query.mysql.ShellMysqlExplainResult;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;

/**
 * MySQL 查询执行计划标签页
 *
 * @author oyzh
 * @since 2024/08/16
 */
public class ShellMysqlQueryExplainTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "mysql/query/shellMysqlQueryExplainTab.fxml";
    }

    /**
     * 初始化
     *
     * @param title 标题
     * @param result 结果
     */
    public void init(String title, ShellMysqlExplainResult result) {
        this.setTitle(title);
        this.controller().init(result);
    }

    @Override
    public ShellMysqlQueryExplainTabController controller() {
        return (ShellMysqlQueryExplainTabController) super.controller();
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
     * @return 实例对象
     */
    public static ShellMysqlQueryExplainTab of(String title, ShellMysqlExplainResult result) {
        ShellMysqlQueryExplainTab tab = new ShellMysqlQueryExplainTab();
        tab.init(title, result);
        return tab;
    }
}
