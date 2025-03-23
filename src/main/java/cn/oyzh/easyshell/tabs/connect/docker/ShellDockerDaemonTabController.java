package cn.oyzh.easyshell.tabs.connect.docker;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.exec.ShellExec;
import cn.oyzh.easyshell.sftp.ShellSftp;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.easyshell.tabs.connect.ShellDockerTabController;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.fx.rich.richtextfx.json.RichJsonTextAreaPane;
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
     * cpu图表
     */
    @FXML
    private RichJsonTextAreaPane data;

    @FXML
    private void refresh() {
        StageManager.showMask(() -> {
            try {
                ShellExec exec = this.client().shellExec();
                ShellSftp sftp = this.client().openSftp();
                if (sftp.exist("/etc/docker/daemon.json")) {
                    String output = exec.cat_docker_daemon();
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
                String jsonFile = "/etc/docker/daemon.json";
                if (!sftp.exist(jsonFile)) {
                    sftp.touch(jsonFile);
                }
                // 创建临时文件
                String tempFile = "/etc/docker/daemon.json.temp";
                if (!sftp.exist(tempFile)) {
                    sftp.touch(tempFile);
                }
                // 上传内容
                sftp.put(new ByteArrayInputStream(text.getBytes()), tempFile);
                // 把临时文件内容copy到真实文件
                String output = exec.echo("$(cat " + tempFile + ")", jsonFile);
                if (!StringUtil.isBlank(output)) {
                    MessageBox.warn(output);
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

    public ShellClient client() {
        return this.parent().getClient();
    }

    @Override
    public ShellDockerTabController parent() {
        return (ShellDockerTabController) super.parent();
    }
}
