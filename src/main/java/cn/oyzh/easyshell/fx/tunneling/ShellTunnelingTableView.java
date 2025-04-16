package cn.oyzh.easyshell.fx.tunneling;

import cn.oyzh.easyshell.domain.ShellTunnelingConfig;
import cn.oyzh.easyshell.store.ShellTunnelingConfigStore;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.tableview.TableViewUtil;

import java.util.List;

/**
 * @author oyzh
 * @since 2025-04-16
 */
public class ShellTunnelingTableView extends FXTableView<ShellTunnelingConfig> {

    {
        TableViewUtil.copyCellDataOnDoubleClicked(this);
    }

    /**
     * 隧道配置存储器
     */
    private final ShellTunnelingConfigStore configStore = ShellTunnelingConfigStore.INSTANCE;

    public void init(String iid) {
        List<ShellTunnelingConfig> configs = this.configStore.listByIid(iid);
        this.setItem(configs);
    }

}
