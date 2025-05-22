package cn.oyzh.easyshell.controller.key;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellKey;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.store.ShellKeyStore;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.text.area.ReadOnlyTextArea;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.text.area.FXTextArea;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.util.ControlUtil;
import cn.oyzh.fx.plus.validator.ValidatorUtil;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
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
        value = FXConst.FXML_PATH + "key/shellUpdateKey.fxml"
)
public class ShellUpdateKeyController extends StageController {

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
    private ReadOnlyTextArea publicKey;

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
     * 密钥
     */
    private ShellKey key;

    /**
     * 密钥存储对象
     */
    private final ShellKeyStore keyStore = ShellKeyStore.INSTANCE;

    /**
     * 保存密钥
     */
    @FXML
    private void save() {
        String name = this.name.getTextTrim();
        // 名称检查
        if (StringUtil.isBlank(name)) {
//            this.name.requestFocus();
            ValidatorUtil.validFail(this.name);
            return;
        }
        String privateKey = this.privateKey.getTextTrim();
        if (StringUtil.isBlank(privateKey)) {
//            MessageBox.warn(ShellI18nHelper.keyTip1());
//            this.privateKey.requestFocus();
            ValidatorUtil.validFail(this.privateKey);
            return;
        }
        try {
            this.key.setName(name);
            this.key.setPrivateKey(privateKey);
            // 保存数据
            if (this.keyStore.update(this.key)) {
                ShellEventUtil.keyUpdated(this.key);
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
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.key = this.getProp("key");
        this.name.setText(this.key.getName());
        this.keyType.setText(this.key.getType());
        this.publicKey.setText(this.key.getPublicKey());
        this.keyLength.setText(this.key.getLength() + "");
        this.privateKey.setText(this.key.getPrivateKey());
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.updateKey1();
    }
}
