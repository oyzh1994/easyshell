package cn.oyzh.easyshell.fx;

import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.fx.editor.EditorPane;
import cn.oyzh.fx.plus.font.FontManager;
import javafx.scene.text.Font;

/**
 * @author oyzh
 * @since 2025-03-26
 */
public class ShellDataTextAreaPane extends EditorPane {

//     @Override
//     protected Font initFont() {
// //        // 禁用字体管理
// //        super.disableFont();
//         ShellSetting setting = ShellSettingStore.SETTING;
//         return FontManager.toFont(setting.editorFontConfig());
//     }

    @Override
    public void changeFont(Font font) {
        ShellSetting setting = ShellSettingStore.SETTING;
        Font font1 = FontManager.toFont(setting.editorFontConfig());
        super.changeFont(font1);
    }
}
