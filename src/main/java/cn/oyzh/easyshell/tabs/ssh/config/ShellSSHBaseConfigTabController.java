package cn.oyzh.easyshell.tabs.ssh.config;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.ssh.exec.ShellSSHExec;
import cn.oyzh.easyshell.ssh.sftp.ShellSFTPClient;
import cn.oyzh.easyshell.tabs.ssh.ShellSSHConfigTabController;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
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
 * 配置基本类
 *
 * @author oyzh
 * @since 2025/03/18
 */
public abstract class ShellSSHBaseConfigTabController extends SubTabController {

    /**
     * 保存
     */
    @FXML
    private SVGGlyph save;

    /**
     * 刷新
     */
    @FXML
    private SVGGlyph refresh;

    /**
     * 应用
     */
    @FXML
    private SVGGlyph apply;

    /**
     * 数据
     */
    @FXML
    private RichDataTextAreaPane data;

    /**
     * 数据过滤
     */
    @FXML
    private ClearableTextField dataFilter;

    /**
     * 刷新
     */
    @FXML
    public void refresh() {
        StageManager.showMask(() -> {
            String output = this.fileContent();
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
        String filePath = this.filePath();
        if (StringUtil.isBlank(filePath)) {
            return;
        }
        String text = this.data.getText();
        StageManager.showMask(() -> {
            ShellSSHExec exec = this.client().sshExec();
            ShellSFTPClient sftpClient = this.sftpClient();
            try {
                // 创建临时文件
                String tempFile;
                if (filePath.startsWith("~")) {
                    tempFile = this.client().getUserHome() + filePath.replace("~/", "") + ".temp";
                } else {
                    tempFile = filePath + ".temp";
                }
                if (!sftpClient.exist(tempFile)) {
                    sftpClient.touch(tempFile);
                }
                // 上传内容
                sftpClient.put(new ByteArrayInputStream(text.getBytes()), tempFile, null);
                // 把临时文件内容copy到真实文件
                String output = exec.cat_file(tempFile, filePath);
                if (!StringUtil.isBlank(output)) {
                    MessageBox.warn(output);
                } else if (sftpClient.exist(tempFile)) { // 删除临时文件
                    sftpClient.delete(tempFile);
                }
            } catch (Exception ex) {
                // 忽略No such file错误
                if (!ExceptionUtil.hasMessage(ex, "No such file")) {
                    ex.printStackTrace();
                    MessageBox.exception(ex);
                }
            }
        });
    }

    /**
     * 应用
     */
    @FXML
    private void apply() {
        String filePath = this.filePath();
        if (StringUtil.isBlank(filePath)) {
            return;
        }
        ShellSSHExec exec = this.client().sshExec();
        StageManager.showMask(() -> {
            String output = exec.source(this.filePath());
            if (!StringUtil.isBlank(output)) {
                MessageBox.warn(output);
            }
        });
    }

    @Override
    public void onTabInit(RichTab tab) {
        super.onTabInit(tab);
        this.contentTab().selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1) {
                this.refresh();
            }
        });
        // 快捷键
        this.contentTab().getContent().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (KeyboardUtil.save_keyCombination.match(event)) {
                if (this.save != null) {
                    this.save();
                    event.consume();
                }
            } else if (KeyboardUtil.refresh_keyCombination.match(event)) {
                if (this.refresh != null) {

                    this.refresh();
                    event.consume();
                }
            } else if (KeyboardUtil.apply_keyCombination.match(event)) {
                if (this.apply != null) {
                    this.apply();
                    event.consume();
                }
            } else if (KeyboardUtil.search_keyCombination.match(event)) {
                if (this.dataFilter != null) {
                    this.dataFilter.requestFocus();
                    event.consume();
                }
            }
        });
        // 快捷键提示
        if (this.save != null) {
            this.save.setTipKeyCombination(KeyboardUtil.save_keyCombination);
        }
        if (this.refresh != null) {
            this.refresh.setTipKeyCombination(KeyboardUtil.refresh_keyCombination);
        }
        if (this.apply != null) {
            this.apply.setTipKeyCombination(KeyboardUtil.apply_keyCombination);
        }
        if (this.dataFilter != null) {
            this.dataFilter.setTipKeyCombination(KeyboardUtil.search_keyCombination);
            // 监听内容，设置高亮
            this.dataFilter.addTextChangeListener((observableValue, s, t1) -> {
                this.data.setHighlightText(t1);
            });
        }
    }

    public ShellSSHClient client() {
        return this.parent().getClient();
    }

    public ShellSFTPClient sftpClient() {
        return this.client().sftpClient();
    }

    @Override
    public ShellSSHConfigTabController parent() {
        return (ShellSSHConfigTabController) super.parent();
    }

    /**
     * 获取内容tab
     *
     * @return 内容tab
     */
    protected abstract FXTab contentTab();

    /**
     * 获取文件路径
     *
     * @return 文件路径
     */
    protected abstract String filePath();

    /**
     * 获取文件内容
     *
     * @return 文件内容
     */
    protected abstract String fileContent();
}
