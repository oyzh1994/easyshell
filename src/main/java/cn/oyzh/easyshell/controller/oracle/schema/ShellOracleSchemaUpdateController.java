package cn.oyzh.easyshell.controller.oracle.schema;

import cn.oyzh.easyshell.oracle.schema.OracleSchema;
import cn.oyzh.easyshell.event.oracle.ShellOracleEventUtil;
import cn.oyzh.easyshell.trees.oracle.root.ShellOracleRootTreeItem;
import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
import cn.oyzh.fx.gui.text.field.PasswordTextField;
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
        value = FXConst.FXML_PATH + "oracle/schema/shellOracleSchemaUpdate.fxml"
)
public class ShellOracleSchemaUpdateController extends StageController {

    /**
     * 名称
     */
    @FXML
    private ReadOnlyTextField name;

    /** 新用户密码，留空则不修改 */
    @FXML
    private PasswordTextField password;

    /**
     * db模式
     */
    private OracleSchema schema;

    /**
     * db连接节点
     */
    private ShellOracleRootTreeItem connectItem;

    /**
     * 编辑db库
     */
    @FXML
    private void save() {
        try {
            OracleSchema schema = new OracleSchema();
            schema.setName(this.name.getText());
            schema.setPassword(this.password.getPassword());
            // 修改数据库
            if (this.connectItem.alterSchema(schema)) {
                ShellOracleEventUtil.schemaUpdated(this.connectItem, schema);
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
