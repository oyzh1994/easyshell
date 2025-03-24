package cn.oyzh.easyshell.tabs.connect.config;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.exec.ShellExec;
import cn.oyzh.easyshell.sftp.ShellSftp;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.easyshell.tabs.connect.ShellConfigTabController;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.fx.rich.richtextfx.data.RichDataTextAreaPane;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;

import java.io.ByteArrayInputStream;

/**
 * ~/.bash_profile信息
 *
 * @author oyzh
 * @since 2025/03/18
 */
public class ShellUserBashProfileTabController extends SubTabController {

    /**
     * 根节点
     */
    @FXML
    private FXTab userBashProfile;

    /**
     * 数据
     */
    @FXML
    private RichDataTextAreaPane data;

    /**
     * 刷新
     */
    @FXML
    private void refresh() {
        ShellExec exec = this.client().shellExec();
        StageManager.showMask(() -> {
            String output = exec.cat_user_bash_profile();
            this.data.setText(output);
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
                // 创建临时文件
                String tempFile = this.client().getUserBase() + ".bash_profile.temp";
                if (!sftp.exist(tempFile)) {
                    sftp.touch(tempFile);
                }
                // 上传内容
                sftp.put(new ByteArrayInputStream(text.getBytes()), tempFile);
                // 把临时文件内容copy到真实文件
                String output = exec.echo("$(cat " + tempFile + ")", "~/.bash_profile");
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

    /**
     * 应用
     */
    @FXML
    private void apply() {
        ShellExec exec = this.client().shellExec();
        StageManager.showMask(() -> {
            String output = exec.source("~/.bash_profile");
            if (!StringUtil.isBlank(output)) {
                MessageBox.warn(output);
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
        this.userBashProfile.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1) {
                this.refresh();
            }
        });
    }

    public ShellClient client() {
        return this.parent().getClient();
    }

    @Override
    public ShellConfigTabController parent() {
        return (ShellConfigTabController) super.parent();
    }
}
