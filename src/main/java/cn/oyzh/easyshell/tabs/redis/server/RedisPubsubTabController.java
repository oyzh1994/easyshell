package cn.oyzh.easyshell.tabs.redis.server;

import cn.oyzh.easyshell.dto.redis.RedisPubsubItem;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import javafx.fxml.FXML;

import java.util.ArrayList;
import java.util.List;

/**
 * redis订阅发布tab内容组件
 *
 * @author oyzh
 * @since 2023/08/02
 */
public class RedisPubsubTabController extends SubTabController {

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
    private FXTableView<RedisPubsubItem> listTable;

    /**
     * 执行初始化
     *
     * @param client redis客户端
     */
    public void init( ShellRedisClient client) {
        this.client = client;
        this.initPubsub();
    }

    /**
     * 执行订阅
     */
    @FXML
    private void subscribe() {
        RedisPubsubItem pubsubItem = this.listTable.getSelectedItem();
        if (pubsubItem != null) {
            pubsubItem.setClient(this.client);
            ShellEventUtil.redisPubsubOpen(pubsubItem);
        }
    }

    /**
     * 刷新
     */
    @FXML
    private void refresh() {
        this.initPubsub();
    }

    /**
     * 初始化订阅发布
     */
    private void initPubsub() {
        List<String> list = this.client.pubsubChannels("*");
        List<RedisPubsubItem> items = new ArrayList<>(list.size());
        int index = 1;
        for (String l : list) {
            RedisPubsubItem item = new RedisPubsubItem();
            item.setIndex(index++);
            item.setChannel(l);
            items.add(item);
        }
        this.listTable.getItems().setAll(items);
    }
}
