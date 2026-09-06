package cn.oyzh.easyshell.tabs.redis.query;

import cn.oyzh.easyshell.query.redis.ShellRedisQueryParam;
import cn.oyzh.easyshell.query.redis.ShellRedisQueryResult;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2025/01/20
 */
public class ShellRedisQueryMsgTab extends RichTab {

    public void init(ShellRedisQueryParam param, ShellRedisQueryResult result) {
        super.flush();
        this.controller().init(param, result);
    }

    @Override
    protected String url() {
        return "/tabs/redis/query/shellRedisQueryMsgTab.fxml";
    }

    @Override
    protected ShellRedisQueryMsgTabController controller() {
        return (ShellRedisQueryMsgTabController) super.controller();
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

    public static ShellRedisQueryMsgTab of(ShellRedisQueryParam param, ShellRedisQueryResult result) {
        ShellRedisQueryMsgTab tab = new ShellRedisQueryMsgTab();
        tab.init(param, result);
        return tab;
    }
}
