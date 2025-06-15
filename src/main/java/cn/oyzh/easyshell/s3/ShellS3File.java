package cn.oyzh.easyshell.s3;

import cn.oyzh.common.date.DateHelper;
import cn.oyzh.easyshell.file.ShellFile;
import cn.oyzh.easyshell.file.ShellFileUtil;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CommonPrefix;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.time.Instant;
import java.util.Date;

/**
 * s3文件
 *
 * @author oyzh
 * @since 2025-06-14
 */
public class ShellS3File implements ShellFile {

    private String fileName;

    private String parentPath;

    private Bucket bucket;

    private S3Object s3Object;

    private CommonPrefix prefix;

    private String bucketName;

    private Instant lastModified;

    private long fileSize;

    public ShellS3File(S3Object s3Object, String bucket) {
        this.bucketName = bucket;
        this.s3Object = s3Object;
        String fPath = ShellFileUtil.concat("/" + bucket, s3Object.key());
        ShellS3Path path = ShellS3Path.of(fPath);
        this.fileName = path.fileName();
        this.parentPath = path.parentPath();
    }

    public ShellS3File(CommonPrefix prefix, String bucket) {
        this.bucketName = bucket;
        this.prefix = prefix;
        String fPath = ShellFileUtil.concat("/", prefix.prefix());
        if (fPath.endsWith("/")) {
            fPath = fPath.substring(0, fPath.length() - 1);
        }
        this.fileName = ShellFileUtil.name(fPath);
        this.parentPath = ShellFileUtil.parent(fPath);
    }

    public ShellS3File(Bucket bucket) {
        this.bucket = bucket;
        this.fileName = bucket.name();
        this.parentPath = "/";
    }

    public ShellS3File(ShellS3Path s3Path, Instant lastModified, Long fileSize) {
        this.lastModified = lastModified;
        this.fileName = s3Path.fileName();
        this.bucketName = s3Path.bucketName();
        this.parentPath = s3Path.parentPath();
        this.fileSize = fileSize;
    }

    @Override
    public boolean isFile() {
        return this.s3Object != null && !this.s3Object.key().endsWith("/");
    }

    @Override
    public boolean isLink() {
        return false;
    }

    @Override
    public String getOwner() {
        return "-";
    }

    @Override
    public String getGroup() {
        return "-";
    }

    @Override
    public long getFileSize() {
        if (this.s3Object != null) {
            return this.s3Object.size();
        }
        return this.fileSize;
    }

    @Override
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public String getFileName() {
        return this.fileName;
    }

    @Override
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public boolean isDirectory() {
        if (this.bucket != null || this.prefix != null) {
            return true;
        }
        return this.s3Object != null && this.s3Object.key().endsWith("/");
    }

    @Override
    public String getParentPath() {
        return this.parentPath;
    }

    @Override
    public String getPermissions() {
        return "-";
    }

    @Override
    public void setPermissions(String permissions) {

    }

    @Override
    public String getModifyTime() {
        Instant instant;
        if (this.s3Object != null) {
            instant = this.s3Object.lastModified();
        } else {
            instant = this.lastModified;
        }
        if (instant != null) {
            Date date = new Date(instant.toEpochMilli());
            return DateHelper.formatDateTime(date);
        }
        return "-";
    }

    @Override
    public void setModifyTime(String modifyTime) {
    }

    @Override
    public void copy(ShellFile t1) {

    }

    @Override
    public String getFilePath() {
        if (this.bucket != null) {
            return ShellFile.super.getFilePath();
        }
        String fPath = ShellFileUtil.concat("/", this.bucketName);
        fPath = ShellFileUtil.concat(fPath, this.getParentPath());
        fPath = ShellFileUtil.concat(fPath, this.getFileName());
        return fPath;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }
}
