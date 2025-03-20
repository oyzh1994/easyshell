package cn.oyzh.easyshell.controller.connect;

import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellGroup;
import cn.oyzh.easyshell.dto.ShellConnectExport;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.store.ShellConnectStore;
import cn.oyzh.easyshell.store.ShellGroupStore;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.chooser.FXChooser;
import cn.oyzh.fx.plus.chooser.FileChooserHelper;
import cn.oyzh.fx.plus.chooser.FileExtensionFilter;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.button.FXButton;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.text.FXText;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.io.File;
import java.util.List;


/**
 * ssh连接导入业务
 *
 * @author oyzh
 * @since 2025/02/21
 */
@StageAttribute(
        stageStyle = FXStageStyle.UNIFIED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "connect/shellImportConnect.fxml"
)
public class ShellImportConnectController extends StageController {

    /**
     * 导入文件
     */
    private File importFile;

    /**
     * 文件名
     */
    @FXML
    private FXText fileName;

    /**
     * 选择文件
     */
    @FXML
    private FXButton selectFile;

    /**
     * 包含分组
     */
    @FXML
    private FXCheckBox includeGroup;

    /**
     * 分组存储
     */
    private final ShellGroupStore groupStore = ShellGroupStore.INSTANCE;

    /**
     * 连接存储
     */
    private final ShellConnectStore connectStore = ShellConnectStore.INSTANCE;

    /**
     * 执行导入
     */
    @FXML
    private void doImport() {
        try {
            String text = FileUtil.readUtf8String(this.importFile);
            ShellConnectExport export = ShellConnectExport.fromJSON(text);
            List<ShellConnect> connects = export.getConnects();
            boolean success = true;
            if (CollectionUtil.isNotEmpty(connects)) {
                for (ShellConnect connect : connects) {
                    if (!this.connectStore.replace(connect)) {
                        success = false;
                    }
                }
            }
            List<ShellGroup> groups = export.getGroups();
            if (this.includeGroup.isSelected() && CollectionUtil.isNotEmpty(groups)) {
                for (ShellGroup group : groups) {
                    if (!this.groupStore.replace(group)) {
                        success = false;
                    }
                }
            }
            if (success) {
                ShellEventUtil.connectImported();
                this.closeWindow();
                MessageBox.okToast(I18nHelper.importConnectionSuccess());
            } else {
                MessageBox.warn(I18nHelper.importConnectionFail());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, I18nHelper.importConnectionFail());
        }
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.importConnect();
    }

    /**
     * 选择文件
     */
    @FXML
    private void selectFile() {
        FileExtensionFilter filter = FXChooser.jsonExtensionFilter();
        this.importFile = FileChooserHelper.choose(I18nHelper.pleaseSelectFile(), filter);
        this.parseFile();
    }

    private void parseFile() {
        if (this.importFile == null) {
            this.fileName.clear();
            return;
        }
        this.fileName.setText(this.importFile.getPath());
        if (!this.importFile.exists()) {
            MessageBox.warn(I18nHelper.fileNotExists());
            return;
        }
        if (this.importFile.isDirectory()) {
            MessageBox.warn(I18nHelper.notSupportFolder());
            return;
        }
        if (!FileNameUtil.isJsonType(FileNameUtil.extName(this.importFile.getName()))) {
            MessageBox.warn(I18nHelper.invalidFormat());
            return;
        }
        if (this.importFile.length() == 0) {
            MessageBox.warn(I18nHelper.contentCanNotEmpty());
        }
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.importFile = this.getWindowProp("file");
        if (this.importFile != null) {
            this.selectFile.disable();
            this.parseFile();
        }
    }
}
