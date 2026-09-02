package cn.oyzh.easyshell.tabs.dameng.table;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.table.DamengTable;
import cn.oyzh.easyshell.tabs.dameng.ShellDamengBaseTab;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * db表设计tab
 *
 * @author oyzh
 * @since 2024/08/07
 */
public class ShellDamengTableDesignTab extends ShellDamengBaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "dameng/table/shellDamengTableDesignTab.fxml";
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new SVGGlyph("/font/table.svg");
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    public void flushTitle() {
        String name = this.tableName();
        if (StringUtil.isBlank(name)) {
            name = I18nHelper.unnamedTable();
        }
        // 设置提示文本
        if (this.isUnsaved()) {
            this.setText("* " + this.schema() + "-" + name);
        } else {
            this.setText(this.schema() + "-" + name);
        }
    }

    public String tableName() {
        return this.controller().tableName();
    }

    public String schema() {
        return this.controller().schema();
    }

    /**
     * 初始化
     *
     * @param table 表
     * @param dbItem    db数据库树节点
     */
    public void init(DamengTable table, ShellDamengSchemaTreeItem dbItem) throws Exception {
        StageManager.showMask(() -> {
            try {
                this.controller().init(table, dbItem);
                this.flush();
            } catch (Exception ex) {
                MessageBox.exception(ex);
            }
        });
    }

    @Override
    public ShellDamengTableDesignTabController controller() {
        return (ShellDamengTableDesignTabController) super.controller();
    }

    public boolean isUnsaved() {
        return this.controller().isUnsaved();
    }

    @Override
    protected void onTabCloseRequest(Event event) {
        if (this.isUnsaved() && !MessageBox.confirm(I18nHelper.unsavedAndContinue())) {
            event.consume();
        } else {
            this.closeTab();
        }
    }

    @Override
    public ShellDamengSchemaTreeItem dbItem() {
        return this.controller().getDbItem();
    }

}
