package cn.oyzh.easyshell.tabs.mongo.collection;

import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.mongo.record.MongoRecordFilter;
import cn.oyzh.easyshell.tabs.mongo.ShellMongoBaseTab;
import cn.oyzh.easyshell.trees.mongo.collection.MongoCollectionTreeItem;
import cn.oyzh.easyshell.trees.mongo.database.MongoDatabaseTreeItem;
import cn.oyzh.fx.gui.svg.glyph.database.TableSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.Cursor;

import java.util.List;

/**
 * db表tab
 *
 * @author oyzh
 * @since 2023/12/24
 */
public class MongoCollectionRecordTab extends ShellMongoBaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "mongo/collection/mongoCollectionRecordTab.fxml";
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
        // 设置提示文本
        this.setText(this.item().collectionName() + "@" + this.item().dbName() + "(" + this.item().infoName() + ")");
    }

    /**
     * 初始化
     *
     * @param item 树键
     */
    public boolean init(MongoCollectionTreeItem item) {
        this.controller().init(item);
        // 刷新tab
        this.flush();
        // 加载耗时处理
        return true;
    }

    @Override
    public MongoCollectionRecordTabController controller() {
        return (MongoCollectionRecordTabController) super.controller();
    }

    @Override
    public void reload() {
        this.controller().reload();
    }

    public ShellMongoClient client() {
        return this.item().client();
    }

    public void setFilters(List<MongoRecordFilter> filters) {
        this.controller().setFilters(filters);
    }

    public MongoCollectionTreeItem item(){
        return this.controller().getItem();
    }
    
    public String collectionName() {
        return this.item().collectionName();
    }

    @Override
    public MongoDatabaseTreeItem dbItem() {
        return this.item().dbItem();
    }

    public String dbName() {
        return this.item().dbName();
    }

//    @Override
//    public void initNode() {
//        this.setClosable(true);
//        super.initNode();
//    }
}
