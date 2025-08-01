package cn.oyzh.easyshell.fx;

import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.fx.editor.JsonEditorPane;
import cn.oyzh.fx.plus.font.FontManager;
import javafx.scene.text.Font;

/**
 * @author oyzh
 * @since 2025-03-26
 */
public class ShellJsonTextAreaPane extends JsonEditorPane {

    @Override
    protected Font initFont() {
//        // 禁用字体管理
//        super.disableFont();
        // 初始化字体
        ShellSetting setting = ShellSettingStore.SETTING;
        return FontManager.toFont(setting.editorFontConfig());
    }
}
