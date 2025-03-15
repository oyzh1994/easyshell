package cn.oyzh.easyshell.controller.tool;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.easyshell.SSHConst;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.text.area.FXTextArea;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;


/**
 * ssh工具箱业务
 *
 * @author oyzh
 * @since 2023/11/09
 */
@StageAttribute(
        stageStyle = FXStageStyle.UNIFIED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "tool/sshTool.fxml"
)
public class SSHToolController extends StageController {

    @Override
    public void onWindowShown(WindowEvent event) {
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.tools();
    }

    /**
     * 缓存文本域
     */
    @FXML
    private FXTextArea cacheArea;

    /**
     * 计算缓存
     */
    @FXML
    private void calcCache() {
        this.disable();
        this.cacheArea.setText("calc cache start.");
        ThreadUtil.start(() -> {
            try {
                File dir = new File(SSHConst.getCachePath());
                this.doCalcCache(dir, new AtomicInteger(0), new LongAdder());
            } finally {
                this.cacheArea.appendLine("calc cache finish.");
                this.enable();
            }
        }, 100);
    }

    /**
     * 计算缓存
     *
     * @param file      文件
     * @param fileCount 文件总数
     * @param fileSize  文件大小
     */
    private void doCalcCache(File file, AtomicInteger fileCount, LongAdder fileSize) {
        if (file.isFile()) {
            fileSize.add(file.length());
            fileCount.incrementAndGet();
            String sizeInfo = NumberUtil.formatSize(fileSize.longValue());
            FXUtil.runWait(() -> this.cacheArea.setText("find file: " + fileCount.get() + " total size: " + sizeInfo));
        } else {
            File[] files = file.listFiles();
            if (files != null) {
                for (File file1 : files) {
                    this.doCalcCache(file1, fileCount, fileSize);
                }
            }
        }
    }

    /**
     * 清理缓存
     */
    @FXML
    private void clearCache() {
        this.cacheArea.setText("clear cache start.");
        ThreadUtil.start(() -> {
            try {
                File dir = new File(SSHConst.getCachePath());
                this.doClearCache(dir, new AtomicInteger(0), new LongAdder());
            } finally {
                this.cacheArea.appendLine("clear cache finish.");
                this.enable();
            }
        }, 100);
    }

    /**
     * 清理缓存
     *
     * @param file      文件
     * @param fileCount 文件总数
     * @param fileSize  文件大小
     */
    private void doClearCache(File file, AtomicInteger fileCount, LongAdder fileSize) {
        if (file.isFile()) {
            fileSize.add(file.length());
            fileCount.incrementAndGet();
            String sizeInfo = NumberUtil.formatSize(fileSize.longValue());
            file.delete();
            FXUtil.runWait(() -> this.cacheArea.setText("delete file: " + fileCount.get() + " total size: " + sizeInfo));
        } else {
            File[] files = file.listFiles();
            if (files != null) {
                for (File file1 : files) {
                    this.doClearCache(file1, fileCount, fileSize);
                }
            }
        }
    }
}
