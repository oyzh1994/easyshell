package cn.oyzh.easyshell.query.redis;

import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.easyshell.redis.ShellRedisKeyUtil;
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
 * @since 2025/01/21
 */
public class ShellRedisQueryPromptPopup extends DBQueryPromptPopup<ShellRedisQueryPromptItem, ShellRedisQueryToken> {

    private Integer dbIndex;

    private ShellRedisClient redisClient;

    @Override
    protected DBQueryPromptListView<ShellRedisQueryPromptItem> initListView() {
        return new ShellRedisQueryPromptListView();
    }

    @Override
    protected boolean initPrompts(ShellRedisQueryToken token) {
        // 初始化提示的键列表
        if (token.isPossibilityKey()) {
            try {
                List<String> keys = ShellRedisKeyUtil.scanKeys(this.dbIndex, this.redisClient, "*", 30);
                ShellRedisQueryUtil.setKeys(keys);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            ShellRedisQueryUtil.setKeys(null);
        }
        // 提示词列表
        List<ShellRedisQueryPromptItem> items = this.tokenAnalyzer().initPrompts(token, 0.5f);
        // 初始化数据
        this.listView().init(items);
        // 判断是否为空
        return !this.listView().isItemEmpty();
    }

    @Override
    protected DBQueryTokenAnalyzer<ShellRedisQueryPromptItem, ShellRedisQueryToken> tokenAnalyzer() {
        return ShellRedisQueryTokenAnalyzer.INSTANCE;
    }

    @Override
    public void prompt(DBQueryEditor editor, KeyEvent event) {
        if (editor instanceof ShellRedisQueryEditor queryEditor) {
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
