package cn.oyzh.easyshell.tabs.sqlite.table;

import cn.oyzh.easyshell.sqlite.record.SQLiteRecordFilter;
import cn.oyzh.easyshell.tabs.sqlite.ShellSqliteBaseTab;
import cn.oyzh.easyshell.trees.sqlite.root.ShellSqliteRootTreeItem;
import cn.oyzh.easyshell.trees.sqlite.table.ShellSqliteTableTreeItem;
import cn.oyzh.fx.gui.svg.glyph.database.TableSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.Cursor;

import java.util.List;

/**
 * SQLite 表记录标签页
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteTableRecordTab extends ShellSqliteBaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "sqlite/table/shellSqliteTableRecordTab.fxml";
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
        this.setText(this.item().tableName() + "@" + this.item().dbName() + "(" + this.item().infoName() + ")");
    }

    /**
     * 初始化
     *
     * @param item 表树节点
     * @return 结果
     */
    public boolean init(ShellSqliteTableTreeItem item) {
        this.controller().init(item);
        this.flush();
        return true;
    }

    @Override
    public ShellSqliteTableRecordTabController controller() {
        return (ShellSqliteTableRecordTabController) super.controller();
    }

    @Override
    public void reload() {
        this.controller().reload();
    }

    /**
     * 设置过滤条件
     *
     * @param filters 过滤条件
     */
    public void setFilters(List<SQLiteRecordFilter> filters) {
        this.controller().setFilters(filters);
    }

    /**
     * 获取树节点
     *
     * @return 树节点
     */
    public ShellSqliteTableTreeItem item() {
        return this.controller().getItem();
    }

    /**
     * 获取表名称
     *
     * @return 表名称
     */
    public String tableName() {
        return this.item().tableName();
    }

    @Override
    public ShellSqliteRootTreeItem dbItem() {
        return this.item().dbItem();
    }
}
