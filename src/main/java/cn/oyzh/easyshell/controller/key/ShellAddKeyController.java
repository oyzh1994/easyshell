package cn.oyzh.easyshell.controller.key;

import cn.oyzh.common.security.KeyGenerator;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellKey;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.fx.key.ShellKeyLengthComboBox;
import cn.oyzh.easyshell.fx.key.ShellKeyTypeComboBox;
import cn.oyzh.easyshell.store.ShellKeyStore;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.text.area.ReadOnlyTextArea;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * ssh密钥新增业务
 *
 * @author oyzh
 * @since 2025/04/03
 */
@StageAttribute(
        stageStyle = FXStageStyle.UNIFIED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "key/shellAddKey.fxml"
)
public class ShellAddKeyController extends StageController {

    /**
     * 密钥名称
     */
    @FXML
    private ClearableTextField name;

    /**
     * 私钥
     */
    @FXML
    private ReadOnlyTextArea privateKey;

    /**
     * 公钥
     */
    @FXML
    private ReadOnlyTextArea publicKey;

    /**
     * 密钥类型
     */
    @FXML
    private ShellKeyTypeComboBox keyType;

    /**
     * 密钥长度
     */
    @FXML
    private ShellKeyLengthComboBox keyLength;

    /**
     * 密钥存储对象
     */
    private final ShellKeyStore keyStore = ShellKeyStore.INSTANCE;

    /**
     * 添加密钥
     */
    @FXML
    private void add() {
        String name = this.name.getTextTrim();
        // 名称检查
        if (StringUtil.isBlank(name)) {
            this.name.requestFocus();
            return;
        }
        // 密钥检查
        String publicKey = this.publicKey.getTextTrim();
        if (StringUtil.isBlank(publicKey)) {
            MessageBox.warn(ShellI18nHelper.keyTip1());
            this.publicKey.requestFocus();
            return;
        }
        String privateKey = this.privateKey.getTextTrim();
        if (StringUtil.isBlank(privateKey)) {
            MessageBox.warn(ShellI18nHelper.keyTip1());
            this.privateKey.requestFocus();
            return;
        }
        try {

            String keyType = this.keyType.getSelectedItem();
            int keyLength = this.keyLength.getSelectedItem();

            ShellKey shellKey = new ShellKey();
            shellKey.setPublicKey(publicKey);
            shellKey.setPrivateKey(privateKey);
            shellKey.setName(name);
            shellKey.setType(keyType);
            shellKey.setLength(keyLength);

            // 保存数据
            if (this.keyStore.insert(shellKey)) {
                ShellEventUtil.keyAdded(shellKey);
                MessageBox.okToast(I18nHelper.operationSuccess());
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
    protected void bindListeners() {
        this.keyType.selectedItemChanged((observableValue, number, t1) -> {
            this.keyLength.init(t1);
            this.publicKey.clear();
            this.privateKey.clear();
        });
        this.keyLength.init("RSA");
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.addKey1();
    }

    @FXML
    private void generateKey() {
        String type = this.keyType.getSelectedItem();
        Integer length = this.keyLength.getSelectedItem();
        StageManager.showMask(() -> {
            try {
                String[] key;
                if ("RSA".equalsIgnoreCase(type)) {
                    key = KeyGenerator.rsa(length);
                    // ssh公钥
                    this.publicKey.setText("ssh-rsa " + key[0]);
                } else {
                    key = KeyGenerator.ed25519();
                    // ssh公钥
                    this.publicKey.setText("ssh-ed25519 " + key[0]);
                }
                // ssh私钥
                StringBuilder builder = new StringBuilder();
                builder.append("-----BEGIN OPENSSH PRIVATE KEY-----")
                        .append("\n")
                        .append(key[1])
                        .append("\n")
                        .append("-----END OPENSSH PRIVATE KEY-----");
                this.privateKey.setText(builder.toString());
            } catch (Exception ex) {
                MessageBox.exception(ex);
            }
        });
    }
}
