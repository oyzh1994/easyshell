package cn.oyzh.easyshell.controller.zk.auth;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.zk.ShellZKAuth;
import cn.oyzh.easyshell.store.zk.ShellZKAuthStore;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;


/**
 * zk认证信息新增业务
 *
 * @author oyzh
 * @since 2022/12/22
 */
@StageAttribute(
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "zk/auth/shellZKAddAuth.fxml"
)
public class ShellZKAddAuthController extends StageController {

    /**
     * 用户名
     */
    @FXML
    private ClearableTextField user;

    /**
     * 密码
     */
    @FXML
    private ClearableTextField password;

    /**
     * 状态
     */
    @FXML
    private FXToggleSwitch status;

    /**
     * 连接
     */
    private ShellConnect connect;

    /**
     * 认证储存
     */
    private final ShellZKAuthStore authStore = ShellZKAuthStore.INSTANCE;

    /**
     * 新增认证信息
     */
    @FXML
    private void addAuth() {
        try {
            String user = this.user.getText().trim();
            String password = this.password.getText().trim();
            if (StringUtil.isBlank(user)) {
                MessageBox.tipMsg(I18nHelper.userNameCanNotEmpty(), this.user);
                return;
            }
            if (StringUtil.isBlank(password)) {
                MessageBox.tipMsg(I18nHelper.passwordCanNotEmpty(), this.password);
                return;
            }
            if (this.authStore.exist(this.connect.getId(), user, password)) {
                MessageBox.warn(I18nHelper.contentAlreadyExists());
                return;
            }
            ShellZKAuth auth = new ShellZKAuth(this.connect.getId(), user, password);
            auth.setEnable(this.status.isSelected());
            if (this.authStore.replace(auth)) {
                this.setProp("auth", auth);
                this.connect.addAuth(auth);
                //MessageBox.okToast(I18nHelper.operationSuccess());
                this.closeWindow();
            } else {
                MessageBox.warn(I18nHelper.operationFail());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        this.connect = this.getProp("connect");
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
        super.onWindowShown(event);
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.addAuth();
    }
}
