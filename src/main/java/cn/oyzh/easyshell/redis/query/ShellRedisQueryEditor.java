package cn.oyzh.easyshell.redis.query;

import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.fx.db.query.DBQueryEditor;
import cn.oyzh.fx.plus.font.FontManager;
import javafx.scene.text.Font;

import java.util.Set;

/**
 * redis查询编辑器
 *
 * @author oyzh
 * @since 2025/01/21
 */
public class ShellRedisQueryEditor extends DBQueryEditor {

    /**
     * db索引
     */
    private int dbIndex;

    /**
     * redis客户端
     */
    private ShellRedisClient client;

    public ShellRedisClient getClient() {
        return client;
    }

    public void setClient(ShellRedisClient client) {
        this.client = client;
    }

    public int getDbIndex() {
        return dbIndex;
    }

    public void setDbIndex(int dbIndex) {
        this.dbIndex = dbIndex;
    }

    /**
     * 提示组件
     */
    private ShellRedisQueryPromptPopup promptPopup;

    @Override
    protected ShellRedisQueryPromptPopup promptPopup() {
        if (this.promptPopup == null) {
            this.promptPopup = new ShellRedisQueryPromptPopup();
        }
        return this.promptPopup;
    }

    @Override
    public Set<String> getPrompts() {
        if (super.getPrompts() == null) {
            // 设置内容提示符
            Set<String> set = ShellRedisQueryUtil.getKeywords();
            set.addAll(ShellRedisQueryUtil.getParams());
            this.setPrompts(set);
        }
        return super.getPrompts();
    }

    @Override
    public void initNode() {
        this.promptPopup().setOnItemSelected(item -> this.promptPopup().autoComplete(this, item));
        super.initNode();
    }

    @Override
    protected Font getEditorFont() {
        ShellSetting setting = ShellSettingStore.SETTING;
        return FontManager.toFont(setting.editorFontConfig());
    }
}
