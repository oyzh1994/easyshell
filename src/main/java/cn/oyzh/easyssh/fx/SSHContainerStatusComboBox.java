package cn.oyzh.easyssh.fx;

import cn.oyzh.common.system.OSUtil;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;
import cn.oyzh.i18n.I18nHelper;

import java.util.List;

/**
 * docker状态选择框
 *
 * @author oyzh
 * @since 25/03/13
 */
public class SSHContainerStatusComboBox extends FXComboBox<String> {

    {
        this.addItem(I18nHelper.running());
        this.addItem(I18nHelper.all());
        this.addItem(I18nHelper.stopped());
    }
}
