package cn.oyzh.easyshell.tabs.sqlite.table;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.easyshell.tabs.sqlite.ShellSqliteBaseTab;
import cn.oyzh.easyshell.trees.sqlite.table.ShellSqliteTableTreeItem;
import cn.oyzh.fx.gui.svg.glyph.database.TableSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Cursor;

/**
 * SQLite 表数据标签页。
 *
 * @since 2026-11-02
 */
public class ShellSqliteTableRecordTab extends ShellSqliteBaseTab {

    private ShellSqliteTableTreeItem item;

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "sqlite/table/shellSqliteTableRecordTab.fxml";
    }

    @Override
    public ShellSqliteClient client() {
        return this.item == null ? null : this.item.client();
    }

    public void init(ShellSqliteTableTreeItem item) {
        this.item = item;
        this.controller().init(item);
        this.flush();
    }

    public String tableName() {
        return this.item == null ? null : this.item.tableName();
    }

    public ShellSqliteTableTreeItem item() {
        return this.item;
    }

    public void reload() {
        this.controller().reload();
    }

    @Override
    protected ShellSqliteTableRecordTabController controller() {
        return (ShellSqliteTableRecordTabController) super.controller();
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
        String name = StringUtil.blankToDefault(this.tableName(), I18nHelper.unnamedTable());
        String connectName = this.item == null ? "" : this.item.infoName();
        this.setText(name + "@main(" + connectName + ")");
    }
}
