package cn.oyzh.easyshell.controller.connect.sqlite;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellGroup;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.fx.ShellOsTypeComboBox;
import cn.oyzh.easyshell.fx.sqlite.SQLiteDatabaseFileField;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.sqlite.ShellSqliteHelper;
import cn.oyzh.easyshell.store.ShellConnectStore;
import cn.oyzh.easyshell.util.ShellConnectUtil;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.text.area.FXTextArea;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.i18n.I18nResourceBundle;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * SQLite 连接新增控制器。
 *
 * @since 2026-11-02
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "connect/sqlite/shellAddSqliteConnect.fxml"
)
public class ShellAddSqliteConnectController extends StageController {

    @FXML
    private ClearableTextField name;

    @FXML
    private SQLiteDatabaseFileField filePath;

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
        this.osType.selectType(ShellPrototype.SQLITE);
        this.connectTimeOut.setValue(5);
        this.env.setText(ShellSqliteHelper.defaultEnvironment());
        this.createIfMissing.setSelected(true);
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
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
            String filePathText = this.filePath.getTextTrim();
            if (StringUtil.isBlank(filePathText)) {
                MessageBox.warn(I18nResourceBundle.i18nString("shell.sqlite.selectFile"),
                        this.getWindow());
                return null;
            }
            Path file = Path.of(filePathText).toAbsolutePath().normalize();
            if (!Boolean.TRUE.equals(this.createIfMissing.isSelected()) && !Files.isRegularFile(file)) {
                MessageBox.warn(I18nResourceBundle.i18nString("shell.sqlite.fileNotExist")
                        + " " + file, this.getWindow());
                return null;
            }
            if (Files.exists(file) && !Files.isRegularFile(file)) {
                MessageBox.warn(I18nResourceBundle.i18nString("shell.sqlite.pathNotFile")
                        + " " + file, this.getWindow());
                return null;
            }
            String nameText = this.name.getTextTrim();
            if (StringUtil.isBlank(nameText)) {
                nameText = file.getFileName().toString();
                this.name.setText(nameText);
            }
            String environmentText = ShellSqliteHelper.setEnvironmentValue(
                    this.env.getText(), "createIfMissing", String.valueOf(this.createIfMissing.isSelected()));
            ShellSqliteHelper.parseEnvironment(environmentText);
            ShellConnect connect = new ShellConnect();
            connect.setType(ShellPrototype.SQLITE);
            connect.setName(nameText);
            connect.setHost(file.toString());
            connect.setOsType(ShellPrototype.SQLITE);
            connect.setConnectTimeOut(this.connectTimeOut.getIntValue());
            connect.setReadonly(this.readOnly.isSelected());
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
