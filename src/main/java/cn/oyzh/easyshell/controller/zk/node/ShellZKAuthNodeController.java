package cn.oyzh.easyshell.controller.zk.node;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.zk.ShellZKAuth;
import cn.oyzh.easyshell.fx.zk.ShellZKAuthComboBox;
import cn.oyzh.easyshell.store.zk.ShellZKAuthStore;
import cn.oyzh.easyshell.trees.zk.ShellZKNodeTreeItem;
import cn.oyzh.easyshell.util.zk.ShellZKAuthUtil;
import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.easyshell.zk.ShellZKNode;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeMutexes;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.util.List;


/**
 * zk节点认证业务
 *
 * @author oyzh
 * @since 2022/06/07
 */
@StageAttribute(
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "zk/node/shellZKAuthNode.fxml"
)
public class ShellZKAuthNodeController extends StageController {

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
     * 节点路径
     */
    @FXML
    private TextField nodePath;

    /**
     * 保存信息1
     */
    @FXML
    private FXCheckBox saveInfo1;

    /**
     * zk节点
     */
    private ShellZKNode zkNode;

    /**
     * zk树节点
     */
    private ShellZKNodeTreeItem zkItem;

    /**
     * 认证方式
     */
    @FXML
    private FXComboBox<String> authType;

    /**
     * 认证方式1
     */
    @FXML
    private FXVBox authType1;

    /**
     * 认证方式2
     */
    @FXML
    private FXVBox authType2;

    /**
     * 认证信息列表
     */
    @FXML
    private ShellZKAuthComboBox authList;

    /**
     * 认证储存
     */
    private final ShellZKAuthStore authStore = ShellZKAuthStore.INSTANCE;

    /**
     * 节点互斥器
     */
    private final NodeMutexes mutexes = new NodeMutexes();

    /**
     * 认证
     */
    @FXML
    protected void auth() {
        try {
            String user = null, password = null;
            if (this.authType1.isVisible()) {
                user = this.user.getText().trim();
                password = this.password.getText().trim();
                if (StringUtil.isBlank(user)) {
                    MessageBox.tipMsg(I18nHelper.userNameCanNotEmpty(), this.user);
                    return;
                }
                if (StringUtil.isBlank(password)) {
                    MessageBox.tipMsg(I18nHelper.passwordCanNotEmpty(), this.password);
                    return;
                }
            } else if (this.authType2.isVisible()) {
                // 获取内容
                ShellZKAuth zkAuth = this.authList.getValue();
                if (zkAuth == null) {
                    MessageBox.tipMsg(I18nHelper.contentCanNotEmpty(), this.authList);
                    return;
                }
                user = zkAuth.getUser();
                password = zkAuth.getPassword();
            }
            if (user != null && password != null) {
                this.auth(user, password);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 认证节点
     *
     * @param user     用户名
     * @param password 密码
     */
    private void auth(String user, String password) {
        try {
            ShellZKClient zkClient = this.zkItem.client();
            int result = ShellZKAuthUtil.authNode(user, password, zkClient, this.zkNode);
            if (result == 1) {
                ShellZKAuth auth = new ShellZKAuth(zkClient.iid(), user, password);
                if (this.saveInfo1.isSelected()) {
                    //ShellZKAuth auth = new ShellZKAuth(zkClient.iid(), user, password);
                    this.authStore.replace(auth);
                    this.zkItem.zkConnect().addAuth(auth);
                }
                //ShellZKEventUtil.authAuthed(this.zkItem, true, user, password);
                this.setProp("auth", auth);
                this.setProp("success", true);
                MessageBox.okToast(I18nHelper.operationSuccess());
                this.closeWindow();
            } else if (this.zkNode.aclEmpty() || this.zkNode.hasDigestACL()) {
                //ShellZKEventUtil.authAuthed(this.zkItem, false, user, password);
                MessageBox.warn(I18nHelper.operationFail());
            } else {
                //ShellZKEventUtil.authAuthed(this.zkItem, false, user, password);
                MessageBox.warn(I18nHelper.authFailTip1());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, I18nHelper.operationFail());
        }
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.authType.selectedIndexChanged((o, toggle, t1) -> {
            if (t1.intValue() == 0) {
                this.mutexes.visible(this.authType1);
            } else if (t1.intValue() == 1) {
                this.mutexes.visible(this.authType2);
            }
        });
        this.user.addTextChangeListener((observableValue, s, t1) -> {
            // 内容包含“:”，则直接切割字符为用户名密码
            if (t1 != null && t1.contains(":")) {
                this.user.setText(t1.split(":")[0]);
                this.password.setText(t1.split(":")[1]);
            }
        });
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.zkItem = this.getProp("zkItem");
        this.zkNode = this.zkItem.value();
        this.nodePath.setText(this.zkNode.decodeNodePath());
        this.mutexes.addNodes(this.authType1, this.authType2);
        this.mutexes.manageBindVisible();

        this.authList.getItems().clear();
        ShellZKClient client = this.zkItem.client();
        List<ShellZKAuth> authList = client.getAuths();
        if (CollectionUtil.isNotEmpty(authList)) {
            this.authList.addItems(authList);
            //this.authList.setConverter(new SimpleStringConverter<>() {
            //    @Override
            //    public String toString(ShellZKAuth auth) {
            //        String text = "";
            //        if (auth != null) {
            //            text = auth.getUser() + ":" + auth.getPassword();
            //            if (client.isAuthed(auth)) {
            //                text += " (" + I18nHelper.authed() + ")";
            //            }
            //        }
            //        return text;
            //    }
            //});
            this.authList.selectFirst();
        }
        // 设置一个摘要用户名
        if (this.zkNode.getDigestACLs().size() == 1) {
            this.user.setText(this.zkNode.getDigestACLs().getFirst().digestUser());
        } else if (this.zkNode.aclEmpty() && !authList.isEmpty()) {// 选中摘要列表认证
            this.authType.select(1);
        }
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public void onWindowHidden(WindowEvent event) {
        super.onWindowHidden(event);
        this.mutexes.destroy();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.authNode();
    }
}
