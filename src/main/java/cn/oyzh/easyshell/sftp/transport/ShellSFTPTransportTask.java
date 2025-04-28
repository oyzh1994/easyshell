package cn.oyzh.easyshell.sftp.transport;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.sftp.ShellSFTPClient;
import cn.oyzh.easyshell.sftp.ShellSFTPFile;
import cn.oyzh.easyshell.sftp.ShellSFTPStatus;
import cn.oyzh.easyshell.sftp.ShellSFTPTask;
import cn.oyzh.easyshell.util.ShellFileUtil;
import cn.oyzh.i18n.I18nHelper;
import com.jcraft.jsch.SftpException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-03-15
 */
public class ShellSFTPTransportTask extends ShellSFTPTask<ShellSFTPTransportMonitor> {

//    /**
//     * 传输状态
//     */
//    private ShellSFTPTransportStatus status;

    /**
     * 更新状态
     *
     * @param status 状态
     */
    @Override
    public void updateStatus(ShellSFTPStatus status) {
        super.updateStatus(status);
        if (status == ShellSFTPStatus.EXECUTE_ING) {
            this.statusProperty.set(I18nHelper.transportIng());
        }
        this.manager.taskStatusChanged(this.getStatus(), this);
    }

    private final ShellSFTPFile localFile;

    private final String remoteFile;

    private final ShellSFTPClient localClient;

    private final ShellSFTPClient remoteClient;

    private final ShellSFTPTransportManager manager;

    @Override
    public String getSrcPath() {
        return this.localFile.getName();
    }

    @Override
    public String getDestPath() {
        return this.remoteFile;
    }

    public ShellSFTPTransportTask(ShellSFTPTransportManager manager, ShellSFTPFile localFile, String remoteFile, ShellSFTPClient localClient, ShellSFTPClient remoteClient) {
        this.manager = manager;
        this.localFile = localFile;
        this.remoteFile = remoteFile;
        this.localClient = localClient;
        this.remoteClient = remoteClient;
//        this.currentFileProperty().set(localFile.getPath());
//        this.executeThread = ThreadUtil.start(() -> {
//            try {
//                this.updateStatus(ShellSFTPTransportStatus.IN_PREPARATION);
//                this.addMonitorRecursive(localFile, remoteFile);
//                this.updateStatus(ShellSFTPTransportStatus.TRANSPORT_ING);
//                this.calcTotalSize();
//                this.updateTotal();
//                this.doTransport();
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            } finally {
//                this.updateTotal();
//                // 如果是非取消和失败，则设置为结束
//                if (!this.isCancelled() && !this.isFailed()) {
//                    this.updateStatus(ShellSFTPTransportStatus.FINISHED);
//                }
//            }
//        });
    }

    /**
     * 执行传输
     */
    public void transport() {
        try {
            this.updateStatus(ShellSFTPStatus.IN_PREPARATION);
            this.addMonitorRecursive(localFile, remoteFile);
            this.updateStatus(ShellSFTPStatus.EXECUTE_ING);
            this.calcTotalSize();
//            this.updateTotal();
            this.doTransport();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
//            this.updateTotal();
            // 如果是非取消和失败，则设置为结束
            if (!this.isCancelled() && !this.isFailed()) {
                this.updateStatus(ShellSFTPStatus.FINISHED);
            }
        }
    }

    /**
     * 递归添加监听器
     *
     * @param localFile  本地文件
     * @param remoteFile 远程文件
     * @throws SftpException 异常
     */
    protected void addMonitorRecursive(ShellSFTPFile localFile, String remoteFile) throws Exception {
        // 已取消则跳过
        if (this.isCancelled()) {
            return;
        }
        this.manager.taskStatusChanged(this.getStatus(), this);
        // 文件夹
        if (localFile.isDirectory()) {
            // 列举文件
            List<ShellSFTPFile> files = this.localClient.lsFileNormal(localFile.getFilePath());
            // 处理文件
            if (CollectionUtil.isNotEmpty(files)) {
                // 远程文件夹
                String remoteDir = ShellFileUtil.concat(remoteFile, localFile.getName());
                // 递归创建文件夹
                this.remoteClient.mkdirRecursive(remoteDir);
                // 添加文件
                for (ShellSFTPFile file : files) {
                    if (file.isDirectory()) {
                        this.addMonitorRecursive(file, remoteDir);
                    } else {
                        String remoteFile1 = ShellFileUtil.concat(remoteDir, file.getName());
                        this.addMonitorRecursive(file, remoteFile1);
                    }
                }
            }
        } else {// 文件
//            this.updateTotal();
            this.monitors.add(new ShellSFTPTransportMonitor(localFile, remoteFile, this));
        }
    }

    /**
     * 执行传输
     */
    private void doTransport() {
        while (!this.isEmpty()) {
            ShellSFTPTransportMonitor monitor = this.takeMonitor();
            if (monitor == null) {
                break;
            }
            if (monitor.isCancelled()) {
                continue;
            }
            if (monitor.isFinished()) {
                ThreadUtil.sleep(5);
                continue;
            }
            try {
                InputStream input = this.localClient.get(monitor.getLocalFilePath());
                OutputStream output = this.remoteClient.put(monitor.getRemoteFile(), monitor);
                input.transferTo(output);
                IOUtil.close(input);
                IOUtil.close(output);
            } catch (Exception ex) {
                if (ExceptionUtil.hasMessage(ex, "InterruptedIOException", "canceled")) {
                    JulLog.warn("transport canceled");
                } else {
                    ex.printStackTrace();
                    JulLog.warn("file:{} transport failed", monitor.getLocalFileName(), ex);
                    this.failed(monitor, ex);
                    break;
                }
            }
            ThreadUtil.sleep(5);
        }
    }

    @Override
    public void cancel() {
        super.cancel();
        this.manager.remove(this);
        this.updateStatus(ShellSFTPStatus.CANCELED);
    }

    @Override
    public void remove() {
        this.manager.remove(this);
    }

//    @Override
//    public boolean isFailed() {
//        return this.status == ShellSFTPTransportStatus.FAILED;
//    }
//
//    @Override
//    public boolean isFinished() {
//        return this.status == ShellSFTPTransportStatus.FINISHED;
//    }
//
//    @Override
//    public boolean isCancelled() {
//        return this.status == ShellSFTPTransportStatus.CANCELED;
//    }

//    @Override
//    public boolean isInPreparation() {
//        return this.status == ShellSFTPTransportStatus.IN_PREPARATION;
//    }
//
//    /**
//     * 是否传输中
//     *
//     * @return 结果
//     */
//    public boolean isTransporting() {
//        return this.status == ShellSFTPTransportStatus.TRANSPORT_ING;
//    }

    @Override
    public void remove(ShellSFTPTransportMonitor monitor) {
        super.remove(monitor);
        this.manager.remove(this);
    }

    @Override
    public void ended(ShellSFTPTransportMonitor monitor) {
        super.ended(monitor);
        this.manager.monitorEnded(monitor, this);
        this.manager.remove(this);
//        this.updateStatus(ShellSFTPStatus.FINISHED);
    }

    @Override
    public void failed(ShellSFTPTransportMonitor monitor, Throwable exception) {
        super.failed(monitor, exception);
        this.manager.monitorFailed(monitor, exception);
        this.manager.remove(this);
//        this.updateStatus(ShellSFTPStatus.FAILED);
    }

    @Override
    public void canceled(ShellSFTPTransportMonitor monitor) {
        super.canceled(monitor);
        this.manager.monitorCanceled(monitor, this);
        this.manager.remove(this);
//        this.updateStatus(ShellSFTPStatus.CANCELED);
    }

    @Override
    public void changed(ShellSFTPTransportMonitor monitor) {
        super.changed(monitor);
        this.manager.monitorChanged(monitor, this);
    }
}
