package cn.oyzh.easyshell.tabs;

import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.fx.plus.information.MessageBox;
import javafx.scene.Node;

import java.io.IOException;

/**
 * @author oyzh
 * @since 2025-07-21
 */
public interface ShellSnippetAdapter {

    /**
     * 片段列表
     *
     * @param node 当前节点
     */
    default void snippetList(Node node) {
        ShellViewFactory.snippetList(node, h -> {
            if (h != null) {
                try {
                    this.runSnippet(h.getContent() + "\r");
                } catch (Exception ex) {
                    MessageBox.exception(ex);
                }
            }
        });
    }

    /**
     * 运行片段
     *
     * @param content 内容
     */
    void runSnippet(String content) throws IOException;
}
