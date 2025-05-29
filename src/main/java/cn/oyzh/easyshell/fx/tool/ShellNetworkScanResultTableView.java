package cn.oyzh.easyshell.fx.tool;

import cn.oyzh.easyshell.dto.ShellPortScanResult;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.tableview.TableViewUtil;

import java.util.Comparator;

/**
 * 网络扫描结果表
 *
 * @author oyzh
 * @since 2025/05/26
 */
public class ShellNetworkScanResultTableView extends FXTableView<ShellPortScanResult> {

    {
        TableViewUtil.copyCellDataOnDoubleClicked(this);
    }

    /**
     * 执行排序
     */
    public void doSort() {
        this.getItems().sort(Comparator.comparingInt(ShellPortScanResult::getPort));
    }

}
