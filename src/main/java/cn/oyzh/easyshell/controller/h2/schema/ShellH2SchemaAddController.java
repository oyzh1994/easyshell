package cn.oyzh.easyshell.controller.h2.schema;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.h2.schema.H2Schema;
import cn.oyzh.easyshell.trees.h2.root.ShellH2RootTreeItem;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
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
 * H2 新增模式页面。
 *
 * @since 2026-11-28
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "h2/schema/shellH2SchemaAdd.fxml"
)
public class ShellH2SchemaAddController extends StageController {

    @FXML
    private ClearableTextField name;

    @FXML
    private ReadOnlyTextField owner;

    private ShellH2RootTreeItem connectItem;

    @FXML
    private void add() {
        try {
            if (!this.name.validate()) {
                return;
            }
            String schemaName = this.name.getTextTrim();
            if (StringUtil.isBlank(schemaName)) {
                MessageBox.warn(I18nHelper.pleaseInputName(), this.getWindow());
                return;
            }
            if (this.connectItem.existSchema(schemaName)) {
                MessageBox.warn(ShellI18nHelper.alreadyExists(schemaName), this.getWindow());
                return;
            }
            H2Schema schema = this.connectItem.client().createSchema(schemaName);
            this.setProp("databaseName", schema.getName());
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
        this.owner.setText(StringUtil.blankToDefault(
                this.connectItem.client().h2Config().getUser(), ""));
        super.onWindowShown(event);
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }
}
