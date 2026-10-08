package cn.oyzh.easyshell.fx.s3;

import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.i18n.I18nHelper;

/**
 * s3区域输入框，V2版本
 *
 * @author oyzh
 * @since 2025-06-06
 */
public class ShellS3RegionTextFieldV2 extends ClearableTextField {

    public void select(String region) {
        this.setText(region);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.pleaseSelectRegion());
        super.initNode();
    }
}
