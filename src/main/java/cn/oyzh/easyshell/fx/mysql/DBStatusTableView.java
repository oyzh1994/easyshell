package cn.oyzh.easyshell.fx.mysql;

import cn.oyzh.easyshell.db.DBObjectStatus;
import cn.oyzh.easyshell.mysql.listener.DBStatusListener;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import javafx.collections.ListChangeListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author oyzh
 * @since 2024/07/22
 */
public class DBStatusTableView<S extends DBObjectStatus> extends FXTableView<S> {

    private List<S> deleteItems;

    public void reset() throws Exception {
        this.deleteItems = null;
        this.clearStatus();
    }

    public void clearStatus() throws Exception {
        for (DBObjectStatus object : this.getItems()) {
            object.clearStatus();
        }
    }

    private DBStatusListener statusListener;

    {
        this.itemList().addListener((ListChangeListener<S>) c -> {
            if (this.statusListener == null) {
                return;
            }
            if (!c.next()) {
                return;
            }
            if (c.wasReplaced()) {
                List<S> list = (List<S>) c.getList();
                if (list != null) {
                    for (S status : list) {
                        status.statusProperty().addListener(this.statusListener);
                    }
                }
            } else if (c.wasAdded()) {
                List<S> list = (List<S>) c.getAddedSubList();
                if (list != null) {
                    for (DBObjectStatus status : list) {
                        status.statusProperty().addListener(this.statusListener);
                    }
                }
                this.statusListener.changed(null, null, null);
            } else if (c.wasRemoved()) {
                List<S> list = (List<S>) c.getRemoved();
                if (list != null) {
                    for (S status : list) {
                        if (!status.isCreated()) {
                            if (this.deleteItems == null) {
                                this.deleteItems = new CopyOnWriteArrayList<>();
                            }
                            this.deleteItems.add(status);
                        }
                        status.statusProperty().removeListener(this.statusListener);
                    }
                }
            }
        });
    }

    public List<S> getDeleteItems() {
        return deleteItems;
    }

    public void setDeleteItems(List<S> deleteItems) {
        this.deleteItems = deleteItems;
    }

    public DBStatusListener getStatusListener() {
        return statusListener;
    }

    public void setStatusListener(DBStatusListener statusListener) {
        this.statusListener = statusListener;
    }
}
