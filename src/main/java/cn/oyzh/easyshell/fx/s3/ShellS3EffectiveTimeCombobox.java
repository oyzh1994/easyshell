package cn.oyzh.easyshell.fx.s3;

import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.i18n.I18nHelper;

/**
 * S3有效期时间单位下拉框
 *
 * @author oyzh
 * @since 2025-07-03
 */
public class ShellS3EffectiveTimeCombobox extends FXComboBox<String> {

    {
        this.addItem(I18nHelper.days());
        this.addItem(I18nHelper.hours());
        this.addItem(I18nHelper.minutes());
        this.addItem(I18nHelper.seconds());
        //this.addItem(I18nHelper.months());
        //this.addItem(I18nHelper.years());
        this.selectFirst();
    }

    /**
     * 是否为天
     *
     * @return 结果
     */
    public boolean isDays() {
        return this.getSelectedIndex() == 0;
    }

    /**
     * 是否为小时
     *
     * @return 结果
     */
    public boolean isHours() {
        return this.getSelectedIndex() == 1;
    }

    /**
     * 是否为分钟
     *
     * @return 结果
     */
    public boolean isMinutes() {
        return this.getSelectedIndex() == 2;
    }

    /**
     * 是否为秒
     *
     * @return 结果
     */
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
