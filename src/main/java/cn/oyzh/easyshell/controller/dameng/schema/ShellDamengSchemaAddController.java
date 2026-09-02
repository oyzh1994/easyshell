package cn.oyzh.easyshell.controller.dameng.schema;

import cn.oyzh.easyshell.dameng.schema.DamengSchema;
import cn.oyzh.easyshell.event.dameng.ShellDamengEventUtil;
import cn.oyzh.easyshell.trees.dameng.root.ShellDamengRootTreeItem;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
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
 * 添加db库业务
 *
 * @author oyzh
 * @since 2024/01/30
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "dameng/database/damengDatabaseAdd.fxml"
)
public class ShellDamengSchemaAddController extends StageController {

    /**
     * 名称
     */
    @FXML
    private ClearableTextField name;

//    /**
//     * 字符集
//     */
//    @FXML
//    private DBCharsetComboBox charset;
//
//    /**
//     * 排序方式
//     */
//    @FXML
//    private DBCollationComboBox collation;

    /**
     * db连接节点
     */
    private ShellDamengRootTreeItem connectItem;

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
                MessageBox.warn(I18nHelper.database() + " " + dbName + " " + I18nHelper.alreadyExists());
                return;
            }
            DamengSchema database = new DamengSchema();
            database.setName(dbName);
            this.connectItem.createSchema(database);
            this.setProp("databaseName", dbName);
            ShellDamengEventUtil.schemaAdded(this.connectItem, database);
            this.closeWindow();
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
//        // 字符集选中事件
//        this.charset.selectedItemChanged((observable, oldValue, newValue) -> {
//            if (newValue != null) {
//                this.collation.init(newValue, this.connectItem.getClient());
//                this.collation.select(0);
//                this.collation.enable();
//            } else {
//                this.collation.clearItems();
//                this.collation.disable();
//            }
//        });
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.addDatabase();
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        this.connectItem = this.getProp("connectItem");

//        // 初始化字符集和排序
//        this.charset.init(this.connectItem.getClient());
//        this.charset.enable();
//        this.collation.disable();

        super.onWindowShown(event);
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }
}
