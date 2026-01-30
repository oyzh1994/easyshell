package cn.oyzh.easyshell.controller.s3;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.s3.ShellS3RetentionModeComboBox;
import cn.oyzh.easyshell.fx.s3.ShellS3RetentionValidityTypeComboBox;
import cn.oyzh.easyshell.s3.ShellS3Bucket;
import cn.oyzh.easyshell.s3.ShellS3Client;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.validator.ValidatorUtil;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * s3桶新增业务
 *
 * @author oyzh
 * @since 2025/06/16
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "s3/shellS3AddBucket.fxml"
)
public class ShellS3AddBucketController extends StageController {

    /**
     * 桶名称
     */
    @FXML
    private ClearableTextField name;

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
     * 添加桶
     */
    @FXML
    private void add() {
        String name = this.name.getTextTrim();
        // 名称检查
        if (StringUtil.isBlank(name)) {
            ValidatorUtil.validFail(this.name);
            return;
        }
        try {
            ShellS3Bucket bucket = new ShellS3Bucket();
            boolean versioning = this.versioning.isSelected();
            boolean objectLock = this.objectLock.isSelected();
            boolean retention = this.retention.isSelected();
            int retentionMode = this.retentionMode.getSelectedIndex();
            int retentionValidity = this.retentionValidity.getIntValue();
            int retentionValidityType = this.retentionValidityType.getSelectedIndex();
            bucket.setName(name);
            bucket.setVersioning(versioning);
            bucket.setObjectLock(objectLock);
            bucket.setRegion(this.client.region().id());
            // 保留
            bucket.setRetention(retention);
            bucket.setRetentionMode(retentionMode);
            bucket.setRetentionValidity(retentionValidity);
            bucket.setRetentionValidityType(retentionValidityType);
            // 创建桶
            this.client.createBucket(bucket);
            this.setProp("bucket", bucket);
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
        this.region.setText(this.client.region().id());
        this.stage.switchOnTab();
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.addBucket();
    }

}
