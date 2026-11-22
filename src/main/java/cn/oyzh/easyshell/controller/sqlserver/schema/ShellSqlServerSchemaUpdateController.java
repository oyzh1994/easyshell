package cn.oyzh.easyshell.controller.sqlserver.schema;

import cn.oyzh.easyshell.sqlserver.schema.SqlServerSchema;
import cn.oyzh.easyshell.event.sqlserver.ShellSqlServerEventUtil;
import cn.oyzh.easyshell.trees.sqlserver.root.ShellSqlServerRootTreeItem;
import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * 编辑db库业务
 *
 * @author oyzh
 * @since 2026-09-02
 */
@StageAttribute(
        modality = Modality.APPLICATION_MODAL,
        stageStyle = FXStageStyle.EXTENDED,
        value = FXConst.FXML_PATH + "sqlserver/schema/shellSqlServerSchemaUpdate.fxml"
)
public class ShellSqlServerSchemaUpdateController extends StageController {

    /**
     * 名称
     */
    @FXML
    private ReadOnlyTextField name;

    /**
     * db模式
     */
    private SqlServerSchema schema;

    /**
     * db连接节点
     */
    private ShellSqlServerRootTreeItem connectItem;

    /**
     * 编辑db库
     */
    @FXML
    private void save() {
        try {
            SqlServerSchema schema = new SqlServerSchema();
            schema.setName(this.name.getText());
            // 修改数据库
            if (this.connectItem.alterSchema(schema)) {
                ShellSqlServerEventUtil.schemaUpdated(this.connectItem, schema);
                this.closeWindow();
            } else {
                MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
            }
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        this.schema = this.getProp("database");
        this.connectItem = this.getProp("connectItem");
        // 数据库名
        this.name.setText(this.schema.getName());
        super.onWindowShown(event);
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.updateSchema();
    }
}
