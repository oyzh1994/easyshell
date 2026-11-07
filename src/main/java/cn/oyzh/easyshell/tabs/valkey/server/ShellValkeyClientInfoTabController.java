package cn.oyzh.easyshell.tabs.valkey.server;

import cn.oyzh.easyshell.dto.valkey.ShellValkeyClientItem;
import cn.oyzh.easyshell.valkey.ShellValkeyClient;
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
 * @since 2025-09-01
 */
public class ShellValkeyClientInfoTabController extends SubTabController {

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
    private FXTableView<ShellValkeyClientItem> listTable;


    /**
     * 执行初始化
     *
     * @param client redis客户端
     */
    public void init(ShellValkeyClient client) {
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
        List<ShellValkeyClientItem> items = new ArrayList<>(24);
        list.lines().forEach(l -> {
            ShellValkeyClientItem item = ShellValkeyClientItem.from(l);
            item.setIndex(index.getAndIncrement());
            items.add(item);
        });
        this.listTable.getItems().setAll(items);
    }

//    @Override
//    public void destroy() {
//        this.listTable.destroy();
//        super.destroy();
//    }
}
