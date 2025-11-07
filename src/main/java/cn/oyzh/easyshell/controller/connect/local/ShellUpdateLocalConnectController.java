package cn.oyzh.easyshell.controller.connect.local;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.fx.term.ShellTermBackspaceTypeCombobox;
import cn.oyzh.easyshell.fx.term.ShellTermTypeComboBox;
import cn.oyzh.easyshell.store.ShellConnectStore;
import cn.oyzh.fx.gui.combobox.CharsetComboBox;
import cn.oyzh.fx.gui.text.field.ChooseFileTextField;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.controls.text.area.FXTextArea;
import cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * local连接修改业务
 *
 * @author oyzh
 * @since 2025/04/24
 */
@StageAttribute(
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "connect/local/shellUpdateLocalConnect.fxml"
)
public class ShellUpdateLocalConnectController extends StageController {

    /**
     * tab组件
     */
    @FXML
    private FXTabPane tabPane;

    /**
     * 名称
     */
    @FXML
    private ClearableTextField name;

    /**
     * 备注
     */
    @FXML
    private FXTextArea remark;

    /**
     * 字符集
     */
    @FXML
    private CharsetComboBox charset;

    /**
     * 终端退格类型
     */
    @FXML
    private ShellTermBackspaceTypeCombobox backspaceType;

    /**
     * alt修饰
     */
    @FXML
    private FXCheckBox altSendsEscape;

    /**
     * 终端类型
     */
    @FXML
    private ShellTermTypeComboBox termType;

    /**
     * 系统类型
     */
    @FXML
    private ShellOsTypeComboBox osType;

    /**
     * 开启背景
     */
    @FXML
    private FXToggleSwitch enableBackground;

    /**
     * 背景面板
     */
    @FXML
    private FXTab backgroundTab;

    /**
     * 背景图片
     */
    @FXML
    private ChooseFileTextField backgroundImage;

    /**
     * 连接
     */
    private ShellConnect shellConnect;

    /**
     * ssh连接储存对象
     */
    private final ShellConnectStore connectStore = ShellConnectStore.INSTANCE;

    /**
     * 修改连接信息
     */
    @FXML
    private void update() {
        // 检查名称
        if (!this.name.validate()) {
            return;
        }
        // 检查背景配置
        if (this.enableBackground.isSelected()) {
            if (!this.backgroundImage.validate()) {
                this.tabPane.select(this.backgroundTab);
                return;
            }
        }
        try {
            String name = this.name.getTextTrim();
            String remark = this.remark.getTextTrim();
            String osType = this.osType.getSelectedItem();
            String charset = this.charset.getCharsetName();
            String termType = this.termType.getSelectedItem();
            String backgroundImage = this.backgroundImage.getText();
            int backspaceType = this.backspaceType.getSelectedIndex();
            boolean altSendsEscape = this.altSendsEscape.isSelected();
            boolean enableBackground = this.enableBackground.isSelected();

            this.shellConnect.setName(name);
            this.shellConnect.setOsType(osType);
            this.shellConnect.setRemark(remark);
            this.shellConnect.setCharset(charset);
            this.shellConnect.setBackspaceType(backspaceType);
            this.shellConnect.setAltSendsEscape(altSendsEscape);
            this.shellConnect.setTermType(termType);
            // 背景配置
            this.shellConnect.setBackgroundImage(backgroundImage);
            this.shellConnect.setEnableBackground(enableBackground);
            // 保存数据
            if (this.connectStore.update(this.shellConnect)) {
                ShellEventUtil.connectUpdated(this.shellConnect);
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
        // 背景配置
        this.enableBackground.selectedChanged((observable, oldValue, newValue) -> {
            if (newValue) {
                NodeGroupUtil.enable(this.backgroundTab, "background");
            } else {
                NodeGroupUtil.disable(this.backgroundTab, "background");
            }
        });
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.shellConnect = this.getProp("shellConnect");
        this.name.setText(this.shellConnect.getName());
        this.remark.setText(this.shellConnect.getRemark());
        this.osType.selectType(this.shellConnect.getOsType());
        this.charset.setValue(this.shellConnect.getCharset());
        this.termType.select(this.shellConnect.getTermType());
        // 退格
        this.backspaceType.selectType(this.shellConnect.getBackspaceType());
        // alt修饰
        this.altSendsEscape.setSelected(this.shellConnect.isAltSendsEscape());
        // 背景配置
        this.backgroundImage.setText(this.shellConnect.getBackgroundImage());
        this.enableBackground.setSelected(this.shellConnect.isEnableBackground());
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.connectUpdateTitle();
    }

    // /**
    //  * 选择背景图片
    //  */
    // @FXML
    // private void chooseBackgroundImage() {
    //     File file = FileChooserHelper.choose(I18nHelper.pleaseSelectFile(), new FileExtensionFilter("Types", "*.jpeg", "*.jpg", "*.png", "*.gif"));
    //     if (file != null) {
    //         this.backgroundImage.setText(file.getPath());
    //     }
    // }
}
