package cn.oyzh.easyshell.controller.zk.acl;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dto.zk.ZKACL;
import cn.oyzh.easyshell.event.zk.ZKEventUtil;
import cn.oyzh.easyshell.trees.zk.ZKNodeTreeItem;
import cn.oyzh.easyshell.util.zk.ZKACLUtil;
import cn.oyzh.easyshell.zk.ZKClient;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import java.util.List;


/**
 * zk权限修改业务
 *
 * @author oyzh
 * @since 2022/12/20
 */
@StageAttribute(
        modality = Modality.WINDOW_MODAL,
        value = FXConst.FXML_PATH + "zk/acl/zkUpdateACL.fxml"
)
public class ZKUpdateACLController extends StageController {

    /**
     * zk权限信息
     */
    private ZKACL acl;

    /**
     * zk树节点
     */
    private ZKNodeTreeItem zkItem;

    /**
     * zk客户端
     */
    private ZKClient zkClient;

    /**
     * 权限
     */
    @FXML
    private FXHBox perms;

    /**
     * 节点路径
     */
    @FXML
    private TextField nodePath;

    /**
     * 权限类型
     */
    @FXML
    private TextField aclType;

    /**
     * 摘要权限
     */
    @FXML
    private TextField digest;

    /**
     * ip权限
     */
    @FXML
    private TextField ip;

    /**
     * ip权限控件
     */
    @FXML
    private VBox ipACL;

    /**
     * 摘要权限控件
     */
    @FXML
    private VBox digestACL;

    /**
     * 修改zk权限
     */
    @FXML
    private void updateACL() {
        try {
            String perms = this.getPerms();
            if (StringUtil.isBlank(perms)) {
                MessageBox.warn(I18nHelper.contentCanNotEmpty());
                return;
            }
            List<ACL> aclList = this.zkClient.getACL(this.zkItem.nodePath());
            for (ACL acl : aclList) {
                if (acl.equals(this.acl)) {
                    acl.setPerms(ZKACLUtil.toPermInt(perms));
                    Stat stat = this.zkClient.setACL(this.zkItem.nodePath(), aclList);
                    if (stat != null) {
                        this.setProp("result", true);
                        ZKEventUtil.nodeACLUpdated(this.zkItem.zkConnect(), this.zkItem.nodePath());
                        this.closeWindow();
                    } else {
                        MessageBox.warn(I18nHelper.operationFail());
                    }
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.acl = this.getProp("acl");
        // 获取初始化对象
        this.zkItem = this.getProp("zkItem");
        this.zkClient = this.getProp("zkClient");

        CheckBox a = (CheckBox) this.perms.getChildren().get(0);
        CheckBox w = (CheckBox) this.perms.getChildren().get(1);
        CheckBox r = (CheckBox) this.perms.getChildren().get(2);
        CheckBox d = (CheckBox) this.perms.getChildren().get(3);
        CheckBox c = (CheckBox) this.perms.getChildren().get(4);
        a.setSelected(this.acl.hasAdminPerm());
        w.setSelected(this.acl.hasWritePerm());
        r.setSelected(this.acl.hasReadPerm());
        d.setSelected(this.acl.hasDeletePerm());
        c.setSelected(this.acl.hasCreatePerm());

        // ip权限相关
        if (this.acl.isIPACL()) {
            // this.ipACL.setManaged(true);
            this.ipACL.setVisible(true);
            this.ip.setText(this.acl.idVal());
        } else if (this.acl.isDigestACL()) {// 摘要权限相关
            // this.digestACL.setManaged(true);
            this.digestACL.setVisible(true);
            this.digest.setText(this.acl.idVal());
        }
        this.aclType.setText(this.acl.schemeFriend().friendlyValue().toString()
                + "(" + this.acl.schemeFriend().value().toString().toUpperCase() + ")");
        this.nodePath.setText(this.zkItem.decodeNodePath());
        this.stage.hideOnEscape();
    }

    /**
     * 获取权限
     *
     * @return 权限内容
     */
    private String getPerms() {
        CheckBox a = (CheckBox) this.perms.getChildren().get(0);
        CheckBox w = (CheckBox) this.perms.getChildren().get(1);
        CheckBox r = (CheckBox) this.perms.getChildren().get(2);
        CheckBox d = (CheckBox) this.perms.getChildren().get(3);
        CheckBox c = (CheckBox) this.perms.getChildren().get(4);
        StringBuilder builder = new StringBuilder();
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
        return builder.toString();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.updateACL();
    }
}
