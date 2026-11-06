package cn.oyzh.easyshell.controller.postgresql.schema;

import cn.oyzh.easyshell.postgresql.schema.PostgresqlSchema;
import cn.oyzh.easyshell.event.postgresql.ShellPostgresqlEventUtil;
import cn.oyzh.easyshell.trees.postgresql.root.ShellPostgresqlRootTreeItem;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
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
        value = FXConst.FXML_PATH + "postgresql/schema/shellPostgresqlSchemaAdd.fxml"
)
public class ShellPostgresqlSchemaAddController extends StageController {

    /**
     * 名称
     */
    @FXML
    private ClearableTextField name;

    /**
     * db连接节点
     */
    private ShellPostgresqlRootTreeItem connectItem;

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
            PostgresqlSchema database = new PostgresqlSchema();
            database.setName(dbName);
            this.connectItem.createSchema(database);
            this.setProp("databaseName", dbName);
            ShellPostgresqlEventUtil.schemaAdded(this.connectItem, database);
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
