package cn.oyzh.easyshell.fx.tunneling;

import cn.oyzh.easyshell.domain.ShellTunnelingConfig;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.tableview.TableViewUtil;

/**
 * 隧道配置表
 *
 * @author oyzh
 * @since 2025-04-16
 */
public class ShellTunnelingTableView extends FXTableView<ShellTunnelingConfig> {

    {
        TableViewUtil.copyCellDataOnDoubleClicked(this);
    }

}
