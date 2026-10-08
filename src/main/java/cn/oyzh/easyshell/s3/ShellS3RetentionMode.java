package cn.oyzh.easyshell.s3;

/**
 * S3 对象锁定保留模式。
 *
 * @author oyzh
 * @since 2026-10-09
 */
public enum ShellS3RetentionMode {

    COMPLIANCE,

    GOVERNANCE;

    public static ShellS3RetentionMode ofIndex(int index) {
        return index == 0 ? COMPLIANCE : GOVERNANCE;
    }

    public int toIndex() {
        return this == COMPLIANCE ? 0 : 1;
    }
}
