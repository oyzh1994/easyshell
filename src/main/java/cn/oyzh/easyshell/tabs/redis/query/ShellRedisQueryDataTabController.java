package cn.oyzh.easyshell.tabs.redis.query;

import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.property.KeyValueProperty;
import javafx.fxml.FXML;
import redis.clients.jedis.util.KeyValue;
import redis.clients.jedis.util.SafeEncoder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * redis查询数据tab内容组件
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellRedisQueryDataTabController extends RichTabController {

    /**
     * 数据表格组件
     */
    @FXML
    private FXTableView<KeyValueProperty<Integer, Object>> dataTable;

    /**
     * 初始化查询数据
     *
     * @param list 数据集合
     */
    public void init(Collection<?> list) {
        List<KeyValueProperty<Integer, Object>> data = new ArrayList<>();
        int index = 1;
        for (Object o : list) {
            this.parseObject(o, index++, data);
        }
        this.dataTable.setItem(data);
    }

    /**
     * 初始化查询数据
     *
     * @param o 数据对象
     */
    public void init(Object o) {
        List<KeyValueProperty<Integer, Object>> data = new ArrayList<>();
        this.parseObject(o, 1, data);
        this.dataTable.setItem(data);
    }

    /**
     * 解析数据对象为表格数据
     *
     * @param o     数据对象
     * @param index 序号
     * @param data  表格数据集合
     */
    private void parseObject(Object o, int index, List<KeyValueProperty<Integer, Object>> data) {
        switch (o) {
            case byte[] bytes -> data.add(KeyValueProperty.of(index, SafeEncoder.encode(bytes)));
            case Collection<?> c -> data.add(KeyValueProperty.of(index, SafeEncoder.encodeObject(o)));
            case KeyValue<?, ?> c -> data.add(KeyValueProperty.of(index, SafeEncoder.encodeObject(o)));
            case null -> data.add(KeyValueProperty.of(index, ""));
            default -> data.add(KeyValueProperty.of(index, o.toString()));
        }
    }
}