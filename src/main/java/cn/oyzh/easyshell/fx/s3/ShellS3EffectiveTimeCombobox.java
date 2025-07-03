package cn.oyzh.easyshell.fx.s3;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.i18n.I18nHelper;
import software.amazon.awssdk.regions.Region;

/**
 * @author oyzh
 * @since 2025-07-03
 */
public class ShellS3EffectiveTimeCombobox extends FXComboBox<Region> {

    {
        this.addItem(I18nHelper.days());
        this.addItem(I18nHelper.hours());
        this.addItem(I18nHelper.minutes());
        this.addItem(I18nHelper.seconds());
        //this.addItem(I18nHelper.months());
        //this.addItem(I18nHelper.years());
        this.selectFirst();
    }

    public boolean isDays() {
        return this.getSelectedIndex() == 0;
    }

    public boolean isHours() {
        return this.getSelectedIndex() == 1;
    }

    public boolean isMinutes() {
        return this.getSelectedIndex() == 2;
    }

    public boolean isSeconds() {
        return this.getSelectedIndex() == 3;
    }

    //public boolean isMonths() {
    //    return this.getSelectedIndex() == 4;
    //}
    //
    //public boolean isYears() {
    //    return this.getSelectedIndex() == 5;
    //}

}
