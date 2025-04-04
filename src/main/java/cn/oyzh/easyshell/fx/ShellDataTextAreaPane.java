package cn.oyzh.easyshell.fx;

import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.fx.plus.font.FontManager;
import cn.oyzh.fx.rich.richtextfx.data.RichDataTextAreaPane;
import javafx.scene.text.Font;

/**
 * @author oyzh
 * @since 2025-03-26
 */
public class ShellDataTextAreaPane extends RichDataTextAreaPane {

    @Override
    protected Font initFont() {
//        // 禁用字体管理
//        super.disableFont();
        ShellSetting setting = ShellSettingStore.SETTING;
        return FontManager.toFont(setting.editorFontConfig());
    }
}
