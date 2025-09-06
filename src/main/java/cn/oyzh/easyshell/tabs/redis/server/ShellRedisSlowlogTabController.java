package cn.oyzh.easyshell.tabs.redis.server;

import cn.oyzh.easyshell.dto.redis.ShellRedisSlowlogItem;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import javafx.fxml.FXML;
import redis.clients.jedis.resps.Slowlog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * redis慢查日志tab内容组件
 *
 * @author oyzh
 * @since 2023/08/01
 */
public class ShellRedisSlowlogTabController extends SubTabController {

    /**
     * redis客户端
     */
    private ShellRedisClient client;

    public ShellRedisClient getClient() {
        return client;
    }

    /**
     * 表格组件
     */
    @FXML
    private FXTableView<ShellRedisSlowlogItem> listTable;

    /**
     * 执行初始化
     *
     * @param client redis客户端
     */
    public void init( ShellRedisClient client) {
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
        List<ShellRedisSlowlogItem> items = new ArrayList<>(list.size());
        for (Slowlog slowlog : list) {
            ShellRedisSlowlogItem item = ShellRedisSlowlogItem.from(slowlog);
            items.add(item);
        }
        Collections.reverse(items);
        this.listTable.setItem(items);
    }
}
