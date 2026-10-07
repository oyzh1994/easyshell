package cn.oyzh.easyshell.redis.key;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.util.redis.ShellRedisCacheUtil;
import redis.clients.jedis.GeoCoordinate;

import java.util.ArrayList;
import java.util.List;

/**
 * redis的zset值
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellRedisZSetValue implements ShellRedisKeyValue<List<ShellRedisZSetValue.RedisZSetRow>> {

    /**
     * 值
     */
    private List<RedisZSetRow> value;

    /**
     * 获取未保存的行
     *
     * @return 未保存的行
     */
    public RedisZSetRow getUnSavedRow() {
        return unSavedRow;
    }

    /**
     * 设置未保存的行
     *
     * @param unSavedRow 未保存的行
     */
    public void setUnSavedRow(RedisZSetRow unSavedRow) {
        this.unSavedRow = unSavedRow;
    }

    @Override
    public List<RedisZSetRow> getValue() {
        return value;
    }

    @Override
    public void setValue(List<RedisZSetRow> value) {
        this.value = value;
    }

    /**
     * 未保存的行
     */
    private RedisZSetRow unSavedRow;

    /**
     * 构造方法
     *
     * @param value 值
     */
    public ShellRedisZSetValue(List<RedisZSetRow> value) {
        this.value = value;
    }

    /**
     * 创建zset值
     *
     * @param members 成员集合
     * @param scores  分数集合
     * @return zset值
     */
    public static ShellRedisZSetValue valueOf(List<String> members, List<Double> scores) {
        List<RedisZSetRow> rows = new ArrayList<>(12);
        if (members != null) {
            int index = 0;
            for (String member : members) {
                rows.add(new RedisZSetRow(member, scores.get(index++)));
            }
        }
        return new ShellRedisZSetValue(rows);
    }

    /**
     * 以地理坐标创建zset值
     *
     * @param members     成员集合
     * @param coordinates 坐标集合
     * @return zset值
     */
    public static ShellRedisZSetValue valueOfCoordinates(List<String> members, List<GeoCoordinate> coordinates) {
        List<RedisZSetRow> rows = new ArrayList<>(12);
        if (members != null) {
            int index = 0;
            for (String member : members) {
                GeoCoordinate coordinate = coordinates.get(index++);
                rows.add(new RedisZSetRow(member, coordinate.getLatitude(), coordinate.getLongitude()));
            }
        }
        return new ShellRedisZSetValue(rows);
    }

    @Override
    public boolean hasValue() {
        return CollectionUtil.isNotEmpty(this.value);
    }

    @Override
    public Object getUnSavedValue() {
        return this.unSavedRow;
    }

    @Override
    public void clearUnSavedValue() {
        if (this.unSavedRow != null) {
            this.unSavedRow.setValue(null);
            this.unSavedRow = null;
        }
    }

    @Override
    public boolean hasUnSavedValue() {
        return this.unSavedRow != null && this.unSavedRow.getValue() != null;
    }

    @Override
    public void setUnSavedValue(Object unSavedValue) {
        if (unSavedValue instanceof RedisZSetRow) {
            this.unSavedRow = (RedisZSetRow) unSavedValue;
        }
    }

    /**
     * redis的zset行
     */
    public static class RedisZSetRow implements ShellRedisKeyRow {

        /**
         * 分数
         */
        private double score;

        /**
         * 纬度
         */
        private double latitude;

        /**
         * 经度
         */
        private double longitude;

        /**
         * 获取分数
         *
         * @return 分数
         */
        public double getScore() {
            return score;
        }

        /**
         * 设置分数
         *
         * @param score 分数
         */
        public void setScore(double score) {
            this.score = score;
        }

        /**
         * 获取纬度
         *
         * @return 纬度
         */
        public double getLatitude() {
            return latitude;
        }

        /**
         * 设置纬度
         *
         * @param latitude 纬度
         */
        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        /**
         * 获取经度
         *
         * @return 经度
         */
        public double getLongitude() {
            return longitude;
        }

        /**
         * 设置经度
         *
         * @param longitude 经度
         */
        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        /**
         * 构造方法
         */
        public RedisZSetRow() {
        }

        /**
         * 构造方法
         *
         * @param value 值
         * @param score 分数
         */
        public RedisZSetRow(String value, double score) {
            this.setValue(value);
            this.score = score;
        }

        /**
         * 构造方法
         *
         * @param value     值
         * @param latitude  纬度
         * @param longitude 经度
         */
        public RedisZSetRow(String value, double latitude, double longitude) {
            this.setValue(value);
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        public void setValue(String value) {
            ShellRedisCacheUtil.cacheValue(this.hashCode(), value, "value");
        }

        @Override
        public String getValue() {
            return (String) ShellRedisCacheUtil.loadValue(this.hashCode(), "value");
        }

        @Override
        public RedisZSetRow clone() {
            RedisZSetRow row = new RedisZSetRow();
            row.score = this.score;
            row.latitude = this.latitude;
            row.longitude = this.longitude;
            row.setValue(this.getValue());
            return row;
        }
    }
}
