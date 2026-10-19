package cn.oyzh.easyshell.tabs.mongo.query;

import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.tabs.mongo.ShellMongoBaseTab;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.fx.gui.svg.glyph.QuerySVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Cursor;

/**
 * mongodb查询tab
 *
 * @author oyzh
 * @since 2026-06-29
 */
public class ShellMongoQueryMainTab extends ShellMongoBaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "mongo/query/shellMongoQueryMainTab.fxml";
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
            this.setText("* " + queryName + "@" + this.dbName() + "(" + this.connectName() + ")");
        } else {
            this.setText(queryName + "@" + this.dbName() + "(" + this.connectName() + ")");
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
    public ShellMongoDatabaseTreeItem dbItem() {
        return this.controller().getDbItem();
    }

    /**
     * 获取数据库名称
     *
     * @return 数据库名称
     */
    public String dbName() {
        return this.dbItem().dbName();
    }

    /**
     * 获取连接名称
     *
     * @return 连接名称
     */
    public String connectName() {
        return this.dbItem().connectName();
    }

    /**
     * 初始化
     *
     * @param query 查询对象
     * @param item  db库树节点
     * @return 是否初始化成功
     */
    public boolean init(ShellQuery query, ShellMongoDatabaseTreeItem item) {
        this.controller().init( query, item);
        this.flush();
        return true;
    }

    @Override
    public ShellMongoQueryMainTabController controller() {
        return (ShellMongoQueryMainTabController) super.controller();
    }

    /**
     * 创建实例
     *
     * @param query 查询对象
     * @param item 树节点
     * @return 实例对象
     */
    public static ShellMongoQueryMainTab of(ShellQuery query, ShellMongoDatabaseTreeItem item) {
        ShellMongoQueryMainTab tab = new ShellMongoQueryMainTab();
        tab.init(query, item);
        return tab;
    }

//    @Override
//    public void initNode() {
//        this.setClosable(true);
//        super.initNode();
//    }
}
