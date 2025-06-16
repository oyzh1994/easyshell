package cn.oyzh.easyshell.s3;

import cn.oyzh.common.date.DateHelper;
import cn.oyzh.i18n.I18nHelper;

import java.time.Instant;
import java.util.Date;

/**
 * s3桶
 *
 * @author oyzh
 * @since 2025-06-14
 */
public class ShellS3Bucket {

    /**
     * 名称
     */
    private String name;

    /**
     * 区域
     */
    private String region;

    /**
     * 创建时间
     */
    private String creationDate;

    /**
     * 版本控制
     */
    private boolean versioning;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegion() {
        return this.region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        Date date = new Date(creationDate.toEpochMilli());
        this.creationDate = DateHelper.formatDateTimeSimple(date);
    }

    public boolean isVersioning() {
        return versioning;
    }

    public void setVersioning(boolean versioning) {
        this.versioning = versioning;
    }

    public String getVersioningStatus() {
        return this.versioning ? I18nHelper.enable() : I18nHelper.disable();
    }
}
