package cn.oyzh.easyshell.tabs.ssh.docker;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.exec.ShellExec;
import cn.oyzh.easyshell.fx.ShellJsonTextAreaPane;
import cn.oyzh.easyshell.sftp.ShellSftp;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.tabs.ssh.ShellDockerTabController;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;

import java.io.ByteArrayInputStream;

/**
 * ssh命令行tab内容组件
 *
 * @author oyzh
 * @since 2023/07/21
 */
public class ShellDockerDaemonTabController extends SubTabController {

    /**
     * 根节点
     */
    @FXML
    private FXTab root;

    /**
     * 文件路径
     */
    @FXML
    private FXLabel filePath;

    /**
     * cpu图表
     */
    @FXML
    private ShellJsonTextAreaPane data;

    @FXML
    private void refresh() {
        // 设置文件路径
        if (this.filePath.isEmpty()) {
            this.filePath.setText(this.client().dockerExec().getDaemonFilePath());
        }
        StageManager.showMask(() -> {
            try {
                ShellSftp sftp = this.client().openSftp();
                String filePath = this.filePath.getText();
                if (sftp.exist(filePath)) {
                    ShellExec exec = this.client().shellExec();
                    String output = exec.cat_docker_daemon(filePath);
                    this.data.setText(output);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    /**
     * 复制
     */
    @FXML
    private void copy() {
        ClipboardUtil.copy(this.data.getText());
        MessageBox.okToast(I18nHelper.operationSuccess());
    }

    /**
     * 保存
     */
    @FXML
    private void save() {
        String text = this.data.getText();
        StageManager.showMask(() -> {
            ShellExec exec = this.client().shellExec();
            try (ShellSftp sftp = this.client().openSftp()) {
                sftp.setUsing(true);
                // 创建json文件
                String jsonFile = this.filePath.getText();
                if (!sftp.exist(jsonFile)) {
                    sftp.touch(jsonFile);
                }
                // 创建临时文件
                String tempFile = this.filePath.getText() + ".temp";
                if (!sftp.exist(tempFile)) {
                    sftp.touch(tempFile);
                }
                // 上传内容
                sftp.put(new ByteArrayInputStream(text.getBytes()), tempFile);
                // 把临时文件内容copy到真实文件
//                String output = exec.echo("$(cat " + tempFile + ")", jsonFile);
                String output = exec.cat_file(tempFile, jsonFile);
                if (!StringUtil.isBlank(output)) {
                    MessageBox.warn(output);
                } else {
                    // 删除临时文件
                    this.client().openSftp().rm(tempFile);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    @FXML
    private void onDataKeyPressed(KeyEvent event) {
        if (KeyboardUtil.isCtrlS(event)) {
            this.save();
        }
    }

    @Override
    public void onTabInit(RichTab tab) {
        super.onTabInit(tab);
        this.root.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1) {
                this.refresh();
            }
        });
    }

    public ShellSSHClient client() {
        return this.parent().getClient();
    }

    @Override
    public ShellDockerTabController parent() {
        return (ShellDockerTabController) super.parent();
    }
}
