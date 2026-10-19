package cn.oyzh.easyshell.s3;

/**
 * Bucket 默认保留规则。
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellS3Retention {

    private final ShellS3RetentionMode mode;

    private final Integer days;

    private final Integer years;

    private ShellS3Retention(ShellS3RetentionMode mode, Integer days, Integer years) {
        this.mode = mode;
        this.days = days;
        this.years = years;
    }

    public static ShellS3Retention ofDays(ShellS3RetentionMode mode, int days) {
        return new ShellS3Retention(mode, days, null);
    }

    public static ShellS3Retention ofYears(ShellS3RetentionMode mode, int years) {
        return new ShellS3Retention(mode, null, years);
    }

    public ShellS3RetentionMode getMode() {
        return this.mode;
    }

    public Integer getDays() {
        return this.days;
    }

    public Integer getYears() {
        return this.years;
    }
}
