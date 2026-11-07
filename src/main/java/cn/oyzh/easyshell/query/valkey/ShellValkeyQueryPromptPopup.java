package cn.oyzh.easyshell.query.valkey;

import cn.oyzh.easyshell.valkey.ShellValkeyClient;
import cn.oyzh.easyshell.valkey.ShellValkeyKeyUtil;
import cn.oyzh.fx.db.query.DBQueryEditor;
import cn.oyzh.fx.db.query.DBQueryPromptPopup;
import cn.oyzh.fx.db.query.DBQueryTokenAnalyzer;
import cn.oyzh.fx.db.query.ui.DBQueryPromptListView;
import javafx.scene.input.KeyEvent;

import java.util.List;

/**
 * redis查询弹框
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellValkeyQueryPromptPopup extends DBQueryPromptPopup<ShellValkeyQueryPromptItem, ShellValkeyQueryToken> {

    /**
     * db索引
     */
    private Integer dbIndex;

    /**
     * redis客户端
     */
    private ShellValkeyClient redisClient;

    @Override
    protected DBQueryPromptListView<ShellValkeyQueryPromptItem> initListView() {
        return new ShellValkeyQueryPromptListView();
    }

    @Override
    protected boolean initPrompts(ShellValkeyQueryToken token) {
        // 初始化提示的键列表
        if (token.isPossibilityKey()) {
            try {
                List<String> keys = ShellValkeyKeyUtil.scanKeys(this.dbIndex, this.redisClient, "*", 30);
                ShellValkeyQueryUtil.setKeys(keys);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            ShellValkeyQueryUtil.setKeys(null);
        }
        // 提示词列表
        List<ShellValkeyQueryPromptItem> items = this.tokenAnalyzer().initPrompts(token, 0.5f);
        // 初始化数据
        this.listView().init(items);
        // 判断是否为空
        return !this.listView().isItemEmpty();
    }

    @Override
    protected DBQueryTokenAnalyzer<ShellValkeyQueryPromptItem, ShellValkeyQueryToken> tokenAnalyzer() {
        return ShellValkeyQueryTokenAnalyzer.INSTANCE;
    }

    @Override
    public void prompt(DBQueryEditor editor, KeyEvent event) {
        if (editor instanceof ShellValkeyQueryEditor queryEditor) {
            this.dbIndex = queryEditor.getDbIndex();
            this.redisClient = queryEditor.getClient();
        }
        super.prompt(editor, event);
    }

    @Override
    protected boolean tokenAvailable() {
        return this.token != null && (this.token.isPossibilityParam() || this.token.isNotEmpty());
    }
}
