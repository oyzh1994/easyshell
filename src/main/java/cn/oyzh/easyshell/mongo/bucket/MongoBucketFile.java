package cn.oyzh.easyshell.mongo.bucket;

import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.easyshell.file.ShellFile;
import cn.oyzh.easyshell.util.mongo.ShellMongoRecordUtil;
import cn.oyzh.easyshell.util.mongo.ShellMongoUtil;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.BsonValue;
import org.bson.Document;

import java.util.Date;

/**
 *
 * @author oyzh
 * @since 2026-06-01
 */
public class MongoBucketFile implements ShellFile {

    private String dbName;

    private String bucketName;

    private long length;

    private BsonValue id;

    private int chunkSize;

    private String fileName;

    private Date uploadDate;

    private Document metadata;

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public BsonValue getId() {
        return id;
    }

    public void setId(BsonValue id) {
        this.id = id;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    public Document getMetadata() {
        return metadata;
    }

    public void setMetadata(Document metadata) {
        this.metadata = metadata;
    }

    public String getMetadataJson() {
        return metadata == null ? "" : JSONUtil.toJson(this.metadata);
    }

    public String getChunkSizeText() {
        return NumberUtil.formatSize(this.chunkSize, 2);
    }

    public String getAddTime() {
        return ShellMongoUtil.DATE_FORMAT.format(this.uploadDate);
    }

    public String getIdText() {
        return ShellMongoRecordUtil.idValue(this.id).toString();
    }
    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public String getParentPath() {
        return this.dbName + "@" + this.bucketName;
    }

    @Override
    public String getPermissions() {
        return "";
    }

    @Override
    public void setPermissions(String permissions) {

    }

    @Override
    public String getModifyTime() {
        return "";
    }

    @Override
    public void setModifyTime(String modifyTime) {

    }

    @Override
    public boolean isFile() {
        return true;
    }

    @Override
    public boolean isLink() {
        return false;
    }

    @Override
    public String getOwner() {
        return "";
    }

    @Override
    public String getGroup() {
        return "";
    }

    @Override
    public long getFileSize() {
        return this.length;
    }

    public String getLengthText() {
        return NumberUtil.formatSize(this.length, 2);
    }

    @Override
    public void setFileSize(long fileSize) {
        this.length = fileSize;
    }

    public static MongoBucketFile of(GridFSFile file) {
        MongoBucketFile bucketFile = new MongoBucketFile();
        bucketFile.setId(file.getId());
        bucketFile.setLength(file.getLength());
        bucketFile.setFileName(file.getFilename());
        bucketFile.setMetadata(file.getMetadata());
        bucketFile.setChunkSize(file.getChunkSize());
        bucketFile.setUploadDate(file.getUploadDate());
        return bucketFile;
    }

    @Override
    public void copy(ShellFile t1) {
        if (t1 instanceof MongoBucketFile f1) {
            this.length = f1.length;
            this.fileName = f1.fileName;
            this.metadata = f1.metadata;
            this.chunkSize = f1.chunkSize;
        }
    }

    @Override
    public void destroy() {

    }
}
