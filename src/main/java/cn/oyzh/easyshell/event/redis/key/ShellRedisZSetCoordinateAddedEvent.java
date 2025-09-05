package cn.oyzh.easyshell.event.redis.key;

import cn.oyzh.easyshell.trees.redis.RedisZSetKeyTreeItem;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2023/11/20
 */
public class ShellRedisZSetCoordinateAddedEvent extends Event<RedisZSetKeyTreeItem> implements EventFormatter {

    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getMember() {
        return member;
    }

    public void setMember(String member) {
        this.member = member;
    }

    private Double longitude;

    private Double latitude;

    private String member;

    @Override
    public String eventFormat() {
        return String.format(
                "[%s] " + I18nHelper.key() + ":%s(db%s) " + I18nHelper.coordinatesAdded() + ":%s " + I18nHelper.longitude() + ":%s " + I18nHelper.latitude() + ":%s",
                this.data().infoName(), this.key, this.data().dbIndex(), this.member, this.longitude, this.latitude
        );
    }
}
