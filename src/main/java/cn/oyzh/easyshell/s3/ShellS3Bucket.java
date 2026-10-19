package cn.oyzh.easyshell.s3;

import cn.oyzh.common.date.DateHelper;
import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.i18n.I18nHelper;

import java.time.Instant;
import java.util.Date;

/**
 * s3桶
 *
 * @author oyzh
 * @since 2025-06-15
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

    /**
     * 获取名称
     *
     * @return 名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置名称
     *
     * @param name 名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取区域
     *
     * @return 区域
     */
    public String getRegion() {
        return this.region;
    }

    /**
     * 设置区域
     *
     * @param region 区域
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * 获取创建时间
     *
     * @return 创建时间
     */
    public String getCreationDate() {
        return creationDate;
    }

    /**
     * 设置创建时间
     *
     * @param creationDate 创建时间
     */
    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * 设置创建时间
     *
     * @param creationDate 创建时间
     */
    public void setCreationDate(Instant creationDate) {
        Date date = new Date(creationDate.toEpochMilli());
        this.creationDate = DateHelper.formatDateTimeSimple(date);
    }

    /**
     * 是否开启版本控制
     *
     * @return 是否开启版本控制
     */
    public boolean isVersioning() {
        return versioning;
    }

    /**
     * 设置是否开启版本控制
     *
     * @param versioning 是否开启版本控制
     */
    public void setVersioning(boolean versioning) {
        this.versioning = versioning;
    }

    /**
     * 获取版本控制状态
     *
     * @return 版本控制状态
     */
    public String getVersioningStatus() {
        return this.versioning ? I18nHelper.enable() : I18nHelper.disable();
    }

    /**
     * 是否开启对象锁定
     *
     * @return 是否开启对象锁定
     */
    public boolean isObjectLock() {
        return objectLock;
    }

    /**
     * 设置是否开启对象锁定
     *
     * @param objectLock 是否开启对象锁定
     */
    public void setObjectLock(boolean objectLock) {
        this.objectLock = objectLock;
    }

    /**
     * 获取对象锁定状态
     *
     * @return 对象锁定状态
     */
    public String getObjectLockStatus() {
        return this.objectLock ? I18nHelper.enable() : I18nHelper.disable();
    }

    /**
     * 是否开启保留
     *
     * @return 是否开启保留
     */
    public boolean isRetention() {
        return retention;
    }

    /**
     * 设置是否开启保留
     *
     * @param retention 是否开启保留
     */
    public void setRetention(boolean retention) {
        this.retention = retention;
    }

    /**
     * 获取保留状态
     *
     * @return 保留状态
     */
    public String getRetentionStatus() {
        return this.retention ? I18nHelper.enable() : I18nHelper.disable();
    }

    /**
     * 获取保留模式
     *
     * @return 保留模式
     */
    public int getRetentionMode() {
        return retentionMode;
    }

    /**
     * 设置保留模式
     *
     * @param retentionMode 保留模式
     */
    public void setRetentionMode(int retentionMode) {
        this.retentionMode = retentionMode;
    }

    /**
     * 获取保留有效期
     *
     * @return 保留有效期
     */
    public int getRetentionValidity() {
        return retentionValidity;
    }

    /**
     * 设置保留有效期
     *
     * @param retentionValidity 保留有效期
     */
    public void setRetentionValidity(int retentionValidity) {
        this.retentionValidity = retentionValidity;
    }

    /**
     * 获取保留有效期类型
     *
     * @return 保留有效期类型
     */
    public int getRetentionValidityType() {
        return retentionValidityType;
    }

    /**
     * 设置保留有效期类型
     *
     * @param retentionValidityType 保留有效期类型
     */
    public void setRetentionValidityType(int retentionValidityType) {
        this.retentionValidityType = retentionValidityType;
    }

    /**
     * 设置保留策略
     *
     * @param retention 保留策略
     */
    public void setRetention(ShellS3Retention retention) {
        if (retention == null) {
            this.retention = false;
            return;
        }
        this.retention = true;
        this.retentionMode = retention.getMode().toIndex();
        if (retention.getDays() == null) {
            this.retentionValidity = retention.getYears();
            this.retentionValidityType = 1;
        } else {
            this.retentionValidity = retention.getDays();
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
