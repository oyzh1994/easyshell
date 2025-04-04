package cn.oyzh.easyshell.controller.key;

import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellKey;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.store.ShellKeyStore;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.chooser.FXChooser;
import cn.oyzh.fx.plus.chooser.FileChooserHelper;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.text.area.FXTextArea;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.ssh.OpenSSHRSAUtil;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.io.File;

/**
 * ssh密钥导入业务
 *
 * @author oyzh
 * @since 2025/04/03
 */
@StageAttribute(
        stageStyle = FXStageStyle.UNIFIED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "key/shellImportKey.fxml"
)
public class ShellImportKeyController extends StageController {

    /**
     * 密钥名称
     */
    @FXML
    private ClearableTextField name;

    /**
     * 私钥
     */
    @FXML
    private FXTextArea privateKey;

    /**
     * 公钥
     */
    @FXML
    private FXTextArea publicKey;

    /**
     * 密钥类型
     */
    @FXML
    private ReadOnlyTextField keyType;

    /**
     * 密钥长度
     */
    @FXML
    private ReadOnlyTextField keyLength;

    /**
     * 密钥存储对象
     */
    private final ShellKeyStore keyStore = ShellKeyStore.INSTANCE;

    @Override
    protected void bindListeners() {
        super.bindListeners();
        // 监听公钥变化事件
        this.publicKey.addTextChangeListener((observableValue, s, t1) -> {
            this.fillPubKey();
        });
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.importKey1();
    }

    /**
     * 导入密钥
     */
    @FXML
    private void importKey() {
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
            String keyType = this.keyType.getText();
            int keyLength = Integer.parseInt(this.keyLength.getText());

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

    /**
     * 粘贴公钥
     */
    @FXML
    private void pastePubKey() {
        this.publicKey.paste();
    }

    /**
     * 选择公钥
     */
    @FXML
    private void choosePubKey() {
        File file = FileChooserHelper.choose(I18nHelper.pleaseSelectFile(), FXChooser.allExtensionFilter());
        // 未选择文件
        if (file == null) {
            return;
        }
        // 文件异常
        if (file.length() > 500 * 1024) {
            MessageBox.warn(I18nHelper.invalidFile());
            return;
        }
        // 读取公钥
        String publicKey = FileUtil.readUtf8String(file);
        this.publicKey.text(publicKey);

        String fileName = file.getName();
        File prifile = new File(file.getParentFile(), FileNameUtil.removeExtName(fileName));
        // 读取私钥
        if (prifile.exists()) {
            String privateKey = FileUtil.readUtf8String(prifile);
            this.privateKey.text(privateKey);
        }
    }

    /**
     * 粘贴私钥
     */
    @FXML
    private void pastePriKey() {
        this.privateKey.paste();
    }

    /**
     * 选择私钥
     */
    @FXML
    private void choosePriKey() {
        File file = FileChooserHelper.choose(I18nHelper.pleaseSelectFile(), FXChooser.allExtensionFilter());
        // 未选择文件
        if (file == null) {
            return;
        }
        // 文件异常
        if (file.length() > 500 * 1024) {
            MessageBox.warn(I18nHelper.invalidFile());
            return;
        }
        // 读取私钥
        String privateKey = FileUtil.readUtf8String(file);
        this.privateKey.text(privateKey);
        String fileName = file.getName();
        File pubFile = new File(file.getParentFile(), FileNameUtil.removeExtName(fileName) + ".pub");
        // 读取公钥
        if (pubFile.exists()) {
            String publicKey = FileUtil.readUtf8String(pubFile);
            this.publicKey.text(publicKey);
        }
    }

    /**
     * 填充公钥
     */
    private void fillPubKey() {
        String publicKey = this.publicKey.getTextTrim();
        if (StringUtil.startWithIgnoreCase(publicKey, "ssh-ed25519")) {
            this.keyType.setText("ED25519");
        } else if (StringUtil.startWithIgnoreCase(publicKey, "ssh-rsa")) {
            this.keyType.setText("RSA");
        } else {
            this.keyType.clear();
        }
        this.fillKeyLength();
    }

    /**
     * 填充密钥长度
     */
    private void fillKeyLength() {
        String keyType = this.keyType.getText();
        if ("ED25519".equals(keyType)) {
            this.keyLength.setValue(256);
        } else if ("RSA".equals(keyType)) {
            String pubKey = this.publicKey.getTextTrim();
            int len = OpenSSHRSAUtil.getKeyLength(pubKey);
            this.keyLength.setValue(len);
        } else {
            this.keyLength.clear();
        }
    }
}
