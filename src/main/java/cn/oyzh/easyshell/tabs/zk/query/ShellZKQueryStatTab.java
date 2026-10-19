package cn.oyzh.easyshell.tabs.zk.query;

import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.i18n.I18nHelper;
import org.apache.zookeeper.data.Stat;

/**
 * zk查询状态标签页
 *
 * @author oyzh
 * @since 2025-09-04
 */
public class ShellZKQueryStatTab extends RichTab {

    //public ShellZKQueryStatTab(Stat stat) {
    //    super();
    //    super.flush();
    //    this.controller().init(stat);
    //}

    /**
     * 初始化状态数据
     *
     * @param stat 状态信息
     */
    public void init(Stat stat) {
        super.flush();
        this.controller().init(stat);
    }

    @Override
    protected String url() {
        return "/tabs/zk/query/shellZKQueryStatTab.fxml";
    }

    @Override
    protected ShellZKQueryStatTabController controller() {
        return (ShellZKQueryStatTabController) super.controller();
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.stat();
    }

    @Override
    public void initNode() {
        this.setClosable(false);
        super.initNode();
    }

    /**
     * 创建zk查询状态标签页
     *
     * @param stat 状态信息
     * @return zk查询状态标签页
     */
    public static ShellZKQueryStatTab of(Stat stat) {
        ShellZKQueryStatTab tab = new ShellZKQueryStatTab();
        tab.init(stat);
        return tab;
    }
}
