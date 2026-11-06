package cn.oyzh.easyshell.controller.postgresql.table;

import cn.oyzh.easyshell.postgresql.table.PostgresqlTable;
import cn.oyzh.easyshell.trees.postgresql.schema.ShellPostgresqlSchemaTreeItem;
import cn.oyzh.easyshell.trees.postgresql.table.ShellPostgresqlTableTreeItem;
import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.gui.text.area.ReadOnlyTextArea;
import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * db表信息业务
 *
 * @author oyzh
 * @since 2025-11-21
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "postgresql/table/shellPostgresqlTableInfo.fxml"
)
public class ShellPostgresqlTableInfoController extends StageController {

    /**
     * 名称
     */
    @FXML
    private ReadOnlyTextField name;

    /**
     * 表空间
     */
    @FXML
    private ReadOnlyTextField tableSpace;

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
    private ShellPostgresqlTableTreeItem treeItem;

    /**
     * 初始化信息
     */
    private void initInfo() {
        ShellPostgresqlSchemaTreeItem dbItem = this.treeItem.dbItem();
        PostgresqlTable table = dbItem.selectTable(treeItem.tableName());
        this.name.setText(table.getName());
        this.comment.setText(table.getComment());
        this.tableSpace.setText(table.getTableSpace());
        this.createDefinition.setText(table.getCreateDefinition());
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.treeItem = this.getProp("item");
        StageManager.showMask(this.getWindow(), this::initInfo);
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.tableInfo();
    }
}
