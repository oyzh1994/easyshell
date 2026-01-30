package cn.oyzh.easyshell.controller.data;

import cn.oyzh.common.file.FileNameUtil;
import cn.oyzh.common.file.FileUtil;
import cn.oyzh.easyshell.dto.ShellDataExport;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.sync.ShellSyncManager;
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


/**
 * 数据导入业务
 *
 * @author oyzh
 * @since 2025/02/21
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "data/shellDataImport.fxml"
)
public class ShellDataImportController extends StageController {

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
     * 连接
     */
    @FXML
    private FXCheckBox connect;

    /**
     * 分组
     */
    @FXML
    private FXCheckBox group;

    /**
     * 密钥
     */
    @FXML
    private FXCheckBox key;

    /**
     * 片段
     */
    @FXML
    private FXCheckBox snippet;

    // /**
    //  * 密钥存储
    //  */
    // private final ShellKeyStore keyStore = ShellKeyStore.INSTANCE;
    //
    // /**
    //  * 分组存储
    //  */
    // private final ShellGroupStore groupStore = ShellGroupStore.INSTANCE;
    //
    // /**
    //  * 片段存储
    //  */
    // private final ShellSnippetStore snippetStore = ShellSnippetStore.INSTANCE;
    //
    // /**
    //  * 连接存储
    //  */
    // private final ShellConnectStore connectStore = ShellConnectStore.INSTANCE;

    /**
     * 执行导入
     */
    @FXML
    private void doImport() {
        if (this.importFile == null) {
            MessageBox.warn(I18nHelper.pleaseSelectFile());
            return;
        }
        try {
            String text = FileUtil.readUtf8String(this.importFile);
            ShellDataExport export = ShellDataExport.fromJSON(text);
            // boolean success = true;
            // List<ShellKey> keys = export.getKeys();
            // if (this.key.isSelected() && CollectionUtil.isNotEmpty(keys)) {
            //     for (ShellKey key : keys) {
            //         if (!this.keyStore.replace(key)) {
            //             success = false;
            //         }
            //     }
            // }
            // List<ShellGroup> groups = export.getGroups();
            // if (this.group.isSelected() && CollectionUtil.isNotEmpty(groups)) {
            //     for (ShellGroup group : groups) {
            //         if (!this.groupStore.replace(group)) {
            //             success = false;
            //         }
            //     }
            // }
            // List<ShellSnippet> snippets = export.getSnippets();
            // if (this.snippet.isSelected() && CollectionUtil.isNotEmpty(snippets)) {
            //     for (ShellSnippet snippet : snippets) {
            //         if (!this.snippetStore.replace(snippet)) {
            //             success = false;
            //         }
            //     }
            // }
            // List<ShellConnect> connects = export.getConnects();
            // if (this.connect.isSelected() && CollectionUtil.isNotEmpty(connects)) {
            //     for (ShellConnect connect : connects) {
            //         if (!this.connectStore.replace(connect)) {
            //             success = false;
            //         }
            //     }
            // }

            // 保存同步数据
            ShellSyncManager.saveSyncData(export,
                    this.key.isSelected(),
                    this.group.isSelected(),
                    this.snippet.isSelected(),
                    this.connect.isSelected());

            // if (success) {
            ShellEventUtil.dataImported();
            this.closeWindow();
            MessageBox.okToast(I18nHelper.importDataSuccess());
            // } else {
            //     MessageBox.warn(I18nHelper.importDataFail());
            // }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, I18nHelper.importDataFail());
        }
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.importData();
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
            MessageBox.warn(I18nHelper.fileNotExists(), this.getStage());
            return;
        }
        if (this.importFile.isDirectory()) {
            MessageBox.warn(I18nHelper.notSupportFolder(), this.getStage());
            return;
        }
        if (!FileNameUtil.isJsonType(FileNameUtil.extName(this.importFile.getName()))) {
            MessageBox.warn(I18nHelper.invalidFormat(), this.getStage());
            return;
        }
        if (this.importFile.length() == 0) {
            MessageBox.warn(I18nHelper.contentCanNotEmpty(), this.getStage());
        }
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.stage.hideOnEscape();
        this.importFile = this.getProp("file");
        if (this.importFile != null) {
            this.selectFile.disable();
            this.parseFile();
        }
    }
}
