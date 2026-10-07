package cn.oyzh.easyshell.tabs.zk.query;

import cn.oyzh.easyshell.query.zk.ShellZKQueryParam;
import cn.oyzh.easyshell.query.zk.ShellZKQueryResult;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.i18n.I18nHelper;

/**
 * zk查询消息标签页
 *
 * @author oyzh
 * @since 2025-09-04
 */
public class ShellZKQueryMsgTab extends RichTab {

    /**
     * 初始化消息数据
     *
     * @param param  查询参数
     * @param result 查询结果
     */
    public void init(ShellZKQueryParam param, ShellZKQueryResult result) {
        super.flush();
        this.controller().init(param, result);
    }

    @Override
    protected String url() {
        return "/tabs/zk/query/shellZKQueryMsgTab.fxml";
    }

    @Override
    protected ShellZKQueryMsgTabController controller() {
        return (ShellZKQueryMsgTabController) super.controller();
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.message();
    }

    @Override
    public void initNode() {
        this.setClosable(false);
        super.initNode();
    }

    /**
     * 创建zk查询消息标签页
     *
     * @param param  查询参数
     * @param result 查询结果
     * @return zk查询消息标签页
     */
    public static ShellZKQueryMsgTab of(ShellZKQueryParam param, ShellZKQueryResult result) {
        ShellZKQueryMsgTab tab = new ShellZKQueryMsgTab();
        tab.init(param, result);
        return tab;
    }
}
