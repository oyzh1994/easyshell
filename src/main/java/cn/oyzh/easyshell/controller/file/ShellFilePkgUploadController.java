package cn.oyzh.easyshell.controller.file;

import cn.oyzh.common.compress.CompressUtil;
import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.util.UUIDUtil;
import cn.oyzh.fx.gui.text.area.ReadOnlyTextArea;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.WindowEvent;

import java.io.File;
import java.util.List;

/**
 * shell文件打包上传业务
 *
 * @author oyzh
 * @since 2025/09/26
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        value = FXConst.FXML_PATH + "file/shellFilePkgUpload.fxml"
)
public class ShellFilePkgUploadController extends StageController {

    /**
     * 目标路径
     */
    private List<File> files;

    /**
     * 信息
     */
    @FXML
    private ReadOnlyTextArea root;

    /**
     * 初始化文件
     */
    private void init() {
        String name = "upload_pack_" + UUIDUtil.uuidSimple() + ".tar.gz";
        File compressFile = FileUtil.newTmpFile(name);
        for (File file : files) {
            this.root.appendLine(I18nHelper.compress() + " " + file.getPath());
        }
        TaskManager.startAsync(() -> {
            try {
                CompressUtil.compress(this.files, compressFile.getPath(), CompressUtil.CompressType.TAR_GZ);
                this.root.appendLine(I18nHelper.uploadFile() + " " + compressFile.getPath());
                this.setProp("compressFile", compressFile);
                this.closeWindow();
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
                this.root.appendLine(I18nHelper.compressFailed());
            }
        });
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.stage.hideOnEscape();
        this.files = this.getProp("files");
        // 初始化
        this.init();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.packageUpload();
    }
}
