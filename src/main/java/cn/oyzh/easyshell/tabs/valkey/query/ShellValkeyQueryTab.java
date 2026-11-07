package cn.oyzh.easyshell.tabs.valkey.query;

import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.tabs.valkey.ShellValkeyBaseTab;
import cn.oyzh.easyshell.trees.valkey.database.ShellValkeyDatabaseTreeItem;
import cn.oyzh.fx.gui.svg.glyph.QuerySVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * redis查询标签页
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellValkeyQueryTab extends ShellValkeyBaseTab {

    /**
     * 数据库节点
     */
    private ShellValkeyDatabaseTreeItem dbItem;

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "valkey/query/shellValkeyQueryTab.fxml";
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
        ShellQuery query = this.query();
        if (query == null) {
            return;
        }
        String name = query.getName();
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
     * 初始化
     *
     * @param query  查询对象
     * @param dbItem 数据库节点
     */
    public void init(ShellQuery query, ShellValkeyDatabaseTreeItem dbItem) {
        this.dbItem = dbItem;
        this.controller().init(query, dbItem);
        this.flush();
    }

    @Override
    public ShellValkeyQueryTabController controller() {
        return (ShellValkeyQueryTabController) super.controller();
    }

    @Override
    public ShellValkeyDatabaseTreeItem dbItem() {
        return dbItem;
    }

    /**
     * 获取查询对象
     *
     * @return 查询对象
     */
    public ShellQuery query() {
        ShellValkeyQueryTabController controller = this.controller();
        return controller == null ? null : controller.getQuery();
    }

    /**
     * 获取查询id
     *
     * @return 查询id
     */
    public String queryId() {
        ShellQuery query = this.query();
        return query == null ? null : query.getUid();
    }

    /**
     * 重命名查询
     *
     * @param name 查询名称
     */
    public void rename(String name) {
        ShellValkeyQueryTabController controller = this.controller();
        if (controller != null) {
            controller.renameQuery(name);
        }
    }

    /**
     * 是否未保存
     *
     * @return 结果
     */
    public boolean isUnsaved() {
        ShellValkeyQueryTabController controller = this.controller();
        return controller != null && controller.isUnsaved();
    }

    @Override
    protected void onTabCloseRequest(Event event) {
        if (this.isUnsaved() && !MessageBox.confirm(I18nHelper.unsavedAndContinue(), this.getWindow())) {
            event.consume();
        } else {
            this.closeTab();
        }
    }
}
