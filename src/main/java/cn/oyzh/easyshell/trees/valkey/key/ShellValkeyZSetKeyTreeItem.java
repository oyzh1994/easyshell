package cn.oyzh.easyshell.trees.valkey.key;

import cn.oyzh.common.util.ArrayUtil;
import cn.oyzh.easyshell.event.valkey.ShellValkeyEventUtil;
import cn.oyzh.easyshell.valkey.key.ShellValkeyKey;
import cn.oyzh.easyshell.valkey.key.ShellValkeyZSetValue;

import cn.oyzh.easyshell.util.valkey.ShellValkeyVersionUtil;
import cn.oyzh.fx.plus.information.MessageBox;
import io.valkey.GeoCoordinate;

import java.util.List;
import java.util.Objects;

/**
 * redis zset类型键节点
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellValkeyZSetKeyTreeItem extends ShellValkeyRowKeyTreeItem<ShellValkeyZSetValue.ValkeyZSetRow> {

    @Override
    public ShellValkeyZSetValue.ValkeyZSetRow currentRow() {
        return super.currentRow();
    }

    @Override
    public ShellValkeyZSetValue.ValkeyZSetRow data() {
        return (ShellValkeyZSetValue.ValkeyZSetRow) super.data();
    }

    @Override
    public void data(Object data) {
        if (data instanceof ShellValkeyZSetValue.ValkeyZSetRow row) {
            super.data(row.clone());
        } else {
            super.clearData();
        }
    }

    @Override
    public ShellValkeyZSetValue.ValkeyZSetRow  unsavedValue() {
        return (ShellValkeyZSetValue.ValkeyZSetRow) super.unsavedValue();
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
    public ShellValkeyZSetKeyTreeItem currentRow(ShellValkeyZSetValue.ValkeyZSetRow currentRow) {
        this.currentRow = currentRow;
        this.clearData();
        return this;
    }

    /**
     * 构造zset类型键节点
     *
     * @param value  键对象
     * @param keysItem 键列表节点
     */
    public ShellValkeyZSetKeyTreeItem(ShellValkeyKey value, ShellValkeyKeysTreeItem keysItem) {
        super(value, keysItem);
    }

    /**
     * 反转显示类型
     */
    public void reverseView() {
        this.showType = (byte) (this.isCoordinateView() ? 0 : 1);
        ShellValkeyEventUtil.redisZSetReverseView(this);
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
        return ShellValkeyVersionUtil.isCommandSupported(this.getServerVersion(), "geopos");
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
        ShellValkeyZSetValue.ValkeyZSetRow value = this.data();
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
            MessageBox.exception(ex, this.getWindow());
        }
    }

    @Override
    protected void setKeyValue(Object value) {
        if (value instanceof ShellValkeyZSetValue.ValkeyZSetRow row) {
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
                MessageBox.exception(ex, this.getWindow());
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
            MessageBox.exception(ex, this.getWindow());
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
            MessageBox.exception(ex, this.getWindow());
        }
    }

    @Override
    public ShellValkeyZSetValue.ValkeyZSetRow rawValue() {
        return this.currentRow;
    }

    @Override
    public boolean checkRowExists() {
        ShellValkeyZSetValue.ValkeyZSetRow row = this.data();
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
        if (o instanceof ShellValkeyZSetValue.ValkeyZSetRow r) {
            String s = r.getValue();
            if (s.length() > DATA_MAX) {
                return true;
            }
            return s.lines().anyMatch(l -> l.length() > LINE_MAX);
        }
        return false;
    }
}
