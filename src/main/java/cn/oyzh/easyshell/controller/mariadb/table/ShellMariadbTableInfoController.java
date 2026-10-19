package cn.oyzh.easyshell.controller.mariadb.table;

import cn.oyzh.easyshell.mariadb.table.MariadbTable;
import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
import cn.oyzh.easyshell.trees.mariadb.table.ShellMariadbTableTreeItem;
import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.gui.text.area.ReadOnlyTextArea;
import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * MariaDB表信息业务
 *
 * @author oyzh
 * @since 2026-10-09
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "mariadb/table/shellMariadbTableInfo.fxml"
)
public class ShellMariadbTableInfoController extends StageController {

    /**
     * 名称
     */
    @FXML
    private ReadOnlyTextField name;

    /**
     * 引擎
     */
    @FXML
    private ReadOnlyTextField tableEngine;

    /**
     * 字符集
     */
    @FXML
    private ReadOnlyTextField tableCharset;

    /**
     * 排序方式
     */
    @FXML
    private ReadOnlyTextField tableCollation;

    /**
     * 行格式组件
     */
    @FXML
    private FXVBox tableRowFormatBox;

    /**
     * 行格式
     */
    @FXML
    private ReadOnlyTextField tableRowFormat;

    /**
     * 自动递增组件
     */
    @FXML
    private FXVBox tableAutoIncrementBox;

    /**
     * 自动递增
     */
    @FXML
    private ReadOnlyTextField tableAutoIncrement;

    /**
     * 注释
     */
    @FXML
    private ReadOnlyTextArea comment;

    /**
     * 定义
     */
    @FXML
    private Editor createDefinition;

    /**
     * 表节点
     */
    private ShellMariadbTableTreeItem treeItem;

    /**
     * 初始化信息
     */
    private void initInfo() {
        ShellMariadbDatabaseTreeItem dbItem = this.treeItem.dbItem();
        MariadbTable table = dbItem.selectTable(treeItem.tableName());
        this.name.setText(table.getName());
        this.comment.setText(table.getComment());
        this.tableEngine.setText(table.getEngine());
        this.tableCharset.setText(table.getCharset());
        this.tableCollation.setText(table.getCollation());
        this.createDefinition.setText(table.getCreateDefinition());
        if (table.isInnoDB()) {
            this.tableRowFormatBox.display();
            this.tableRowFormat.setText(table.getRowFormat());
        }
        if (table.hasAutoIncrement()) {
            this.tableAutoIncrementBox.display();
            this.tableAutoIncrement.setText(table.getAutoIncrement() + "");
        }
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.treeItem = this.getProp("item");
        StageManager.showMask(this::initInfo);
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.tableInfo();
    }
}
