package cn.oyzh.easyshell.controller.key;

import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellKey;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.store.ShellKeyStore;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.PasswordTextField;
import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.chooser.FXChooser;
import cn.oyzh.fx.plus.chooser.FileChooserHelper;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.text.area.FXTextArea;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.validator.ValidatorUtil;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.ssh.util.SSHKeyUtil;
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
    private ClearableTextField keyLength;

    /**
     * 密码
     */
    @FXML
    private PasswordTextField keyPassword;

    /**
     * 密钥存储对象
     */
    private final ShellKeyStore keyStore = ShellKeyStore.INSTANCE;

    @Override
    protected void bindListeners() {
        super.bindListeners();
        // 监听密钥变化事件
        this.publicKey.addTextChangeListener((observableValue, s, t1) -> {
            this.fillKeyType();
            this.fillKeySize();
        });
        this.privateKey.addTextChangeListener((observableValue, s, t1) -> {
            this.fillKeyType();
            this.fillKeySize();
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
            String keyType = this.keyType.getText();
            String password = this.keyPassword.getPassword();
            int keyLength = Integer.parseInt(this.keyLength.getText());

            ShellKey shellKey = new ShellKey();
            shellKey.setName(name);
            shellKey.setType(keyType);
            shellKey.setLength(keyLength);
            shellKey.setPassword(password);
            shellKey.setPublicKey(publicKey);
            shellKey.setPrivateKey(privateKey);

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
        String fileName = file.getName();
        String extName = FileNameUtil.extName(fileName);
        // 设置名称
        if (this.name.isEmpty()) {
            this.name.text(FileNameUtil.removeExtName(fileName));
        }
        // 读取公钥
        String content = FileUtil.readUtf8String(file);
        this.publicKey.text(this.handlePubKey(content));
        // putty
        if ("ppk".equalsIgnoreCase(extName)) {
            this.privateKey.text(this.handlePriKey(content));
        } else {
            File prifile = new File(file.getParentFile(), FileNameUtil.removeExtName(fileName));
            // 读取私钥
            if (prifile.exists()) {
                String privateKey = FileUtil.readUtf8String(prifile);
                this.privateKey.text(this.handlePriKey(privateKey));
            }
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
        String fileName = file.getName();
        String extName = FileNameUtil.extName(fileName);
        // 设置名称
        if (this.name.isEmpty()) {
            this.name.text(FileNameUtil.removeExtName(fileName));
        }
        // 读取私钥
        String content = FileUtil.readUtf8String(file);
        this.privateKey.text(this.handlePriKey(content));
        // putty
        if ("ppk".equalsIgnoreCase(extName)) {
            this.publicKey.text(this.handlePubKey(content));
        } else {
            File pubFile = new File(file.getParentFile(), FileNameUtil.removeExtName(fileName) + ".pub");
            // 读取公钥
            if (pubFile.exists()) {
                String publicKey = FileUtil.readUtf8String(pubFile);
                this.publicKey.text(this.handlePubKey(publicKey));
            }
        }
    }

    /**
     * 填充密钥类型
     */
    private void fillKeyType() {
        String privateKey = this.privateKey.getTextTrim();
        String keyType = SSHKeyUtil.getKeyType(privateKey);
        if (keyType == null) {
            String publicKey = this.publicKey.getTextTrim();
            keyType = SSHKeyUtil.getKeyType(publicKey);
        }
        if (keyType != null) {
            this.keyType.setText(keyType);
        } else {
            this.keyType.clear();
        }
        //if (StringUtil.startWithIgnoreCase(publicKey, "ssh-ed25519")) {
        //    this.keyType.setText("ED25519");
        //} else if (StringUtil.startWithIgnoreCase(publicKey, "ssh-rsa")) {
        //    this.keyType.setText("RSA");
        //} else if (StringUtil.startWithIgnoreCase(publicKey, "ecdsa")) {
        //    this.keyType.setText("ECDSA");
        //} else {
        //    this.keyType.clear();
        //}
        //this.fillKeyLength();
    }

    /**
     * 填充密钥长度
     */
    private void fillKeySize() {
        //String keyType = this.keyType.getText();
        //if ("ED25519".equals(keyType)) {
        //    this.keyLength.setValue(256);
        //} else if ("RSA".equals(keyType)) {
        //    String priKey = this.privateKey.getTextTrim();
        //    int len = SSHKeyUtil.getKeySize(priKey);
        //    this.keyLength.setValue(len);
        //} else if ("ECDSA".equals(keyType)) {
        //    String pubKey = this.publicKey.getTextTrim();
        //    int len = OpenSSHECDSAUtil.getKeyLength(pubKey);
        //    this.keyLength.setValue(len);
        //} else {
        //    this.keyLength.clear();
        //}
        String privateKey = this.privateKey.getTextTrim();
        int len = SSHKeyUtil.getKeySize(privateKey);
        if (len == -1) {
            String publicKey = this.publicKey.getTextTrim();
            len = SSHKeyUtil.getKeySize(publicKey);
        }
        if (len != -1) {
            this.keyLength.setValue(len);
        } else {
            this.keyLength.clear();
        }
    }

    /**
     * 处理公钥
     *
     * @param pubKey 公钥
     * @return 结果
     */
    private String handlePubKey(String pubKey) {
        // putty公钥
        if (StringUtil.startWithIgnoreCase(pubKey, "PuTTY")) {
            String[] lines = pubKey.split("\n");
            String type = lines[0].split(":")[1].trim();
            String comment = lines[2].split(":")[1].trim();
            comment = comment.substring(1, comment.length() - 1);
            StringBuilder builder = new StringBuilder();
            builder.append(type).append(" ");
            boolean pubStart = false;
            for (int i = 2; i < lines.length - 1; i++) {
                String line = lines[i];
                if (line.startsWith("Public-Lines")) {
                    pubStart = true;
                    continue;
                }
                if (!pubStart) {
                    continue;
                }
                builder.append(lines[i]);
            }
            builder.append(" ").append(comment);
            pubKey = builder.toString();
        }
        return pubKey;
    }

    /**
     * 处理私钥
     *
     * @param priKey 私钥
     * @return 结果
     */
    private String handlePriKey(String priKey) {
        // putty私钥
        if (StringUtil.startWithIgnoreCase(priKey, "PuTTY")) {
            String[] lines = priKey.split("\n");
            StringBuilder builder = new StringBuilder();
            String keyType = this.keyType.getTextTrim();
            if (StringUtil.equalsIgnoreCase(keyType, "ssh-rsa")) {
                builder.append("-----BEGIN RSA PRIVATE KEY-----");
            } else if (StringUtil.containsAnyIgnoreCase(keyType, "ecdsa")) {
                builder.append("-----BEGIN EC PRIVATE KEY-----");
            } else {
                builder.append("-----BEGIN PRIVATE KEY-----");
            }
            builder.append("\n");
            boolean priStart = false;
            for (int i = 2; i < lines.length - 1; i++) {
                String line = lines[i];
                if (line.startsWith("Private-Lines")) {
                    priStart = true;
                    continue;
                }
                if (!priStart) {
                    continue;
                }
                builder.append(line).append("\n");
            }
            if (StringUtil.equalsIgnoreCase(keyType, "ssh-rsa")) {
                builder.append("-----END RSA PRIVATE KEY-----");
            } else if (StringUtil.containsIgnoreCase(keyType, "ecdsa")) {
                builder.append("-----END EC PRIVATE KEY-----");
            } else {
                builder.append("-----BEGIN PRIVATE KEY-----");
            }
            priKey = builder.toString();
        }
        return priKey;
    }

}
