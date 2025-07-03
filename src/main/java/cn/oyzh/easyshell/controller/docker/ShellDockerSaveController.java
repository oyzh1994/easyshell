package cn.oyzh.easyshell.controller.docker;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.easyshell.sftp2.ShellSFTPClient;
import cn.oyzh.easyshell.sftp2.ShellSFTPFile;
import cn.oyzh.easyshell.ssh2.docker.ShellDockerExec;
import cn.oyzh.easyshell.ssh2.docker.ShellDockerImage;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.FXProgressBar;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * docker镜像保存业务
 *
 * @author oyzh
 * @since 2025/07/03
 */
@StageAttribute(
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "docker/shellDockerSave.fxml"
)
public class ShellDockerSaveController extends StageController {

    /**
     * 文件名称
     */
    @FXML
    private ClearableTextField name;

    /**
     * 镜像名称
     */
    @FXML
    private ReadOnlyTextField imageName;

    /**
     * 进度
     */
    @FXML
    private FXProgressBar process;

    /**
     * exec对象
     */
    private ShellDockerExec exec;

    /**
     * 镜像
     */
    private ShellDockerImage image;

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.exec = this.getProp("exec");
        this.image = this.getProp("image");
        String fPath = this.exec.getClient().getUserHome();
        String imgName = this.image.getRepository() + "_" + this.image.getTag();
        imgName = imgName.replace("/", "_");
        fPath = fPath + imgName + ".tar";
        this.imageName.setText(this.image.getImageName());
        this.name.setText(fPath);
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public void onWindowHidden(WindowEvent event) {
        super.onWindowHidden(event);
        ThreadUtil.interrupt(this.execThread);
        ThreadUtil.interrupt(this.processThread);
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.saveImage();
    }

    /**
     * 执行线程
     */
    private Thread execThread;

    /**
     * 进度线程
     */
    private Thread processThread;

    @FXML
    private void run() {
        try {
            // 禁用按钮
            NodeGroupUtil.disable(this.stage, "run");
            // 文件路径
            String filePath = this.name.getTextTrim();
            // sftp客户端
            ShellSFTPClient client = this.exec.getClient().sftpClient();
            // 文件大小
            double fSize = NumberUtil.parseSize(this.image.getSize());
            // 导出执行状态
            AtomicBoolean status = new AtomicBoolean(true);
            // 更新进度
            this.processThread = ThreadUtil.start(() -> {
                try {
                    while (status.get()) {
                        ThreadUtil.sleep(500);
                        try {
                            ShellSFTPFile file = client.fileInfo(filePath);
                            if (file != null) {
                                double p = file.getFileSize() / fSize;
                                // 对进度进行修正
                                p *= 1.02;
                                if (p > 1) {
                                    p = 1;
                                }
                                this.process.setProgress(p);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                } finally {
                    NodeGroupUtil.enable(this.stage, "run");
                }
            });
            // 执行导出
            this.execThread = ThreadUtil.start(() -> {
                try {
                    this.exec.docker_save(filePath, this.image.getImageId());
                } finally {
                    status.set(false);
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }
}
