package cn.oyzh.easyshell.tabs.mariadb.table;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mariadb.table.MariadbTable;
import cn.oyzh.easyshell.tabs.mariadb.ShellMariadbBaseTab;
import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
import cn.oyzh.fx.gui.svg.glyph.EditSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * MariaDB 表设计标签页
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbTableDesignTab extends ShellMariadbBaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "mariadb/table/shellMariadbTableDesignTab.fxml";
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new EditSVGGlyph();
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
            this.setText("* " + name + "@" + this.dbName() + "(" + this.connectName() + ")");
        } else {
            this.setText(name + "@" + this.dbName() + "(" + this.connectName() + ")");
        }
    }

    /**
     * 获取表名称
     *
     * @return 表名称
     */
    public String tableName() {
        return this.controller().tableName();
    }

    // public String dbName() {
    //     return this.controller().dbName();
    // }

    /**
     * 初始化
     *
     * @param table  表
     * @param dbItem 数据库树节点
     * @throws Exception 异常
     */
    public void init(MariadbTable table, ShellMariadbDatabaseTreeItem dbItem) throws Exception {
//        StageManager.showMask(() -> {
            try {
                this.controller().init(table, dbItem);
                this.flush();
            } catch (Exception ex) {
                MessageBox.exception(ex);
            }
//        });
    }

    @Override
    public ShellMariadbTableDesignTabController controller() {
        return (ShellMariadbTableDesignTabController) super.controller();
    }

    /**
     * 是否未保存
     *
     * @return 是否未保存
     */
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
    public ShellMariadbDatabaseTreeItem dbItem() {
        return this.controller().getDbItem();
    }
}
