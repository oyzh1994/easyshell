package cn.oyzh.easyshell.data.mariadb.ui;

import cn.oyzh.easyshell.data.mariadb.dto.ShellMariadbDataImportFile;
import cn.oyzh.fx.plus.controls.table.FXTableView;

/**
 * Mariadb数据导入文件表格视图
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbDataImportFileTableView extends FXTableView<ShellMariadbDataImportFile> {

    ///**
    // * 数据库名称
    // */
    //private String dbName;
    //
    ///**
    // * 数据库客户端
    // */
    //private ShellMariadbClient dbClient;
    //
    //public void setDbName(String dbName) {
    //    this.dbName = dbName;
    //    this.initItems();
    //}
    //
    //public void setDbClient(ShellMariadbClient dbClient) {
    //    this.dbClient = dbClient;
    //    this.initItems();
    //}
    //
    //private void initItems() {
    //    for (ShellMariadbDataImportFile file : this.getItems()) {
    //        file.setDbName(this.dbName);
    //        file.setDbClient(this.dbClient);
    //    }
    //}
    //
    //@Override
    //public void initNode() {
    //    super.initNode();
    //    this.itemList().addListener((ListChangeListener<ShellMariadbDataImportFile>) c -> {
    //        while (c.next()) {
    //            if (c.wasAdded() || c.wasReplaced()) {
    //                this.initItems();
    //            }
    //        }
    //    });
    //}
}
