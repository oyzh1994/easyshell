package cn.oyzh.easyssh.fx;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyssh.ssh.SSHClient;
import cn.oyzh.easyssh.ssh.SSHSftp;
import cn.oyzh.easyssh.ssh.SftpFile;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class SSHSftpTableView extends FXTableView<SftpFile> {

    @Setter
    @Getter
    private SSHClient client;

    @Setter
    @Getter
    private String currPath = "/";

    public SSHSftp sftp() {
        return this.client.getSftp();
    }

    public void loadFile() throws JSchException, SftpException, IOException {
        List<SftpFile> files = this.sftp().ls(this.currPath, this.client);
        this.setItem(files);
    }

    public void deleteFile() throws SftpException {
        List<SftpFile> files = this.getSelectedItems();
        if (CollectionUtil.isEmpty(files)) {
            return;
        }
        if (files.size() == 1) {
            SftpFile file = files.getFirst();
            if (file.isDir() && !MessageBox.confirm(I18nHelper.deleteDir() + " " + file.getFileName())) {
                return;
            } else if (!MessageBox.confirm(I18nHelper.deleteFile() + " " + file.getFileName())) {
                return;
            }
        } else if (!MessageBox.confirm(I18nHelper.deleteFiles())) {
            return;
        }
        if (CollectionUtil.isNotEmpty(files)) {
            for (SftpFile file : files) {
                this.sftp().rm(file);
            }
            this.removeItem(files);
        }
    }
}
