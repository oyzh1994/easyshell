package cn.oyzh.easyshell.fx.tool;

import cn.oyzh.easyshell.dto.ShellNetworkScanResult;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.tableview.TableViewUtil;

/**
 * 网络扫描结果表
 *
 * @author oyzh
 * @since 2025/05/26
 */
public class ShellNetworkScanResultTableView extends FXTableView<ShellNetworkScanResult> {

    {
        TableViewUtil.copyCellDataOnDoubleClicked(this);
    }
}
