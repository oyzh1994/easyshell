package cn.oyzh.easyshell.trees.docker;

import cn.oyzh.easyshell.docker.DockerPort;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.tableview.TableViewUtil;

/**
 * @author oyzh
 * @since 2025-03-14
 */
public class DockerPortTableView extends FXTableView<DockerPort> {

    {
        TableViewUtil.copyCellDataOnDoubleClicked(this);
    }
}
