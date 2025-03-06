package cn.oyzh.easyssh.tabs.connect;

import cn.oyzh.easyssh.domain.SSHSetting;
import cn.oyzh.easyssh.store.SSHSettingStore;
import cn.oyzh.easyssh.trees.sftp.SSHSftpTableView;
import cn.oyzh.easyssh.ssh.SSHClient;
import cn.oyzh.easyssh.ssh.SSHSftp;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
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

    @FXML
    private ClearableTextField filterFile;

    private final SSHSetting setting = SSHSettingStore.SETTING;

    private final SSHSettingStore settingStore = SSHSettingStore.INSTANCE;

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
            this.fileTable.setShowHiddenFile(this.setting.isShowHiddenFile());
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
        try {
            super.onTabInit(tab);
            this.hiddenFile.setSelected(this.setting.isShowHiddenFile());
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
                    this.setting.setShowHiddenFile(t1);
                    this.settingStore.update(this.setting);
                } catch (Exception ex) {
                    MessageBox.exception(ex);
                }
            });
            this.filterFile.addTextChangeListener((observableValue, aBoolean, t1) -> {
                try {
                    this.fileTable.setFilterText(t1);
                } catch (Exception ex) {
                    MessageBox.exception(ex);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
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
            MessageBox.okToast(I18nHelper.copySuccess());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @FXML
    private void mkDir() {
        try {
            String name = MessageBox.prompt(I18nHelper.pleaseInputName());
            this.fileTable.mkDir(name);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @FXML
    private void touchFile() {
        try {
            String name = MessageBox.prompt(I18nHelper.pleaseInputName());
            this.fileTable.touchFile(name);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }
}
