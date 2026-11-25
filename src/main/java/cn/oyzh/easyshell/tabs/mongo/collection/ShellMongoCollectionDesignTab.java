package cn.oyzh.easyshell.tabs.mongo.collection;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mongo.collection.MongoCollection;
import cn.oyzh.easyshell.tabs.mongo.ShellMongoBaseTab;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.svg.glyph.database.TableSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * MongoDB 集合设计标签页。
 *
 * @author oyzh
 * @since 2026-11-25
 */
public class ShellMongoCollectionDesignTab extends ShellMongoBaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "mongo/collection/shellMongoCollectionDesignTab.fxml";
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
        String name = this.collectionName();
        if (StringUtil.isBlank(name)) {
            name = ShellI18nHelper.mongoUnnamedCollection();
        }
        if (this.isUnsaved()) {
            this.text("* " + name + "@" + this.dbName() + "(" + this.connectName() + ")");
        } else {
            this.text(name + "@" + this.dbName() + "(" + this.connectName() + ")");
        }
    }

    /**
     * 获取集合名称
     *
     * @return 集合名称
     */
    public String collectionName() {
        return this.controller().collectionName();
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
     * @param collection 集合
     * @param dbItem     数据库节点
     */
    public void init(MongoCollection collection, ShellMongoDatabaseTreeItem dbItem) {
        this.controller().init(collection, dbItem);
        this.flush();
    }

    @Override
    public ShellMongoCollectionDesignTabController controller() {
        return (ShellMongoCollectionDesignTabController) super.controller();
    }

    @Override
    public ShellMongoDatabaseTreeItem dbItem() {
        return this.controller().getDbItem();
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
}
