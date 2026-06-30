package cn.oyzh.easyshell.controller.mongo.database;

import cn.oyzh.easyshell.event.mongo.ShellMongoEventUtil;
import cn.oyzh.easyshell.mongo.database.MongoDatabase;
import cn.oyzh.easyshell.trees.mongo.root.ShellMongoRootTreeItem;
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
        modality = Modality.APPLICATION_MODAL,
        stageStyle = FXStageStyle.EXTENDED,
        value = FXConst.FXML_PATH + "mongo/database/mongoDatabaseAdd.fxml"
)
public class MongoDatabaseAddController extends StageController {

    /**
     * 名称
     */
    @FXML
    private ClearableTextField name;

    /**
     * db连接节点
     */
    private ShellMongoRootTreeItem connectItem;

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
            if (this.connectItem.existDatabase(dbName)) {
                MessageBox.warn(I18nHelper.database() + " " + dbName + " " + I18nHelper.alreadyExists());
                return;
            }
            this.connectItem.createDatabase(dbName);
            this.setProp("databaseName", dbName);
            MongoDatabase database = new MongoDatabase();
            database.setName(dbName);
            ShellMongoEventUtil.databaseAdded(this.connectItem, database);
            this.closeWindow();
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.addDatabase();
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        this.connectItem = this.getProp("connectItem");

        super.onWindowShown(event);
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }
}
