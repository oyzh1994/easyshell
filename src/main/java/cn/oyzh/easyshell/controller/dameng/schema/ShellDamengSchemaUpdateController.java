package cn.oyzh.easyshell.controller.dameng.schema;

import cn.oyzh.easyshell.dameng.schema.DamengSchema;
import cn.oyzh.easyshell.event.dameng.ShellDamengEventUtil;
import cn.oyzh.easyshell.trees.dameng.root.ShellDamengRootTreeItem;
import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * 编辑db库业务
 *
 * @author oyzh
 * @since 2024/01/30
 */
@StageAttribute(
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "dameng/schema/shellDamengSchemaUpdate.fxml"
)
public class ShellDamengSchemaUpdateController extends StageController {

    /**
     * 名称
     */
    @FXML
    private ReadOnlyTextField name;

    /**
     * db模式
     */
    private DamengSchema schema;

    /**
     * db连接节点
     */
    private ShellDamengRootTreeItem connectItem;

    /**
     * 编辑db库
     */
    @FXML
    private void save() {
        try {
            DamengSchema schema = new DamengSchema();
            schema.setName(this.name.getText());
            // 修改数据库
            if (this.connectItem.alterSchema(schema)) {
                ShellDamengEventUtil.schemaUpdated(this.connectItem, schema);
                this.closeWindow();
            } else {
                MessageBox.warn(I18nHelper.operationFail());
            }
        } catch (Exception ex) {
            MessageBox.exception(ex);
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
