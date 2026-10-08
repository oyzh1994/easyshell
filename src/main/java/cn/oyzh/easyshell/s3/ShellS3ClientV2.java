package cn.oyzh.easyshell.s3;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.common.util.Competitor;
import cn.oyzh.common.util.IOUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.UUIDUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.file.ShellFileClient;
import cn.oyzh.easyshell.file.ShellFileDeleteTask;
import cn.oyzh.easyshell.file.ShellFileDownloadTask;
import cn.oyzh.easyshell.file.ShellFileProgressMonitor;
import cn.oyzh.easyshell.file.ShellFileTransportTask;
import cn.oyzh.easyshell.file.ShellFileUploadTask;
import cn.oyzh.easyshell.file.ShellFileUtil;
import cn.oyzh.easyshell.internal.ShellClientActionUtil;
import cn.oyzh.easyshell.internal.ShellClientChecker;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.util.ShellProxyUtil;
import io.minio.BucketExistsArgs;
import io.minio.CopyObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.Http;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveBucketArgs;
import io.minio.RemoveObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.SetBucketVersioningArgs;
import io.minio.SetObjectLockConfigurationArgs;
import io.minio.SourceObject;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.XmlParserException;
import io.minio.messages.DeleteRequest;
import io.minio.messages.DeleteResult;
import io.minio.messages.Item;
import io.minio.messages.ListAllMyBucketsResult;
import io.minio.messages.ObjectLockConfiguration;
import io.minio.messages.ObjectLockConfiguration.RetentionDuration;
import io.minio.messages.ObjectLockConfiguration.RetentionDurationDays;
import io.minio.messages.ObjectLockConfiguration.RetentionDurationUnit;
import io.minio.messages.ObjectLockConfiguration.RetentionDurationYears;
import io.minio.messages.RetentionMode;
import io.minio.messages.VersioningConfiguration;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 使用 MinIO SDK 的 S3 客户端。
 *
 * <p>网络操作均通过 MinIO SDK 完成，模型类型由项目自行提供，不依赖旧客户端或 AWS S3 SDK。</p>
 *
 * @author oyzh
 * @since 2026-10-08
 */
public class ShellS3ClientV2 implements ShellFileClient<ShellS3File> {

    private static final long DEFAULT_UPLOAD_PART_SIZE = 10L * 1024 * 1024;

    private static final String RUSTFS_FORCE_DELETE_HEADER = "X-Rustfs-Force-Delete";

    private static final String MINIO_FORCE_DELETE_HEADER = "X-Minio-Force-Delete";

    private static final String OBJECT_LOCK_RETAIN_UNTIL_HEADER = "x-amz-object-lock-retain-until-date";

    private static final DateTimeFormatter HTTP_DATE_FORMATTER = DateTimeFormatter
            .ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US)
            .withZone(ZoneOffset.UTC);

    private static final DateTimeFormatter MINIO_RESPONSE_DATE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            .withZone(ZoneOffset.UTC);

    private MinioClient minioClient;

    private MinioClient forceDeleteClient;

    private OkHttpClient httpClient;

    private OkHttpClient forceDeleteHttpClient;

    private final Map<String, MinioClient> regionClients = new ConcurrentHashMap<>();

    private final Map<String, MinioClient> forceDeleteRegionClients = new ConcurrentHashMap<>();

    private final ShellConnect connect;

    private final SimpleObjectProperty<ShellConnState> state = new SimpleObjectProperty<>();

    private final ChangeListener<ShellConnState> stateListener =
            (state1, state2, state3) -> ShellFileClient.super.onStateChanged(state3);

    private final Competitor deleteCompetitor = new Competitor(5);

    private final ObservableList<ShellFileDeleteTask> deleteTasks = FXCollections.observableArrayList();

    private final Competitor uploadCompetitor = new Competitor(2);

    private final ObservableList<ShellFileUploadTask> uploadTasks = FXCollections.observableArrayList();

    private final Competitor downloadCompetitor = new Competitor(2);

    private final ObservableList<ShellFileDownloadTask> downloadTasks = FXCollections.observableArrayList();

    private final Competitor transportCompetitor = new Competitor(2);

    private final ObservableList<ShellFileTransportTask> transportTasks = FXCollections.observableArrayList();

    private final Map<String, String> bucketRegions = new ConcurrentHashMap<>();

    private int clientTimeout;

    private String appId;

    public ShellS3ClientV2(ShellConnect connect) {
        this.connect = connect;
        this.state.set(ShellConnState.NOT_INITIALIZED);
        this.addStateListener(this.stateListener);
    }

    /**
     * 获取区域 id。
     *
     * @return 区域 id
     */
    public String regionId() {
        String region = this.connect.getRegion();
        if (StringUtil.isBlank(region)) {
            region = ShellS3Util.parseRegion(this.connect.getHost());
        }
        return StringUtil.isBlank(region) ? "us-east-1" : region;
    }

    private String endpoint() {
        String endpoint = this.connect.getHost();
        if (StringUtil.isBlank(endpoint)) {
            throw new IllegalStateException("S3 endpoint is blank");
        }
        if (!endpoint.contains("://")) {
            endpoint = "http://" + endpoint;
        }
        return endpoint;
    }

    private void initClient(int timeout) {
        this.clientTimeout = timeout;
        OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofMillis(Math.max(timeout, 1)))
                .readTimeout(Duration.ofMillis(Math.max(timeout, 1)))
                .writeTimeout(Duration.ofMillis(Math.max(timeout, 1)))
                .addInterceptor(chain -> {
                    Response response = chain.proceed(chain.request());
                    String retainUntilDate = response.header(OBJECT_LOCK_RETAIN_UNTIL_HEADER);
                    if (StringUtil.isBlank(retainUntilDate)) {
                        return response;
                    }
                    try {
                        ZonedDateTime retainDate = this.parseObjectLockDate(retainUntilDate);
                        String normalized = MINIO_RESPONSE_DATE_FORMATTER.format(retainDate);
                        return response.newBuilder()
                                .removeHeader(OBJECT_LOCK_RETAIN_UNTIL_HEADER)
                                .header(OBJECT_LOCK_RETAIN_UNTIL_HEADER, normalized)
                                .build();
                    } catch (Exception ex) {
                        return response;
                    }
                });
        if (this.connect.isEnableProxy() && ShellProxyUtil.isNeedProxy(this.connect.getProxyConfig())) {
            Proxy proxy = ShellProxyUtil.initProxy1(this.connect.getProxyConfig());
            if (proxy != null && proxy != Proxy.NO_PROXY) {
                httpBuilder.proxy(proxy);
                String user = this.connect.getProxyConfig().getUser();
                String password = this.connect.getProxyConfig().getPassword();
                if (StringUtil.isNotBlank(user) && StringUtil.isNotBlank(password)) {
                    String authorization = Credentials.basic(user, password);
                    httpBuilder.proxyAuthenticator((route, response) -> response.request().newBuilder()
                            .header("Proxy-Authorization", authorization)
                            .build());
                }
            }
        }
        OkHttpClient httpClient = httpBuilder.build();
        this.httpClient = httpClient;
        this.minioClient = this.buildClient(httpClient, timeout, this.endpoint(), this.regionId());
        this.regionClients.put(this.regionId(), this.minioClient);
        if (this.connect.isMinioS3Type()) {
            OkHttpClient forceDeleteHttpClient = httpClient.newBuilder()
                    .addInterceptor(chain -> {
                        if (!"DELETE".equalsIgnoreCase(chain.request().method())) {
                            return chain.proceed(chain.request());
                        }
                        return chain.proceed(chain.request().newBuilder()
                                .header(RUSTFS_FORCE_DELETE_HEADER, "true")
                                .header(MINIO_FORCE_DELETE_HEADER, "true")
                                .build());
                    })
                    .build();
            this.forceDeleteHttpClient = forceDeleteHttpClient;
            this.forceDeleteClient = this.buildClient(forceDeleteHttpClient, timeout, this.endpoint(), this.regionId());
            this.forceDeleteRegionClients.put(this.regionId(), this.forceDeleteClient);
        }
    }

    private MinioClient buildClient(OkHttpClient httpClient, int timeout, String endpoint, String region) {
        MinioClient client = MinioClient.builder()
                .endpoint(endpoint)
                .region(region)
                .credentials(this.connect.getUser(), this.connect.getPassword())
                .httpClient(httpClient, false)
                .build();
        if ((this.connect.isAlibabaS3Type()
                || this.connect.isTencentS3Type()
                || this.connect.isHuaweiS3Type())
                && !this.isLoopbackHost(HttpUrl.get(endpoint).host())) {
            client.enableVirtualStyleEndpoint();
        }
        int clientTimeout = Math.max(timeout, 1);
        client.setTimeout(clientTimeout, clientTimeout, clientTimeout);
        return client;
    }

    private String endpointForRegion(String region) {
        HttpUrl endpoint = HttpUrl.get(this.endpoint());
        String host = endpoint.host();
        String lowerHost = host.toLowerCase();
        String suffix = ".aliyuncs.com";
        if (lowerHost.endsWith(suffix)) {
            int marker = lowerHost.indexOf(".oss-");
            if (marker >= 0) {
                host = host.substring(0, marker + 1) + region + suffix;
            } else if (lowerHost.startsWith("oss-")) {
                host = region + suffix;
            }
        } else if (this.connect.isHuaweiS3Type()) {
            String huaweiSuffix = ".myhuaweicloud.com";
            if (lowerHost.equals("obs" + huaweiSuffix)
                    || (lowerHost.startsWith("obs.") && lowerHost.endsWith(huaweiSuffix))) {
                host = "obs." + region + huaweiSuffix;
            } else {
                String configuredRegion = this.regionId();
                if (!StringUtil.equals(configuredRegion, region)) {
                    host = host.replace(configuredRegion, region);
                }
            }
        } else {
            String configuredRegion = this.regionId();
            if (!StringUtil.equals(configuredRegion, region)) {
                host = host.replace(configuredRegion, region);
            }
        }
        return endpoint.newBuilder().host(host).build().toString();
    }

    private MinioClient clientForBucket(String bucketName, boolean forceDelete) {
        boolean useForceDelete = forceDelete && this.connect.isMinioS3Type()
                && this.forceDeleteHttpClient != null;
        String region = StringUtil.isBlank(bucketName)
                ? this.regionId()
                : this.bucketRegions.getOrDefault(bucketName, this.regionId());
        return this.clientForRegion(region, useForceDelete);
    }

    private MinioClient clientForRegion(String region, boolean forceDelete) {
        String regionId = StringUtil.isBlank(region) ? this.regionId() : region;
        if (forceDelete) {
            return this.forceDeleteRegionClients.computeIfAbsent(regionId, key ->
                    this.buildClient(this.forceDeleteHttpClient, this.clientTimeout,
                            this.endpointForRegion(key), key));
        }
        return this.regionClients.computeIfAbsent(regionId, key ->
                this.buildClient(this.httpClient, this.clientTimeout,
                        this.endpointForRegion(key), key));
    }

    private <T> T withBucketClient(String bucketName, boolean forceDelete,
                                   BucketClientOperation<T> operation) throws Exception {
        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                return operation.execute(this.clientForBucket(bucketName, forceDelete));
            } catch (Exception ex) {
                if (attempt == 0 && this.applyBucketRegionFromRedirect(bucketName, ex)) {
                    continue;
                }
                throw ex;
            }
        }
        throw new IllegalStateException("S3 bucket client retry failed: " + bucketName);
    }

    private <T> T withBucketClient(String bucketName, BucketClientOperation<T> operation) throws Exception {
        return this.withBucketClient(bucketName, false, operation);
    }

    private boolean applyBucketRegionFromRedirect(String bucketName, Throwable ex) {
        Throwable current = ex;
        boolean missingBucket = false;
        while (current != null) {
            if (current instanceof ErrorResponseException error) {
                String code = error.errorResponse() == null ? null : error.errorResponse().code();
                int status = error.response() == null ? 0 : error.response().code();
                String region = error.response() == null
                        ? null : error.response().header("x-amz-bucket-region");
                missingBucket = missingBucket || StringUtil.equals(code, "NoSuchBucket");
                boolean redirect = status == 301 || status == 307
                        || StringUtil.equalsAny(code, "PermanentRedirect", "TemporaryRedirect")
                        || StringUtil.isNotBlank(region);
                if (redirect && StringUtil.isNotBlank(region)
                        && !StringUtil.equals(this.bucketRegions.get(bucketName), region)) {
                    this.bucketRegions.put(bucketName, region);
                    return true;
                }
            }
            current = current.getCause();
        }
        if (missingBucket && this.connect.isHuaweiS3Type()) {
            try {
                String region = this.getHuaweiBucketLocation(bucketName);
                if (StringUtil.isNotBlank(region)
                        && !StringUtil.equals(this.bucketRegions.get(bucketName), region)) {
                    this.bucketRegions.put(bucketName, region);
                    return true;
                }
            } catch (Exception ex2) {
                JulLog.debug("Get Huawei OBS bucket location error", ex2);
            }
        }
        return false;
    }

    @FunctionalInterface
    private interface BucketClientOperation<T> {
        T execute(MinioClient client) throws Exception;
    }

    @Override
    public void start(int timeout) throws Exception {
        try {
            this.initClient(timeout);
            this.state.set(ShellConnState.CONNECTING);
            this.rawBuckets();
            this.state.set(ShellConnState.CONNECTED);
            ShellClientChecker.push(this);
        } catch (Throwable ex) {
            this.state.set(ShellConnState.FAILED);
            throw ex;
        } finally {
            SystemUtil.gc();
        }
    }

    @Override
    public ShellConnect getShellConnect() {
        return this.connect;
    }

    @Override
    public synchronized boolean isConnected() {
        if (this.minioClient != null) {
            try {
                String name = UUIDUtil.uuidSimple();
                if (this.connect.isTencentS3Type()) {
                    name = name + "-" + this.getAppId();
                }
                this.minioClient.bucketExists(BucketExistsArgs.builder().bucket(name).build());
                return true;
            } catch (Exception ex) {
                JulLog.warn("S3 V2 client check error", ex);
            }
        }
        return false;
    }

    @Override
    public ObjectProperty<ShellConnState> stateProperty() {
        return this.state;
    }

    @Override
    public synchronized void close() {
        try {
            for (MinioClient client : this.regionClients.values()) {
                IOUtil.close(client);
            }
            this.regionClients.clear();
            this.minioClient = null;
            for (MinioClient client : this.forceDeleteRegionClients.values()) {
                IOUtil.close(client);
            }
            this.forceDeleteRegionClients.clear();
            this.forceDeleteClient = null;
            if (this.httpClient != null) {
                this.httpClient.dispatcher().executorService().shutdown();
                this.httpClient.connectionPool().evictAll();
                this.httpClient = null;
            }
            if (this.forceDeleteHttpClient != null) {
                this.forceDeleteHttpClient.dispatcher().executorService().shutdown();
                this.forceDeleteHttpClient.connectionPool().evictAll();
                this.forceDeleteHttpClient = null;
            }
            this.bucketRegions.clear();
            this.state.set(ShellConnState.CLOSED);
            this.removeStateListener(this.stateListener);
        } catch (Exception ex) {
            JulLog.warn("S3 V2 client close error", ex);
        }
    }

    @Override
    public void lsFileDynamic(String filePath, Consumer<ShellS3File> fileCallback) {
        try {
            ShellClientActionUtil.forAction(this.connectName(), "ls " + filePath);
            if (StringUtil.equalsAny(filePath, "/", "")) {
                for (ListAllMyBucketsResult.Bucket bucket : this.rawBuckets()) {
                    String bucketName = bucket.name();
                    this.fillAppId(bucketName);
                    fileCallback.accept(ShellS3File.ofBucket(bucketName));
                }
                return;
            }
            ShellS3Path path = ShellS3Path.of(filePath);
            for (Item item : this.listItems(path.bucketName(), path.prefix(), false, false)) {
                if (item.objectName().equals(path.prefix())) {
                    continue;
                }
                fileCallback.accept(this.toFile(path.bucketName(), item));
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void delete(String file) throws Exception {
        ShellClientActionUtil.forAction(this.connectName(), "rm " + file);
        ShellS3Path path = ShellS3Path.of(file);
        String key = path.filePath();
        String bucketName = path.bucketName();
        this.removeObjects(bucketName, key, false, this.isBucketVersioning(bucketName));
        //        if (!this.connect.isMinioS3Type()) {
        //            this.awaitObjectDeleted(bucketName, key);
        //        }
    }

    @Override
    public void deleteDir(String dir) {
        try {
            ShellClientActionUtil.forAction(this.connectName(), "rmdir " + dir);
            ShellS3Path path = ShellS3Path.of(dir);
            String prefix = path.prefix();
            this.removeObjects(path.bucketName(), prefix, true, this.isBucketVersioning(path.bucketName()));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void deleteDirRecursive(String dir) {
        this.deleteDir(dir);
    }

    @Override
    public boolean rename(ShellS3File file, String newName) throws Exception {
        String oldPath = file.getFilePath();
        int slash = oldPath.lastIndexOf("/");
        String newPath = slash < 0 ? "/" + newName : oldPath.substring(0, slash + 1) + newName;
        ShellClientActionUtil.forAction(this.connectName(), "rename " + oldPath + " " + newPath);
        ShellS3Path oldS3Path = ShellS3Path.of(oldPath);
        ShellS3Path newS3Path = ShellS3Path.of(newPath);
        if (file.isDirectory()) {
            String oldPrefix = ShellS3Util.toPrefix(file.getFileKey());
            String newPrefix = ShellS3Util.toPrefix(newS3Path.filePath());
            for (Item item : this.listItems(oldS3Path.bucketName(), oldPrefix, true, false)) {
                String sourceKey = item.objectName();
                String destinationKey = newPrefix + sourceKey.substring(oldPrefix.length());
                try {
                    this.copyObject(oldS3Path.bucketName(), sourceKey, newS3Path.bucketName(), destinationKey);
                } catch (Exception ex) {
                    if (!this.isNotFound(ex)) {
                        throw ex;
                    }
                }
            }
        } else {
            this.copyObject(oldS3Path.bucketName(), file.getFileKey(), newS3Path.bucketName(), newS3Path.filePath());
        }
        this.removeObjects(oldS3Path.bucketName(),
                file.isDirectory() ? ShellS3Util.toPrefix(file.getFileKey()) : file.getFileKey(),
                file.isDirectory(), this.isBucketVersioning(oldS3Path.bucketName()));
        return true;
    }

    @Override
    public boolean exist(String filePath) throws Exception {
        if ("/".equals(filePath)) {
            return true;
        }
        ShellClientActionUtil.forAction(this.connectName(), "exist " + filePath);
        ShellS3Path path = ShellS3Path.of(filePath);
        String bucketName = path.bucketName();
        if (StringUtil.isBlank(bucketName)) {
            return false;
        }
        String key = path.filePath();
        if ("/".equals(key)) {
            return this.withBucketClient(bucketName, client -> client.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build()));
        }
        if (filePath.endsWith("/")) {
            return this.existsDirectory(bucketName, key);
        }
        try {
            this.withBucketClient(bucketName, client -> client.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(key)
                    .build()));
            return true;
        } catch (ErrorResponseException ex) {
            if (!this.isNotFound(ex)) {
                throw ex;
            }
        }
        return this.existsDirectory(bucketName, key);
    }

    @Override
    public String realpath(String filePath) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void touch(String filePath) throws Exception {
        ShellClientActionUtil.forAction(this.connectName(), "touch " + filePath);
        ShellS3Path path = ShellS3Path.of(filePath);
        this.withBucketClient(path.bucketName(), client -> client.putObject(PutObjectArgs.builder()
                .bucket(path.bucketName())
                .object(path.filePath())
                .stream(new ByteArrayInputStream(new byte[0]), 0L, null)
                .build()));
    }

    @Override
    public boolean createDir(String filePath) throws Exception {
        ShellClientActionUtil.forAction(this.connectName(), "mkdir " + filePath);
        ShellS3Path path = ShellS3Path.of(filePath);
        String key = path.filePath();
        if (!key.endsWith("/")) {
            key += "/";
        }
        final String objectKey = key;
        this.withBucketClient(path.bucketName(), client -> client.putObject(PutObjectArgs.builder()
                .bucket(path.bucketName())
                .object(objectKey)
                .stream(new ByteArrayInputStream(new byte[0]), 0L, null)
                .build()));
        return true;
    }

    @Override
    public void createDirRecursive(String filePath) throws Exception {
        filePath = ShellFileUtil.fixFilePath(filePath);
        int bucketSlash = filePath.indexOf("/", 1);
        if (bucketSlash < 0) {
            return;
        }
        String bucket = filePath.substring(1, bucketSlash);
        String prefix = filePath.substring(bucketSlash + 1);
        if (!prefix.endsWith("/")) {
            prefix += "/";
        }
        int index = 0;
        while ((index = prefix.indexOf("/", index + 1)) >= 0) {
            String current = prefix.substring(0, index + 1);
            if (!this.existsDirectory(bucket, current)) {
                this.createDir("/" + bucket + "/" + current);
            }
        }
    }

    @Override
    public String workDir() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void cd(String filePath) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void get(ShellS3File remoteFile, String localFile, Function<Long, Boolean> callback) throws Exception {
        ShellClientActionUtil.forAction(this.connectName(), "get " + remoteFile.getFilePath());
        try (InputStream input = this.getStream(remoteFile, callback);
             OutputStream output = new FileOutputStream(localFile)) {
            input.transferTo(output);
        }
    }

    @Override
    public InputStream getStream(ShellS3File remoteFile, Function<Long, Boolean> callback) throws IOException {
        ShellClientActionUtil.forAction(this.connectName(), "get " + remoteFile.getFilePath());
        try {
            GetObjectResponse response = this.withBucketClient(remoteFile.getBucketName(),
                    client -> client.getObject(GetObjectArgs.builder()
                            .bucket(remoteFile.getBucketName())
                            .object(remoteFile.getFileKey())
                            .build()));
            return callback == null ? response : ShellFileProgressMonitor.of(response, callback);
        } catch (Exception ex) {
            if (ex instanceof IOException ioException) {
                throw ioException;
            }
            throw new IOException(ex);
        }
    }

    @Override
    public void put(InputStream localFile, String remoteFile, Function<Long, Boolean> callback) throws Exception {
        ShellClientActionUtil.forAction(this.connectName(), "put " + remoteFile);
        ShellS3Path path = ShellS3Path.of(remoteFile);
        InputStream input = callback == null ? localFile : ShellFileProgressMonitor.of(localFile, callback);
        try {
            this.withBucketClient(path.bucketName(), client -> client.putObject(PutObjectArgs.builder()
                    .bucket(path.bucketName())
                    .object(path.filePath())
                    .stream(input, null, DEFAULT_UPLOAD_PART_SIZE)
                    .build()));
        } finally {
            IOUtil.close(input);
        }
    }

    @Override
    public OutputStream putStream(String remoteFile, Function<Long, Boolean> callback) throws Exception {
        ShellClientActionUtil.forAction(this.connectName(), "put " + remoteFile);
        return new DeferredUploadOutputStream(this, remoteFile, callback);
    }

    private void copyObject(String sourceBucket, String sourceKey,
                            String destinationBucket, String destinationKey) throws Exception {
        this.withBucketClient(destinationBucket, client -> client.copyObject(CopyObjectArgs.builder()
                .source(SourceObject.builder()
                        .bucket(sourceBucket)
                        .object(sourceKey)
                        .build())
                .bucket(destinationBucket)
                .object(destinationKey)
                .build()));
    }

    private List<Item> listItems(String bucketName, String prefix,
                                 boolean recursive, boolean includeVersions) throws Exception {
        List<Item> items = new ArrayList<>();
        this.withBucketClient(bucketName, client -> {
            Iterable<Result<Item>> results = client.listObjects(ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .prefix(prefix == null ? "" : prefix)
                    .recursive(recursive)
                    .includeVersions(includeVersions)
                    .build());
            for (Result<Item> result : results) {
                items.add(result.get());
            }
            return null;
        });
        return items;
    }

    private void removeObjects(String bucketName, String prefix,
                               boolean recursive, boolean includeVersions) throws Exception {
        List<DeleteTarget> objects = new ArrayList<>();
        boolean prefixMarkerFound = false;
        List<Item> removeItems = this.listItems(bucketName, prefix, recursive, includeVersions);
        for (Item item : removeItems) {
            String key = item.objectName();
            if (recursive && prefix != null && !prefix.isEmpty() && !key.startsWith(prefix)) {
                continue;
            }
            if (!recursive && prefix != null && !prefix.isEmpty() && !key.equals(prefix)) {
                continue;
            }
            if (recursive && key.equals(prefix)) {
                prefixMarkerFound = true;
            }
            objects.add(new DeleteTarget(key, includeVersions ? item.versionId() : null));
            if (objects.size() >= 1000) {
                this.removeObjectBatch(bucketName, objects, includeVersions);
                objects.clear();
            }
        }
        if (recursive && prefix != null && prefix.endsWith("/") && !prefixMarkerFound) {
            objects.add(new DeleteTarget(prefix, null));
        }
        this.removeObjectBatch(bucketName, objects, includeVersions);
    }

    private void removeObjectBatch(String bucketName, List<DeleteTarget> targets,
                                   boolean purgeVersions) throws Exception {
        if (targets.isEmpty()) {
            return;
        }
        if (this.connect.isMinioS3Type()) {
            for (DeleteTarget target : targets) {
                this.removeRustfsObject(bucketName, target, purgeVersions);
            }
            return;
        }
        List<DeleteRequest.Object> objects = new ArrayList<>(targets.size());
        for (DeleteTarget target : targets) {
            objects.add(target.versionId == null
                    ? new DeleteRequest.Object(target.key)
                    : new DeleteRequest.Object(target.key, target.versionId));
        }
        this.withBucketClient(bucketName, client -> {
            Iterable<Result<DeleteResult.Error>> errors = client.removeObjects(RemoveObjectsArgs.builder()
                    .bucket(bucketName)
                    .bypassGovernanceMode(true)
                    .objects(objects)
                    .build());
            for (Result<DeleteResult.Error> error : errors) {
                DeleteResult.Error deleteError = error.get();
                throw new IOException("删除对象失败: " + deleteError.objectName() + " - " + deleteError.message());
            }
            return null;
        });
    }

    private void removeRustfsObject(String bucketName, DeleteTarget target,
                                    boolean purgeVersions) throws Exception {
        this.withBucketClient(bucketName, purgeVersions, client -> {
            RemoveObjectArgs.Builder builder = RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(target.key)
                    .bypassGovernanceMode(true);
            if (target.versionId != null) {
                builder.versionId(target.versionId);
            }
            client.removeObject(builder.build());
            return null;
        });
    }

    //    private void awaitObjectDeleted(String bucketName, String key) throws Exception {
    //        for (int index = 0; index < 40; index++) {
    //            try {
    //                this.withBucketClient(bucketName, client -> client.statObject(StatObjectArgs.builder()
    //                        .bucket(bucketName)
    //                        .object(key)
    //                        .build()));
    //            } catch (ErrorResponseException ex) {
    //                if (this.isNotFound(ex)) {
    //                    return;
    //                }
    //                throw ex;
    //            }
    //            Thread.sleep(50L);
    //        }
    //        throw new IOException("对象删除后仍存在: " + key);
    //    }

    private static final class DeleteTarget {
        private final String key;
        private final String versionId;

        private DeleteTarget(String key, String versionId) {
            this.key = key;
            this.versionId = versionId;
        }
    }

    private boolean existsDirectory(String bucketName, String key) throws Exception {
        String prefix = ShellS3Util.toPrefix(key);
        try {
            this.withBucketClient(bucketName, client -> client.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(prefix)
                    .build()));
            return true;
        } catch (ErrorResponseException ex) {
            if (!this.isNotFound(ex)) {
                throw ex;
            }
        }
        List<Item> directoryItems = this.listItems(bucketName, prefix, true, false);
        for (Item item : directoryItems) {
            if (!item.objectName().equals(prefix) && item.objectName().startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private boolean isDirectoryKey(String bucketName, String key) throws Exception {
        try {
            StatObjectResponse response = this.withBucketClient(bucketName,
                    client -> client.statObject(StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(key.endsWith("/") ? key : key + "/")
                            .build()));
            return response != null;
        } catch (ErrorResponseException ex) {
            if (!this.isNotFound(ex)) {
                throw ex;
            }
            return false;
        }
    }

    private boolean isNotFound(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            if (current instanceof ErrorResponseException error) {
                String code = error.errorResponse() == null ? null : error.errorResponse().code();
                int status = error.response() == null ? 0 : error.response().code();
                return status == 404 || StringUtil.equalsAny(code,
                        "NoSuchKey", "NoSuchBucket", "ResourceNotFound",
                        "ObjectLockConfigurationNotFoundError", "NoSuchLifecycleConfiguration");
            }
            current = current.getCause();
        }
        return false;
    }

    private ShellS3File toFile(String bucketName, Item item) {
        String key = item.objectName();
        boolean directory = item.isDir() || key.endsWith("/");
        String normalized = directory && key.endsWith("/") ? key.substring(0, key.length() - 1) : key;
        int index = normalized.lastIndexOf("/");
        String parentPath = index < 0 ? "/" : "/" + normalized.substring(0, index);
        String fileName = index < 0 ? normalized : normalized.substring(index + 1);
        Instant modified = item.lastModified() == null ? null : item.lastModified().toInstant();
        return new ShellS3File(bucketName, parentPath, fileName, directory, item.size(), modified);
    }

    private ShellS3File toFile(String bucketName, String key, Instant modified, Long size) {
        boolean directory = key.endsWith("/");
        String normalized = directory ? key.substring(0, key.length() - 1) : key;
        int index = normalized.lastIndexOf("/");
        String parentPath = index < 0 ? "/" : "/" + normalized.substring(0, index);
        String fileName = index < 0 ? normalized : normalized.substring(index + 1);
        return new ShellS3File(bucketName, parentPath, fileName, directory, size, modified);
    }

    @Override
    public Competitor deleteCompetitor() {
        return this.deleteCompetitor;
    }

    @Override
    public ObservableList<ShellFileDeleteTask> deleteTasks() {
        return this.deleteTasks;
    }

    @Override
    public Competitor uploadCompetitor() {
        return this.uploadCompetitor;
    }

    @Override
    public ObservableList<ShellFileUploadTask> uploadTasks() {
        return this.uploadTasks;
    }

    @Override
    public Competitor downloadCompetitor() {
        return this.downloadCompetitor;
    }

    @Override
    public ObservableList<ShellFileDownloadTask> downloadTasks() {
        return this.downloadTasks;
    }

    @Override
    public Competitor transportCompetitor() {
        return this.transportCompetitor;
    }

    @Override
    public ObservableList<ShellFileTransportTask> transportTasks() {
        return this.transportTasks;
    }

    @Override
    public void closeDelayResources() {
    }

    @Override
    public boolean chmod(int permissions, String filePath) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ShellS3File fileInfo(String filePath) throws Exception {
        ShellClientActionUtil.forAction(this.connectName(), "fileInfo " + filePath);
        if ("/".equals(filePath)) {
            return null;
        }
        ShellS3Path path = ShellS3Path.of(filePath);
        String bucketName = path.bucketName();
        if (StringUtil.isBlank(bucketName)) {
            return null;
        }
        String key = path.filePath();
        if ("/".equals(key)) {
            return ShellS3File.ofBucket(bucketName);
        }
        try {
            StatObjectResponse response = this.withBucketClient(bucketName,
                    client -> client.statObject(StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(key)
                            .build()));
            Instant modified = response.lastModified() == null ? null : response.lastModified().toInstant();
            return this.toFile(bucketName, key, modified, response.size());
        } catch (ErrorResponseException ex) {
            if (!this.isNotFound(ex)) {
                throw ex;
            }
        }
        String prefix = ShellS3Util.toPrefix(key);
        try {
            StatObjectResponse marker = this.withBucketClient(bucketName,
                    client -> client.statObject(StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(prefix)
                            .build()));
            Instant modified = marker.lastModified() == null ? null : marker.lastModified().toInstant();
            return this.toFile(bucketName, prefix, modified, marker.size());
        } catch (ErrorResponseException ex) {
            if (!this.isNotFound(ex)) {
                throw ex;
            }
        }
        for (Item item : this.listItems(bucketName, prefix, true, false)) {
            if (!item.objectName().equals(prefix) && item.objectName().startsWith(prefix)) {
                return this.toFile(bucketName, key + "/", null, 0L);
            }
        }
        return null;
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
        return true;
    }

    @Override
    public boolean isCreateDirSupport() {
        return true;
    }

    @Override
    public boolean isCreateDirRecursiveSupport() {
        return true;
    }

    private List<ListAllMyBucketsResult.Bucket> rawBuckets() throws Exception {
        List<ListAllMyBucketsResult.Bucket> buckets = this.minioClient.listBuckets();
        for (ListAllMyBucketsResult.Bucket bucket : buckets) {
            if (StringUtil.isNotBlank(bucket.bucketRegion())) {
                this.bucketRegions.put(bucket.name(), bucket.bucketRegion());
            }
        }
        return buckets;
    }

    /**
     * 列举桶。
     *
     * @return 桶列表
     */
    public List<ShellS3Bucket> listBuckets() {
        try {
            List<ShellS3Bucket> result = new ArrayList<>();
            for (ListAllMyBucketsResult.Bucket bucket : this.rawBuckets()) {
                String bucketName = bucket.name();
                this.fillAppId(bucketName);
                ShellS3Bucket item = new ShellS3Bucket();
                item.setName(bucketName);
                item.setRegion(StringUtil.isBlank(bucket.bucketRegion()) ? this.regionId() : bucket.bucketRegion());
                if (bucket.creationDate() != null) {
                    item.setCreationDate(bucket.creationDate().toInstant());
                }
                item.setRetention(this.getBucketRetention(bucket.name()));
                item.setVersioning(this.isBucketVersioning(bucket.name()));
                item.setObjectLock(this.isBucketObjectLock(bucket.name()));
                result.add(item);
            }
            return result;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 按需加载桶配置。
     *
     * <p>编辑桶时重新加载版本控制、对象锁定和保留策略，避免使用列表中的旧值。</p>
     */
    public void fillBucketMetadata(ShellS3Bucket bucket) {
        bucket.setRetention(this.getBucketRetention(bucket.getName()));
        bucket.setVersioning(this.isBucketVersioning(bucket.getName()));
        bucket.setObjectLock(this.isBucketObjectLock(bucket.getName()));
    }

    /**
     * 获取桶。
     *
     * @param bucketName 桶名称
     * @return 桶
     */
    public ShellS3Bucket getBucket(String bucketName) {
        try {
            this.fillAppId(bucketName);
            for (ListAllMyBucketsResult.Bucket bucket : this.rawBuckets()) {
                if (bucket.name().equals(bucketName)) {
                    String region = StringUtil.isBlank(bucket.bucketRegion())
                            ? this.regionId() : bucket.bucketRegion();
                    ShellS3Bucket result = new ShellS3Bucket();
                    result.setName(bucket.name());
                    result.setRegion(region);
                    if (bucket.creationDate() != null) {
                        result.setCreationDate(bucket.creationDate().toInstant());
                    }
                    return result;
                }
            }
            return null;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private String getAppId() throws Exception {
        if (this.appId == null) {
            this.appId = ShellS3Util.getAppId(this.connect.getUser(), this.connect.getPassword());
        }
        return this.appId;
    }

    private void fillAppId(String bucketName) {
        if (this.appId != null || !this.connect.isTencentS3Type()) {
            return;
        }
        int index = bucketName.lastIndexOf("-");
        if (index >= 0) {
            this.appId = bucketName.substring(index + 1);
        }
    }

    /**
     * 创建桶。
     *
     * @param bucket 桶对象
     */
    public void createBucket(ShellS3Bucket bucket) throws Exception {
        String bucketName = bucket.getName();
        if (this.connect.isTencentS3Type()) {
            bucketName += "-" + this.getAppId();
            bucket.setName(bucketName);
        }
        final String targetBucketName = bucketName;
        this.withBucketClient(bucketName, client -> {
            client.makeBucket(MakeBucketArgs.builder()
                    .bucket(targetBucketName)
                    .region(this.regionId())
                    .objectLock(bucket.isObjectLock())
                    .build());
            return null;
        });
        if (bucket.isVersioning()) {
            this.setBucketVersioning(bucketName, true);
        }
        if (bucket.isRetention()) {
            ShellS3RetentionMode mode = ShellS3RetentionMode.ofIndex(bucket.getRetentionMode());
            if (bucket.getRetentionValidityType() == 0) {
                this.setBucketRetentionByDays(bucketName, bucket.getRetentionValidity(), mode);
            } else {
                this.setBucketRetentionByYears(bucketName, bucket.getRetentionValidity(), mode);
            }
        }
    }

    /**
     * 修改桶。
     *
     * @param bucket 桶对象
     */
    public void updateBucket(ShellS3Bucket bucket) {
        try {
            this.setBucketVersioning(bucket.getName(), bucket.isVersioning());
            if (bucket.isRetention()) {
                ShellS3RetentionMode mode = ShellS3RetentionMode.ofIndex(bucket.getRetentionMode());
                if (bucket.getRetentionValidityType() == 0) {
                    this.setBucketRetentionByDays(bucket.getName(), bucket.getRetentionValidity(), mode);
                } else {
                    this.setBucketRetentionByYears(bucket.getName(), bucket.getRetentionValidity(), mode);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 删除桶。
     *
     * @param bucket 桶对象
     * @param force  是否强制删除
     */
    public void deleteBucket(ShellS3Bucket bucket, boolean force) {
        try {
            Exception lastError = null;
            for (int index = 0; index < 5; index++) {
                try {
                    if (force) {
                        this.removeObjects(bucket.getName(), "", true, true);
                    }
                    this.withBucketClient(bucket.getName(), force, client -> {
                        client.removeBucket(RemoveBucketArgs.builder()
                                .bucket(bucket.getName())
                                .build());
                        return null;
                    });
                    return;
                } catch (Exception ex) {
                    lastError = ex;
                    Thread.sleep(100L);
                }
            }
            throw lastError;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 判断桶是否启用版本控制。
     */
    public boolean isBucketVersioning(String bucketName) {
        try {
            return this.withBucketClient(bucketName, client -> client.getBucketVersioning(
                            io.minio.GetBucketVersioningArgs.builder().bucket(bucketName).build()))
                    .status() == VersioningConfiguration.Status.ENABLED;
        } catch (Exception ex) {
            if (!this.isUnsupported(ex)) {
                JulLog.warn("Get bucket versioning error", ex);
            }
            return false;
        }
    }

    /**
     * 设置桶版本控制。
     */
    public void setBucketVersioning(String bucketName, boolean enable) {
        try {
            boolean current = this.isBucketVersioning(bucketName);
            if (current == enable) {
                return;
            }
            this.withBucketClient(bucketName, client -> {
                client.setBucketVersioning(SetBucketVersioningArgs.builder()
                        .bucket(bucketName)
                        .config(new VersioningConfiguration(
                                enable ? VersioningConfiguration.Status.ENABLED
                                        : VersioningConfiguration.Status.SUSPENDED,
                                null, null, null))
                        .build());
                return null;
            });
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 判断桶是否启用对象锁定。
     */
    public boolean isBucketObjectLock(String bucketName) {
        try {
            if (this.connect.isAlibabaS3Type()) {
                return this.callAliyunObjectWorm(bucketName, "GET", null) != null;
            }
            ObjectLockConfiguration configuration = this.withBucketClient(bucketName,
                    client -> client.getObjectLockConfiguration(
                            io.minio.GetObjectLockConfigurationArgs.builder().bucket(bucketName).build()));
            return configuration != null;
        } catch (Exception ex) {
            if (!this.isUnsupported(ex) && !this.isNotFound(ex)) {
                JulLog.warn("Get bucket object lock error", ex);
            }
            return false;
        }
    }

    /**
     * 开启桶对象锁定。
     */
    public void enableBucketObjectLocking(String bucketName) {
        try {
            if (this.connect.isAlibabaS3Type()) {
                this.callAliyunObjectWorm(bucketName, "PUT",
                        this.aliyunObjectWormXml(null, null, null));
                return;
            }
            this.withBucketClient(bucketName, client -> {
                client.setObjectLockConfiguration(SetObjectLockConfigurationArgs.builder()
                        .bucket(bucketName)
                        .config(new ObjectLockConfiguration())
                        .build());
                return null;
            });
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 设置默认按天保留模式。
     */
    public void setBucketRetentionByDays(String bucketName, int days, ShellS3RetentionMode mode) {
        if (days < 1 || days > 36500) {
            throw new IllegalArgumentException("保留天数必须在 1-36500 之间");
        }
        this.setBucketRetention(bucketName, mode, new RetentionDurationDays(days), RetentionDurationUnit.DAYS);
    }

    /**
     * 设置默认按年保留模式。
     */
    public void setBucketRetentionByYears(String bucketName, int years, ShellS3RetentionMode mode) {
        if (years < 1 || years > 100) {
            throw new IllegalArgumentException("保留年数必须在 1-100 之间");
        }
        this.setBucketRetention(bucketName, mode,
                new RetentionDurationYears(years), RetentionDurationUnit.YEARS);
    }

    private void setBucketRetention(String bucketName, ShellS3RetentionMode mode,
                                    RetentionDuration duration, RetentionDurationUnit unit) {
        try {
            RetentionMode retentionMode = mode == ShellS3RetentionMode.COMPLIANCE
                    ? RetentionMode.COMPLIANCE
                    : RetentionMode.GOVERNANCE;
            RetentionDuration requestDuration = duration;
            RetentionDurationUnit requestUnit = unit;
            if ((this.connect.isTencentS3Type() || this.connect.isHuaweiS3Type()) && unit == RetentionDurationUnit.YEARS) {
                requestDuration = new RetentionDurationDays(Math.multiplyExact(duration.duration(), 365));
                requestUnit = RetentionDurationUnit.DAYS;
            }
            final RetentionDuration normalizedDuration = requestDuration;
            final RetentionDurationUnit normalizedUnit = requestUnit;
            if (this.connect.isAlibabaS3Type()) {
                this.callAliyunObjectWorm(bucketName, "PUT", this.aliyunObjectWormXml(retentionMode, normalizedDuration, normalizedUnit));
                return;
            }
            this.withBucketClient(bucketName, client -> {
                client.setObjectLockConfiguration(SetObjectLockConfigurationArgs.builder()
                        .bucket(bucketName)
                        .config(new ObjectLockConfiguration(retentionMode,
                                normalizedUnit == RetentionDurationUnit.DAYS
                                        ? new RetentionDurationDays(normalizedDuration.duration())
                                        : new RetentionDurationYears(normalizedDuration.duration())))
                        .build());
                return null;
            });
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private String aliyunObjectWormXml(RetentionMode mode, RetentionDuration duration,
                                       RetentionDurationUnit unit) {
        StringBuilder xml = new StringBuilder(192);
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .append("<ObjectWormConfiguration><ObjectWormEnabled>Enabled</ObjectWormEnabled>");
        if (duration != null) {
            xml.append("<Rule><DefaultRetention><Mode>").append(mode.name()).append("</Mode>");
            if (unit == RetentionDurationUnit.DAYS) {
                xml.append("<Days>").append(duration.duration()).append("</Days>");
            } else {
                xml.append("<Years>").append(duration.duration()).append("</Years>");
            }
            xml.append("</DefaultRetention></Rule>");
        }
        return xml.append("</ObjectWormConfiguration>").toString();
    }

    private String callAliyunObjectWorm(String bucketName, String method, String body) throws Exception {
        String date = DateTimeFormatter
                .ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US)
                .format(ZonedDateTime.now(ZoneOffset.UTC));
        byte[] bodyBytes = body == null ? new byte[0] : body.getBytes(StandardCharsets.UTF_8);
        String contentMd5 = body == null ? "" : Base64.getEncoder().encodeToString(
                MessageDigest.getInstance("MD5").digest(bodyBytes));
        String contentType = body == null ? "" : "application/xml";
        String canonical = method + "\n" + contentMd5 + "\n" + contentType + "\n" + date
                + "\n/" + bucketName + "/?objectWorm";
        String signature = Base64.getEncoder().encodeToString(this.hmacSha1(
                this.connect.getPassword().getBytes(StandardCharsets.UTF_8),
                canonical.getBytes(StandardCharsets.UTF_8)));
        Request.Builder request = new Request.Builder()
                .url(this.aliyunObjectWormUrl(bucketName))
                .header("Date", date)
                .header("Authorization", "OSS " + this.connect.getUser() + ":" + signature);
        if (body != null) {
            request.header("Content-MD5", contentMd5)
                    .header("Content-Type", contentType);
        }
        request.method(method, body == null ? null : RequestBody.create(bodyBytes, MediaType.get(contentType)));
        try (Response response = this.httpClient.newCall(request.build()).execute()) {
            String responseText = response.body() == null ? "" : response.body().string();
            if (!response.isSuccessful()) {
                if ("GET".equalsIgnoreCase(method) && response.code() == 404) {
                    return null;
                }
                throw new IOException("阿里云 ObjectWorm 请求失败: HTTP " + response.code()
                        + " " + responseText);
            }
            return responseText;
        }
    }

    private String getHuaweiBucketLocation(String bucketName) throws Exception {
        String date = HTTP_DATE_FORMATTER.format(ZonedDateTime.now(ZoneOffset.UTC));
        String stringToSign = "GET\n\n\n" + date + "\n/" + bucketName + "/?location";
        String signature = Base64.getEncoder().encodeToString(this.hmacSha1(
                this.connect.getPassword().getBytes(StandardCharsets.UTF_8),
                stringToSign.getBytes(StandardCharsets.UTF_8)));
        Request request = new Request.Builder()
                .url(this.huaweiBucketLocationUrl(bucketName))
                .header("Date", date)
                .header("Authorization", "OBS " + this.connect.getUser() + ":" + signature)
                .get()
                .build();
        try (Response response = this.httpClient.newCall(request).execute()) {
            String responseText = response.body() == null ? "" : response.body().string();
            if (!response.isSuccessful()) {
                if (response.code() == 404) {
                    return null;
                }
                throw new IOException("华为云 OBS 获取桶区域失败: HTTP " + response.code()
                        + " " + responseText);
            }
            return this.parseHuaweiBucketLocation(responseText);
        }
    }

    private HttpUrl huaweiBucketLocationUrl(String bucketName) {
        HttpUrl endpoint = HttpUrl.get(this.endpointForRegion(this.regionId()));
        HttpUrl.Builder builder = endpoint.newBuilder().encodedQuery("location");
        if (this.isLoopbackHost(endpoint.host())) {
            return builder.addPathSegment(bucketName).addPathSegment("").build();
        }
        String host = endpoint.host();
        if (!host.startsWith(bucketName + ".")) {
            host = bucketName + "." + host;
        }
        return builder.host(host).build();
    }

    private String parseHuaweiBucketLocation(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
        org.w3c.dom.Node location = document.getElementsByTagName("Location").item(0);
        if (location == null) {
            location = document.getElementsByTagName("LocationConstraint").item(0);
        }
        if (location == null) {
            return null;
        }
        String region = location.getTextContent().trim();
        return region.isEmpty() ? null : region;
    }

    private boolean isLoopbackHost(String host) {
        String lowerHost = host.toLowerCase(Locale.ROOT);
        return "localhost".equals(lowerHost)
                || lowerHost.endsWith(".localhost")
                || lowerHost.startsWith("127.")
                || "::1".equals(lowerHost)
                || "[::1]".equals(lowerHost);
    }

    private HttpUrl aliyunObjectWormUrl(String bucketName) {
        String region = this.bucketRegions.getOrDefault(bucketName, this.regionId());
        HttpUrl endpoint = HttpUrl.get(this.endpointForRegion(region));
        String host = endpoint.host();
        if (!host.startsWith(bucketName + ".")) {
            host = bucketName + "." + host;
        }
        return endpoint.newBuilder().host(host).encodedQuery("objectWorm").build();
    }

    private byte[] hmacSha1(byte[] key, byte[] data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(key, "HmacSHA1"));
        return mac.doFinal(data);
    }

    private ZonedDateTime parseObjectLockDate(String value) {
        try {
            return ZonedDateTime.parse(value, DateTimeFormatter.ISO_ZONED_DATE_TIME);
        } catch (Exception ex) {
            return ZonedDateTime.parse(value, HTTP_DATE_FORMATTER);
        }
    }

    private ShellS3Retention parseAliyunObjectWorm(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
        String enabled = document.getElementsByTagName("ObjectWormEnabled").item(0).getTextContent();
        if (!"Enabled".equals(enabled)) {
            return null;
        }
        if (document.getElementsByTagName("DefaultRetention").getLength() == 0) {
            return null;
        }
        String mode = document.getElementsByTagName("Mode").item(0).getTextContent();
        ShellS3RetentionMode retentionMode = "COMPLIANCE".equals(mode)
                ? ShellS3RetentionMode.COMPLIANCE : ShellS3RetentionMode.GOVERNANCE;
        if (document.getElementsByTagName("Days").getLength() > 0) {
            return ShellS3Retention.ofDays(retentionMode,
                    Integer.parseInt(document.getElementsByTagName("Days").item(0).getTextContent()));
        }
        return ShellS3Retention.ofYears(retentionMode,
                Integer.parseInt(document.getElementsByTagName("Years").item(0).getTextContent()));
    }

    /**
     * 获取 Bucket 的默认保留规则。
     */
    public ShellS3Retention getBucketRetention(String bucketName) {
        try {
            if (this.connect.isAlibabaS3Type()) {
                String xml = this.callAliyunObjectWorm(bucketName, "GET", null);
                return xml == null ? null : this.parseAliyunObjectWorm(xml);
            }
            ObjectLockConfiguration configuration = this.withBucketClient(bucketName,
                    client -> client.getObjectLockConfiguration(
                            io.minio.GetObjectLockConfigurationArgs.builder().bucket(bucketName).build()));
            if (configuration == null || configuration.mode() == null || configuration.duration() == null) {
                return null;
            }
            RetentionDuration duration = configuration.duration();
            ShellS3RetentionMode mode = configuration.mode() == RetentionMode.COMPLIANCE
                    ? ShellS3RetentionMode.COMPLIANCE : ShellS3RetentionMode.GOVERNANCE;
            if (duration.unit() == RetentionDurationUnit.DAYS) {
                return ShellS3Retention.ofDays(mode, duration.duration());
            }
            return ShellS3Retention.ofYears(mode, duration.duration());
        } catch (Exception ex) {
            if (!this.isUnsupported(ex) && !this.isNotFound(ex)) {
                JulLog.warn("Get bucket retention error", ex);
            }
            return null;
        }
    }

    /**
     * 创建带签名的访问地址。
     */
    public String generatePresignedUrl(String bucketName, String key, Duration duration) {
        ShellClientActionUtil.forAction(this.connectName(), "generatePresignedUrl " + key);
        try {
            long seconds = duration == null ? 600 : Math.max(1, duration.toSeconds());
            final int expirySeconds = (int) Math.min(seconds, TimeUnit.DAYS.toSeconds(7));
            return this.withBucketClient(bucketName,
                    client -> client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                            .method(Http.Method.GET)
                            .bucket(bucketName)
                            .object(ShellS3Util.parseFileKey(key))
                            .expiry(expirySeconds)
                            .build()));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private boolean isUnsupported(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            if (current instanceof XmlParserException) {
                return true;
            }
            if (current instanceof ErrorResponseException error) {
                String code = error.errorResponse() == null ? null : error.errorResponse().code();
                int status = error.response() == null ? 0 : error.response().code();
                return status == 403 || status == 501 || StringUtil.equalsAny(code,
                        "NotImplemented", "MethodNotAllowed", "ObjectLockConfigurationNotFoundError",
                        "NoSuchLifecycleConfiguration");
            }
            current = current.getCause();
        }
        return false;
    }

    private static final class DeferredUploadOutputStream extends OutputStream {

        private final ShellS3ClientV2 client;

        private final String remoteFile;

        private final Function<Long, Boolean> callback;

        private final Path tempFile;

        private final OutputStream output;

        private boolean closed;

        private DeferredUploadOutputStream(ShellS3ClientV2 client, String remoteFile,
                                           Function<Long, Boolean> callback) throws IOException {
            this.client = client;
            this.remoteFile = remoteFile;
            this.callback = callback;
            this.tempFile = Files.createTempFile("easyshell-s3-upload-", ".tmp");
            this.output = new FileOutputStream(this.tempFile.toFile());
        }

        @Override
        public void write(int value) throws IOException {
            this.output.write(value);
        }

        @Override
        public void write(byte[] bytes, int offset, int length) throws IOException {
            this.output.write(bytes, offset, length);
        }

        @Override
        public void flush() throws IOException {
            this.output.flush();
        }

        @Override
        public void close() throws IOException {
            if (this.closed) {
                return;
            }
            this.closed = true;
            this.output.close();
            try (InputStream input = new FileInputStream(this.tempFile.toFile())) {
                this.client.put(input, this.remoteFile, this.callback);
            } catch (Exception ex) {
                throw new IOException(ex);
            } finally {
                Files.deleteIfExists(this.tempFile);
            }
        }
    }
}
