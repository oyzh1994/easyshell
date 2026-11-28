package cn.oyzh.easyshell.controller.connect.h2;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellGroup;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.fx.h2.H2DatabaseFileField;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.h2.ShellH2Helper;
import cn.oyzh.easyshell.store.ShellConnectStore;
import cn.oyzh.easyshell.util.ShellConnectUtil;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.gui.text.field.PasswordTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.button.FXRadioButton;
import cn.oyzh.fx.plus.controls.text.area.FXTextArea;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.i18n.I18nResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * H2 连接新增控制器。
 *
 * <p>支持文件、远程两种连接模式。</p>
 *
 * @since 2026-11-28
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "connect/h2/shellAddH2Connect.fxml"
)
public class ShellAddH2ConnectController extends StageController {

    @FXML
    private ClearableTextField name;

    @FXML
    private ToggleGroup modeGroup;

    @FXML
    private FXRadioButton modeFile;

    @FXML
    private FXRadioButton modeTcp;

    @FXML
    private FXVBox filePane;

    @FXML
    private FXVBox tcpPane;

    @FXML
    private H2DatabaseFileField filePath;

    @FXML
    private ClearableTextField host;

    @FXML
    private NumberTextField port;

    @FXML
    private ClearableTextField database;

    @FXML
    private ClearableTextField user;

    @FXML
    private PasswordTextField password;

    @FXML
    private ClearableTextField schema;

    @FXML
    private NumberTextField connectTimeOut;

    @FXML
    private FXCheckBox readOnly;

    @FXML
    private FXCheckBox createIfMissing;

    @FXML
    private ShellOsTypeComboBox osType;

    @FXML
    private FXTextArea env;

    @FXML
    private FXTextArea remark;

    private ShellGroup group;

    private final ShellConnectStore connectStore = ShellConnectStore.INSTANCE;

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.group = this.getProp("group");
        this.osType.selectType(ShellPrototype.H2);
        this.connectTimeOut.setValue(5);
        this.env.setText(ShellH2Helper.defaultEnvironment());
        this.createIfMissing.setSelected(false);
        this.modeFile.setSelected(true);
        this.switchMode();
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    /**
     * 切换连接模式，显示对应字段区域。
     */
    @FXML
    private void switchMode() {
        String mode = this.currentMode();
        boolean file = "file".equals(mode);
        this.filePane.setVisible(file);
        this.filePane.setManaged(file);
        this.tcpPane.setVisible(!file);
        this.tcpPane.setManaged(!file);
    }

    /**
     * 获取当前连接模式。
     *
     * @return 模式：file/tcp
     */
    private String currentMode() {
        Object data = this.modeGroup.getSelectedToggle() == null ? null : this.modeGroup.getSelectedToggle().getUserData();
        return data == null ? "file" : data.toString();
    }

    @FXML
    private void testConnect() {
        ShellConnect connect = this.buildConnect();
        if (connect != null) {
            ShellConnectUtil.testConnect(this.stage, connect, connect.connectTimeOutMs());
        }
    }

    @FXML
    private void add() {
        ShellConnect connect = this.buildConnect();
        if (connect == null) {
            return;
        }
        connect.setGroupId(this.group == null ? null : this.group.getGid());
        try {
            if (this.connectStore.insert(connect)) {
                ShellEventUtil.connectAdded(connect);
                MessageBox.okToast(I18nHelper.operationSuccess(), this.getWindow());
                this.setProp("connect", connect);
                this.closeWindow();
            } else {
                MessageBox.warn(I18nHelper.operationFail(), this.getWindow());
            }
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    private ShellConnect buildConnect() {
        try {
            String mode = this.currentMode();
            String schemaText = this.schema.getTextTrim();
            String hostValue;
            String environmentText = ShellH2Helper.setEnvironmentValue(
                    this.env.getText(), "urlMode", mode);
            environmentText = ShellH2Helper.setEnvironmentValue(
                    environmentText, "schema", StringUtil.isBlank(schemaText) ? "PUBLIC" : schemaText);
            environmentText = ShellH2Helper.setEnvironmentValue(
                    environmentText, "createIfMissing", String.valueOf(this.createIfMissing.isSelected()));
            ShellH2Helper.parseEnvironment(environmentText);
            ShellConnect connect = new ShellConnect();
            connect.setType(ShellPrototype.H2);
            connect.setOsType(ShellPrototype.H2);
            connect.setConnectTimeOut(this.connectTimeOut.getIntValue());
            connect.setReadonly(this.readOnly.isSelected());
            if ("tcp".equals(mode)) {
                String hostText = this.host.getTextTrim();
                if (StringUtil.isBlank(hostText)) {
                    MessageBox.warn(I18nResourceBundle.i18nString("base.host"), this.getWindow());
                    return null;
                }
                String databaseText = this.database.getTextTrim();
                if (StringUtil.isBlank(databaseText)) {
                    MessageBox.warn(I18nResourceBundle.i18nString("shell.h2.dbName"), this.getWindow());
                    return null;
                }
                hostValue = hostText + ":" + this.port.getIntValue();
                connect.setDatabase(databaseText);
                connect.setUser(this.user.getTextTrim());
                connect.setPassword(this.password.getText());
            } else {
                String filePathText = this.filePath.getTextTrim();
                if (StringUtil.isBlank(filePathText)) {
                    MessageBox.warn(I18nResourceBundle.i18nString("shell.h2.selectFile"), this.getWindow());
                    return null;
                }
                Path file = Path.of(filePathText).toAbsolutePath().normalize();
                boolean databaseFile = ShellH2Helper.isDatabaseFile(filePathText);
                if (Files.isRegularFile(file) && !databaseFile) {
                    MessageBox.warn(I18nResourceBundle.i18nString("shell.h2.invalidFileSuffix")
                            + " " + file, this.getWindow());
                    return null;
                }
                if (Files.exists(file) && !Files.isRegularFile(file)) {
                    MessageBox.warn(I18nResourceBundle.i18nString("shell.h2.pathNotFile")
                            + " " + file, this.getWindow());
                    return null;
                }
                Path actualFile = databaseFile ? file : ShellH2Helper.actualFile(file);
                if (!Boolean.TRUE.equals(this.createIfMissing.isSelected())
                        && !Files.isRegularFile(actualFile)) {
                    MessageBox.warn(I18nResourceBundle.i18nString("shell.h2.fileNotExist")
                            + " " + actualFile, this.getWindow());
                    return null;
                }
                hostValue = ShellH2Helper.databasePath(file.toString()).toString();
                connect.setDatabase(null);
                connect.setUser(null);
                connect.setPassword(null);
            }
            String nameText = this.name.getTextTrim();
            if (StringUtil.isBlank(nameText)) {
                nameText = switch (mode) {
                    case "file" -> Path.of(hostValue).getFileName().toString();
                    default -> this.database.getTextTrim();
                };
                this.name.setText(nameText);
            }
            connect.setName(nameText);
            connect.setHost(hostValue);
            connect.setEnvironment(environmentText);
            connect.setRemark(this.remark.getTextTrim());
            return connect;
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
            return null;
        }
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.connectAddTitle();
    }
}
