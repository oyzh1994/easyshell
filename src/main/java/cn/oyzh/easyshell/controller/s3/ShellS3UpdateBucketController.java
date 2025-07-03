package cn.oyzh.easyshell.controller.s3;

import cn.oyzh.easyshell.fx.s3.ShellS3RetentionModeComboBox;
import cn.oyzh.easyshell.fx.s3.ShellS3RetentionValidityTypeComboBox;
import cn.oyzh.easyshell.s3.ShellS3Bucket;
import cn.oyzh.easyshell.s3.ShellS3Client;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * s3桶修改业务
 *
 * @author oyzh
 * @since 2025/06/16
 */
@StageAttribute(
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "s3/shellS3UpdateBucket.fxml"
)
public class ShellS3UpdateBucketController extends StageController {

    /**
     * 密钥名称
     */
    @FXML
    private ReadOnlyTextField name;

    /**
     * 版本控制
     */
    @FXML
    private FXToggleSwitch versioning;

    /**
     * 对象锁定
     */
    @FXML
    private FXToggleSwitch objectLock;

    /**
     * 区域
     */
    @FXML
    private ReadOnlyTextField region;

    /**
     * 保留
     */
    @FXML
    private FXToggleSwitch retention;

    /**
     * 保留期
     */
    @FXML
    private NumberTextField retentionValidity;

    /**
     * 保留模式
     */
    @FXML
    private ShellS3RetentionModeComboBox retentionMode;

    /**
     * 保留时间
     */
    @FXML
    private ShellS3RetentionValidityTypeComboBox retentionValidityType;

    /**
     * 客户端
     */
    private ShellS3Client client;

    /**
     * 桶对象
     */
    private ShellS3Bucket bucket;

    /**
     * 修改桶
     */
    @FXML
    private void update() {
        try {
            boolean versioning = this.versioning.isSelected();
            boolean objectLock = this.objectLock.isSelected();

            int retentionMode = this.retentionMode.getSelectedIndex();
            int retentionValidity = this.retentionValidity.getIntValue();
            int retentionValidityType = this.retentionValidityType.getSelectedIndex();

            ShellS3Bucket bucket = new ShellS3Bucket();
            bucket.setVersioning(versioning);
            bucket.setObjectLock(objectLock);
            bucket.setName(this.bucket.getName());
            // 保留
            bucket.setRetentionMode(retentionMode);
            bucket.setRetention(this.bucket.isRetention());
            bucket.setRetentionValidity(retentionValidity);
            bucket.setRetentionValidityType(retentionValidityType);
            // 修改桶
            this.client.updateBucket(bucket);
            this.bucket.copy(bucket);
            this.setProp("bucket", this.bucket);
            MessageBox.okToast(I18nHelper.operationSuccess());
            this.closeWindow();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.objectLock.selectedChanged((observable, oldValue, newValue) -> {
            if (newValue) {
                this.versioning.setSelected(true);
                this.versioning.disable();
            } else {
                this.versioning.enable();
            }
        });
        this.retention.selectedChanged((observable, oldValue, newValue) -> {
            if (newValue) {
                this.objectLock.setSelected(true);
                this.objectLock.disable();
                NodeGroupUtil.enable(this.stage, "retention");
            } else {
                this.objectLock.enable();
                NodeGroupUtil.disable(this.stage, "retention");
            }
        });
        this.retentionValidityType.selectedIndexChanged((observable, oldValue, newValue) -> {
            if (newValue.intValue() == 1) {
                this.retentionValidity.setValue(1);
            }
        });
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.client = this.getProp("client");
        this.bucket = this.getProp("bucket");
        this.name.setText(this.bucket.getName());
        this.region.setText(this.bucket.getRegion());
        this.versioning.setSelected(this.bucket.isVersioning());
        this.objectLock.setSelected(this.bucket.isObjectLock());
        this.objectLock.disable();
        this.retention.setSelected(this.bucket.isRetention());
        this.retention.disable();
        this.retentionMode.select(this.bucket.getRetentionMode());
        this.retentionValidity.setValue(this.bucket.getRetentionValidity());
        this.retentionValidityType.select(this.bucket.getRetentionValidityType());
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.updateBucket();
    }

}
