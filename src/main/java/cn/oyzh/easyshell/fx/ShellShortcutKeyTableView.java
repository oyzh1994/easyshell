package cn.oyzh.easyshell.fx;

import cn.oyzh.common.system.OSUtil;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.property.KeyValueProperty;
import cn.oyzh.i18n.I18nHelper;

import java.util.ArrayList;
import java.util.List;

/***
 * 快捷键列表
 * @author oyzh
 * @since 2024/04/13
 */
public class ShellShortcutKeyTableView extends FXTableView<KeyValueProperty<String, Object>> {

    {
        this.init();
    }

    public void init() {
        List<KeyValueProperty<String, Object>> data = new ArrayList<>();
        if (OSUtil.isMacOS()) {
            data.add(KeyValueProperty.of("Meta + C (⌘ + C)", ShellI18nHelper.termTip1()));
        } else {
            data.add(KeyValueProperty.of("Ctrl + Shift + C (^ + ⇧ + C)", ShellI18nHelper.termTip1()));
        }
        if (OSUtil.isMacOS()) {
            data.add(KeyValueProperty.of("Meta + V (⌘ + V)", ShellI18nHelper.termTip2()));
        } else {
            data.add(KeyValueProperty.of("Ctrl + Shift + V (^ + ⇧ + V)", ShellI18nHelper.termTip2()));
        }
        if (OSUtil.isMacOS()) {
            data.add(KeyValueProperty.of("Meta + W (⌘ + W)", I18nHelper.closeTab()));
        } else {
            data.add(KeyValueProperty.of("Ctrl + W (^ + W)", I18nHelper.closeTab()));
        }
        if (OSUtil.isMacOS()) {
            data.add(KeyValueProperty.of("Meta + F (⌘ + F)", ShellI18nHelper.termTip3()));
        } else {
            data.add(KeyValueProperty.of("Ctrl + F (^ + F)", ShellI18nHelper.termTip3()));
        }
        if (OSUtil.isMacOS()) {
            data.add(KeyValueProperty.of("Meta + L (⌘ + L)", ShellI18nHelper.termTip4()));
        } else {
            data.add(KeyValueProperty.of("Ctrl + L (^ + L)", ShellI18nHelper.termTip4()));
        }
        data.add(KeyValueProperty.of("Shift + PageUp (⇧ + ⇞)", ShellI18nHelper.termTip5()));
        data.add(KeyValueProperty.of("Shift + PageDown (⇧ + ⇟)", ShellI18nHelper.termTip6()));
        if (OSUtil.isMacOS()) {
            data.add(KeyValueProperty.of("Meta + Up (⌘ + ↑)", ShellI18nHelper.termTip7()));
        } else {
            data.add(KeyValueProperty.of("Ctrl + Up (^ + ↑)", ShellI18nHelper.termTip7()));
        }
        if (OSUtil.isMacOS()) {
            data.add(KeyValueProperty.of("Meta + Down (⌘ + ↓)", ShellI18nHelper.termTip8()));
        } else {
            data.add(KeyValueProperty.of("Ctrl + Down (^ + ↓)", ShellI18nHelper.termTip8()));
        }
        this.setItem(data);
    }
}
