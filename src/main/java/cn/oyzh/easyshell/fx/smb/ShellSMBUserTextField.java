package cn.oyzh.easyshell.fx.smb;

import cn.oyzh.fx.gui.text.field.SelectTextFiled;
import cn.oyzh.fx.plus.menu.FXContextMenu;
import cn.oyzh.i18n.I18nHelper;

import java.util.Arrays;

/**
 * smb用户输入框，可搜索
 *
 * @author oyzh
 * @since 2025-07-24
 */
public class ShellSMBUserTextField extends SelectTextFiled<String> {

    {
        // 覆盖默认菜单
        this.setContextMenu(FXContextMenu.EMPTY);
        this.setTipText(I18nHelper.pleaseInputUserName());
    }

    // @Override
    // public ShellSMBUserTextFieldSkin skin() {
    //     return (ShellSMBUserTextFieldSkin) super.skin();
    // }
    //
    // @Override
    // protected ShellSMBUserTextFieldSkin createDefaultSkin() {
    //     if (this.getSkin() != null) {
    //         return (ShellSMBUserTextFieldSkin) this.getSkin();
    //     }
    //     return new ShellSMBUserTextFieldSkin(this);
    // }

    @Override
    public void initNode() {
        super.initNode();
        this.setItemList(Arrays.asList("Guest", "Anonymous"));
    }
}
