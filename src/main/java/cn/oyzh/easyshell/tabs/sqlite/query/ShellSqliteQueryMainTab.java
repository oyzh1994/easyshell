package cn.oyzh.easyshell.tabs.sqlite.query;

import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.tabs.sqlite.ShellSqliteBaseTab;
import cn.oyzh.easyshell.trees.sqlite.root.ShellSqliteRootTreeItem;
import cn.oyzh.fx.gui.svg.glyph.QuerySVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * SQLite 查询标签页
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteQueryMainTab extends ShellSqliteBaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "sqlite/query/shellSqliteQueryMainTab.fxml";
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new QuerySVGGlyph();
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    public void flushTitle() {
        String name = this.query().getName();
        if (name == null) {
            name = I18nHelper.newQuery();
        }
        if (this.isUnsaved()) {
            this.setText("* " + name + "@" + this.dbName() + "(" + this.connectName() + ")");
        } else {
            this.setText(name + "@" + this.dbName() + "(" + this.connectName() + ")");
        }
    }

    /**
     * 获取查询对象
     *
     * @return 查询对象
     */
    public ShellQuery query() {
        return this.controller().getQuery();
    }

    /**
     * 获取查询id
     *
     * @return 查询id
     */
    public String queryId() {
        return this.query().getUid();
    }

    @Override
    public ShellSqliteRootTreeItem dbItem() {
        return this.controller().getDbItem();
    }

    /**
     * 初始化
     *
     * @param query  查询对象
     * @param dbItem 数据库树节点
     * @return 结果
     */
    public boolean init(ShellQuery query, ShellSqliteRootTreeItem dbItem) {
        this.controller().init(query, dbItem);
        this.flush();
        return true;
    }

    @Override
    public ShellSqliteQueryMainTabController controller() {
        return (ShellSqliteQueryMainTabController) super.controller();
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

    /**
     * 创建实例
     *
     * @param query  查询对象
     * @param dbItem 数据库树节点
     * @return 实例对象
     */
    public static ShellSqliteQueryMainTab of(ShellQuery query, ShellSqliteRootTreeItem dbItem) {
        ShellSqliteQueryMainTab tab = new ShellSqliteQueryMainTab();
        tab.init(query, dbItem);
        return tab;
    }
}
