package cn.oyzh.easyshell.controller.mongo.user;

import cn.oyzh.easyshell.mongo.user.MongoUser;
import cn.oyzh.easyshell.mongo.user.MongoUserRole;
import cn.oyzh.easyshell.mongo.user.MongoUserRoleDb;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.PasswordTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建用户业务
 *
 * @author oyzh
 * @since 2026/06/03
 */
@StageAttribute(
        modality = Modality.APPLICATION_MODAL,
        stageStyle = FXStageStyle.EXTENDED,
        value = FXConst.FXML_PATH + "mongo/user/shellMongoUserCreate.fxml"
)
public class ShellMongoUserCreateController extends StageController {

    /**
     * 用户名
     */
    @FXML
    private ClearableTextField user;

    /**
     * 密码
     */
    @FXML
    private PasswordTextField password;

    /**
     * 角色
     */
    @FXML
    private FXTableView<MongoUserRoleDb> roleTableView;

    /**
     * db节点
     */
    private ShellMongoDatabaseTreeItem dbItem;

    /**
     * 创建角色
     */
    @FXML
    private void create() {
        try {
            if (!this.user.validate()) {
                return;
            }
            if (!this.password.validate()) {
                return;
            }
            String user = this.user.getTextTrim();
            String password = this.password.getPassword();
            MongoUser mongoUser = new MongoUser();
            mongoUser.setDb(this.dbItem.dbName());
            mongoUser.setUser(user);
            mongoUser.setPassword(password);

            List<MongoUserRole> roles = new ArrayList<>();
            for (MongoUserRoleDb item : this.roleTableView.getItems()) {
                if (item.isEmpty()) {
                    continue;
                }
                for (String role : item.getRoles()) {
                    MongoUserRole userRole = new MongoUserRole();
                    userRole.setDb(item.getDb());
                    userRole.setRole(role);
                    roles.add(userRole);
                }
            }
            mongoUser.setRoles(roles);
            if (this.dbItem.createUser(mongoUser)) {
                this.setProp("user", mongoUser);
                this.closeWindow();
            } else {
                MessageBox.warn(I18nHelper.operationFail());
            }
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.createUser();
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.dbItem = this.getProp("dbItem");
        List<String> dbs = this.dbItem.listDatabaseNames();
        for (String db : dbs) {
            MongoUserRoleDb roleDb = new MongoUserRoleDb();
            roleDb.setDb(db);
            this.roleTableView.addItem(roleDb);
        }
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }
}
