package cn.oyzh.easyshell.controller.zk.node;

import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.trees.zk.ShellZKNodeTreeItem;
import cn.oyzh.easyshell.util.zk.ShellZKACLUtil;
import cn.oyzh.easyshell.util.zk.ShellZKAuthUtil;
import cn.oyzh.easyshell.util.zk.ShellZKNodeUtil;
import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;

import java.util.List;


/**
 * zk节点添加业务
 *
 * @author oyzh
 * @since 2020/10/09
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "zk/node/shellZKAddNode.fxml"
)
public class ShellZKAddNodeController extends StageController {

    /**
     * 权限tab
     */
    @FXML
    private FXTab aclTab;

    /**
     * 数据tab
     */
    @FXML
    private FXTab dataTab;

    /**
     * 节点路径
     */
    @FXML
    private ClearableTextField nodePath;

    /**
     * 节点数据
     */
    @FXML
    private Editor nodeData;

    /**
     * 节点路径预览
     */
    @FXML
    private TextField nodePathPreview;

    /**
     * 父节点值组件
     */
    @FXML
    private VBox parentNodeBox;

    /**
     * 父节点值
     */
    @FXML
    private TextField parentNode;

    /**
     * 创建模式下拉选择框
     */
    @FXML
    private FXComboBox<String> createMode;

    /**
     * zk客户端
     */
    private ShellZKClient zkClient;

    /**
     * 节点路径数据
     */
    private String nodePathText;

    /**
     * 权限类型
     */
    @FXML
    private FXComboBox<String> aclType;

    /**
     * 权限组件
     */
    @FXML
    private FXHBox perms;

    /**
     * ip权限
     */
    @FXML
    private FXVBox ipACL;

    /**
     * 摘要权限
     */
    @FXML
    private FXVBox digestACL;

    /**
     * 摘要权限内容
     */
    @FXML
    private FXLabel digestText;

    /**
     * ip权限内容
     */
    @FXML
    private ClearableTextField ipContent;

    /**
     * 摘要权限用户名
     */
    @FXML
    private ClearableTextField digestUser;

    /**
     * 摘要权限密码
     */
    @FXML
    private ClearableTextField digestPassword;

    /**
     * 添加zk节点
     */
    @FXML
    private void addNode() {
        if (!this.nodePath.validate()) {
            this.dataTab.selectTab();
            return;
        }
        // 获取权限
        ACL acl = this.getACL();
        if (acl == null) {
            this.aclTab.selectTab();
            return;
        }
        // 获取节点值
        String nodeData = this.nodeData.getText();
        try {
            // 获取创建模式
            CreateMode createMode = CreateMode.fromFlag(this.createMode.getSelectedIndex());
            // 新增节点
            String node = this.zkClient.create(this.nodePathText, nodeData.getBytes(), List.of(acl), null, createMode, true);
            if (node == null) {
                MessageBox.warnToast(I18nHelper.operationFail());
            } else {
                //ShellZKEventUtil.nodeAdded(this.zkClient.getShellConnect(), this.nodePathText);
                this.setProp("addedNodePath", this.nodePathText);
                this.closeWindow();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 粘贴数据
     */
    @FXML
    private void pasteData() {
        this.nodeData.paste();
        this.nodeData.requestFocus();
    }

    /**
     * 清空数据
     */
    @FXML
    private void clearData() {
        this.nodeData.clear();
        this.nodeData.requestFocus();
    }

    /**
     * 解析为json
     */
    @FXML
    private void parseToJson() {
        String text = this.nodeData.getTextTrim();
        try {
            if ("json".equals(this.nodeData.getUserData())) {
                String jsonStr = JSONUtil.toJson(text);
                this.nodeData.setText(jsonStr);
                this.nodeData.setUserData("text");
            } else if (text.contains("{") || text.contains("[") || "text".equals(this.nodeData.getUserData())) {
                String jsonStr = JSONUtil.toPretty(text);
                this.nodeData.setText(jsonStr);
                this.nodeData.setUserData("json");
            }
        } catch (Exception ignore) {
        }
    }

    /**
     * 获取权限
     *
     * @return 权限
     */
    private ACL getACL() {
        StringBuilder builder = new StringBuilder();
        CheckBox a = (CheckBox) this.perms.getChildren().get(0);
        CheckBox w = (CheckBox) this.perms.getChildren().get(1);
        CheckBox r = (CheckBox) this.perms.getChildren().get(2);
        CheckBox d = (CheckBox) this.perms.getChildren().get(3);
        CheckBox c = (CheckBox) this.perms.getChildren().get(4);
        if (a.isSelected()) {
            builder.append("a");
        }
        if (w.isSelected()) {
            builder.append("w");
        }
        if (r.isSelected()) {
            builder.append("r");
        }
        if (d.isSelected()) {
            builder.append("d");
        }
        if (c.isSelected()) {
            builder.append("c");
        }

        if (builder.isEmpty()) {
            this.perms.requestFocus();
            MessageBox.warn(I18nHelper.invalidData());
            return null;
        }

        // 获取权限
        int perms = ShellZKACLUtil.toPermInt(builder.toString());

        // 开放权限
        if (this.aclType.getSelectedIndex() == 0) {
            return new ACL(perms, new Id("world", "anyone"));
        }

        // 摘要权限
        if (this.aclType.getSelectedIndex() == 1) {
            // 获取内容
            String user = this.digestUser.getText().trim();
            if (StringUtil.isBlank(user)) {
                MessageBox.tipMsg(I18nHelper.userNameCanNotEmpty(), this.digestUser);
                return null;
            }
            String password = this.digestPassword.getText().trim();
            if (StringUtil.isBlank(password)) {
                MessageBox.tipMsg(I18nHelper.passwordCanNotEmpty(), this.digestPassword);
                return null;
            }
            String digest = ShellZKAuthUtil.digest(user, password);
            return new ACL(perms, new Id("digest", digest));
        }

        // ip权限
        if (this.aclType.getSelectedIndex() == 2) {
            // 获取内容
            String ip = this.ipContent.getTextTrim();
            try {
                ShellZKACLUtil.checkIP(ip);
            } catch (Exception ex) {
                MessageBox.tipMsg(ex.getMessage(), this.digestPassword);
                return null;
            }
            return new ACL(perms, new Id("ip", ip));
        }
        return null;
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        // 节点路径变化处理
        this.nodePath.addTextChangeListener((observableValue, s, t1) -> {
            if (StringUtil.isNotBlank(t1)) {
                this.nodePathText = ShellZKNodeUtil.concatPath(this.parentNode.getText(), t1).trim();
            } else {
                this.nodePathText = "";
            }
            this.nodePathPreview.setText(this.nodePathText);
        });

        // 权限变化处理
        this.aclType.selectedIndexChanged((observable, oldValue, newValue) -> {
            if (newValue.intValue() == 0) {
                this.ipACL.disappear();
                this.digestACL.disappear();
            } else if (newValue.intValue() == 1) {
                this.ipACL.disappear();
                this.digestACL.display();
            } else if (newValue.intValue() == 2) {
                this.ipACL.display();
                this.digestACL.disappear();
            }
        });

        // 文本监听器
        ChangeListener<String> info1listener = (observableValue, s, t1) -> {
            String user = this.digestUser.getText().trim();
            String password = this.digestPassword.getTextTrim();
            if (StringUtil.isNotBlank(user) && StringUtil.isNotBlank(password)) {
                String digest = ShellZKAuthUtil.digest(user, password);
                this.digestText.setText(digest);
            } else {
                this.digestText.setText("");
            }
        };

        // 认证信息更新时，动态显示摘要信息
        this.digestUser.addTextChangeListener(info1listener);
        this.digestPassword.addTextChangeListener(info1listener);
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        this.parentNodeBox.managedProperty().bind(this.parentNode.visibleProperty());
        ShellZKNodeTreeItem zkItem = this.getProp("zkItem");
        this.zkClient = this.getProp("zkClient");
        if (zkItem != null) {
            this.parentNode.setVisible(true);
            this.parentNode.setText(zkItem.nodePath());
        } else {
            this.parentNode.setVisible(false);
        }
        this.nodePath.requestFocus();
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
        super.onWindowShown(event);
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.addNode();
    }
}
