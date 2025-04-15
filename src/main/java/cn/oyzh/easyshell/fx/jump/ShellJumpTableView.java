package cn.oyzh.easyshell.fx.jump;

import cn.oyzh.easyshell.domain.ShellJumpConfig;
import cn.oyzh.easyshell.store.ShellJumpConfigStore;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.tableview.TableViewUtil;

import java.util.List;

/**
 * @author oyzh
 * @since 2025-04-03
 */
public class ShellJumpTableView extends FXTableView<ShellJumpConfig> {

    {
        TableViewUtil.copyCellDataOnDoubleClicked(this);
    }

    /**
     * 跳板配置存储器
     */
    private final ShellJumpConfigStore configStore = ShellJumpConfigStore.INSTANCE;

    public void init(String iid) {
        List<ShellJumpConfig> configs = this.configStore.listByIid(iid);
        this.setItem(configs);
    }

    /**
     * 更新排序
     */
    public void updateOrder() {
        for (int i = 0; i < this.getItemSize(); i++) {
            ShellJumpConfig config = (ShellJumpConfig) this.getItem(i);
            config.setOrder(i);
        }
    }

}
