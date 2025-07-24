package cn.oyzh.easyshell.fx.smb;

import cn.oyzh.fx.plus.controls.text.field.FXTextField;
import cn.oyzh.fx.plus.menu.FXContextMenu;
import cn.oyzh.i18n.I18nHelper;

/**
 * smb用户输入框，可搜索
 *
 * @author oyzh
 * @since 2025-07-24
 */
public class ShellSMBUserTextField extends FXTextField {

    {
        // 覆盖默认菜单
        this.setContextMenu(FXContextMenu.EMPTY);
        this.setTipText(I18nHelper.pleaseInputUserName());
    }

    /**
     * 当前皮肤
     *
     * @return 皮肤
     */
    public ShellSMBUserTextFieldSkin skin() {
        ShellSMBUserTextFieldSkin skin = (ShellSMBUserTextFieldSkin) this.getSkin();
        if (skin == null) {
            skin = this.createDefaultSkin();
            this.setSkin(skin);
        }
        return skin;
    }

    @Override
    protected ShellSMBUserTextFieldSkin createDefaultSkin() {
        return new ShellSMBUserTextFieldSkin(this);
    }

    @Override
    public boolean validate() {
        if (this.isRequire() && this.skin().getSelectedItem() == null) {
            return false;
        }
        return super.validate();
    }
}
