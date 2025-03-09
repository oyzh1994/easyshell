package cn.oyzh.easyssh.sftp.delete;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyssh.sftp.SSHSftp;
import cn.oyzh.easyssh.sftp.SftpFile;
import cn.oyzh.easyssh.sftp.SftpUtil;
import cn.oyzh.easyssh.sftp.download.SftpDownloadCanceled;
import cn.oyzh.easyssh.sftp.download.SftpDownloadChanged;
import cn.oyzh.easyssh.sftp.download.SftpDownloadEnded;
import cn.oyzh.easyssh.sftp.download.SftpDownloadFailed;
import cn.oyzh.easyssh.sftp.download.SftpDownloadInPreparation;
import cn.oyzh.easyssh.sftp.download.SftpDownloadMonitor;
import cn.oyzh.fx.plus.information.MessageBox;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import lombok.Setter;

import java.io.File;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class SftpDeleteManager {

    @Setter
    private Consumer<SftpDeleteEnded> deleteEndedCallback;

    @Setter
    private Consumer<SftpDeleteDeleted> deleteDeletedCallback;

    public void deleteEnded() {
        if (this.deleteEndedCallback != null) {
            SftpDeleteEnded deleteEnded = new SftpDeleteEnded();
            this.deleteEndedCallback.accept(deleteEnded);
        }
    }

    public void deleteDeleted(SftpFile file) {
        if (this.deleteDeletedCallback != null) {
            String path = SftpUtil.concat(file.getFilePath(), file.getFileName());
            SftpDeleteDeleted deleteDeleted = new SftpDeleteDeleted();
            deleteDeleted.setRemoteFile(path);
            this.deleteDeletedCallback.accept(deleteDeleted);
        }
    }

    public void deleteDeleted(String path) {
        if (this.deleteDeletedCallback != null) {
            SftpDeleteDeleted deleteDeleted = new SftpDeleteDeleted();
            deleteDeleted.setRemoteFile(path);
            this.deleteDeletedCallback.accept(deleteDeleted);
        }
    }
}
