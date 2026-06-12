package cn.oyzh.easyshell.query.zk;

import cn.oyzh.easyshell.query.ShellQueryEditor;
import cn.oyzh.easyshell.zk.ShellZKClient;

import java.util.Set;

/**
 * zk查询编辑器
 *
 * @author oyzh
 * @since 2025/01/21
 */
public class ShellZKQueryEditor extends ShellQueryEditor {

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
     * 提示组件
     */
    private ShellZKQueryPromptPopup promptPopup;

    @Override
    protected ShellZKQueryPromptPopup promptPopup() {
        if (this.promptPopup == null) {
            this.promptPopup = new ShellZKQueryPromptPopup();
        }
        return this.promptPopup;
    }

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

    @Override
    public void initNode() {
        this.promptPopup().setOnItemSelected(item -> this.promptPopup().autoComplete(this, item));
        super.initNode();
    }

}
