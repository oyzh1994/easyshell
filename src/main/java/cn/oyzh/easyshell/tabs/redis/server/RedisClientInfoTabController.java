package cn.oyzh.easyshell.tabs.redis.server;

import cn.oyzh.easyshell.dto.redis.RedisClientItem;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import javafx.fxml.FXML;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * redis客户端信息tab内容组件
 *
 * @author oyzh
 * @since 2023/08/01
 */
public class RedisClientInfoTabController extends SubTabController {

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
    private FXTableView<RedisClientItem> listTable;


    /**
     * 执行初始化
     *
     * @param client redis客户端
     */
    public void init( ShellRedisClient client) {
        this.client = client;
        this.initClientList();
    }

    /**
     * 刷新
     */
    @FXML
    private void refresh() {
        this.initClientList();
    }

    /**
     * 初始化客户端信息
     */
    private void initClientList() {
        String list = this.client.clientList();
        AtomicInteger index = new AtomicInteger(1);
        List<RedisClientItem> items = new ArrayList<>(24);
        list.lines().forEach(l -> {
            RedisClientItem item = RedisClientItem.from(l);
            item.setIndex(index.getAndIncrement());
            items.add(item);
        });
        this.listTable.getItems().setAll(items);
    }
}
