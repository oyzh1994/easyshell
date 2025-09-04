package cn.oyzh.easyshell.query.zk;

import cn.oyzh.easyshell.domain.zk.ZKSetting;
import cn.oyzh.easyshell.store.zk.ZKSettingStore;
import cn.oyzh.easyshell.zk.ZKClient;
import cn.oyzh.fx.editor.tm4javafx.Editor;
import cn.oyzh.fx.plus.font.FontManager;
import javafx.scene.text.Font;

import java.util.Set;

/**
 * @author oyzh
 * @since 2025/01/21
 */
public class ZKQueryEditor extends Editor {

    /**
     * zk客户端
     */
    private ZKClient client;

    public ZKClient getClient() {
        return client;
    }

    public void setClient(ZKClient client) {
        this.client = client;
    }

    /**
     * 提示词组件
     */
    private final ZKQueryPromptPopup promptPopup = new ZKQueryPromptPopup();

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
            ZKSetting setting = ZKSettingStore.SETTING;
            Font font = FontManager.toFont(setting.queryFontConfig());
            super.setEditorFont(font);
        }
        return super.getEditorFont();
    }

    //@Override
    //public void changeFont(Font font) {
    //    // 初始化字体
    //    ZKSetting setting = ZKSettingStore.SETTING;
    //    Font font1 = FontManager.toFont(setting.queryFontConfig());
    //    super.changeFont(font1);
    //}

    @Override
    public Set<String> getPrompts() {
        if (super.getPrompts() == null) {
            // 设置内容提示符
            Set<String> set = ZKQueryUtil.getKeywords();
            set.addAll(ZKQueryUtil.getParams());
            this.setPrompts(set);
        }
        return super.getPrompts();
    }
}
