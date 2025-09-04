package cn.oyzh.easyshell.tabs.zk.query;//package cn.oyzh.easyshell.tabs.zk.query;
//
//import cn.oyzh.fx.gui.tabs.RichTab;
//import cn.oyzh.i18n.I18nHelper;
//import org.apache.zookeeper.StatsTrack;
//
///**
// * @author oyzh
// * @since 2025/02/26
// */
//public class ZKQueryCountTab extends RichTab {
//
//    public ZKQueryCountTab(Integer count) {
//        super();
//        super.flush();
//        this.controller().init(count);
//    }
//
//    @Override
//    protected String url() {
//        return "/tabs/query/zkQueryCountTab.fxml";
//    }
//
//    @Override
//    protected ZKQueryCountTabController controller() {
//        return (ZKQueryCountTabController) super.controller();
//    }
//
//    @Override
//    public String getTabTitle() {
//        return I18nHelper.count();
//    }
//}
