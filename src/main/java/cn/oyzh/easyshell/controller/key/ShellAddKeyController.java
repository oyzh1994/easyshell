package cn.oyzh.easyshell.controller.key;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellKey;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.fx.key.ShellKeyLengthComboBox;
import cn.oyzh.easyshell.fx.key.ShellKeyTypeComboBox;
import cn.oyzh.easyshell.store.ShellKeyStore;
import cn.oyzh.fx.gui.text.area.ReadOnlyTextArea;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.PasswordTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.validator.ValidatorUtil;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.ssh.util.SSHKeyUtil;
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
     * 密码
     */
    @FXML
    private PasswordTextField keyPassword;

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
            ValidatorUtil.validFail(this.name);
            return;
        }
        // 密钥检查
        String publicKey = this.publicKey.getTextTrim();
        if (StringUtil.isBlank(publicKey)) {
            ValidatorUtil.validFail(this.publicKey);
            return;
        }
        String privateKey = this.privateKey.getTextTrim();
        if (StringUtil.isBlank(privateKey)) {
            ValidatorUtil.validFail(this.privateKey);
            return;
        }
        try {
            String keyType = this.keyType.getSelectedItem();
            String password = this.keyPassword.getPassword();
            int keyLength = this.keyLength.getSelectedItem();

            ShellKey shellKey = new ShellKey();
            shellKey.setPublicKey(publicKey);
            shellKey.setPrivateKey(privateKey);
            shellKey.setName(name);
            shellKey.setType(keyType);
            shellKey.setLength(keyLength);
            shellKey.setPassword(password);

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
        super.bindListeners();
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
        Integer length = this.keyLength.getSelectedItem();
        StageManager.showMask(() -> {
            try {
                String password = this.keyPassword.getPassword();
                String[] key;
                if (this.keyType.isRsaType()) {
                    key = SSHKeyUtil.generateRsa(length, password);
                    // ssh公钥
                    this.publicKey.setText(key[0]);
                } else if (this.keyType.isEd25519Type()) {
                    key = SSHKeyUtil.generateEd25519(256, password);
                    // ssh公钥
                    this.publicKey.setText(key[0]);
                } else if (this.keyType.isEcdsaType()) {
                    key = SSHKeyUtil.generateEcdsa(length, password);
                    // ssh公钥
                    this.publicKey.setText(key[0]);
                } else {
                    key = SSHKeyUtil.generateDsa(length, password);
                    // ssh公钥
                    this.publicKey.setText(key[0]);
                }
                // ssh私钥
                this.privateKey.setText(key[1]);
            } catch (Exception ex) {
                MessageBox.exception(ex);
            }
        });
    }
}
