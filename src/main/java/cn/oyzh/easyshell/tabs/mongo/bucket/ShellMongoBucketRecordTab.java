package cn.oyzh.easyshell.tabs.mongo.bucket;

import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.mongo.record.MongoRecordFilter;
import cn.oyzh.easyshell.tabs.mongo.ShellMongoBaseTab;
import cn.oyzh.easyshell.trees.mongo.bucket.ShellMongoBucketTreeItem;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.fx.gui.svg.glyph.BucketSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.Cursor;

import java.util.List;

/**
 * mongodb存储桶tab
 *
 * @author oyzh
 * @since 2026-06-29
 */
public class ShellMongoBucketRecordTab extends ShellMongoBaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "mongo/bucket/shellMongoBucketRecordTab.fxml";
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
        this.setText(this.item().bucketName() + "@" + this.item().dbName() + "(" + this.item().connectName() + ")");
    }

    /**
     * 初始化
     *
     * @param item 存储桶树节点
     * @return 是否初始化成功
     */
    public boolean init(ShellMongoBucketTreeItem item) {
        this.controller().init(item);
        // 刷新tab
        this.flush();
        // 加载耗时处理
        return true;
    }

    @Override
    public ShellMongoBucketRecordTabController controller() {
        return (ShellMongoBucketRecordTabController) super.controller();
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
    public ShellMongoBucketTreeItem item(){
        return this.controller().getItem();
    }
    
    /**
     * 获取桶名称
     *
     * @return 桶名称
     */
    public String bucketName() {
        return this.item().bucketName();
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
