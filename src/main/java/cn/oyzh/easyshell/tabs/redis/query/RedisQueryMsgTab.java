package cn.oyzh.easyshell.tabs.redis.query;

import cn.oyzh.easyshell.query.redis.ShellRedisQueryParam;
import cn.oyzh.easyshell.query.redis.ShellRedisQueryResult;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2025/01/20
 */
public class RedisQueryMsgTab extends RichTab {

    public RedisQueryMsgTab(ShellRedisQueryParam param, ShellRedisQueryResult result) {
        super();
        super.flush();
        this.controller().init(param, result);
    }

    @Override
    protected String url() {
        return "/tabs/redis/query/redisQueryMsgTab.fxml";
    }

    @Override
    protected RedisQueryMsgTabController controller() {
        return (RedisQueryMsgTabController) super.controller();
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.message();
    }


}
