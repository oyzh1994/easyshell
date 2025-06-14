package cn.oyzh.easyshell.s3;

import cn.oyzh.easyshell.file.ShellFile;
import cn.oyzh.easyshell.file.ShellFileUtil;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CommonPrefix;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.time.Instant;

public class ShellS3File implements ShellFile {

    private String fileName;

    private String parentPath;

    private String modifyTime;

    private Bucket bucket;

    private S3Object s3Object;

    private CommonPrefix prefix;

    private String bucketName;

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

    public ShellS3File() {

    }

    @Override
    public boolean isFile() {
        return this.s3Object != null;
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
        return 0;
    }

    @Override
    public void setFileSize(long fileSize) {


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
        return this.bucket != null || this.prefix != null;
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
        return this.modifyTime;
    }

    @Override
    public void setModifyTime(String modifyTime) {
        this.modifyTime = modifyTime;
    }

    @Override
    public void copy(ShellFile t1) {

    }

    public void setMTime(Instant mTime) {
        this.setModifyTime(mTime.toString());
    }

    public void setFilePath(String filePath) {
        int index = filePath.lastIndexOf("/");
        this.parentPath = filePath.substring(0, index);
        this.fileName = filePath.substring(index + 1);
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
}
