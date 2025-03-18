package cn.oyzh.easyshell.fx;

import cn.oyzh.easyshell.exec.DiskInfo;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.tableview.TableViewUtil;

/**
 * @author oyzh
 * @since 2025-03-18
 */
public class DiskInfoTableView extends FXTableView<DiskInfo> {

    {
        TableViewUtil.copyCellDataOnDoubleClicked(this);
    }
}
