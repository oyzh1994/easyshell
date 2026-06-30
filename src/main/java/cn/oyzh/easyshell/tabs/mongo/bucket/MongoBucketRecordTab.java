package cn.oyzh.easyshell.tabs.mongo.bucket;

import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.mongo.record.MongoRecordFilter;
import cn.oyzh.easyshell.tabs.mongo.ShellMongoBaseTab;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mongo.bucket.ShellMongoBucketTreeItem;
import cn.oyzh.fx.gui.svg.glyph.BucketSVGGlyph;
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
public class MongoBucketRecordTab extends ShellMongoBaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "mongo/bucket/mongoBucketRecordTab.fxml";
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new BucketSVGGlyph();
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    public void flushTitle() {
        // 设置提示文本
        this.setText(this.item().bucketName() + "@" + this.item().dbName() + "(" + this.item().infoName() + ")");
    }

    /**
     * 初始化
     *
     * @param item 树键
     */
    public boolean init(ShellMongoBucketTreeItem item) {
        this.controller().init(item);
        // 刷新tab
        this.flush();
        // 加载耗时处理
        return true;
    }

    @Override
    public MongoBucketRecordTabController controller() {
        return (MongoBucketRecordTabController) super.controller();
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

    public ShellMongoBucketTreeItem item(){
        return this.controller().getItem();
    }
    
    public String bucketName() {
        return this.item().bucketName();
    }

    @Override
    public ShellMongoDatabaseTreeItem dbItem() {
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
