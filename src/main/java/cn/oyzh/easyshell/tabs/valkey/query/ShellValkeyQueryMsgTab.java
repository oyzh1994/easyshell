package cn.oyzh.easyshell.tabs.valkey.query;

import cn.oyzh.easyshell.query.valkey.ShellValkeyQueryParam;
import cn.oyzh.easyshell.query.valkey.ShellValkeyQueryResult;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.i18n.I18nHelper;

/**
 * redis查询消息tab
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellValkeyQueryMsgTab extends RichTab {

    /**
     * 初始化查询消息
     *
     * @param param  查询参数
     * @param result 查询结果
     */
    public void init(ShellValkeyQueryParam param, ShellValkeyQueryResult result) {
        super.flush();
        this.controller().init(param, result);
    }

    @Override
    protected String url() {
        return "/tabs/valkey/query/shellValkeyQueryMsgTab.fxml";
    }

    @Override
    public ShellValkeyQueryMsgTabController controller() {
        return (ShellValkeyQueryMsgTabController) super.controller();
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
     * 创建查询消息tab
     *
     * @param param  查询参数
     * @param result 查询结果
     * @return 查询消息tab
     */
    public static ShellValkeyQueryMsgTab of(ShellValkeyQueryParam param, ShellValkeyQueryResult result) {
        ShellValkeyQueryMsgTab tab = new ShellValkeyQueryMsgTab();
        tab.init(param, result);
        return tab;
    }
}
