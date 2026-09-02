package cn.oyzh.easyshell.tabs.dameng;

import cn.oyzh.easyshell.trees.dameng.schema.DamengSchemaTreeItem;
import cn.oyzh.fx.gui.tabs.RichTab;

/**
 * @author oyzh
 * @since 2024-09-12
 */
public abstract class DamengTab extends RichTab {

    public static final String BASE_PATH = "/tabs/";

    protected String getBasePath() {
        return BASE_PATH;
    }

    public abstract DamengSchemaTreeItem dbItem() ;
}
