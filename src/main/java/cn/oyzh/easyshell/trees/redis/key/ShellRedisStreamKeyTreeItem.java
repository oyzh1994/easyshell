package cn.oyzh.easyshell.trees.redis.key;

import cn.oyzh.easyshell.redis.key.ShellRedisKey;
import cn.oyzh.easyshell.redis.key.ShellRedisStreamValue;
import cn.oyzh.easyshell.trees.redis.database.ShellRedisDatabaseTreeItem;
import cn.oyzh.fx.plus.information.MessageBox;
import redis.clients.jedis.resps.StreamEntry;

import java.util.List;

/**
 * redis stream类型键节点
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellRedisStreamKeyTreeItem extends ShellRedisRowKeyTreeItem<ShellRedisStreamValue.RedisStreamRow> {

    /**
     * 构造stream类型键节点
     *
     * @param value  键对象
     * @param dbItem 数据库节点
     */
    public ShellRedisStreamKeyTreeItem(ShellRedisKey value, ShellRedisDatabaseTreeItem dbItem) {
        super(value, dbItem);
    }

    @Override
    public boolean deleteRow() {
        try {
            long count = this.client().xdel(this.dbIndex(), this.key(), this.currentRow.getStreamId());
            if (count > 0) {
                this.rows().remove(this.currentRow);
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
        return false;
    }

    @Override
    public void refreshKeyValue() {
        List<StreamEntry> value = this.client().xrange(this.dbIndex(), this.key());
        this.value.valueOfStream(value);
        this.clearData();
    }

    @Override
    public ShellRedisStreamValue.RedisStreamRow rawValue() {
        return this.currentRow;
    }
}
