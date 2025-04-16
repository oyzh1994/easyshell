package cn.oyzh.easyshell.fx;

import cn.oyzh.easyshell.exec.ShellDiskInfo;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.tableview.TableViewUtil;

/**
 * @author oyzh
 * @since 2025-03-18
 */
public class ShellDiskInfoTableView extends FXTableView<ShellDiskInfo> {

    {
        TableViewUtil.copyCellDataOnDoubleClicked(this);
    }
}
