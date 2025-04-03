package cn.oyzh.easyshell.fx.key;

import cn.oyzh.easyshell.domain.ShellKey;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.tableview.TableViewUtil;

/**
 * @author oyzh
 * @since 2025-03-18
 */
public class ShellKeyTableView extends FXTableView<ShellKey> {

    {
        TableViewUtil.copyCellDataOnDoubleClicked(this);
    }
}
