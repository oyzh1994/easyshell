package cn.oyzh.easyssh.tabs.connect;

import cn.oyzh.easyssh.trees.sftp.SSHSftpTableView;
import cn.oyzh.easyssh.ssh.SSHClient;
import cn.oyzh.easyssh.ssh.SSHSftp;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.fxml.FXML;

/**
 * ssh命令行tab内容组件
 *
 * @author oyzh
 * @since 2023/07/21
 */
public class SSHSftpTabController extends SubTabController {

    /**
     * ssh命令行文本域
     */
    @FXML
    private FXTab root;

    @FXML
    private FXLabel filePath;

    @FXML
    private SVGGlyph copyFilePath;

    @FXML
    private FXToggleSwitch hiddenFile;

    @FXML
    private SSHSftpTableView fileTable;

    private void init() {
        try {
            SSHClient client = this.client();
            SSHSftp sftp = client.openSftp();
            if (sftp.isConnected()) {
                return;
            }
            sftp.connect(client.connectTimeout());
            if (!sftp.isConnected()) {
                MessageBox.warn(I18nHelper.connectFail());
                return;
            }
            this.fileTable.setClient(client);
            this.fileTable.loadFile();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        this.client().close();
    }

    @Override
    public void onTabInit(RichTab tab) {
        super.onTabInit(tab);
        this.root.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1) {
                this.init();
            }
        });
        this.fileTable.currPathProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1 == null) {
                this.filePath.clear();
                this.copyFilePath.disable();
            } else {
                this.filePath.setText(t1);
                this.copyFilePath.enable();
            }
        });
        this.hiddenFile.selectedChanged((observableValue, aBoolean, t1) -> {
            try {
                this.fileTable.setShowHiddenFile(t1);
            } catch (Exception ex) {
                MessageBox.exception(ex);
            }
        });
    }

    @Override
    public SSHConnectTabController parent() {
        return (SSHConnectTabController) super.parent();
    }

    public SSHClient client() {
        return this.parent().client();
    }

    @FXML
    private void refreshFile() {
        try {
            this.fileTable.loadFile();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @FXML
    private void deleteFile() {
        try {
            this.fileTable.deleteFile();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @FXML
    private void returnDir() {
        try {
            this.fileTable.returnDir();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @FXML
    private void copyFilePath() {
        try {
            this.fileTable.copyFilePath();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }
}
