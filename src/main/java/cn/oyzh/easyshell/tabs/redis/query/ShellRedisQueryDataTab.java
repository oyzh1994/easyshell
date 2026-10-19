package cn.oyzh.easyshell.tabs.redis.query;

import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.i18n.I18nHelper;

import java.util.Collection;

/**
 * redis查询数据tab
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellRedisQueryDataTab extends RichTab {

    /**
     * 初始化查询数据
     *
     * @param object 数据对象
     */
    public void init(Object object) {
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

    @Override
    public void initNode() {
        this.setClosable(false);
        super.initNode();
    }

    /**
     * 创建查询数据tab
     *
     * @param object 数据对象
     * @return 查询数据tab
     */
    public static ShellRedisQueryDataTab of(Object object) {
        ShellRedisQueryDataTab tab = new ShellRedisQueryDataTab();
        tab.init(object);
        return tab;
    }
}
