package cn.oyzh.easyshell.trees.redis;

import cn.oyzh.easyshell.redis.key.ShellRedisKey;
import cn.oyzh.easyshell.redis.key.ShellRedisStreamValue;
import cn.oyzh.fx.plus.information.MessageBox;
import redis.clients.jedis.resps.StreamEntry;

import java.util.List;

/**
 * @author oyzh
 * @since 2023/1/30
 */
public class RedisStreamKeyTreeItem extends RedisRowKeyTreeItem<ShellRedisStreamValue.RedisStreamRow> {

    public RedisStreamKeyTreeItem(ShellRedisKey value, RedisDatabaseTreeItem dbItem) {
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
