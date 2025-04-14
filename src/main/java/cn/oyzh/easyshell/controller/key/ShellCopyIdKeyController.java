package cn.oyzh.easyshell.controller.key;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellKey;
import cn.oyzh.easyshell.fx.ShellConnectComboBox;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.easyshell.shell.ShellClientUtil;
import cn.oyzh.easyshell.util.ShellKeyUtil;
import cn.oyzh.fx.gui.text.area.MsgTextArea;
import cn.oyzh.fx.gui.text.area.ReadOnlyTextArea;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.util.ControlUtil;
import cn.oyzh.fx.plus.validator.ValidatorUtil;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.util.List;

/**
 * ssh密钥导入业务
 *
 * @author oyzh
 * @since 2025/04/03
 */
@StageAttribute(
        stageStyle = FXStageStyle.UNIFIED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "key/shellCopyIdKey.fxml"
)
public class ShellCopyIdKeyController extends StageController {

    /**
     * 连接列表
     */
    @FXML
    private ShellConnectComboBox host;

    /**
     * 密钥信息
     */
    @FXML
    private ReadOnlyTextArea keyInfo;

    /**
     * 消息
     */
    @FXML
    private MsgTextArea message;

    /**
     * 密钥列表
     */
    private List<ShellKey> keys;

    @Override
    protected void bindListeners() {
        super.bindListeners();
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.keys = this.getWindowProp("keys");
        StringBuilder sb = new StringBuilder();
        for (ShellKey key : keys) {
            sb.append(",").append(key.getName());
        }
        this.keyInfo.setText(sb.substring(1));
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.copyKeys1();
    }

    /**
     * 复制密钥
     */
    @FXML
    private void copyKeys() {
        ShellConnect connect = this.host.getSelectedItem();
        if (connect == null) {
//            this.host.requestFocus();
//            MessageBox.warn(I18nHelper.pleaseSelectConnect());
            ValidatorUtil.validFail(this.host);
            return;
        }
        ShellClient client = ShellClientUtil.newClient(connect);
        StageManager.showMask(() -> {
            this.message.appendLine("client connecting");
            client.start(3000);
            if (!client.isConnected()) {
                this.message.appendLine("client not connected");
                MessageBox.warn(I18nHelper.connectFail());
                return;
            }
            this.message.appendLine("client connected successfully");
            // 复制公钥到服务器
            boolean result = ShellKeyUtil.sshCopyId(this.keys, client);
            if (result) {
                this.message.appendLine("keys copied successfully, size: " + this.keys.size());
            } else {
                this.message.appendLine("keys copied failed");
            }
            // 关闭客户端
            client.close();
        });
    }
}
