package cn.oyzh.easyshell.tabs.sqlserver.query;

import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.tabs.sqlserver.ShellSqlServerBaseTab;
import cn.oyzh.easyshell.trees.sqlserver.schema.ShellSqlServerSchemaTreeItem;
import cn.oyzh.fx.gui.svg.glyph.QuerySVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Cursor;

/**
 * SqlServer查询主标签页，负责SQL的编辑与执行
 *
 * @author oyzh
 * @since 2026-06-29
 */
public class ShellSqlServerQueryMainTab extends ShellSqlServerBaseTab {

    // /**
    //  * 内容已变化
    //  */
    // private boolean contentChanged;
    //
    // public void setContentChanged(boolean contentChanged) {
    //     this.contentChanged = contentChanged;
    //     this.flush();
    // }

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "sqlserver/query/shellSqlServerQueryMainTab.fxml";
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
        String queryName = this.query().getName();
        if (queryName == null) {
            queryName = I18nHelper.newQuery();
        }
        // 设置提示文本
        if (this.controller().isUnsaved()) {
            this.text("* " + queryName + "@" + this.schema() + "(" + this.connectName() + ")");
        } else {
            this.text(queryName + "@" + this.schema() + "(" + this.connectName() + ")");
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
    public ShellSqlServerSchemaTreeItem dbItem() {
        return this.controller().getDbItem();
    }

    /**
     * 初始化
     *
     * @param query 查询对象
     * @param item  db库树节点
     */
    public void init(ShellQuery query, ShellSqlServerSchemaTreeItem item) {
        this.controller().init(query, item);
        this.flush();
    }

    @Override
    public ShellSqlServerQueryMainTabController controller() {
        return (ShellSqlServerQueryMainTabController) super.controller();
    }

    /**
     * 创建实例
     *
     * @param query 查询对象
     * @param item 树节点
     * @return 实例对象
     */
    public static ShellSqlServerQueryMainTab of(ShellQuery query, ShellSqlServerSchemaTreeItem item) {
        ShellSqlServerQueryMainTab tab = new ShellSqlServerQueryMainTab();
        tab.init(query, item);
        return tab;
    }
}
