package cn.oyzh.easyshell.fx.docker;

import cn.oyzh.easyshell.ssh.docker.ShellDockerImageHistory;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.tableview.TableViewUtil;

/**
 * @author oyzh
 * @since 2025-03-14
 */
public class ShellDockerImageHistoryTableView extends FXTableView<ShellDockerImageHistory> {

    {
        TableViewUtil.copyCellDataOnDoubleClicked(this);
    }
}
