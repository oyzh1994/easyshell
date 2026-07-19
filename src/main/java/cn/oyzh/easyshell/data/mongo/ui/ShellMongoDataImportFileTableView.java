package cn.oyzh.easyshell.data.mongo.ui;

import cn.oyzh.easyshell.data.mongo.dto.ShellMongoDataImportFile;
import cn.oyzh.fx.plus.controls.table.FXTableView;

/**
 * @author oyzh
 * @since 2024/08/30
 */
public class ShellMongoDataImportFileTableView extends FXTableView<ShellMongoDataImportFile> {

    ///**
    // * 数据库名称
    // */
    //private String dbName;
    //
    ///**
    // * 数据库客户端
    // */
    //private ShellMongoClient dbClient;
    //
    //public void setDbName(String dbName) {
    //    this.dbName = dbName;
    //    this.initItems();
    //}
    //
    //public void setDbClient(ShellMongoClient dbClient) {
    //    this.dbClient = dbClient;
    //    this.initItems();
    //}
    //
    //private void initItems() {
    //    for (ShellMongoDataImportFile file : this.getItems()) {
    //        file.setDbName(this.dbName);
    //        file.setDbClient(this.dbClient);
    //    }
    //}
    //
    //@Override
    //public void initNode() {
    //    super.initNode();
    //    this.itemList().addListener((ListChangeListener<ShellMongoDataImportFile>) c -> {
    //        while (c.next()) {
    //            if (c.wasAdded() || c.wasReplaced()) {
    //                this.initItems();
    //            }
    //        }
    //    });
    //}
}
