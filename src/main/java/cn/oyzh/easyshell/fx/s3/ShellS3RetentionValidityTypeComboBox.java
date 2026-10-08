package cn.oyzh.easyshell.fx.s3;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.i18n.I18nHelper;

/**
 * S3对象保留期限类型下拉框
 *
 * @author oyzh
 * @since 2025-06-16
 */
public class ShellS3RetentionValidityTypeComboBox extends FXComboBox<String> {

    @Override
    public void initNode() {
        this.addItem(I18nHelper.days());
        this.addItem(I18nHelper.years());
        super.initNode();
    }

}
