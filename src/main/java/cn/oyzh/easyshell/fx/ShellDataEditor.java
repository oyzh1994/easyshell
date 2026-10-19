package cn.oyzh.easyshell.fx;

import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.plus.font.FontManager;
import javafx.scene.text.Font;

/**
 * Shell 数据编辑器
 *
 * @author oyzh
 * @since 2025-03-26
 */
public class ShellDataEditor extends Editor {

    @Override
    public Font getEditorFont() {
        ShellSetting setting = ShellSettingStore.SETTING;
        return FontManager.toFont(setting.editorFontConfig());
    }

}
