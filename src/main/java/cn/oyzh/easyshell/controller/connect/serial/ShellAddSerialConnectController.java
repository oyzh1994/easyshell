package cn.oyzh.easyshell.controller.connect.serial;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellGroup;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.fx.serial.ShellSerialBaudRateTextFiled;
import cn.oyzh.easyshell.fx.serial.ShellSerialFlowControlComboBox;
import cn.oyzh.easyshell.fx.serial.ShellSerialNumDataBitsComboBox;
import cn.oyzh.easyshell.fx.serial.ShellSerialNumStopBitsComboBox;
import cn.oyzh.easyshell.fx.serial.ShellSerialParityBitsComboBox;
import cn.oyzh.easyshell.fx.serial.ShellSerialPortNameTextFiled;
import cn.oyzh.easyshell.fx.term.ShellTermBackspaceTypeCombobox;
import cn.oyzh.easyshell.store.ShellConnectStore;
import cn.oyzh.easyshell.util.ShellConnectUtil;
import cn.oyzh.fx.gui.combobox.CharsetComboBox;
import cn.oyzh.fx.gui.text.field.ChooseFileTextField;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.NumberTextField;
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
 * serial连接新增业务
 *
 * @author oyzh
 * @since 2025/04/24
 */
@StageAttribute(
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "connect/serial/shellAddSerialConnect.fxml"
)
public class ShellAddSerialConnectController extends StageController {

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
     * 端口名
     */
    @FXML
    private ShellSerialPortNameTextFiled portName;

    /**
     * 波特率
     */
    @FXML
    private ShellSerialBaudRateTextFiled baudRate;

    /**
     * 数据位
     */
    @FXML
    private ShellSerialNumDataBitsComboBox numDataBits;

    /**
     * 校验位
     */
    @FXML
    private ShellSerialParityBitsComboBox parityBits;

    /**
     * 停止位
     */
    @FXML
    private ShellSerialNumStopBitsComboBox numStopBits;

    /**
     * 流控
     */
    @FXML
    private ShellSerialFlowControlComboBox flowControl;

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
     * 连接超时时间
     */
    @FXML
    private NumberTextField connectTimeOut;

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
     * 分组
     */
    private ShellGroup group;

    /**
     * ssh连接储存对象
     */
    private final ShellConnectStore connectStore = ShellConnectStore.INSTANCE;

    /**
     * 测试连接
     */
    @FXML
    private void testConnect() {
        // 检查连接地址
        String portName = this.portName.getTextTrim();
        if (!this.portName.validate() || StringUtil.isBlank(portName) || StringUtil.isBlank(portName.split(":")[0])) {
//            MessageBox.warn(I18nHelper.contentCanNotEmpty());
        } else {
            // 创建连接信息
            ShellConnect shellConnect = new ShellConnect();
            shellConnect.setConnectTimeOut(3);
            // 串口信息
            shellConnect.setType("serial");
            shellConnect.setSerialPortName(portName);
            shellConnect.setSerialBaudRate(this.baudRate.getBaudRate());
            shellConnect.setSerialParityBits(this.parityBits.getParityBits());
            shellConnect.setSerialFlowControl(this.flowControl.getFlowControl());
            shellConnect.setSerialNumDataBits(this.numDataBits.getNumDataBits());
            shellConnect.setSerialNumStopBits(this.numStopBits.getNumStopBits());
            ShellConnectUtil.testConnect(this.stage, shellConnect);
        }
    }

    /**
     * 添加连接信息
     */
    @FXML
    private void add() {
        // 检查端口名称
        if (!this.portName.validate()) {
            return;
        }
        // 检查波特率
        if (!this.baudRate.validate()) {
            return;
        }
        // 检查背景配置
        if (this.enableBackground.isSelected()) {
            if (!this.backgroundImage.validate()) {
                this.tabPane.select(this.backgroundTab);
                return;
            }
        }
        String portName = this.portName.getTextTrim();
        // 名称未填，则直接以portName为名称
        if (StringUtil.isBlank(this.name.getTextTrim())) {
            this.name.setText(portName);
        }
        try {
            ShellConnect shellConnect = new ShellConnect();
            int baudRate = this.baudRate.getBaudRate();
            int parityBits = this.parityBits.getParityBits();
            int numDataBits = this.numDataBits.getNumDataBits();
            int flowControl = this.flowControl.getFlowControl();
            int numStopBits = this.numStopBits.getNumStopBits();
            String name = this.name.getTextTrim();
            String remark = this.remark.getTextTrim();
            String osType = this.osType.getSelectedItem();
            String charset = this.charset.getCharsetName();
            int connectTimeOut = this.connectTimeOut.getIntValue();
            String backgroundImage = this.backgroundImage.getText();
            int backspaceType = this.backspaceType.getSelectedIndex();
            boolean altSendsEscape = this.altSendsEscape.isSelected();
            boolean enableBackground = this.enableBackground.isSelected();

            shellConnect.setName(name);
            shellConnect.setOsType(osType);
            shellConnect.setRemark(remark);
            shellConnect.setCharset(charset);
            shellConnect.setBackspaceType(backspaceType);
            shellConnect.setAltSendsEscape(altSendsEscape);
            shellConnect.setConnectTimeOut(connectTimeOut);
            // 串口设置
            shellConnect.setSerialPortName(portName);
            shellConnect.setSerialBaudRate(baudRate);
            shellConnect.setSerialParityBits(parityBits);
            shellConnect.setSerialNumDataBits(numDataBits);
            shellConnect.setSerialNumStopBits(numStopBits);
            shellConnect.setSerialFlowControl(flowControl);
            // 背景配置
            shellConnect.setBackgroundImage(backgroundImage);
            shellConnect.setEnableBackground(enableBackground);
            // 分组及类型
            shellConnect.setType("serial");
            shellConnect.setGroupId(this.group == null ? null : this.group.getGid());
            // 保存数据
            if (this.connectStore.insert(shellConnect)) {
                ShellEventUtil.connectAdded(shellConnect);
                MessageBox.okToast(I18nHelper.operationSuccess());
                this.setProp("connect", shellConnect);
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
        this.group = this.getProp("group");
        this.osType.select("Serial");
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.connectAddTitle();
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
