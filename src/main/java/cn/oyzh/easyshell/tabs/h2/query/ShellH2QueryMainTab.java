package cn.oyzh.easyshell.tabs.h2.query;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.tabs.h2.ShellH2BaseTab;
import cn.oyzh.easyshell.trees.h2.root.ShellH2RootTreeItem;
import cn.oyzh.fx.gui.svg.glyph.QuerySVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * H2 查询标签页
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellH2QueryMainTab extends ShellH2BaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "h2/query/shellH2QueryMainTab.fxml";
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
            this.text("* " + name + "@" + this.dbName() + "(" + this.connectName() + ")");
        } else {
            this.text(name + "@" + this.dbName() + "(" + this.connectName() + ")");
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
    public ShellH2RootTreeItem dbItem() {
        return this.controller().getDbItem();
    }

    @Override
    public String dbName() {
        ShellQuery query = this.query();
        return query == null || StringUtil.isBlank(query.getDbName())
                ? super.dbName() : query.getDbName();
    }

    /**
     * 初始化
     *
     * @param query  查询对象
     * @param dbItem 数据库树节点
     * @return 结果
     */
    public boolean init(ShellQuery query, ShellH2RootTreeItem dbItem) {
        this.controller().init(query, dbItem);
        this.flush();
        return true;
    }

    @Override
    public ShellH2QueryMainTabController controller() {
        return (ShellH2QueryMainTabController) super.controller();
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
    public static ShellH2QueryMainTab of(ShellQuery query, ShellH2RootTreeItem dbItem) {
        ShellH2QueryMainTab tab = new ShellH2QueryMainTab();
        tab.init(query, dbItem);
        return tab;
    }
}
