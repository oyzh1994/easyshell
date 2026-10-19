package cn.oyzh.easyshell.tabs.zk.query;

import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.i18n.I18nHelper;
import org.apache.zookeeper.StatsTrack;

/**
 * zk查询配额标签页
 *
 * @author oyzh
 * @since 2025-09-04
 */
public class ShellZKQueryQuotaTab extends RichTab {

    //public ShellZKQueryQuotaTab(StatsTrack track) {
    //    super();
    //    super.flush();
    //    this.controller().init(track);
    //}

    /**
     * 初始化配额数据
     *
     * @param track 配额信息
     */
    public void init(StatsTrack track) {
        super.flush();
        this.controller().init(track);
    }

    @Override
    protected String url() {
        return "/tabs/zk/query/shellZKQueryQuotaTab.fxml";
    }

    @Override
    protected ShellZKQueryQuotaTabController controller() {
        return (ShellZKQueryQuotaTabController) super.controller();
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.quota();
    }

    @Override
    public void initNode() {
        this.setClosable(false);
        super.initNode();
    }

    /**
     * 创建zk查询配额标签页
     *
     * @param track 配额信息
     * @return zk查询配额标签页
     */
    public static ShellZKQueryQuotaTab of(StatsTrack track) {
        ShellZKQueryQuotaTab tab = new ShellZKQueryQuotaTab();
        tab.init(track);
        return tab;
    }
}
