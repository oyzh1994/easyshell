package cn.oyzh.easyshell.fx.ssh;

import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.plus.font.FontUtil;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * @author oyzh
 * @since 2025-03-26
 */
public class ShellGpuEditor extends Editor {

    @Override
    protected Font getEditorFont() {
        ShellSetting setting = ShellSettingStore.SETTING;
        FontWeight weight = FontWeight.findByWeight(setting.getFontWeight());
        return FontUtil.newFont("Monaco", weight, setting.getFontSize());
    }

    @Override
    public void initNode() {
        super.initNode();
        this.setEditable(false);
        this.setLineNumbersEnabled(false);
    }
}
