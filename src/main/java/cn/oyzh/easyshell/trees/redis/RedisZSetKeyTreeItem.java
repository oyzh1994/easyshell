package cn.oyzh.easyshell.trees.redis;

import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.easyshell.event.redis.RedisEventUtil;
import cn.oyzh.easyshell.redis.key.RedisKey;
import cn.oyzh.easyshell.redis.key.RedisZSetValue;
import cn.oyzh.easyshell.util.RedisVersionUtil;
import cn.oyzh.fx.plus.information.MessageBox;
import redis.clients.jedis.GeoCoordinate;

import java.util.List;
import java.util.Objects;

/**
 * @author oyzh
 * @since 2023/06/30
 */
public class RedisZSetKeyTreeItem extends RedisRowKeyTreeItem<RedisZSetValue.RedisZSetRow> {

    @Override
    public RedisZSetValue.RedisZSetRow currentRow() {
        return super.currentRow();
    }

    @Override
    public RedisZSetValue.RedisZSetRow data() {
        return (RedisZSetValue.RedisZSetRow) super.data();
    }

    @Override
    public void data(Object data) {
        if (data instanceof RedisZSetValue.RedisZSetRow row) {
            super.data(row.clone());
        } else {
            super.clearData();
        }
    }

    @Override
    public RedisZSetValue.RedisZSetRow  unsavedValue() {
        return (RedisZSetValue.RedisZSetRow) super.unsavedValue();
    }

    /**
     * 获取分数
     *
     * @return 分数
     */
    public Double score() {
        if (this.data() == null) {
            return null;
        }
        return this.data().getScore();
    }

    /**
     * 设置分数
     *
     * @param score 分数
     */
    public void score(Double score) {
        if (this.data() != null) {
            this.data().setScore(score);
        }
    }

    /**
     * 获取纬度
     *
     * @return 纬度
     */
    public Double latitude() {
        if (this.data() == null) {
            return null;
        }
        return this.data().getLatitude();
    }

    /**
     * 设置纬度
     *
     * @param latitude 纬度
     */
    public void latitude(Double latitude) {
        if (this.data() != null) {
            this.data().setLatitude(latitude);
        }
    }

    /**
     * 获取经度
     *
     * @return 经度
     */
    public Double longitude() {
        if (this.data() == null) {
            return null;
        }
        return this.data().getLongitude();
    }

    /**
     * 设置经度
     *
     * @param longitude 经度
     */
    public void longitude(Double longitude) {
        if (this.data() != null) {
            this.data().setLongitude(longitude);
        }
    }

    /**
     * 显示类型 0: 有序集合 1: 地理坐标
     */
    private byte showType;

    @Override
    public RedisZSetKeyTreeItem currentRow(RedisZSetValue.RedisZSetRow currentRow) {
        this.currentRow = currentRow;
        this.clearData();
        return this;
    }

    public RedisZSetKeyTreeItem(RedisKey value, RedisKeyTreeView treeView) {
        super(value, treeView);
    }

    /**
     * 反转显示类型
     */
    public void reverseView() {
        this.showType = (byte) (this.isCoordinateView() ? 0 : 1);
        RedisEventUtil.zSetReverseView(this);
    }

    /**
     * 是否地理坐标视图
     *
     * @return 结果
     */
    public boolean isCoordinateView() {
        return this.showType == 1;
    }

    /**
     * 是否支持地理坐标
     *
     * @return 结果
     */
    public boolean isSupportCoordinate() {
        return RedisVersionUtil.isCommandSupported(this.getServerVersion(), "geopos");
    }

    /**
     * 获取服务端版本号
     *
     * @return 服务端版本号
     */
    public String getServerVersion() {
        return this.client().getServerVersion();
    }

    @Override
    public void saveKeyValue() {
        RedisZSetValue.RedisZSetRow value = this.data();
        try {
            this.setKeyValue(value);
            this.currentRow.setValue(value.getValue());
            if (this.isCoordinateView()) {
                this.currentRow.setLatitude(value.getLatitude());
                this.currentRow.setLongitude(value.getLongitude());
            } else {
                this.currentRow.setScore(value.getScore());
            }
            // 清除旧数据
            this.clearData();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    protected void setKeyValue(Object value) {
        if (value instanceof RedisZSetValue.RedisZSetRow row) {
            try {
                String rowValue = row.getValue();
                double rowScore = row.getScore();
                double rowLatitude = row.getLatitude();
                double rowLongitude = row.getLongitude();
                // 删除旧成员
                if (!Objects.equals(rowValue, this.currentRow.getValue())) {
                    this.client().zrem(this.dbIndex(), this.key(), this.currentRow.getValue());
                }
                // 新增坐标
                if (this.isCoordinateView()) {
                    this.client().geoadd(this.dbIndex(), this.key(), rowLongitude, rowLatitude, rowValue);
                } else {// 新增成员
                    this.client().zadd(this.dbIndex(), this.key(), rowScore, rowValue);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        }
    }

    @Override
    public boolean deleteRow() {
        try {
            long count = this.client().zrem(this.dbIndex(), this.key(), this.currentRow.getValue());
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
        try {
            List<String> value = this.client().zrange(this.dbIndex(), this.key());
            if (this.isCoordinateView()) {
                List<GeoCoordinate> coordinates = this.client().geopos(this.dbIndex(), this.key(), ArrayUtil.toArray(value, String.class));
                this.value.valueOfCoordinates(value, coordinates);
            } else {
                List<Double> scores = this.client().zmscore_ext(this.dbIndex(), this.key(), ArrayUtil.toArray(value, String.class));
                this.value.valueOfZSet(value, scores);
            }
            this.clearData();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    public RedisZSetValue.RedisZSetRow rawValue() {
        return this.currentRow;
    }

    @Override
    public boolean checkRowExists() {
        RedisZSetValue.RedisZSetRow row = this.data();
        String rowValue = row.getValue();
        if (this.isDataUnsaved() && !Objects.equals(this.currentRow.getValue(), rowValue)) {
            Long zrank = this.client().zrank(this.dbIndex(), this.key(), rowValue);
            return zrank != null;
        }
        return false;
    }

    @Override
    public boolean isDataTooBig() {
        Object o = this.data();
        if (o instanceof RedisZSetValue.RedisZSetRow r) {
            String s = r.getValue();
            if (s.length() > DATA_MAX) {
                return true;
            }
            return s.lines().anyMatch(l -> l.length() > LINE_MAX);
        }
        return false;
    }
}
