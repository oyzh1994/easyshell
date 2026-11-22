package cn.oyzh.easyshell.tabs.sqlserver.table;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlserver.table.SqlServerTable;
import cn.oyzh.easyshell.tabs.sqlserver.ShellSqlServerBaseTab;
import cn.oyzh.easyshell.trees.sqlserver.schema.ShellSqlServerSchemaTreeItem;
import cn.oyzh.fx.gui.svg.glyph.database.TableSVGGlyph;
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
 * @since 2025-11-06
 */
public class ShellSqlServerTableDesignTab extends ShellSqlServerBaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "sqlserver/table/shellSqlServerTableDesignTab.fxml";
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new TableSVGGlyph();
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
            this.text("* " + this.schema() + "-" + name);
        } else {
            this.text(this.schema() + "-" + name);
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

    /**
     * 初始化
     *
     * @param table  表
     * @param dbItem db数据库树节点
     * @throws Exception 异常
     */
    public void init(SqlServerTable table, ShellSqlServerSchemaTreeItem dbItem) throws Exception {
        StageManager.showMask(this.getWindow(), () -> {
            try {
                this.controller().init(table, dbItem);
                this.flush();
            } catch (Exception ex) {
                MessageBox.exception(ex, this.getWindow());
            }
        });
    }

    @Override
    public ShellSqlServerTableDesignTabController controller() {
        return (ShellSqlServerTableDesignTabController) super.controller();
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
        if (this.isUnsaved() && !MessageBox.confirm(I18nHelper.unsavedAndContinue(), this.getWindow())) {
            event.consume();
        } else {
            this.closeTab();
        }
    }

    @Override
    public ShellSqlServerSchemaTreeItem dbItem() {
        return this.controller().getDbItem();
    }

}
