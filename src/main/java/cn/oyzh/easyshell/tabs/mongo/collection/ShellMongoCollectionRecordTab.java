package cn.oyzh.easyshell.tabs.mongo.collection;

import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.mongo.record.MongoRecordFilter;
import cn.oyzh.easyshell.tabs.mongo.ShellMongoBaseTab;
import cn.oyzh.easyshell.trees.mongo.collection.ShellMongoCollectionTreeItem;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
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
public class ShellMongoCollectionRecordTab extends ShellMongoBaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "mongo/collection/shellMongoCollectionRecordTab.fxml";
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
     * @param item 集合树节点
     * @return 是否初始化成功
     */
    public boolean init(ShellMongoCollectionTreeItem item) {
        this.controller().init(item);
        // 刷新tab
        this.flush();
        // 加载耗时处理
        return true;
    }

    @Override
    public ShellMongoCollectionRecordTabController controller() {
        return (ShellMongoCollectionRecordTabController) super.controller();
    }

    @Override
    public void reload() {
        this.controller().reload();
    }

    /**
     * 获取客户端
     *
     * @return 客户端
     */
    public ShellMongoClient client() {
        return this.item().client();
    }

    /**
     * 设置过滤条件
     *
     * @param filters 过滤条件
     */
    public void setFilters(List<MongoRecordFilter> filters) {
        this.controller().setFilters(filters);
    }

    /**
     * 获取树节点
     *
     * @return 树节点
     */
    public ShellMongoCollectionTreeItem item(){
        return this.controller().getItem();
    }
    
    /**
     * 获取集合名称
     *
     * @return 集合名称
     */
    public String collectionName() {
        return this.item().collectionName();
    }

    @Override
    public ShellMongoDatabaseTreeItem dbItem() {
        return this.item().dbItem();
    }

    /**
     * 获取数据库名称
     *
     * @return 数据库名称
     */
    public String dbName() {
        return this.item().dbName();
    }

//    @Override
//    public void initNode() {
//        this.setClosable(true);
//        super.initNode();
//    }
}
