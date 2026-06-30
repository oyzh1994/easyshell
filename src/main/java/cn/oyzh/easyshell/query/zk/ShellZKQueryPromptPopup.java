package cn.oyzh.easyshell.query.zk;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.query.ShellQueryEditor;
import cn.oyzh.easyshell.query.ShellQueryPromptListView;
import cn.oyzh.easyshell.query.ShellQueryPromptPopup;
import cn.oyzh.easyshell.query.ShellQueryTokenAnalyzer;
import cn.oyzh.easyshell.util.zk.ShellZKNodeUtil;
import cn.oyzh.easyshell.zk.ShellZKClient;
import javafx.scene.input.KeyEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * zk查询弹框
 *
 * @author oyzh
 * @since 2025/01/21
 */
public class ShellZKQueryPromptPopup extends ShellQueryPromptPopup<ShellZKQueryPromptItem,ShellZKQueryToken> {

    private ShellZKClient zkClient;

    @Override
    protected ShellQueryPromptListView<ShellZKQueryPromptItem> initListView() {
        return new ShellZKQueryPromptListView();
    }

    @Override
    protected synchronized boolean initPrompts(ShellZKQueryToken token) {
        // 初始化提示的子节点列表
        if (token.isPossibilityNode()) {
            try {
                String path = token.getPath();
                if (path == null) {
                    ShellZKQueryUtil.setNodes(null);
                } else {
                    List<String> children = this.zkClient.getChildren(path);
                    if (CollectionUtil.isNotEmpty(children)) {
                        List<String> list = new ArrayList<>();
                        for (String s : children) {
                            list.add(ShellZKNodeUtil.concatPath(path, s));
                        }
                        ShellZKQueryUtil.setNodes(list);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            ShellZKQueryUtil.setNodes(null);
        }
        // 提示词列表
        List<ShellZKQueryPromptItem> items = this.tokenAnalyzer().initPrompts(token, 0.5f);
        // 初始化数据
        this.listView().init(items);
        // 判断是否为空
        return !this.listView().isItemEmpty();
    }

    @Override
    protected ShellQueryTokenAnalyzer<ShellZKQueryPromptItem,ShellZKQueryToken> tokenAnalyzer() {
        return ShellZKQueryTokenAnalyzer.INSTANCE;
    }

    @Override
    public void prompt(ShellQueryEditor editor, KeyEvent event) {
        if (editor instanceof ShellZKQueryEditor queryEditor) {
            this.zkClient = queryEditor.getClient();
        }
        super.prompt(editor, event);
    }

    @Override
    protected boolean tokenAvailable() {
        return this.token != null && (this.token.isPossibilityParam() || this.token.isNotEmpty());
    }
}
