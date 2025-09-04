package cn.oyzh.easyshell.tabs.redis.query;

import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.i18n.I18nHelper;

import java.util.Collection;

/**
 * @author oyzh
 * @since 2025/02/07
 */
public class ShellRedisQueryDataTab extends RichTab {

    public ShellRedisQueryDataTab(Object object) {
        super();
        super.flush();
        if (object instanceof Collection<?> collection) {
            this.controller().init(collection);
        } else {
            this.controller().init(object);
        }
    }

    @Override
    protected String url() {
        return "/tabs/redis/query/shellRedisQueryDataTab.fxml";
    }

    @Override
    protected ShellRedisQueryDataTabController controller() {
        return (ShellRedisQueryDataTabController) super.controller();
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.data();
    }
}
