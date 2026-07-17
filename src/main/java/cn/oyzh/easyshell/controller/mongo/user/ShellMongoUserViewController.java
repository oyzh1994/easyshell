//package cn.oyzh.easyshell.controller.mongo.user;
//
//import cn.oyzh.easyshell.mongo.user.MongoUser;
//import cn.oyzh.easyshell.mongo.user.MongoUserRole;
//import cn.oyzh.easyshell.mongo.user.MongoUserRoleDb;
//import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
//import cn.oyzh.fx.plus.FXConst;
//import cn.oyzh.fx.plus.controller.StageController;
//import cn.oyzh.fx.plus.controls.table.FXTableView;
//import cn.oyzh.fx.plus.window.FXStageStyle;
//import cn.oyzh.fx.plus.window.StageAttribute;
//import cn.oyzh.i18n.I18nHelper;
//import javafx.fxml.FXML;
//import javafx.stage.Modality;
//import javafx.stage.WindowEvent;
//
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * 查看用户业务
// *
// * @author oyzh
// * @since 2026/06/03
// */
//@StageAttribute(
//        modality = Modality.APPLICATION_MODAL,
//        stageStyle = FXStageStyle.EXTENDED,
//        value = FXConst.FXML_PATH + "mongo/user/shellMongoUserView.fxml"
//)
//public class ShellMongoUserViewController extends StageController {
//
//    /**
//     * 用户名
//     */
//    @FXML
//    private ReadOnlyTextField user;
//
//    /**
//     * 数据库
//     */
//    @FXML
//    private ReadOnlyTextField database;
//
//    /**
//     * 角色
//     */
//    @FXML
//    private FXTableView<MongoUserRoleDb> roleTableView;
//
//    @Override
//    public String getViewTitle() {
//        return I18nHelper.view1User();
//    }
//
//    @Override
//    public void onWindowShown(WindowEvent event) {
//        super.onWindowShown(event);
//        MongoUser user = this.getProp("user");
//        Map<String, MongoUserRoleDb> dbs = new HashMap<>();
//        for (MongoUserRole role : user.getRoles()) {
//            MongoUserRoleDb roleDb;
//            if (dbs.containsKey(role.getDb())) {
//                roleDb = dbs.get(role.getDb());
//            } else {
//                roleDb = new MongoUserRoleDb();
//                roleDb.setDb(role.getDb());
//                dbs.put(role.getDb(), roleDb);
//            }
//            roleDb.roles().add(role.getRole());
//        }
//        this.user.setText(user.getUser());
//        this.database.setText(user.getDb());
//        this.roleTableView.setItem(dbs.values());
//        this.stage.switchOnTab();
//        this.stage.hideOnEscape();
//    }
//}
