package cn.oyzh.easyshell.sftp.download;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.easyshell.sftp.ShellSftp;
import cn.oyzh.easyshell.sftp.SftpFile;
import cn.oyzh.i18n.I18nHelper;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.File;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

/**
 * @author oyzh
 * @since 2025-03-15
 */
public class SftpDownloadTask {

    /**
     * 执行线程
     */
    private final Thread executeThread;

    /**
     * 下载监控列表
     */
    private final Queue<SftpDownloadMonitor> monitors = new ArrayDeque<>();

    public SftpDownloadMonitor takeMonitor() {
        return this.monitors.peek();
    }

    public void removeMonitor(SftpDownloadMonitor monitor) {
        this.monitors.remove(monitor);
        this.updateTotal();
    }

    public boolean isEmpty() {
        return this.monitors.isEmpty();
    }

    /**
     * 下载状态
     */
    private SftpDownloadStatus status;

    /**
     * 状态属性
     */
    private final StringProperty statusProperty = new SimpleStringProperty();

    public StringProperty statusProperty() {
        return statusProperty;
    }

    /**
     * 更新状态
     *
     * @param status 状态
     */
    private void updateStatus(SftpDownloadStatus status) {
        this.status = status;
        switch (status) {
            case FAILED -> this.statusProperty.set(I18nHelper.failed());
            case FINISHED -> this.statusProperty.set(I18nHelper.finished());
            case CANCELED -> this.statusProperty.set(I18nHelper.canceled());
            case DOWNLOADING -> this.statusProperty.set(I18nHelper.downloadIng());
            default -> this.statusProperty.set(I18nHelper.inPreparation());
        }
    }

    private final SftpDownloadManager manager;

    public SftpDownloadTask(SftpDownloadManager manager, File localFile, SftpFile remoteFile, ShellSftp sftp) {
        this.manager = manager;
        // 执行线程
        this.executeThread = ThreadUtil.start(() -> {
            try {
                sftp.setHolding(true);
                this.updateStatus(SftpDownloadStatus.IN_PREPARATION);
                this.addMonitorRecursive(localFile, remoteFile, sftp);
                this.updateStatus(SftpDownloadStatus.DOWNLOADING);
                this.updateTotal();
                this.doDownload();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                sftp.setHolding(false);
                // 如果是非取消，则设置为结束
                if (this.status != SftpDownloadStatus.CANCELED) {
                    this.updateStatus(SftpDownloadStatus.FINISHED);
                }
                this.updateTotal();
            }
        });
    }

    /**
     * 递归添加监听器
     *
     * @param localFile  本地文件
     * @param remoteFile 远程文件
     * @param sftp       sftp操作器
     * @throws SftpException 异常
     */
    protected void addMonitorRecursive(File localFile, SftpFile remoteFile, ShellSftp sftp) throws SftpException {
        // 文件夹
        if (remoteFile.isDir()) {
            // 列举文件
            List<SftpFile> files = sftp.lsFileNormal(remoteFile.getFilePath());
            // 处理文件
            if (CollectionUtil.isNotEmpty(files)) {
                // 本地文件夹
                File localDir = new File(localFile.getPath(), remoteFile.getFileName());
                FileUtil.mkdir(localDir);
                // 添加文件
                for (SftpFile file : files) {
                    file.setParentPath(remoteFile.getFilePath());
                    if (file.isDir()) {
                        this.addMonitorRecursive(localDir, file, sftp);
                    } else {
                        File localFile1 = new File(localDir, file.getFileName());
                        this.addMonitorRecursive(localFile1, file, sftp);
                    }
                }
            }
        } else {// 文件
            this.monitors.add(new SftpDownloadMonitor(localFile, remoteFile, this, sftp));
        }
    }

    /**
     * 执行下载
     */
    private void doDownload() {
        while (!this.isEmpty()) {
            SftpDownloadMonitor monitor = this.takeMonitor();
            if (monitor == null) {
                break;
            }
            if (monitor.isFinished()) {
                ThreadUtil.sleep(5);
                continue;
            }
            ShellSftp sftp = monitor.getSftp();
            try {
                sftp.get(monitor.getRemoteFilePath(), monitor.getLocalFilePath(), monitor, ChannelSftp.OVERWRITE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JulLog.warn("file:{} download failed", monitor.getRemoteFileName(), ex);
                this.downloadFailed(monitor, ex);
            }
            ThreadUtil.sleep(5);
        }
    }

    /**
     * 下载完成
     *
     * @param monitor 监听器
     */
    public void downloadEnded(SftpDownloadMonitor monitor) {
        this.monitors.remove(monitor);
        this.updateTotal();
    }

    /**
     * 下载失败
     *
     * @param monitor   监听器
     * @param exception 异常
     */
    public void downloadFailed(SftpDownloadMonitor monitor, Exception exception) {
        this.monitors.remove(monitor);
        this.updateTotal();
    }

    /**
     * 下载取消
     *
     * @param monitor 监听器
     */
    public void downloadCanceled(SftpDownloadMonitor monitor) {
        this.monitors.remove(monitor);
        this.updateTotal();
    }

    /**
     * 下载变化
     *
     * @param monitor 监听器
     */
    public void downloadChanged(SftpDownloadMonitor monitor) {
        this.currentFileProperty.set(monitor.getRemoteFilePath());
        this.currentProgressProperty.set(NumberUtil.formatSize(monitor.getCurrent(), 2) + "/" + NumberUtil.formatSize(monitor.getTotal(), 2));
        JulLog.debug("current file:{}", this.currentFileProperty.get());
        JulLog.debug("current progress:{}", this.currentProgressProperty.get());
    }

    /**
     * 总大小属性
     */
    private final StringProperty totalSizeProperty = new SimpleStringProperty();

    public IntegerProperty totalCountProperty() {
        return totalCountProperty;
    }

    /**
     * 总数量属性
     */
    private final IntegerProperty totalCountProperty = new SimpleIntegerProperty(0);

    public StringProperty totalSizeProperty() {
        return totalSizeProperty;
    }

    /**
     * 更新总信息
     */
    private void updateTotal() {
        this.totalCountProperty.set(this.monitors.size());
        long totalSize = 0;
        for (SftpDownloadMonitor monitor : this.monitors) {
            totalSize += monitor.getRemoteLength();
        }
        this.totalSizeProperty.set(NumberUtil.formatSize(totalSize, 2));
        JulLog.debug("total size:{}", this.totalSizeProperty.get());
        JulLog.debug("total count:{}", this.totalCountProperty.get());
        this.manager.updateDownloading();
    }

    /**
     * 当前文件属性
     */
    private final StringProperty currentFileProperty = new SimpleStringProperty();

    public StringProperty currentFileProperty() {
        return currentFileProperty;
    }

    /**
     * 当前进度属性
     */
    private final StringProperty currentProgressProperty = new SimpleStringProperty();

    public StringProperty currentProgressProperty() {
        return currentProgressProperty;
    }

    /**
     * 取消
     */
    public void cancel() {
        // 停止线程
        ThreadUtil.interrupt(this.executeThread);
        // 取消业务
        for (SftpDownloadMonitor monitor : this.monitors) {
            try {
                monitor.cancel();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        ThreadUtil.start(this.monitors::clear, 500);
        this.updateStatus(SftpDownloadStatus.CANCELED);
    }

    /**
     * 是否结束
     *
     * @return 结果
     */
    public boolean isFinished() {
        return this.status == SftpDownloadStatus.FINISHED;
    }

    /**
     * 是否下载中
     *
     * @return 结果
     */
    public boolean isDownloading() {
        return this.status == SftpDownloadStatus.DOWNLOADING;
    }
}
