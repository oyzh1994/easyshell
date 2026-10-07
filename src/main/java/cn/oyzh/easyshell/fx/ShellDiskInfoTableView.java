package cn.oyzh.easyshell.fx;

import cn.oyzh.easyshell.ssh2.exec.ShellSSHDiskInfo;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.tableview.TableViewUtil;

/**
 * 磁盘信息表格视图
 *
 * @author oyzh
 * @since 2025-03-14
 */
public class ShellDiskInfoTableView extends FXTableView<ShellSSHDiskInfo> {

    {
        TableViewUtil.copyCellDataOnDoubleClicked(this);
    }
}
