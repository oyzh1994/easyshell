package cn.oyzh.easyshell.controller.h2.schema;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.h2.schema.H2Schema;
import cn.oyzh.easyshell.store.ShellQueryStore;
import cn.oyzh.easyshell.trees.h2.root.ShellH2RootTreeItem;
import cn.oyzh.easyshell.trees.h2.schema.ShellH2SchemaTreeItem;
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

import java.util.List;

/**
 * H2 编辑模式页面。
 *
 * @since 2026-11-28
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "h2/schema/shellH2SchemaUpdate.fxml"
)
public class ShellH2SchemaUpdateController extends StageController {

    @FXML
    private ClearableTextField name;

    @FXML
    private ReadOnlyTextField owner;

    private H2Schema schema;

    private ShellH2RootTreeItem connectItem;

    @FXML
    private void save() {
        try {
            if (!this.name.validate()) {
                return;
            }
            String targetName = this.name.getTextTrim();
            if (StringUtil.isBlank(targetName)) {
                MessageBox.warn(I18nHelper.pleaseInputName(), this.getWindow());
                return;
            }
            if (StringUtil.equalsIgnoreCase(targetName, this.schema.getName())) {
                this.closeWindow();
                return;
            }
            if (this.connectItem.existSchema(targetName)) {
                MessageBox.warn(ShellI18nHelper.alreadyExists(targetName), this.getWindow());
                return;
            }
            String originalName = this.schema.getName();
            List<ShellQuery> queries = ShellQueryStore.INSTANCE.list(
                    this.connectItem.info().getId(), originalName);
            ShellH2SchemaTreeItem item = this.connectItem.schemaGroup(originalName);
            this.connectItem.renameSchema(originalName, targetName);
            this.connectItem.tabPane().closeSchemaTabs(originalName);
            this.schema.setName(targetName);
            if (item != null) {
                item.setSchemaName(targetName);
            }
            if (queries != null) {
                for (ShellQuery query : queries) {
                    query.setDbName(targetName);
                    ShellQueryStore.INSTANCE.update(query);
                }
            }
            this.connectItem.doSort();
            this.closeWindow();
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        this.schema = this.getProp("database");
        this.connectItem = this.getProp("connectItem");
        this.name.setText(this.schema.getName());
        this.owner.setText(StringUtil.blankToDefault(this.schema.getOwner(), ""));
        super.onWindowShown(event);
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.updateSchema();
    }
}
