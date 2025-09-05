package cn.oyzh.easyshell.controller.tool;

import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.easyshell.ShellConst;
import cn.oyzh.fx.plus.controller.SubStageController;
import cn.oyzh.fx.plus.controls.text.area.FXTextArea;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.StageManager;
import javafx.fxml.FXML;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;


/**
 * shell工具箱 缓存业务
 *
 * @author oyzh
 * @since 2025/03/09
 */
public class ShellToolCacheTabController extends SubStageController {

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
        StageManager.showMask(() -> {
            try {
                this.cacheArea.setText("calc cache start.");
                File dir = new File(ShellConst.getCachePath());
                this.doCalcCache(dir, new AtomicInteger(0), new LongAdder());
            } finally {
                this.cacheArea.appendLine("calc cache finish.");
            }
        });
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
        StageManager.showMask(() -> {
            try {
                this.cacheArea.setText("clear cache start.");
                File dir = new File(ShellConst.getCachePath());
                this.doClearCache(dir, new AtomicInteger(0), new LongAdder());
            } finally {
                this.cacheArea.appendLine("clear cache finish.");
            }
        });
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
