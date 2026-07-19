package cn.oyzh.easyshell.data.mysql.ui;

import cn.oyzh.easyshell.data.mysql.dto.ShellMysqlDataImportFile;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import javafx.collections.ListChangeListener;

/**
 * @author oyzh
 * @since 2024/08/30
 */
public class ShellMysqlDataImportFileTableView extends FXTableView<ShellMysqlDataImportFile> {

    /**
     * 数据库名称
     */
    private String dbName;

    /**
     * 数据库客户端
     */
    private ShellMysqlClient dbClient;

    public void setDbName(String dbName) {
        this.dbName = dbName;
        this.initItems();
    }

    public void setDbClient(ShellMysqlClient dbClient) {
        this.dbClient = dbClient;
        this.initItems();
    }

    private void initItems() {
        for (ShellMysqlDataImportFile file : this.getItems()) {
            file.setDbName(this.dbName);
            file.setDbClient(this.dbClient);
        }
    }

    @Override
    public void initNode() {
        super.initNode();
        this.itemList().addListener((ListChangeListener<ShellMysqlDataImportFile>) c -> {
            while (c.next()) {
                if (c.wasAdded() || c.wasReplaced()) {
                    this.initItems();
                }
            }
        });
    }
}
