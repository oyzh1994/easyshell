package cn.oyzh.easyshell.s3;

import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.file.ShellFileClient;
import cn.oyzh.easyshell.file.ShellFileDeleteTask;
import cn.oyzh.easyshell.file.ShellFileDownloadTask;
import cn.oyzh.easyshell.file.ShellFileTransportTask;
import cn.oyzh.easyshell.file.ShellFileUploadTask;
import cn.oyzh.easyshell.internal.ShellConnState;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CommonPrefix;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

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
                .region(Region.US_EAST_1) // MinIO 不强制要求区域，使用任意有效区域即可
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
            String fPath = s3Path.filePath();
            String fPrefix;
            if ("/".equals(fPath)) {
                fPrefix = "";
            } else if (!fPath.endsWith("/")) {
                fPrefix = fPath.substring(1) + "/";
            } else {
                fPrefix = fPath;
            }
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

    }

    @Override
    public void deleteDir(String dir) throws Exception {

    }

    @Override
    public void deleteDirRecursive(String dir) throws Exception {

    }

    @Override
    public boolean rename(String filePath, String newPath) throws Exception {
        return false;
    }

    @Override
    public boolean exist(String filePath) throws Exception {
        return false;
    }

    @Override
    public void touch(String filePath) throws Exception {

    }

    @Override
    public boolean createDir(String filePath) throws Exception {
        return false;
    }

    @Override
    public void createDirRecursive(String filePath) throws Exception {

    }

    @Override
    public String workDir() throws Exception {
        return "";
    }

    @Override
    public void cd(String filePath) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void get(ShellS3File remoteFile, String localFile, Function<Long, Boolean> callback) throws Exception {

    }

    @Override
    public InputStream getStream(ShellS3File remoteFile, Function<Long, Boolean> callback) throws Exception {
        return null;
    }

    @Override
    public void put(InputStream localFile, String remoteFile, Function<Long, Boolean> callback) throws Exception {

    }

    @Override
    public OutputStream putStream(String remoteFile, Function<Long, Boolean> callback) throws Exception {
        return null;
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
        // 构建请求
        HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(s3Path.bucketName())
                .key(s3Path.filePath())
                .build();
        HeadObjectResponse response = this.s3Client.headObject(request);
        ShellS3File file = new ShellS3File();
        file.setFilePath(filePath);
        file.setFileSize(response.contentLength());
        file.setMTime(response.lastModified());
        return null;
    }

    public List<ShellS3Bucket> ListBuckets() {
        ListBucketsResponse response = this.s3Client.listBuckets();
        List<Bucket> buckets = response.buckets();
        List<ShellS3Bucket> shellS3Buckets = new ArrayList<>();
        for (Bucket bucket : buckets) {
            ShellS3Bucket shellS3Bucket = new ShellS3Bucket();
            shellS3Bucket.setName(bucket.name());
            shellS3Buckets.add(shellS3Bucket);
        }
        return shellS3Buckets;
    }


}
