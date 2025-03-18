package cn.oyzh.easyshell.controller.sftp;

import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.common.util.UUIDUtil;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.sftp.SftpFile;
import cn.oyzh.easyshell.sftp.ShellSftp;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.fx.rich.richtextfx.data.RichDataTextAreaPane;
import cn.oyzh.fx.rich.richtextfx.data.RichDataType;
import cn.oyzh.fx.rich.richtextfx.data.RichDataTypeComboBox;
import cn.oyzh.i18n.I18nHelper;
import com.jcraft.jsch.SftpATTRS;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.io.FileInputStream;

/**
 * ssh文件编辑业务
 *
 * @author oyzh
 * @since 2025/03/18
 */
@StageAttribute(
        stageStyle = FXStageStyle.UNIFIED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "sftp/shellSftpFileEdit.fxml"
)
public class ShellSftpFileEditController extends StageController {

    /**
     * 远程文件
     */
    private SftpFile file;

    /**
     * 目标路径
     */
    private String destPath;

    /**
     * ssh客户端
     */
    private ShellClient client;

    /**
     * 数据
     */
    @FXML
    private RichDataTextAreaPane data;

    /**
     * 格式
     */
    @FXML
    private RichDataTypeComboBox format;

    /**
     * 保存文件
     */
    @FXML
    private void save() {
        StageManager.showMask(() -> {
            try {
                String content = data.getText();
                FileUtil.writeUtf8String(content, this.destPath);
                ShellSftp sftp = this.client.openSftp();
                sftp.put(new FileInputStream(this.destPath), file.getFilePath());
                SftpATTRS attrs = sftp.stat(file.getFilePath());
                this.file.setAttrs(attrs);
                ShellEventUtil.fileSaved(this.file);
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    /**
     * 初始化文件
     */
    private void init() {
        ShellSftp sftp = this.client.openSftp();
        StageManager.showMask(() -> {
            try {
                sftp.get(this.file.getFilePath(), this.destPath);
                this.data.setText(this.getData());
                String extName = FileNameUtil.extName(this.file.getFilePath());
                if (FileNameUtil.isJsonType(extName)) {
                    this.format.select(RichDataType.JSON);
                } else if (FileNameUtil.isHtmType(extName) || FileNameUtil.isHtmlType(extName)) {
                    this.format.select(RichDataType.HTML);
                } else if (FileNameUtil.isXmlType(extName)) {
                    this.format.select(RichDataType.XML);
                } else {
                    this.format.select(RichDataType.RAW);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    private String getData() {
        byte[] content = FileUtil.readBytes(this.destPath);
        if (content != null) {
            return new String(content);
        }
        return "";
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
        this.file = this.getWindowProp("file");
        this.client = this.getWindowProp("client");
        this.destPath = SystemUtil.tmpdir() + "/" + UUIDUtil.uuidSimple();
        this.init();
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.format.selectedItemChanged((observableValue, number, t1) -> {
            if (this.format.isJsonFormat()) {
                this.data.showJsonData(this.getData());
            } else if (this.format.isXmlFormat()) {
                this.data.showXmlData(this.getData());
            } else if (this.format.isHtmlFormat()) {
                this.data.showHtmlData(this.getData());
            } else if (this.format.isStringFormat()) {
                this.data.showStringData(this.getData());
            } else {
                this.data.showRawData(this.getData());
            }
        });
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.editFile();
    }
}
