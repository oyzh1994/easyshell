package cn.oyzh.easyshell.controller.s3;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.s3.ShellS3Bucket;
import cn.oyzh.easyshell.s3.ShellS3Client;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.validator.ValidatorUtil;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * ssh密钥新增业务
 *
 * @author oyzh
 * @since 2025/04/03
 */
@StageAttribute(
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "s3/shellAddS3Bucket.fxml"
)
public class ShellAddS3BucketController extends StageController {

    /**
     * 密钥名称
     */
    @FXML
    private ClearableTextField name;

    /**
     * 区域
     */
    @FXML
    private ReadOnlyTextField region;

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
            bucket.setName(name);
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
