package cn.oyzh.easyshell.tabs.sqlite.view;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.easyshell.tabs.sqlite.ShellSqliteBaseTab;
import cn.oyzh.easyshell.trees.sqlite.view.ShellSqliteViewTreeItem;
import cn.oyzh.fx.gui.svg.glyph.database.ViewSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Cursor;

/**
 * SQLite 视图数据标签页。
 *
 * @since 2026-11-02
 */
public class ShellSqliteViewRecordTab extends ShellSqliteBaseTab {

    private ShellSqliteViewTreeItem item;

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "sqlite/view/shellSqliteViewRecordTab.fxml";
    }

    @Override
    public ShellSqliteClient client() {
        return this.item == null ? null : this.item.client();
    }

    public void init(ShellSqliteViewTreeItem item) {
        this.item = item;
        this.controller().init(item);
        this.flush();
    }

    public String viewName() {
        return this.item == null ? null : this.item.viewName();
    }

    public ShellSqliteViewTreeItem item() {
        return this.item;
    }

    public void reload() {
        this.controller().reload();
    }

    @Override
    protected ShellSqliteViewRecordTabController controller() {
        return (ShellSqliteViewRecordTabController) super.controller();
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new ViewSVGGlyph();
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    public void flushTitle() {
        String name = StringUtil.blankToDefault(this.viewName(), I18nHelper.unnamedView());
        String connectName = this.item == null ? "" : this.item.infoName();
        this.setText(name + "@main(" + connectName + ")");
    }
}
