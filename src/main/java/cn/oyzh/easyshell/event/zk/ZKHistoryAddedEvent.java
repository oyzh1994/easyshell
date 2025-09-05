package cn.oyzh.easyshell.event.zk;

import cn.oyzh.easyshell.domain.zk.ShellZKDataHistory;
import cn.oyzh.event.Event;
import javafx.scene.control.TreeItem;

/**
 * @author oyzh
 * @since 2024/4/23
 */
public class ZKHistoryAddedEvent extends Event<ShellZKDataHistory> {

    private TreeItem<?> item;

    public TreeItem<?> getItem() {
        return item;
    }

    public void setItem(TreeItem<?> item) {
        this.item = item;
    }
}
