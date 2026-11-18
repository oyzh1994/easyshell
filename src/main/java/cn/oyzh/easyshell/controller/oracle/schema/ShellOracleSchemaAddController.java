package cn.oyzh.easyshell.controller.oracle.schema;

import cn.oyzh.easyshell.oracle.schema.OracleSchema;
import cn.oyzh.easyshell.event.oracle.ShellOracleEventUtil;
import cn.oyzh.easyshell.trees.oracle.root.ShellOracleRootTreeItem;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.PasswordTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * 添加db库业务
 *
 * @author oyzh
 * @since 2026-09-02
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "oracle/schema/shellOracleSchemaAdd.fxml"
)
public class ShellOracleSchemaAddController extends StageController {

    /**
     * 名称
     */
    @FXML
    private ClearableTextField name;

    /** 用户密码 */
    @FXML
    private PasswordTextField password;

    /**
     * db连接节点
     */
    private ShellOracleRootTreeItem connectItem;

    /**
     * 添加db库
     */
    @FXML
    private void add() {
        try {
            // 检查库名称
            if (!this.name.validate()) {
                return;
            }
            // 检查字段是否存在
            String dbName = this.name.getText();
            if (this.connectItem.existSchema(dbName)) {
                MessageBox.warn(ShellI18nHelper.databaseAlreadyExists(dbName), this.getWindow());
                return;
            }
            OracleSchema database = new OracleSchema();
            database.setName(dbName);
            database.setPassword(this.password.getPassword());
            this.connectItem.createSchema(database);
            this.setProp("databaseName", dbName);
            ShellOracleEventUtil.schemaAdded(this.connectItem, database);
            this.closeWindow();
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.addSchema();
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        this.connectItem = this.getProp("connectItem");
        super.onWindowShown(event);
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }
}
