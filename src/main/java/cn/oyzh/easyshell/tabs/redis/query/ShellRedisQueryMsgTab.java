package cn.oyzh.easyshell.tabs.redis.query;

import cn.oyzh.easyshell.query.redis.ShellRedisQueryParam;
import cn.oyzh.easyshell.query.redis.ShellRedisQueryResult;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.i18n.I18nHelper;

/**
 * redis查询消息tab
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellRedisQueryMsgTab extends RichTab {

    /**
     * 初始化查询消息
     *
     * @param param  查询参数
     * @param result 查询结果
     */
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

    /**
     * 创建查询消息tab
     *
     * @param param  查询参数
     * @param result 查询结果
     * @return 查询消息tab
     */
    public static ShellRedisQueryMsgTab of(ShellRedisQueryParam param, ShellRedisQueryResult result) {
        ShellRedisQueryMsgTab tab = new ShellRedisQueryMsgTab();
        tab.init(param, result);
        return tab;
    }
}
