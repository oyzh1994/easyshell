package cn.oyzh.easyshell.tabs.zk.query;

import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.trees.zk.query.ShellZKQueriesTreeItem;
import cn.oyzh.fx.gui.svg.glyph.QuerySVGGlyph;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * zk查询标签页
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellZKQueryTab extends RichTab {

    /**
     * 查询分类节点
     */
    private ShellZKQueriesTreeItem queriesItem;

    @Override
    protected String url() {
        return "/tabs/zk/query/shellZKQueryTab.fxml";
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
        String connectName = this.queriesItem == null ? "" : this.queriesItem.connect().getName();
        if (this.isUnsaved()) {
            this.setText("* " + name + "(" + connectName + ")");
        } else {
            this.setText(name + "(" + connectName + ")");
        }
    }

    /**
     * 初始化
     *
     * @param query       查询对象
     * @param queriesItem 查询分类节点
     */
    public void init(ShellQuery query, ShellZKQueriesTreeItem queriesItem) {
        this.queriesItem = queriesItem;
        this.controller().init(query, queriesItem);
        this.flush();
    }

    @Override
    public ShellZKQueryTabController controller() {
        return (ShellZKQueryTabController) super.controller();
    }

    /**
     * 获取查询对象
     *
     * @return 查询对象
     */
    public ShellQuery query() {
        ShellZKQueryTabController controller = this.controller();
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
        ShellZKQueryTabController controller = this.controller();
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
        ShellZKQueryTabController controller = this.controller();
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
