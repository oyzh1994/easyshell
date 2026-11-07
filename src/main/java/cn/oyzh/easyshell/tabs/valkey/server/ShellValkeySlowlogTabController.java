package cn.oyzh.easyshell.tabs.valkey.server;

import cn.oyzh.easyshell.dto.valkey.ShellValkeySlowlogItem;
import cn.oyzh.easyshell.valkey.ShellValkeyClient;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import javafx.fxml.FXML;
import io.valkey.resps.Slowlog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * redis慢查日志tab内容组件
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellValkeySlowlogTabController extends SubTabController {

    /**
     * redis客户端
     */
    private ShellValkeyClient client;

    /**
     * 获取redis客户端
     *
     * @return redis客户端
     */
    public ShellValkeyClient getClient() {
        return client;
    }

    /**
     * 表格组件
     */
    @FXML
    private FXTableView<ShellValkeySlowlogItem> listTable;

    /**
     * 执行初始化
     *
     * @param client redis客户端
     */
    public void init(ShellValkeyClient client) {
        this.client = client;
        this.initSlowlog();
    }

    /**
     * 刷新
     */
    @FXML
    private void refresh() {
        this.initSlowlog();
    }

    /**
     * 初始化慢查日志
     */
    private void initSlowlog() {
        List<Slowlog> list = this.client.slowlogGet(1024);
        List<ShellValkeySlowlogItem> items = new ArrayList<>(list.size());
        for (Slowlog slowlog : list) {
            ShellValkeySlowlogItem item = ShellValkeySlowlogItem.from(slowlog);
            items.add(item);
        }
        Collections.reverse(items);
        this.listTable.setItem(items);
    }

//    @Override
//    public void destroy() {
//        this.listTable.destroy();
//        super.destroy();
//    }
}
