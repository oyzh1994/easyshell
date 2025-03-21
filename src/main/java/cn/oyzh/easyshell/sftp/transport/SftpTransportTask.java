package cn.oyzh.easyshell.sftp.transport;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.easyshell.sftp.SftpFile;
import cn.oyzh.easyshell.sftp.SftpUtil;
import cn.oyzh.easyshell.sftp.ShellSftp;
import cn.oyzh.i18n.I18nHelper;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author oyzh
 * @since 2025-03-15
 */
public class SftpTransportTask {

    /**
     * 执行线程
     */
    private final Thread executeThread;

    /**
     * 传输监听器
     */
    private final Queue<SftpTransportMonitor> monitors = new ConcurrentLinkedQueue<>();

    public SftpTransportMonitor takeMonitor() {
        return this.monitors.peek();
    }

    public void removeMonitor(SftpTransportMonitor monitor) {
        this.monitors.remove(monitor);
        this.updateTotal();
    }

    public boolean isEmpty() {
        return this.monitors.isEmpty();
    }

    /**
     * 传输状态
     */
    private SftpTransportStatus status;

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
    private void updateStatus(SftpTransportStatus status) {
        this.status = status;
        switch (status) {
            case FAILED -> this.statusProperty.set(I18nHelper.failed());
            case FINISHED -> this.statusProperty.set(I18nHelper.finished());
            case CANCELED -> this.statusProperty.set(I18nHelper.canceled());
            case TRANSPORT_ING -> this.statusProperty.set(I18nHelper.transportIng());
            default -> this.statusProperty.set(I18nHelper.inPreparation());
        }
    }

    private final String destPath;

    public String getDestPath() {
        return destPath;
    }

    private final SftpTransportManager manager;

    public SftpTransportTask(SftpTransportManager manager, SftpFile localFile, String remoteFile, ShellSftp localSftp, ShellSftp remoteSftp) {
        this.manager = manager;
        this.destPath = remoteFile;
        this.executeThread = ThreadUtil.start(() -> {
            try {
                localSftp.setHolding(true);
                remoteSftp.setHolding(true);
                this.updateStatus(SftpTransportStatus.IN_PREPARATION);
                this.addMonitorRecursive(localFile, remoteFile, localSftp, remoteSftp);
                this.updateStatus(SftpTransportStatus.TRANSPORT_ING);
                this.updateTotal();
                this.doTransport();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                localSftp.setHolding(false);
                remoteSftp.setHolding(false);
                // 如果是非取消，则设置为结束
                if (this.status != SftpTransportStatus.CANCELED) {
                    this.updateStatus(SftpTransportStatus.FINISHED);
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
     * @param localSftp  sftp操作器
     * @throws SftpException 异常
     */
    protected void addMonitorRecursive(SftpFile localFile, String remoteFile, ShellSftp localSftp, ShellSftp remoteSftp) throws SftpException {
        // 文件夹
        if (localFile.isDirectory()) {
            // 列举文件
            List<SftpFile> files = localSftp.lsFileNormal(localFile.getPath());
            // 处理文件
            if (CollectionUtil.isNotEmpty(files)) {
                // 远程文件夹
                String remoteDir = SftpUtil.concat(remoteFile, localFile.getName());
                // 递归创建文件夹
                localSftp.mkdirRecursive(remoteDir);
                // 添加文件
                for (SftpFile file : files) {
                    if (file.isDirectory()) {
                        this.addMonitorRecursive(file, remoteDir, localSftp, remoteSftp);
                    } else {
                        String remoteFile1 = SftpUtil.concat(remoteDir, file.getName());
                        this.addMonitorRecursive(file, remoteFile1, localSftp, remoteSftp);
                    }
                }
            }
        } else {// 文件
            this.updateTotal();
            this.monitors.add(new SftpTransportMonitor(localFile, remoteFile, this, localSftp, remoteSftp));
        }
    }

    /**
     * 执行传输
     */
    private void doTransport() {
        while (!this.isEmpty()) {
            SftpTransportMonitor monitor = this.takeMonitor();
            if (monitor == null) {
                break;
            }
            if (monitor.isFinished()) {
                ThreadUtil.sleep(5);
                continue;
            }
            ShellSftp localSftp = monitor.getLocalSftp();
            ShellSftp remoteSftp = monitor.getRemoteSftp();
            try {
                InputStream input = localSftp.get(monitor.getLocalFilePath());
                OutputStream output = remoteSftp.put(monitor.getRemoteFile(), monitor);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
                IOUtil.close(input);
                IOUtil.close(output);
            } catch (Exception ex) {
                if (ExceptionUtil.hasMessage(ex, "InterruptedIOException")) {
                    JulLog.warn("transport canceled");
                } else {
                    ex.printStackTrace();
                    JulLog.warn("file:{} transport failed", monitor.getLocalFileName(), ex);
                    this.transportFailed(monitor, ex);
                }
            }
            ThreadUtil.sleep(5);
        }
    }

    /**
     * 传输完成
     *
     * @param monitor 监听器
     */
    public void transportEnded(SftpTransportMonitor monitor) {
        this.monitors.remove(monitor);
        this.updateTotal();
    }

    /**
     * 传输失败
     *
     * @param monitor   监听器
     * @param exception 异常
     */
    public void transportFailed(SftpTransportMonitor monitor, Exception exception) {
        this.monitors.remove(monitor);
        this.updateTotal();
    }

    /**
     * 传输取消
     *
     * @param monitor 监听器
     */
    public void transportCanceled(SftpTransportMonitor monitor) {
        this.monitors.remove(monitor);
        this.updateTotal();
    }

    /**
     * 传输变化
     *
     * @param monitor 监听器
     */
    public void transportChanged(SftpTransportMonitor monitor) {
        this.currentFileProperty.set(monitor.getLocalFilePath());
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
    private final IntegerProperty totalCountProperty = new SimpleIntegerProperty();

    public StringProperty totalSizeProperty() {
        return totalSizeProperty;
    }

    /**
     * 更新总信息
     */
    private void updateTotal() {
        this.totalCountProperty.set(this.monitors.size());
        long totalSize = 0;
        for (SftpTransportMonitor monitor : this.monitors) {
            totalSize += monitor.getLocalFileLength();
        }
        this.totalSizeProperty.set(NumberUtil.formatSize(totalSize, 2));
        JulLog.debug("total size:{}", this.totalSizeProperty.get());
        JulLog.debug("total count:{}", this.totalCountProperty.get());
//        this.manager.updateUploading();
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
        for (SftpTransportMonitor monitor : this.monitors) {
            try {
                monitor.cancel();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        ThreadUtil.start(this.monitors::clear, 500);
        this.updateStatus(SftpTransportStatus.CANCELED);
    }

    /**
     * 移除
     */
    public void remove() {
        this.manager.remove(this);
    }

    /**
     * 是否已完成
     *
     * @return 结果
     */
    public boolean isFinished() {
        return this.status == SftpTransportStatus.FINISHED;
    }

    /**
     * 是否传输中
     *
     * @return 结果
     */
    public boolean isTransporting() {
        return this.status == SftpTransportStatus.TRANSPORT_ING;
    }

    /**
     * 是否准备中
     *
     * @return 结果
     */
    public boolean isInPreparation() {
        return this.status == SftpTransportStatus.IN_PREPARATION;
    }
}
