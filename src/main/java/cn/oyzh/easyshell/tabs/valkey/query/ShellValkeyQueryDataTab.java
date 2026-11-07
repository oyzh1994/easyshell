package cn.oyzh.easyshell.tabs.valkey.query;

import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.i18n.I18nHelper;

import java.util.Collection;

/**
 * redis查询数据tab
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellValkeyQueryDataTab extends RichTab {

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
        return "/tabs/valkey/query/shellValkeyQueryDataTab.fxml";
    }

    @Override
    protected ShellValkeyQueryDataTabController controller() {
        return (ShellValkeyQueryDataTabController) super.controller();
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
    public static ShellValkeyQueryDataTab of(Object object) {
        ShellValkeyQueryDataTab tab = new ShellValkeyQueryDataTab();
        tab.init(object);
        return tab;
    }
}
