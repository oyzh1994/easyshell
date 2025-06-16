package cn.oyzh.easyshell.s3;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.file.ShellFileClient;
import cn.oyzh.easyshell.file.ShellFileDeleteTask;
import cn.oyzh.easyshell.file.ShellFileDownloadTask;
import cn.oyzh.easyshell.file.ShellFileProgressMonitor;
import cn.oyzh.easyshell.file.ShellFileTransportTask;
import cn.oyzh.easyshell.file.ShellFileUploadTask;
import cn.oyzh.easyshell.file.ShellFileUtil;
import cn.oyzh.easyshell.internal.ShellConnState;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CommonPrefix;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketConfiguration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectVersionsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectVersionsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * s3协议客户端
 *
 * @author oyzh
 * @since 2025-06-14
 */
public class ShellS3Client implements ShellFileClient<ShellS3File> {

    private S3Client s3Client;

    private final ShellConnect connect;

    /**
     * 连接状态
     */
    private final ReadOnlyObjectWrapper<ShellConnState> state = new ReadOnlyObjectWrapper<>();

    /**
     * 当前状态监听器
     */
    private final ChangeListener<ShellConnState> stateListener = (state1, state2, state3) -> ShellFileClient.super.onStateChanged(state3);

    public ShellS3Client(ShellConnect connect) {
        this.connect = connect;
        this.state.set(ShellConnState.NOT_INITIALIZED);
        this.addStateListener(this.stateListener);
    }

    /**
     * 获取区域
     *
     * @return 区域
     */
    public Region region() {
        String region = this.connect.getRegion();
        if (StringUtil.isBlank(region)) {
            return Region.US_EAST_1;
        }
        return Region.of(region);
    }

    private void initClient() {
        String endpoint = "http://" + this.connect.getHost();
        String accessKey = this.connect.getUser();
        String secretKey = this.connect.getPassword();
        // 创建凭证提供者
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);
        // 构建 S3 客户端，指定 endpoint 和凭证
        this.s3Client = S3Client.builder()
                .endpointOverride(java.net.URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .region(this.region())
                .build();
    }

    @Override
    public void start(int timeout) throws Exception {
        this.initClient();
        try {
            this.state.set(ShellConnState.CONNECTING);
            this.s3Client.listBuckets();
            this.state.set(ShellConnState.CONNECTED);
        } catch (Exception ex) {
            this.state.set(ShellConnState.FAILED);
            throw ex;
        }
    }

    @Override
    public ShellConnect getShellConnect() {
        return this.connect;
    }

    @Override
    public boolean isConnected() {
        return this.state.get().isConnected();
    }

    @Override
    public ReadOnlyObjectProperty<ShellConnState> stateProperty() {
        return this.state.getReadOnlyProperty();
    }

    @Override
    public void close() {
        try {
            this.state.set(ShellConnState.CLOSED);
            this.removeStateListener(this.stateListener);
            if (this.s3Client != null) {
                IOUtil.close(this.s3Client);
                this.s3Client = null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public List<ShellS3File> lsFile(String filePath) throws Exception {
        List<ShellS3File> files = new ArrayList<>();
        if (StringUtil.equalsAny(filePath, "/", "")) {
            ListBucketsRequest request = ListBucketsRequest.builder()
                    .build();
            ListBucketsResponse response = this.s3Client.listBuckets(request);
            List<Bucket> list = response.buckets();
            for (Bucket bucket : list) {
                ShellS3File file = new ShellS3File(bucket);
                files.add(file);
            }
        } else {
            ShellS3Path s3Path = ShellS3Path.of(filePath);
            String fPrefix = s3Path.prefix();
            String bucketName = s3Path.bucketName();
            ListObjectsRequest request = ListObjectsRequest.builder()
                    .bucket(bucketName)
                    .prefix(fPrefix)
                    .delimiter("/")
                    .build();
            ListObjectsResponse response = this.s3Client.listObjects(request);
            List<S3Object> list = response.contents();
            for (S3Object s3Object : list) {
                ShellS3File file = new ShellS3File(s3Object, bucketName);
                files.add(file);
            }
            List<CommonPrefix> list2 = response.commonPrefixes();
            for (CommonPrefix prefix : list2) {
                ShellS3File file = new ShellS3File(prefix, bucketName);
                files.add(file);
            }
        }
        return files;
    }

    @Override
    public void delete(String file) throws Exception {
        ShellS3Path s3Path = ShellS3Path.of(file);
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(s3Path.bucketName())
                .key(s3Path.filePath())
                .build();
        this.s3Client.deleteObject(request);
    }

    @Override
    public void deleteDir(String dir) throws Exception {
        ShellS3Path s3Path = ShellS3Path.of(dir);
        List<ObjectIdentifier> objectIdentifiers = new ArrayList<>();
        String continuationToken = null;
        do {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(s3Path.bucketName())
                    .prefix(s3Path.prefix())
                    .continuationToken(continuationToken)
                    .build();
            ListObjectsV2Response listResponse = this.s3Client.listObjectsV2(listRequest);
            // 将结果转换为ObjectIdentifier列表
            listResponse.contents().forEach(s3Object -> {
                objectIdentifiers.add(ObjectIdentifier.builder()
                        .key(s3Object.key())
                        .build());
            });

            continuationToken = listResponse.nextContinuationToken();
        } while (continuationToken != null);
        DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                .bucket(s3Path.bucketName())
                .delete(d -> d.objects(objectIdentifiers))
                .build();
        this.s3Client.deleteObjects(deleteRequest);
    }

    @Override
    public void deleteDirRecursive(String dir) throws Exception {
        this.deleteDir(dir);
    }

    @Override
    public boolean rename(String filePath, String newPath) throws Exception {
        ShellS3Path oldS3Path1 = ShellS3Path.of(filePath);
        ShellS3Path newS3Path2 = ShellS3Path.of(newPath);

        CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                .sourceBucket(oldS3Path1.bucketName())
                .sourceKey(oldS3Path1.filePath())
                .destinationBucket(newS3Path2.bucketName())
                .destinationKey(newS3Path2.filePath())
                .build();
        this.s3Client.copyObject(copyRequest);

        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(oldS3Path1.bucketName())
                .key(oldS3Path1.filePath())
                .build();
        this.s3Client.deleteObject(deleteRequest);

        return true;
    }

    @Override
    public boolean exist(String filePath) throws Exception {
        try {
            ShellS3Path s3Path = ShellS3Path.of(filePath);
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(s3Path.bucketName())
                    .key(s3Path.filePath())
                    .build();
            this.s3Client.headObject(request);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    @Override
    public void touch(String filePath) throws Exception {
        ShellS3Path s3Path = ShellS3Path.of(filePath);
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(s3Path.bucketName())
                .key(s3Path.filePath())
                .build();
        this.s3Client.putObject(putRequest, RequestBody.empty());
    }

    @Override
    public boolean createDir(String filePath) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void createDirRecursive(String filePath) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public String workDir() throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cd(String filePath) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void get(ShellS3File remoteFile, String localFile, Function<Long, Boolean> callback) throws Exception {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(remoteFile.getBucketName())
                .key(remoteFile.getFileKey())
                .build();
        if (callback != null) {
            ShellFileProgressMonitor.ShellFTPOutputStream output = ShellFileProgressMonitor.of(new FileOutputStream(localFile), callback);
            this.s3Client.getObject(request, ResponseTransformer.toOutputStream(output));
        } else {
            this.s3Client.getObject(request, Path.of(localFile));
        }
    }

    @Override
    public InputStream getStream(ShellS3File remoteFile, Function<Long, Boolean> callback) throws Exception {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(remoteFile.getBucketName())
                .key(remoteFile.getFilePath())
                .build();
        ResponseInputStream<?> stream = this.s3Client.getObject(request);
        if (callback == null) {
            return stream;
        }
        return ShellFileProgressMonitor.of(stream, callback);
    }

    @Override
    public void put(InputStream localFile, String remoteFile, Function<Long, Boolean> callback) throws Exception {
        ShellS3Path s3Path = ShellS3Path.of(remoteFile);
        InputStream in;
        if (callback != null) {
            in = ShellFileProgressMonitor.of(localFile, callback);
        } else {
            in = localFile;
        }
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(s3Path.bucketName())
                .key(s3Path.filePath())
                .build();
        this.s3Client.putObject(request, RequestBody.fromInputStream(in, localFile.available()));
    }

    @Override
    public OutputStream putStream(String remoteFile, Function<Long, Boolean> callback) throws Exception {
        throw new UnsupportedOperationException();
    }

    private final ObservableList<ShellFileDeleteTask> deleteTasks = FXCollections.observableArrayList();

    private final ObservableList<ShellFileUploadTask> uploadTasks = FXCollections.observableArrayList();

    private final ObservableList<ShellFileDownloadTask> downloadTasks = FXCollections.observableArrayList();

    private final ObservableList<ShellFileTransportTask> transportTasks = FXCollections.observableArrayList();

    @Override
    public ObservableList<ShellFileDeleteTask> deleteTasks() {
        return this.deleteTasks;
    }

    @Override
    public ObservableList<ShellFileUploadTask> uploadTasks() {
        return this.uploadTasks;
    }

    @Override
    public ObservableList<ShellFileDownloadTask> downloadTasks() {
        return this.downloadTasks;
    }

    @Override
    public ObservableList<ShellFileTransportTask> transportTasks() {
        return this.transportTasks;
    }

    @Override
    public void closeDelayResources() {

    }

    @Override
    public boolean chmod(int permissions, String filePath) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public ShellS3File fileInfo(String filePath) throws Exception {
        ShellS3Path s3Path = ShellS3Path.of(filePath);
        String bucketName = s3Path.bucketName();
        // 查找文件
        String parentPath = s3Path.parentPath();
        String prefix = ShellS3Util.toPrefix(parentPath);
        ListObjectsRequest request = ListObjectsRequest.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .delimiter("/")
                .build();
        ListObjectsResponse response = this.s3Client.listObjects(request);
        // 文件
        List<S3Object> list1 = response.contents();
        if (CollectionUtil.isNotEmpty(list1)) {
            for (S3Object object : list1) {
                String cPrefix = "/" + bucketName;
                cPrefix = ShellFileUtil.concat(cPrefix, object.key());
                if (StringUtil.equals(cPrefix, filePath)) {
                    return new ShellS3File(object, bucketName);
                }
            }
        }
        // 目录
        String dirPath = ShellFileUtil.concat(filePath, "/");
        List<CommonPrefix> list2 = response.commonPrefixes();
        if (CollectionUtil.isNotEmpty(list2)) {
            for (CommonPrefix commonPrefix : list2) {
                String cPrefix = "/" + bucketName;
                cPrefix = ShellFileUtil.concat(cPrefix, commonPrefix.prefix());
                if (StringUtil.equals(cPrefix, dirPath)) {
                    return new ShellS3File(commonPrefix, bucketName);
                }
            }
        }
        return null;
    }

    @Override
    public boolean isSupport(String action) {
        if ("cd".equals(action)) {
            return false;
        }
        if ("chmod".equals(action)) {
            return false;
        }
        if ("workDir".equals(action)) {
            return false;
        }
        if ("putStream".equals(action)) {
            return false;
        }
        if ("createDir".equals(action)) {
            return false;
        }
        if ("createDirRecursive".equals(action)) {
            return false;
        }
        return ShellFileClient.super.isSupport(action);
    }

    /**
     * 列举桶
     *
     * @return 桶列表
     */
    public List<ShellS3Bucket> listBuckets() {
        ListBucketsRequest request = ListBucketsRequest.builder().build();
        ListBucketsResponse response = this.s3Client.listBuckets(request);
        if (response.hasBuckets()) {
            List<ShellS3Bucket> list = new ArrayList<>();
            List<Bucket> buckets = response.buckets();
            for (Bucket bucket : buckets) {
                ShellS3Bucket s3Bucket = new ShellS3Bucket();
                s3Bucket.setName(bucket.name());
                s3Bucket.setRegion(bucket.bucketRegion());
                s3Bucket.setCreationDate(bucket.creationDate());
                list.add(s3Bucket);
            }
            return list;
        }
        return Collections.emptyList();
    }

    /**
     * 创建桶
     *
     * @param bucket 桶对象
     */
    public void createBucket(ShellS3Bucket bucket) {
        CreateBucketRequest request = CreateBucketRequest.builder()
                .bucket(bucket.getName())
                .createBucketConfiguration(
                        CreateBucketConfiguration.builder()
                                .locationConstraint(this.region().id())
                                .build()
                )
                .build();
        this.s3Client.createBucket(request);
    }

    /**
     * 删除桶
     *
     * @param bucket 桶对象
     * @param force  是否强制删除
     */
    public void deleteBucket(ShellS3Bucket bucket, boolean force) {
        if (force) {
            this.deleteAllObjectVersions(bucket.getName());
        }
        DeleteBucketRequest request = DeleteBucketRequest.builder()
                .bucket(bucket.getName())
                .build();
        this.s3Client.deleteBucket(request);
    }

    /**
     * 删除所有对象及版本
     *
     * @param bucketName 桶名称
     */
    private void deleteAllObjectVersions(String bucketName) {
        // 列出所有对象版本
        ListObjectVersionsRequest listRequest = ListObjectVersionsRequest.builder()
                .bucket(bucketName)
                .build();
        ListObjectVersionsResponse response;
        do {
            response = this.s3Client.listObjectVersions(listRequest);
            ListObjectVersionsResponse finalResponse = response;
            // 删除所有对象版本
            if (!response.versions().isEmpty()) {
                DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                        .bucket(bucketName)
                        .delete(delete -> {
                            finalResponse.versions().forEach(version -> {
                                delete.objects(obj -> obj
                                        .key(version.key())
                                        .versionId(version.versionId()));
                            });
                        })
                        .build();
                this.s3Client.deleteObjects(deleteRequest);
            }

            // 删除所有删除标记（DeleteMarker）
            if (!response.deleteMarkers().isEmpty()) {
                DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                        .bucket(bucketName)
                        .delete(delete -> {
                            finalResponse.deleteMarkers().forEach(marker -> {
                                delete.objects(obj -> obj
                                        .key(marker.key())
                                        .versionId(marker.versionId()));
                            });
                        })
                        .build();
                this.s3Client.deleteObjects(deleteRequest);
            }

            // 处理分页
            listRequest = listRequest.toBuilder()
                    .keyMarker(response.nextKeyMarker())
                    .versionIdMarker(response.nextVersionIdMarker())
                    .build();
        } while (response.isTruncated());
    }
}
