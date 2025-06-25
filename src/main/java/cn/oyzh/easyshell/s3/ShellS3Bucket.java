package cn.oyzh.easyshell.s3;

import cn.oyzh.common.date.DateHelper;
import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.i18n.I18nHelper;
import software.amazon.awssdk.services.s3.model.DefaultRetention;

import java.time.Instant;
import java.util.Date;

import static software.amazon.awssdk.services.s3.model.ObjectLockRetentionMode.COMPLIANCE;

/**
 * s3桶
 *
 * @author oyzh
 * @since 2025-06-14
 */
public class ShellS3Bucket implements ObjectCopier<ShellS3Bucket> {

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

    /**
     * 对象锁定
     */
    private boolean objectLock;

    /**
     * 是否开启保留
     */
    private boolean retention;

    /**
     * 保留模式
     * 0 compliance
     * 1 governance
     */
    private int retentionMode;

    /**
     * 保留有效期
     */
    private int retentionValidity;

    /**
     * 保留有效期类型
     * 0 day
     * 1 year
     */
    private int retentionValidityType;

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

    public boolean isObjectLock() {
        return objectLock;
    }

    public void setObjectLock(boolean objectLock) {
        this.objectLock = objectLock;
    }

    public String getObjectLockStatus() {
        return this.objectLock ? I18nHelper.enable() : I18nHelper.disable();
    }

    public boolean isRetention() {
        return retention;
    }

    public void setRetention(boolean retention) {
        this.retention = retention;
    }

    public String getRetentionStatus() {
        return this.retention ? I18nHelper.enable() : I18nHelper.disable();
    }

    public int getRetentionMode() {
        return retentionMode;
    }

    public void setRetentionMode(int retentionMode) {
        this.retentionMode = retentionMode;
    }

    public int getRetentionValidity() {
        return retentionValidity;
    }

    public void setRetentionValidity(int retentionValidity) {
        this.retentionValidity = retentionValidity;
    }

    public int getRetentionValidityType() {
        return retentionValidityType;
    }

    public void setRetentionValidityType(int retentionValidityType) {
        this.retentionValidityType = retentionValidityType;
    }

    public void setRetention(DefaultRetention retention) {
        if (retention == null) {
            this.retention = false;
            return;
        }
        this.retention = true;
        this.retentionMode = retention.mode() == COMPLIANCE ? 0 : 1;
        if (retention.days() == null) {
            this.retentionValidity = retention.years();
            this.retentionValidityType = 1;
        } else {
            this.retentionValidity = retention.days();
            this.retentionValidityType = 0;
        }
    }

    @Override
    public void copy(ShellS3Bucket bucket) {
        if (bucket != null) {
            if (bucket.name != null) {
                this.name = bucket.name;
            }
            if (bucket.region != null) {
                this.region = bucket.region;
            }
            if (bucket.creationDate != null) {
                this.creationDate = bucket.creationDate;
            }
            this.versioning = bucket.versioning;
            this.objectLock = bucket.objectLock;
            this.retention = bucket.retention;
            this.retentionMode = bucket.retentionMode;
            this.retentionValidity = bucket.retentionValidity;
            this.retentionValidityType = bucket.retentionValidityType;
        }
    }
}
