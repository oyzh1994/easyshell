package cn.oyzh.easyshell.controller.data;

import cn.oyzh.common.date.DateHelper;
import cn.oyzh.common.file.FileUtil;
import cn.oyzh.easyshell.dto.ShellDataExport;
import cn.oyzh.easyshell.sync.ShellSyncManager;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.chooser.FXChooser;
import cn.oyzh.fx.plus.chooser.FileChooserHelper;
import cn.oyzh.fx.plus.chooser.FileExtensionFilter;
import cn.oyzh.fx.plus.controller.StageController;
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
 * 数据导出业务
 *
 * @author oyzh
 * @since 2025/02/21
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "data/shellDataExport.fxml"
)
public class ShellDataExportController extends StageController {

    /**
     * 导出文件
     */
    private File exportFile;

    /**
     * 文件名
     */
    @FXML
    private FXText fileName;

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
     * 执行导出
     */
    @FXML
    private void doExport() {
        if (this.exportFile == null) {
            MessageBox.warn(I18nHelper.pleaseSelectFile());
            return;
        }
        try {
            // 获取同步数据
            ShellDataExport export = ShellSyncManager.getSyncData(this.key.isSelected(),
                    this.group.isSelected(),
                    this.snippet.isSelected(),
                    this.connect.isSelected());
            // ShellDataExport export = ShellDataExport.of();
            // // 密钥
            // if (this.key.isSelected()) {
            //     export.setKeys(this.keyStore.selectList());
            // }
            // // 分组
            // if (this.group.isSelected()) {
            //     export.setGroups(this.groupStore.load());
            // }
            // // 连接
            // if (this.connect.isSelected()) {
            //     export.setConnects(this.connectStore.loadFull());
            // }
            // // 片段
            // if (this.snippet.isSelected()) {
            //     export.setSnippets(this.snippetStore.selectList());
            // }
            FileUtil.writeUtf8String(export.toJSONString(), this.exportFile);
            this.closeWindow();
            MessageBox.okToast(I18nHelper.exportDataSuccess());
        } catch (Exception ex) {
            MessageBox.exception(ex, I18nHelper.exportConnectionFail());
        }
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.exportData();
    }

    /**
     * 选择文件
     */
    @FXML
    private void selectFile() {
        FileExtensionFilter filter = FXChooser.jsonExtensionFilter();
        String fileName = "EasyShell-" + I18nHelper.connect() + "-" + DateHelper.formatDate() + ".json";
        this.exportFile = FileChooserHelper.save(fileName, fileName, filter);
        if (this.exportFile != null) {
            this.fileName.setText(this.exportFile.getPath());
        } else {
            this.fileName.clear();
        }
    }
}
