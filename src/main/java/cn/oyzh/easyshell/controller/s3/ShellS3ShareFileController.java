package cn.oyzh.easyshell.controller.s3;

import cn.oyzh.easyshell.fx.s3.ShellS3EffectiveTimeCombobox;
import cn.oyzh.easyshell.s3.ShellS3Client;
import cn.oyzh.easyshell.s3.ShellS3File;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.util.ClipboardUtil;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.time.Duration;

/**
 * s3文件分享业务
 *
 * @author oyzh
 * @since 2025/07/03
 */
@StageAttribute(
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "s3/shellS3ShareFile.fxml"
)
public class ShellS3ShareFileController extends StageController {

    /**
     * 文件
     */
    @FXML
    private ReadOnlyTextField file;

    /**
     * 分享地址
     */
    @FXML
    private ReadOnlyTextField shareUrl;

    /**
     * 持续时间
     */
    @FXML
    private NumberTextField duration;

    /**
     * 持续单位
     */
    @FXML
    private ShellS3EffectiveTimeCombobox durationType;

    /**
     * 文件
     */
    private ShellS3File s3File;

    /**
     * 客户端
     */
    private ShellS3Client client;

    /**
     * 生成
     */
    @FXML
    private void generate() {
        try {
            // 创建桶
            Duration duration=null;
            if (this.durationType.isDays()) {
                duration = Duration.ofDays(this.duration.getIntValue());
            } else if (this.durationType.isMinutes()) {
                duration = Duration.ofMinutes(this.duration.getIntValue());
            } else if (this.durationType.isSeconds()) {
                duration = Duration.ofSeconds(this.duration.getIntValue());
            } else if (this.durationType.isHours()) {
                duration = Duration.ofHours(this.duration.getIntValue());
            }
            String url = this.client.generatePresignedUrl(this.s3File.getBucketName(), this.s3File.getFileKey(), duration);
            this.shareUrl.setText(url);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 复制地址
     */
    @FXML
    private void copyUrl() {
        try {
            ClipboardUtil.copy(this.shareUrl.getText());
            MessageBox.okToast(I18nHelper.operationSuccess());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.s3File = this.getProp("s3File");
        this.client = this.getProp("client");
        this.file.setText(this.s3File.getFileKey());
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.shareFile();
    }

}
