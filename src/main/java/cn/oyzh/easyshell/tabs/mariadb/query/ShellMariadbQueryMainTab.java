package cn.oyzh.easyshell.tabs.mariadb.query;

import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.tabs.mariadb.ShellMariadbBaseTab;
import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
import cn.oyzh.fx.gui.svg.glyph.QuerySVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * MariaDB 查询标签页
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbQueryMainTab extends ShellMariadbBaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "mariadb/query/shellMariadbQueryMainTab.fxml";
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
        // 设置提示文本
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
    public ShellMariadbDatabaseTreeItem dbItem() {
        return this.controller().getDbItem();
    }

    /**
     * 初始化
     *
     * @param query 查询对象
     * @param item  db库树节点
     * @return 结果
     */
    public boolean init(ShellQuery query, ShellMariadbDatabaseTreeItem item) {
        this.controller().init(query, item);
        this.flush();
        return true;
    }

    @Override
    public ShellMariadbQueryMainTabController controller() {
        return (ShellMariadbQueryMainTabController) super.controller();
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

    /**
     * 创建实例
     *
     * @param query 查询对象
     * @param item 树节点
     * @return 实例对象
     */
    public static ShellMariadbQueryMainTab of(ShellQuery query, ShellMariadbDatabaseTreeItem item) {
        ShellMariadbQueryMainTab tab = new ShellMariadbQueryMainTab();
        tab.init(query, item);
        return tab;
    }
}
