package cn.oyzh.easyshell.sftp.transport;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.easyshell.sftp.SftpFile;
import cn.oyzh.easyshell.sftp.SftpTask;
import cn.oyzh.easyshell.sftp.SftpUtil;
import cn.oyzh.easyshell.sftp.ShellSftp;
import cn.oyzh.i18n.I18nHelper;
import com.jcraft.jsch.SftpException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-03-15
 */
public class SftpTransportTask extends SftpTask<SftpTransportMonitor> {

    /**
     * 传输状态
     */
    private SftpTransportStatus status;

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
                    this.failed(monitor, ex);
                }
            }
            ThreadUtil.sleep(5);
        }
    }

    @Override
    public void remove() {
        this.manager.remove(this);
    }

    @Override
    public boolean isFinished() {
        return this.status == SftpTransportStatus.FINISHED;
    }

    @Override
    public boolean isInPreparation() {
        return this.status == SftpTransportStatus.IN_PREPARATION;
    }

    public boolean isTransporting() {
        return this.status == SftpTransportStatus.TRANSPORT_ING;
    }

    @Override
    public void ended(SftpTransportMonitor monitor) {
        super.ended(monitor);
        this.manager.monitorEnded(monitor);
    }

    @Override
    public void failed(SftpTransportMonitor monitor, Throwable exception) {
        super.failed(monitor, exception);
        this.manager.monitorFailed(monitor,exception);
    }

    @Override
    public void canceled(SftpTransportMonitor monitor) {
        super.canceled(monitor);
        this.manager.monitorCanceled(monitor);
    }

    @Override
    public void changed(SftpTransportMonitor monitor) {
        super.changed(monitor);
        this.manager.monitorChanged(monitor);
    }
}
