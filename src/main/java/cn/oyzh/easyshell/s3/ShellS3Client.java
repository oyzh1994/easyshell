package cn.oyzh.easyshell.s3;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.Competitor;
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
import cn.oyzh.easyshell.internal.ShellClientActionUtil;
import cn.oyzh.easyshell.internal.ShellConnState;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CommonPrefix;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketConfiguration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DefaultRetention;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetBucketVersioningRequest;
import software.amazon.awssdk.services.s3.model.GetBucketVersioningResponse;
import software.amazon.awssdk.services.s3.model.GetObjectLockConfigurationRequest;
import software.amazon.awssdk.services.s3.model.GetObjectLockConfigurationResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.ObjectLockConfiguration;
import software.amazon.awssdk.services.s3.model.ObjectLockEnabled;
import software.amazon.awssdk.services.s3.model.ObjectLockRetentionMode;
import software.amazon.awssdk.services.s3.model.ObjectLockRule;
import software.amazon.awssdk.services.s3.model.PutBucketVersioningRequest;
import software.amazon.awssdk.services.s3.model.PutObjectLockConfigurationRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.VersioningConfiguration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * s3协议客户端
 *
 * @author oyzh
 * @since 2025-06-14
 */
public class ShellS3Client implements ShellFileClient<ShellS3File> {

    /**
     * s3客户端
     */
    private S3Client s3Client;

    /**
     * 连接
     */
    private final ShellConnect connect;

    /**
     * 连接状态
     */
    private final SimpleObjectProperty<ShellConnState> state = new SimpleObjectProperty<>();

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
        return ShellS3Util.ofRegion(region);
    }

    /**
     * 签名提供者
     */
    private StaticCredentialsProvider credentialsProvider;

    /**
     * 初始化客户端
     */
    private void initClient() {
        String endpoint = this.connect.getHost();
        String accessKey = this.connect.getUser();
        String secretKey = this.connect.getPassword();
        // 创建凭证提供者
        this.credentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
        // http客户端
        UrlConnectionHttpClient httpClient = (UrlConnectionHttpClient) UrlConnectionHttpClient.builder()
                .build();

        // s3配置
        S3Configuration s3Configuration;
        // 阿里云
        if (this.connect.isAlibabaS3Type()) {
            s3Configuration = S3Configuration.builder()
                    .pathStyleAccessEnabled(false)
                    .chunkedEncodingEnabled(false)
                    .build();
        } else {
            s3Configuration = S3Configuration.builder().build();
        }

        // 客户端配置
        ClientOverrideConfiguration overrideConfiguration;
        System.setProperty(SdkSystemSetting.AWS_REQUEST_CHECKSUM_CALCULATION.property(), "WHEN_REQUIRED");
        ShellS3MD5Interceptor md5Interceptor = new ShellS3MD5Interceptor();
        overrideConfiguration = ClientOverrideConfiguration.builder()
                .addExecutionInterceptor(md5Interceptor)
                .build();

        // 构建 S3 客户端，指定 endpoint 和凭证
        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(this.credentialsProvider)
                .serviceConfiguration(s3Configuration)
                .overrideConfiguration(overrideConfiguration)
                .httpClient(httpClient)
                .region(this.region())
                .build();
    }

    @Override
    public void start(int timeout) throws Exception {
        try {
            this.initClient();
            this.state.set(ShellConnState.CONNECTING);
            this.s3Client.listBuckets();
            this.state.set(ShellConnState.CONNECTED);
        } catch (Throwable ex) {
            ex.printStackTrace();
            this.state.set(ShellConnState.FAILED);
            throw ex;
        } finally {
            // 执行一次gc，快速回收内存
            SystemUtil.gc();
        }
    }

    @Override
    public ShellConnect getShellConnect() {
        return this.connect;
    }

    @Override
    public boolean isConnected() {
        return this.s3Client != null;
    }

    @Override
    public ObjectProperty<ShellConnState> stateProperty() {
        return this.state;
    }

    @Override
    public void close() {
        try {
            if (this.s3Client != null) {
                IOUtil.close(this.s3Client);
                this.s3Client = null;
            }
            this.state.set(ShellConnState.CLOSED);
            this.removeStateListener(this.stateListener);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // @Override
    // public List<ShellS3File> lsFile(String filePath) throws Exception {
    //     List<ShellS3File> files = new ArrayList<>();
    //     if (StringUtil.equalsAny(filePath, "/", "")) {
    //         ListBucketsRequest request = ListBucketsRequest.builder()
    //                 .build();
    //         ListBucketsResponse response = this.s3Client.listBuckets(request);
    //         List<Bucket> list = response.buckets();
    //         for (Bucket bucket : list) {
    //             ShellS3File file = new ShellS3File(bucket);
    //             files.add(file);
    //         }
    //     } else {
    //         ShellS3Path s3Path = ShellS3Path.of(filePath);
    //         String fPrefix = s3Path.prefix();
    //         String bucketName = s3Path.bucketName();
    //         ListObjectsRequest request = ListObjectsRequest.builder()
    //                 .bucket(bucketName)
    //                 .prefix(fPrefix)
    //                 .delimiter("/")
    //                 .build();
    //         ListObjectsResponse response = this.s3Client.listObjects(request);
    //         List<S3Object> list = response.contents();
    //         for (S3Object s3Object : list) {
    //             ShellS3File file = new ShellS3File(s3Object, bucketName);
    //             files.add(file);
    //         }
    //         List<CommonPrefix> list2 = response.commonPrefixes();
    //         for (CommonPrefix prefix : list2) {
    //             ShellS3File file = new ShellS3File(prefix, bucketName);
    //             files.add(file);
    //         }
    //     }
    //     return files;
    // }

    @Override
    public void lsFileDynamic(String filePath, Consumer<ShellS3File> fileCallback) {
        // 操作
        ShellClientActionUtil.forAction(this.connectName(), "ls " + filePath);
        if (StringUtil.equalsAny(filePath, "/", "")) {
            ListBucketsRequest request = ListBucketsRequest.builder()
                    .build();
            ListBucketsResponse response = this.s3Client.listBuckets(request);
            List<Bucket> list = response.buckets();
            for (Bucket bucket : list) {
                ShellS3File file = new ShellS3File(bucket);
                fileCallback.accept(file);
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
                fileCallback.accept(file);
            }
            List<CommonPrefix> list2 = response.commonPrefixes();
            for (CommonPrefix prefix : list2) {
                ShellS3File file = new ShellS3File(prefix, bucketName);
                fileCallback.accept(file);
            }
        }
    }

    @Override
    public void delete(String file) throws Exception {
        // 操作
        ShellClientActionUtil.forAction(this.connectName(), "rm " + file);
        ShellS3Path s3Path = ShellS3Path.of(file);
        String filePath = s3Path.filePath();
        String bucketName = s3Path.bucketName();
        if (this.isBucketVersioning(s3Path.bucketName())) {
            ShellS3Util.deleteAllVersions(s3Client, bucketName, filePath);
        }
        ShellS3Util.deleteNormalFile(s3Client, bucketName, filePath);
    }

    @Override
    public void deleteDir(String dir) {
        // 操作
        ShellClientActionUtil.forAction(this.connectName(), "rmdir " + dir);
        ShellS3Path s3Path = ShellS3Path.of(dir);
        String prefix = s3Path.prefix();
        String bucketName = s3Path.bucketName();
        if (this.isBucketVersioning(s3Path.bucketName())) {
            ShellS3Util.deleteAllVersionsInDirectory(s3Client, bucketName, prefix);
        } else {
            ShellS3Util.deleteNonVersionedDirectory(s3Client, bucketName, prefix);
        }
    }

    @Override
    public void deleteDirRecursive(String dir) {
        this.deleteDir(dir);
    }

    @Override
    public boolean rename(String filePath, String newPath) throws Exception {
        // 操作
        ShellClientActionUtil.forAction(this.connectName(), "rename " + filePath + " " + newPath);
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
        // 操作
        ShellClientActionUtil.forAction(this.connectName(), "exist " + filePath);
        ShellS3Path s3Path = ShellS3Path.of(filePath);
        String bucketName = s3Path.bucketName();
        if (bucketName == null) {
            return false;
        }
        boolean notBucket = StringUtil.checkCountOccurrences(filePath, '/', 2);
        // 对象
        if (notBucket) {
            if (filePath.endsWith("/")) {// 目录
                // ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                //         .bucket(bucketName)
                //         .prefix(fPath)
                //         .maxKeys(1) // 高效：仅需1个结果
                //         .build();
                // ListObjectsV2Response response = s3Client.listObjectsV2(listRequest);
                // if (response != null && !response.contents().isEmpty()) {
                //     return true;
                // }
                // 目录直接返回true
                return true;
            }
            // 检查文件是否存在
            try {
                HeadObjectRequest request = HeadObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3Path.filePath())
                        .build();
                this.s3Client.headObject(request);
                return true;
            } catch (NoSuchKeyException ignored) {

            }
        } else { // 桶
            try {
                HeadBucketRequest request = HeadBucketRequest.builder()
                        .bucket(bucketName)
                        .build();
                this.s3Client.headBucket(request);
                return true;
            } catch (NoSuchBucketException ignored) {
            }
        }
        return false;
    }

    @Override
    public String realpath(String filePath) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void touch(String filePath) throws Exception {
        // 操作
        ShellClientActionUtil.forAction(this.connectName(), "touch " + filePath);
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
        // 操作
        ShellClientActionUtil.forAction(this.connectName(), "get " + remoteFile.getFilePath());
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(remoteFile.getBucketName())
                .key(remoteFile.getFileKey())
                .build();
        if (callback != null) {
            OutputStream output = ShellFileProgressMonitor.of(new FileOutputStream(localFile), callback);
            this.s3Client.getObject(request, ResponseTransformer.toOutputStream(output));
        } else {
            FileOutputStream fos = new FileOutputStream(localFile);
            this.s3Client.getObject(request, ResponseTransformer.toOutputStream(fos));
            // localFile = ResourceUtil.getLocalFileUrl(localFile);
            // this.s3Client.getObject(request, Path.of(localFile));
        }
    }

    @Override
    public InputStream getStream(ShellS3File remoteFile, Function<Long, Boolean> callback) throws IOException {
        // 操作
        ShellClientActionUtil.forAction(this.connectName(), "get " + remoteFile.getFilePath());
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
        // 操作
        ShellClientActionUtil.forAction(this.connectName(), "put " + remoteFile);
        ShellS3Path s3Path = ShellS3Path.of(remoteFile);
        InputStream in;
        if (callback != null) {
            // if (localFile instanceof FileInputStream fIn) {
            //     in = ShellFileProgressMonitor.of3(fIn, callback);
            // } else {
            in = ShellFileProgressMonitor.of(localFile, callback);
            // }
        } else {
            in = localFile;
        }
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(s3Path.bucketName())
                .key(s3Path.filePath())
                // .contentLength((long) in.available())
                .build();
        this.s3Client.putObject(request, RequestBody.fromInputStream(in, in.available()));
    }

    @Override
    public OutputStream putStream(String remoteFile, Function<Long, Boolean> callback) throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * 删除竞争器
     */
    private final Competitor deleteCompetitor = new Competitor(5);

    @Override
    public Competitor deleteCompetitor() {
        return this.deleteCompetitor;
    }

    private final ObservableList<ShellFileDeleteTask> deleteTasks = FXCollections.observableArrayList();

    private final ObservableList<ShellFileUploadTask> uploadTasks = FXCollections.observableArrayList();

    private final ObservableList<ShellFileDownloadTask> downloadTasks = FXCollections.observableArrayList();

    private final ObservableList<ShellFileTransportTask> transportTasks = FXCollections.observableArrayList();

    @Override
    public ObservableList<ShellFileDeleteTask> deleteTasks() {
        return this.deleteTasks;
    }

    /**
     * 上传竞争器
     */
    private final Competitor uploadCompetitor = new Competitor(2);

    @Override
    public Competitor uploadCompetitor() {
        return this.uploadCompetitor;
    }

    @Override
    public ObservableList<ShellFileUploadTask> uploadTasks() {
        return this.uploadTasks;
    }

    /**
     * 下载竞争器
     */
    private final Competitor downloadCompetitor = new Competitor(2);

    @Override
    public Competitor downloadCompetitor() {
        return this.downloadCompetitor;
    }

    @Override
    public ObservableList<ShellFileDownloadTask> downloadTasks() {
        return this.downloadTasks;
    }

    /**
     * 传输竞争器
     */
    private final Competitor transportCompetitor = new Competitor(2);

    @Override
    public Competitor transportCompetitor() {
        return transportCompetitor;
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
        // 操作
        ShellClientActionUtil.forAction(this.connectName(), "fileInfo " + filePath);
        ShellS3Path s3Path = ShellS3Path.of(filePath);
        String bucketName = s3Path.bucketName();
        if (bucketName == null) {
            return null;
        }
        boolean notBucket = StringUtil.checkCountOccurrences(filePath, '/', 2);
        if (notBucket) {
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
        return new ShellS3File(this.getBucket(bucketName));
    }

    @Override
    public boolean isCdSupport() {
        return false;
    }

    @Override
    public boolean isChmodSupport() {
        return false;
    }

    @Override
    public boolean isRealpathSupport() {
        return false;
    }

    @Override
    public boolean isWorkDirSupport() {
        return false;
    }

    @Override
    public boolean isPutStreamSupport() {
        return false;
    }

    @Override
    public boolean isCreateDirSupport() {
        return false;
    }

    @Override
    public boolean isCreateDirRecursiveSupport() {
        return false;
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
                String region = bucket.bucketRegion();
                if (StringUtil.isBlank(region)) {
                    region = this.getShellConnect().getRegion();
                }
                s3Bucket.setRegion(region);
                s3Bucket.setCreationDate(bucket.creationDate());
                s3Bucket.setRetention(this.getBucketRetention(bucket.name()));
                s3Bucket.setVersioning(this.isBucketVersioning(bucket.name()));
                s3Bucket.setObjectLock(this.isBucketObjectLock(bucket.name()));
                list.add(s3Bucket);
            }
            return list;
        }
        return Collections.emptyList();
    }

    /**
     * 获取桶
     *
     * @param bucketName 桶名称
     * @return ShellS3Bucket
     */
    public Bucket getBucket(String bucketName) {
        ListBucketsRequest request = ListBucketsRequest.builder().build();
        ListBucketsResponse response = this.s3Client.listBuckets(request);
        if (response.hasBuckets()) {
            List<Bucket> buckets = response.buckets();
            for (Bucket bucket : buckets) {
                if (bucket.name().equals(bucketName)) {
                    return bucket;
                }
            }
        }
        return null;
    }

    /**
     * 创建桶
     *
     * @param bucket 桶对象
     */
    public void createBucket(ShellS3Bucket bucket) {
        String bucketName = bucket.getName();
        if (this.connect.isTencentS3Type()) {
            bucketName = bucketName + "-" + this.connect.getS3AppId();
        }
        CreateBucketRequest request = CreateBucketRequest.builder()
                .bucket(bucketName)
                .createBucketConfiguration(CreateBucketConfiguration.builder().build())
                .objectLockEnabledForBucket(bucket.isObjectLock())
                .createBucketConfiguration(
                        CreateBucketConfiguration.builder()
                                .locationConstraint(this.region().id())
                                .build()
                )
                .build();
        this.s3Client.createBucket(request);
        // 版本控制
        if (bucket.isVersioning()) {
            this.setBucketVersioning(bucketName, true);
        }
        // 保留
        if (bucket.isRetention()) {
            ObjectLockRetentionMode mode = bucket.getRetentionMode() == 0 ? ObjectLockRetentionMode.COMPLIANCE : ObjectLockRetentionMode.GOVERNANCE;
            if (bucket.getRetentionValidityType() == 0) {
                this.setBucketRetentionByDays(bucketName, bucket.getRetentionValidity(), mode);
            } else {
                this.setBucketRetentionByYears(bucketName, bucket.getRetentionValidity(), mode);
            }
        }
    }

    /**
     * 修改桶
     *
     * @param bucket 桶对象
     */
    public void updateBucket(ShellS3Bucket bucket) {
        String bucketName = bucket.getName();
        // 版本控制
        this.setBucketVersioning(bucketName, bucket.isVersioning());
        // 保留
        if (bucket.isRetention()) {
            ObjectLockRetentionMode mode = bucket.getRetentionMode() == 0 ? ObjectLockRetentionMode.COMPLIANCE : ObjectLockRetentionMode.GOVERNANCE;
            if (bucket.getRetentionValidityType() == 0) {
                this.setBucketRetentionByDays(bucketName, bucket.getRetentionValidity(), mode);
            } else {
                this.setBucketRetentionByYears(bucketName, bucket.getRetentionValidity(), mode);
            }
        }
    }

    /**
     * 删除桶
     *
     * @param bucket 桶对象
     * @param force  是否强制删除
     */
    public void deleteBucket(ShellS3Bucket bucket, boolean force) {
        // 删除数据
        if (force) {
            if (bucket.isVersioning()) {
                ShellS3Util.deleteVersionedBucketObjects(this.s3Client, bucket.getName());
            } else {
                ShellS3Util.deleteNonVersionedBucketObjects(this.s3Client, bucket.getName());
            }
        }
        // 删除桶
        DeleteBucketRequest request = DeleteBucketRequest.builder()
                .bucket(bucket.getName())
                .build();
        this.s3Client.deleteBucket(request);
    }

    /**
     * 判断桶是否启用版本控制
     *
     * @param bucketName 桶名称
     * @return 结果
     */
    public boolean isBucketVersioning(String bucketName) {
        GetBucketVersioningRequest request = GetBucketVersioningRequest.builder().bucket(bucketName).build();
        // 验证桶是否启用版本控制
        GetBucketVersioningResponse versioningResponse = this.s3Client.getBucketVersioning(request);
        return "Enabled".equals(versioningResponse.statusAsString());
    }

    /**
     * 设置桶版本控制
     *
     * @param bucketName 桶名称
     * @param enable     是否开启
     */
    public void setBucketVersioning(String bucketName, boolean enable) {
        // 判断是否已经开启了版本控制
        boolean versioning = this.isBucketVersioning(bucketName);
        // 判断是否符合当前状态
        if (enable && versioning) {
            return;
        }
        if (!enable && !versioning) {
            return;
        }
        PutBucketVersioningRequest request = PutBucketVersioningRequest.builder()
                .bucket(bucketName)
                .versioningConfiguration(VersioningConfiguration.builder()
                        .status(enable ? "Enabled" : "Suspended")
                        .build())
                .build();
        this.s3Client.putBucketVersioning(request);
    }

    /**
     * 判断桶是否启用对象锁定
     *
     * @param bucketName 桶名称
     * @return 结果
     */
    public boolean isBucketObjectLock(String bucketName) {
        try {
            GetObjectLockConfigurationResponse response = this.s3Client.getObjectLockConfiguration(
                    GetObjectLockConfigurationRequest.builder()
                            .bucket(bucketName)
                            .build());
            // 检查对象锁定是否启用
            return response.objectLockConfiguration().objectLockEnabled() == ObjectLockEnabled.ENABLED;
        } catch (NoSuchBucketException | NoSuchKeyException e) {
            return false;
        } catch (S3Exception ex) {
            if (ExceptionUtil.hasMessage(ex, "does not exist")) {
                return false;
            }
        }
        return false;
    }

    /**
     * 开启桶对象锁定
     *
     * @param bucketName 桶名称
     */
    public void enableBucketObjectLocking(String bucketName) {
        ObjectLockConfiguration objectLockConfig = ObjectLockConfiguration.builder()
                .objectLockEnabled("Enabled")
                .build();
        PutObjectLockConfigurationRequest lockConfigRequest = PutObjectLockConfigurationRequest.builder()
                .bucket(bucketName)
                .objectLockConfiguration(objectLockConfig)
                .build();
        this.s3Client.putObjectLockConfiguration(lockConfigRequest);
    }

    /**
     * 设置通的按天保留模式
     *
     * @param bucketName 桶名称
     * @param days       天数
     * @param mode       模式
     */
    public void setBucketRetentionByDays(String bucketName, int days, ObjectLockRetentionMode mode) {
        ObjectLockConfiguration lockConfig = ObjectLockConfiguration.builder()
                .objectLockEnabled("Enabled")
                .rule(ObjectLockRule.builder()
                        .defaultRetention(DefaultRetention.builder()
                                .mode(mode) // GOVERNANCE或COMPLIANCE
                                .days(days)
                                .build())
                        .build())
                .build();

        PutObjectLockConfigurationRequest request = PutObjectLockConfigurationRequest.builder()
                .bucket(bucketName)
                .objectLockConfiguration(lockConfig)
                .build();

        this.s3Client.putObjectLockConfiguration(request);
    }

    /**
     * 设置通的按年保留模式
     *
     * @param bucketName 桶名称
     * @param years      年数
     * @param mode       模式
     */
    public void setBucketRetentionByYears(String bucketName, int years, ObjectLockRetentionMode mode) {
        ObjectLockConfiguration lockConfig = ObjectLockConfiguration.builder()
                .objectLockEnabled("Enabled")
                .rule(ObjectLockRule.builder()
                        .defaultRetention(DefaultRetention.builder()
                                .mode(mode) // GOVERNANCE或COMPLIANCE
                                .years(years)
                                .build())
                        .build())
                .build();
        PutObjectLockConfigurationRequest request = PutObjectLockConfigurationRequest.builder()
                .bucket(bucketName)
                .objectLockConfiguration(lockConfig)
                .build();
        this.s3Client.putObjectLockConfiguration(request);
    }

    /**
     * 获取Bucket的默认保留规则
     *
     * @param bucketName 桶
     * @return 默认保留规则
     */
    public DefaultRetention getBucketRetention(String bucketName) {
        try {
            GetObjectLockConfigurationRequest request = GetObjectLockConfigurationRequest.builder()
                    .bucket(bucketName)
                    .build();

            GetObjectLockConfigurationResponse response = s3Client.getObjectLockConfiguration(request);
            ObjectLockConfiguration config = response.objectLockConfiguration();

            if (config == null || config.rule() == null || config.rule().defaultRetention() == null) {
                return null; // 未配置默认保留规则
            }

            return config.rule().defaultRetention();
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                System.out.println("Bucket未启用对象锁定或不存在: " + bucketName);
                return null;
            }
            throw e;
        }
    }

    /**
     * 创建一个带签名的访问地址
     *
     * @param bucketName 桶
     * @param key        键
     * @param duration   时长
     * @return 值
     */
    public String generatePresignedUrl(String bucketName, String key, Duration duration) {
        // 操作
        ShellClientActionUtil.forAction(this.connectName(), "generatePresignedUrl " + key);
        // 签名器
        S3Presigner signer = S3Presigner.builder().region(this.region())
                .credentialsProvider(this.credentialsProvider)
                .endpointOverride(URI.create(this.connect.getHost()))
                .build();
        try (signer) {
            // 1. 构建请求
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            // 2. 设置签名有效期（例如 10 分钟）
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(duration)
                    .getObjectRequest(getObjectRequest)
                    .build();

            // 3. 生成签名 URL
            PresignedGetObjectRequest presignedRequest = signer.presignGetObject(presignRequest);
            return presignedRequest.url().toString();
        }
    }
}
