package cn.oyzh.easyshell.query.redis;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.query.ShellQueryEditor;
import cn.oyzh.easyshell.query.ShellQueryPromptListView;
import cn.oyzh.easyshell.query.ShellQueryPromptPopup;
import cn.oyzh.easyshell.query.ShellQueryTokenAnalyzer;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.easyshell.redis.ShellRedisKeyUtil;
import javafx.scene.input.KeyEvent;

import java.util.List;

/**
 * redis查询弹框
 *
 * @author oyzh
 * @since 2025/01/21
 */
public class ShellRedisQueryPromptPopup extends ShellQueryPromptPopup<ShellRedisQueryPromptItem, ShellRedisQueryToken> {

    private Integer dbIndex;

    private ShellRedisClient redisClient;

    @Override
    protected ShellQueryPromptListView<ShellRedisQueryPromptItem> initListView() {
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
    protected ShellQueryTokenAnalyzer<ShellRedisQueryPromptItem, ShellRedisQueryToken> tokenAnalyzer() {
        return ShellRedisQueryTokenAnalyzer.INSTANCE;
    }

    @Override
    public void prompt(ShellQueryEditor editor, KeyEvent event) {
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
