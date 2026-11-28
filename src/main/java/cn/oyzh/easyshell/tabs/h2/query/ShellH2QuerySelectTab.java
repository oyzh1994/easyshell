package cn.oyzh.easyshell.tabs.h2.query;

import cn.oyzh.easyshell.query.h2.H2ExecuteResult;
import cn.oyzh.easyshell.trees.h2.root.ShellH2RootTreeItem;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;

/**
 * H2 查询结果标签页
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellH2QuerySelectTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "h2/query/shellH2QuerySelectTab.fxml";
    }

    /**
     * 初始化
     *
     * @param title  标题
     * @param result 执行结果
     * @param dbItem 数据库树节点
     */
    public void init(String title, H2ExecuteResult result, ShellH2RootTreeItem dbItem) {
        this.init(title, result, dbItem, dbItem.client().schema());
    }

    public void init(String title, H2ExecuteResult result,
                     ShellH2RootTreeItem dbItem, String schema) {
        this.setTitle(title);
        this.controller().init(result, dbItem, schema);
    }

    @Override
    public ShellH2QuerySelectTabController controller() {
        return (ShellH2QuerySelectTabController) super.controller();
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
     * @param dbItem 数据库树节点
     * @return 实例对象
     */
    public static ShellH2QuerySelectTab of(String title, H2ExecuteResult result, ShellH2RootTreeItem dbItem) {
        ShellH2QuerySelectTab tab = new ShellH2QuerySelectTab();
        tab.init(title, result, dbItem);
        return tab;
    }

    public static ShellH2QuerySelectTab of(String title, H2ExecuteResult result,
                                           ShellH2RootTreeItem dbItem, String schema) {
        ShellH2QuerySelectTab tab = new ShellH2QuerySelectTab();
        tab.init(title, result, dbItem, schema);
        return tab;
    }
}
