package cn.oyzh.easyshell.sftp;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * @author oyzh
 * @since 2025-04-28
 */
public class ShellSFTPDeleteTask {

    /**
     * 状态
     */
    private final StringProperty statusProperty = new SimpleStringProperty();

    /**
     * 开始时间
     */
    private long startTime;

    /**
     * 错误
     */
    private Throwable error;

    /**
     * 远程文件
     */
    private final ShellSFTPFile remoteFile;

    /**
     * 客户端
     */
    private final ShellSFTPClient client;

    /**
     * 状态
     */
    private transient ShellSFTPStatus status;

//    /**
//     * 上传文件
//     */
//    private final ShellSFTPDeleteFile deleteFile;

    public ShellSFTPDeleteTask(ShellSFTPFile remoteFile, ShellSFTPClient client) {
        this.client = client;
        this.remoteFile = remoteFile;
    }

    /**
     * 执行删除
     *
     * @throws Exception 异常
     */
    public void doDelete() throws Exception {
        this.updateStatus(ShellSFTPStatus.IN_PREPARATION);
        this.startTime = System.currentTimeMillis();
        this.updateStatus(ShellSFTPStatus.EXECUTE_ING);
        try {
            this.remoteFile.startWaiting();
            // 执行删除
            if (this.remoteFile.isDirectory()) {
                this.client.rmdirRecursive(this.remoteFile.getFilePath());
            } else {
                this.client.rm(this.remoteFile.getFilePath());
            }
        } catch (Exception ex) {// 其他
            // 忽略中断异常
            if (!ExceptionUtil.hasMessage(ex, "InterruptedIOException")) {
                this.error = ex;
                this.updateStatus(ShellSFTPStatus.FAILED);
                throw ex;
            }
        } finally {
            this.remoteFile.stopWaiting();
        }
        if (this.status != ShellSFTPStatus.CANCELED && this.status != ShellSFTPStatus.FAILED) {
            this.updateStatus(ShellSFTPStatus.FINISHED);
        }
    }

    /**
     * 取消
     */
    public void cancel() {
//        this.deleteFile.getTask().interrupt();
        ThreadUtil.interrupt(this.worker);
        this.updateStatus(ShellSFTPStatus.CANCELED);
    }

    public String getFilePath() {
        return this.remoteFile.getFilePath();
    }

    /**
     * 更新状态
     *
     * @param status 状态
     */
    private void updateStatus(ShellSFTPStatus status) {
        this.status = status;
        switch (status) {
            case FAILED:
                this.statusProperty.set(I18nHelper.deleteFailed());
                break;
            case CANCELED:
                this.statusProperty.set(I18nHelper.cancel());
                break;
            case FINISHED:
                this.statusProperty.set(I18nHelper.finished());
                break;
            case EXECUTE_ING:
                this.statusProperty.set(I18nHelper.deleteIng());
                break;
            case IN_PREPARATION:
                this.statusProperty.set(I18nHelper.inPreparation());
                break;
        }
        JulLog.info("status:{}", this.statusProperty.get());
    }

    public StringProperty statusProperty() {
        return this.statusProperty;
    }

    private Thread worker;

    public void setWorker(Thread worker) {
        this.worker = worker;
    }
}
