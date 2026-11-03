package cn.oyzh.easyshell.tabs.sqlite.view;

import cn.oyzh.easyshell.sqlite.record.SQLiteRecordFilter;
import cn.oyzh.easyshell.tabs.sqlite.ShellSqliteBaseTab;
import cn.oyzh.easyshell.trees.sqlite.root.ShellSqliteRootTreeItem;
import cn.oyzh.easyshell.trees.sqlite.view.ShellSqliteViewTreeItem;
import cn.oyzh.fx.gui.svg.glyph.database.ViewSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import javafx.scene.Cursor;

import java.util.List;

/**
 * SQLite 视图记录标签页
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteViewRecordTab extends ShellSqliteBaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "sqlite/view/shellSqliteViewRecordTab.fxml";
    }

    @Override
    public void flushGraphic() {
        ViewSVGGlyph graphic = (ViewSVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new ViewSVGGlyph();
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    public void flushTitle() {
        this.setText(this.item().viewName() + "@" + this.item().dbName() + "(" + this.item().infoName() + ")");
    }

    /**
     * 初始化
     *
     * @param item 视图树节点
     * @return 结果
     */
    public boolean init(ShellSqliteViewTreeItem item) {
        this.controller().init(item);
        this.flush();
        return true;
    }

    @Override
    public ShellSqliteViewRecordTabController controller() {
        return (ShellSqliteViewRecordTabController) super.controller();
    }

    @Override
    public void reload() {
        this.controller().reload();
    }

    /**
     * 获取树节点
     *
     * @return 树节点
     */
    public ShellSqliteViewTreeItem item() {
        return this.controller().getItem();
    }

    /**
     * 获取视图名称
     *
     * @return 视图名称
     */
    public String viewName() {
        return this.item().viewName();
    }

    @Override
    public ShellSqliteRootTreeItem dbItem() {
        return this.item().dbItem();
    }

    /**
     * 设置过滤条件
     *
     * @param filters 过滤条件
     */
    public void setFilters(List<SQLiteRecordFilter> filters) {
        this.controller().setFilters(filters);
    }
}
