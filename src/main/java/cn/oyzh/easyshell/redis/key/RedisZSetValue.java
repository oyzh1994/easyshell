package cn.oyzh.easyshell.redis.key;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.util.RedisCacheUtil;
import redis.clients.jedis.GeoCoordinate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024-12-02
 */
public class RedisZSetValue implements RedisKeyValue<List<RedisZSetValue.RedisZSetRow>> {

    private List<RedisZSetRow> value;

    public RedisZSetRow getUnSavedRow() {
        return unSavedRow;
    }

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

    private RedisZSetRow unSavedRow;

    public RedisZSetValue(List<RedisZSetRow> value) {
        this.value = value;
    }

    public static RedisZSetValue valueOf(List<String> members, List<Double> scores) {
        List<RedisZSetRow> rows = new ArrayList<>(12);
        if (members != null) {
            int index = 0;
            for (String member : members) {
                rows.add(new RedisZSetRow(member, scores.get(index++)));
            }
        }
        return new RedisZSetValue(rows);
    }

    public static RedisZSetValue valueOfCoordinates(List<String> members, List<GeoCoordinate> coordinates) {
        List<RedisZSetRow> rows = new ArrayList<>(12);
        if (members != null) {
            int index = 0;
            for (String member : members) {
                GeoCoordinate coordinate = coordinates.get(index++);
                rows.add(new RedisZSetRow(member, coordinate.getLatitude(), coordinate.getLongitude()));
            }
        }
        return new RedisZSetValue(rows);
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

    public static class RedisZSetRow implements RedisKeyRow {

        private double score;

        private double latitude;

        private double longitude;

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public RedisZSetRow() {
        }

        public RedisZSetRow(String value, double score) {
            this.setValue(value);
            this.score = score;
        }

        public RedisZSetRow(String value, double latitude, double longitude) {
            this.setValue(value);
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        public void setValue(String value) {
            RedisCacheUtil.cacheValue(this.hashCode(), value, "value");
        }

        @Override
        public String getValue() {
            return (String) RedisCacheUtil.loadValue(this.hashCode(), "value");
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
