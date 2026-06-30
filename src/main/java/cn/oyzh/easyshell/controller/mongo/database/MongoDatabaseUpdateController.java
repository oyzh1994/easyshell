//package cn.oyzh.easymongo.controller.database;
//
//import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
//import cn.oyzh.fx.plus.FXConst;
//import cn.oyzh.fx.plus.controller.StageController;
//import cn.oyzh.fx.plus.information.MessageBox;
//import cn.oyzh.fx.plus.window.StageAttribute;
//import cn.oyzh.i18n.I18nHelper;
//import cn.oyzh.easymongo.event.ShellMongoEventUtil;
//import cn.oyzh.easymongo.mongo.MongoDatabase;
//import cn.oyzh.easymongo.trees.connect.MongoConnectTreeItem;
//import javafx.fxml.FXML;
//import javafx.stage.Modality;
//import javafx.stage.WindowEvent;
//
///**
// * 编辑db库业务
// *
// * @author oyzh
// * @since 2024/01/30
// */
//@StageAttribute(
//        modality = Modality.APPLICATION_MODAL,
//        value = FXConst.FXML_PATH + "mongo/database/mongoDatabaseUpdate.fxml"
//)
//public class MongoDatabaseUpdateController extends StageController {
//
//    /**
//     * 名称
//     */
//    @FXML
//    private ReadOnlyTextField name;
//
//    /**
//     * db库对象
//     */
//    private MongoDatabase database;
//
//    /**
//     * db连接节点
//     */
//    private MongoConnectTreeItem connectItem;
//
//    /**
//     * 编辑db库
//     */
//    @FXML
//    private void save() {
//        try {
//            MongoDatabase database = new MongoDatabase();
//            database.setName(this.name.getText());
//            // 修改数据库
//            if (this.connectItem.alterDatabase(database)) {
//                ShellMongoEventUtil.databaseUpdated(this.connectItem, database);
//                this.closeWindow();
//            } else {
//                MessageBox.warn(I18nHelper.operationFail());
//            }
//        } catch (Exception ex) {
//            MessageBox.exception(ex);
//        }
//    }
//
//    @Override
//    public void onWindowShown(WindowEvent event) {
//        this.database = this.getProp("database");
//        this.connectItem = this.getProp("connectItem");
//
//        // 数据库名
//        this.name.setText(this.database.getName());
//
//        super.onWindowShown(event);
//        this.stage.switchOnTab();
//        this.stage.hideOnEscape();
//    }
//
//    @Override
//    public String getViewTitle() {
//        return I18nHelper.updateDatabase();
//    }
//}
