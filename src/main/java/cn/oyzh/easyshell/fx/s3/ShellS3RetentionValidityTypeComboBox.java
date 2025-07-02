package cn.oyzh.easyshell.fx.s3;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.i18n.I18nHelper;
import software.amazon.awssdk.regions.Region;

/**
 * @author oyzh
 * @since 2025-06-16
 */
public class ShellS3RetentionValidityTypeComboBox extends FXComboBox<Region> {

    {
        this.addItem(I18nHelper.days());
        this.addItem(I18nHelper.years());
    }

}
