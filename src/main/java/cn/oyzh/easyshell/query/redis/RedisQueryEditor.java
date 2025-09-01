package cn.oyzh.easyshell.query.redis;

import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.redis.RedisClient;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.fx.editor.tm4javafx.Editor;
import cn.oyzh.fx.plus.font.FontManager;
import javafx.scene.text.Font;

import java.util.Set;

/**
 * @author oyzh
 * @since 2025/01/21
 */
public class RedisQueryEditor extends Editor {

    /**
     * db索引
     */
    private int dbIndex;

    /**
     * redis客户端
     */
    private RedisClient client;

    public RedisClient getClient() {
        return client;
    }

    public void setClient(RedisClient client) {
        this.client = client;
    }

    public int getDbIndex() {
        return dbIndex;
    }

    public void setDbIndex(int dbIndex) {
        this.dbIndex = dbIndex;
    }

    /**
     * 提示词组件
     */
    private final RedisQueryPromptPopup promptPopup = new RedisQueryPromptPopup();

    {
        this.setOnMouseReleased(e -> this.promptPopup.hide());
        this.promptPopup.setOnItemSelected(item -> this.promptPopup.autoComplete(this, item));
        this.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                this.promptPopup.hide();
            }
        });
        this.setOnKeyReleased(event -> this.promptPopup.prompt(this, event));
    }

    // @Override
    // public void initNode() {
    //     this.initFont();
    //     this.initPrompts();
    // }

//     @Override
//     protected Font initFont() {
// //        // 禁用字体管理
// //        super.disableFont();
//         // 初始化字体
//         RedisSetting setting = RedisSettingStore.SETTING;
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
    //
    //@Override
    //public void changeFont(Font font) {
    //    RedisSetting setting = RedisSettingStore.SETTING;
    //    Font font1 = FontManager.toFont(setting.queryFontConfig());
    //    super.changeFont(font1);
    //}

    @Override
    public Set<String> getPrompts() {
        if (super.getPrompts() == null) {
            // 设置内容提示符
            Set<String> set = RedisQueryUtil.getKeywords();
            set.addAll(RedisQueryUtil.getParams());
            this.setPrompts(set);
        }
        return super.getPrompts();
    }
}
