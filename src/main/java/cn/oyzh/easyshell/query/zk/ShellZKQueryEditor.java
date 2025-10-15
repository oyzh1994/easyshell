package cn.oyzh.easyshell.query.zk;

import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.plus.font.FontManager;
import javafx.scene.text.Font;

import java.util.Set;

/**
 * zk查询编辑器
 *
 * @author oyzh
 * @since 2025/01/21
 */
public class ShellZKQueryEditor extends Editor {

    /**
     * zk客户端
     */
    private ShellZKClient client;

    public ShellZKClient getClient() {
        return client;
    }

    public void setClient(ShellZKClient client) {
        this.client = client;
    }

    /**
     * 提示词组件
     */
    private final ShellZKQueryPromptPopup promptPopup = new ShellZKQueryPromptPopup();

    {
//        this.showLineNum();
        this.setOnMouseReleased(e -> this.promptPopup.hide());
//        this.addTextChangeListener((observable, oldValue, newValue) -> this.initTextStyle());
        this.promptPopup.setOnItemSelected(item -> this.promptPopup.autoComplete(this, item));
        this.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                this.promptPopup.hide();
            }
        });
        this.setOnKeyReleased(event -> this.promptPopup.prompt(this, event));
    }

//    @Override
//    public void initNode() {
//        this.initFont();
//        this.initContentPrompts();
//    }

//     @Override
//     protected Font initFont() {
// //        // 禁用字体管理
// //        super.disableFont();
//         // 初始化字体
//         ZKSetting setting = ZKSettingStore.SETTING;
// //        this.setFontSize(setting.getQueryFontSize());
// //        this.setFontFamily(setting.getQueryFontFamily());
// //        this.setFontWeight2(setting.getQueryFontWeight());
//         return FontManager.toFont(setting.queryFontConfig());
//     }

    @Override
    protected Font getEditorFont() {
        if (super.getEditorFont() == null) {
            ShellSetting setting = ShellSettingStore.SETTING;
            Font font = FontManager.toFont(setting.queryFontConfig());
            super.setEditorFont(font);
        }
        return super.getEditorFont();
    }

    //@Override
    // public void changeFont(Font font) {
    //    // 初始化字体
    //    ZKSetting setting = ZKSettingStore.SETTING;
    //    Font font1 = FontManager.toFont(setting.queryFontConfig());
    //    super.changeFont(font1);
    //}

    @Override
    public Set<String> getPrompts() {
        if (super.getPrompts() == null) {
            // 设置内容提示符
            Set<String> set = ShellZKQueryUtil.getKeywords();
            set.addAll(ShellZKQueryUtil.getParams());
            this.setPrompts(set);
        }
        return super.getPrompts();
    }
}
