package cn.oyzh.easyshell.test.s3;

import com.sun.net.httpserver.HttpServer;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.s3.ShellS3Bucket;
import cn.oyzh.easyshell.s3.ShellS3ClientV2;
import cn.oyzh.easyshell.s3.ShellS3File;
import cn.oyzh.easyshell.s3.ShellS3Retention;
import cn.oyzh.easyshell.s3.ShellS3RetentionMode;
import org.junit.Assume;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * ShellS3ClientV2 集成测试。
 *
 * <p>测试默认跳过；通过 EASY_SHELL_RUSTFS_* 或 EASY_SHELL_ALIYUN_* 环境变量提供
 * 临时测试凭据即可运行。测试会在存储中创建唯一桶并在 finally 中清理。</p>
 */
public class ShellS3ClientV2IntegrationTest {

    @Test
    public void testRustfs() throws Exception {
        this.run("EASY_SHELL_RUSTFS_", "minio", "us-east-1");
    }

    @Test
    public void testAliyun() throws Exception {
        this.run("EASY_SHELL_ALIYUN_", "alibaba", "oss-cn-beijing");
    }

    @Test
    public void testAliyunBucketRetention() throws Exception {
        String endpoint = System.getenv("EASY_SHELL_ALIYUN_ENDPOINT");
        String accessKey = System.getenv("EASY_SHELL_ALIYUN_ACCESS_KEY");
        String secretKey = System.getenv("EASY_SHELL_ALIYUN_SECRET_KEY");
        Assume.assumeTrue("跳过未配置的阿里云保留策略测试",
                endpoint != null && accessKey != null && secretKey != null);

        ShellConnect connect = new ShellConnect();
        connect.setName("EASY_SHELL_ALIYUN_BUCKET_RETENTION");
        connect.setHost(endpoint);
        connect.setUser(accessKey);
        connect.setPassword(secretKey);
        connect.setS3Type("alibaba");
        connect.setRegion("oss-cn-beijing");
        connect.setConnectTimeOut(30);

        ShellS3Bucket bucket = new ShellS3Bucket();
        bucket.setName("easyshell-v2-worm-" + UUID.randomUUID().toString().replace("-", ""));
        bucket.setRegion(connect.getRegion());
        bucket.setObjectLock(true);
        bucket.setVersioning(true);
        bucket.setRetention(true);
        bucket.setRetentionMode(1);
        bucket.setRetentionValidity(1);
        bucket.setRetentionValidityType(0);

        ShellS3ClientV2 client = new ShellS3ClientV2(connect);
        try {
            client.start(30_000);
            client.createBucket(bucket);
            assertTrue(client.isBucketObjectLock(bucket.getName()));
            ShellS3Retention retention = client.getBucketRetention(bucket.getName());
            assertNotNull(retention);
            assertEquals(ShellS3RetentionMode.GOVERNANCE, retention.getMode());
            assertEquals(Integer.valueOf(1), retention.getDays());
        } finally {
            try {
                client.deleteBucket(bucket, true);
            } catch (Exception ex) {
                System.err.println("清理保留策略测试桶失败: " + ex.getMessage());
            }
            client.close();
        }
    }

    @Test
    public void testTencentBucketRetentionByYears() throws Exception {
        String endpoint = System.getenv("EASY_SHELL_TENCENT_ENDPOINT");
        String accessKey = System.getenv("EASY_SHELL_TENCENT_ACCESS_KEY");
        String secretKey = System.getenv("EASY_SHELL_TENCENT_SECRET_KEY");
        Assume.assumeTrue("跳过未配置的腾讯云保留策略测试",
                endpoint != null && accessKey != null && secretKey != null);

        ShellConnect connect = new ShellConnect();
        connect.setName("EASY_SHELL_TENCENT_BUCKET_RETENTION");
        connect.setHost(endpoint);
        connect.setUser(accessKey);
        connect.setPassword(secretKey);
        connect.setS3Type("tencent");
        connect.setRegion("ap-guangzhou");
        connect.setConnectTimeOut(30);

        ShellS3Bucket bucket = new ShellS3Bucket();
        bucket.setName("easyshell-v2-cos-" + UUID.randomUUID().toString().replace("-", ""));
        bucket.setRegion(connect.getRegion());
        bucket.setObjectLock(true);
        bucket.setVersioning(true);
        bucket.setRetention(true);
        bucket.setRetentionMode(0);
        bucket.setRetentionValidity(1);
        bucket.setRetentionValidityType(1);

        ShellS3ClientV2 client = new ShellS3ClientV2(connect);
        try {
            client.start(30_000);
            client.createBucket(bucket);
            assertTrue(client.isBucketObjectLock(bucket.getName()));
            ShellS3Retention retention = client.getBucketRetention(bucket.getName());
            assertNotNull(retention);
            assertEquals(ShellS3RetentionMode.COMPLIANCE, retention.getMode());
            assertEquals(Integer.valueOf(365), retention.getDays());
        } finally {
            try {
                client.deleteBucket(bucket, true);
            } catch (Exception ex) {
                System.err.println("清理腾讯云保留策略测试桶失败: " + ex.getMessage());
            }
            client.close();
        }
    }

    @Test
    public void testObjectLockRetainUntilHttpDate() throws Exception {
        AtomicBoolean headCalled = new AtomicBoolean();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            if ("HEAD".equalsIgnoreCase(exchange.getRequestMethod())) {
                headCalled.set(true);
                exchange.getResponseHeaders().add("ETag", "\"test\"");
                exchange.getResponseHeaders().add("Content-Length", "4");
                exchange.getResponseHeaders().add("Last-Modified", "Fri, 08 Oct 2027 14:00:25 GMT");
                exchange.getResponseHeaders().add("x-amz-object-lock-retain-until-date",
                        "Fri, 08 Oct 2027 14:00:25 GMT");
                exchange.sendResponseHeaders(200, -1);
            } else {
                byte[] body = ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                        + "<ListAllMyBucketsResult xmlns=\"http://s3.amazonaws.com/doc/2006-03-01/\">"
                        + "<Owner><ID>test</ID><DisplayName>test</DisplayName></Owner>"
                        + "<Buckets></Buckets></ListAllMyBucketsResult>")
                        .getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/xml");
                exchange.sendResponseHeaders(200, body.length);
                try (OutputStream output = exchange.getResponseBody()) {
                    output.write(body);
                }
            }
            exchange.close();
        });
        server.start();
        ShellConnect connect = new ShellConnect();
        connect.setName("EASY_SHELL_OBJECT_LOCK_DATE");
        connect.setHost("http://127.0.0.1:" + server.getAddress().getPort());
        connect.setUser("test");
        connect.setPassword("test");
        connect.setS3Type("minio");
        connect.setRegion("us-east-1");
        connect.setConnectTimeOut(30);
        ShellS3ClientV2 client = new ShellS3ClientV2(connect);
        try {
            client.start(30_000);
            ShellS3File remote = client.fileInfo("/bucket/key.bin");
            assertNotNull(remote);
            assertEquals(4, remote.getFileSize());
            assertTrue(headCalled.get());
        } finally {
            client.close();
            server.stop(0);
        }
    }

    @Test
    public void testConnectionCheckUsesListBuckets() throws Exception {
        AtomicInteger headRequests = new AtomicInteger();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            if ("HEAD".equalsIgnoreCase(exchange.getRequestMethod())) {
                headRequests.incrementAndGet();
                exchange.sendResponseHeaders(400, -1);
                exchange.close();
                return;
            }
            byte[] body = ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<ListAllMyBucketsResult xmlns=\"http://s3.amazonaws.com/doc/2006-03-01/\">"
                    + "<Owner><ID>test</ID><DisplayName>test</DisplayName></Owner>"
                    + "<Buckets></Buckets></ListAllMyBucketsResult>")
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/xml");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(body);
            }
            exchange.close();
        });
        server.start();
        ShellConnect connect = new ShellConnect();
        connect.setName("EASY_SHELL_CONNECTION_CHECK");
        connect.setHost("http://127.0.0.1:" + server.getAddress().getPort());
        connect.setUser("test");
        connect.setPassword("test");
        connect.setS3Type("tencent");
        connect.setRegion("ap-guangzhou");
        connect.setConnectTimeOut(30);
        ShellS3ClientV2 client = new ShellS3ClientV2(connect);
        try {
            client.start(30_000);
            assertTrue(client.isConnected());
            assertEquals(0, headRequests.get());
        } finally {
            client.close();
            server.stop(0);
        }
    }

    @Test
    public void testListBucketsIncludesMetadata() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            String body;
            if (query != null && query.contains("versioning")) {
                body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                        + "<VersioningConfiguration xmlns=\"http://s3.amazonaws.com/doc/2006-03-01/\">"
                        + "<Status>Enabled</Status></VersioningConfiguration>";
            } else if (query != null && query.contains("object-lock")) {
                body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                        + "<ObjectLockConfiguration xmlns=\"http://s3.amazonaws.com/doc/2006-03-01/\">"
                        + "<ObjectLockEnabled>Enabled</ObjectLockEnabled><Rule><DefaultRetention>"
                        + "<Mode>GOVERNANCE</Mode><Days>365</Days>"
                        + "</DefaultRetention></Rule></ObjectLockConfiguration>";
            } else {
                body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                        + "<ListAllMyBucketsResult xmlns=\"http://s3.amazonaws.com/doc/2006-03-01/\">"
                        + "<Owner><ID>test</ID><DisplayName>test</DisplayName></Owner><Buckets><Bucket>"
                        + "<Name>metadata-bucket</Name>"
                        + "<CreationDate>2026-10-08T12:00:00.000Z</CreationDate>"
                        + "<BucketRegion>cn-south-1</BucketRegion>"
                        + "</Bucket></Buckets></ListAllMyBucketsResult>";
            }
            byte[] responseBytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/xml");
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(responseBytes);
            }
            exchange.close();
        });
        server.start();
        ShellConnect connect = new ShellConnect();
        connect.setName("EASY_SHELL_BUCKET_METADATA");
        connect.setHost("http://127.0.0.1:" + server.getAddress().getPort());
        connect.setUser("test");
        connect.setPassword("test");
        connect.setS3Type("minio");
        connect.setRegion("us-east-1");
        connect.setConnectTimeOut(30);
        ShellS3ClientV2 client = new ShellS3ClientV2(connect);
        try {
            client.start(30_000);
            List<ShellS3Bucket> buckets = client.listBuckets();
            assertEquals(1, buckets.size());
            ShellS3Bucket bucket = buckets.get(0);
            assertEquals("metadata-bucket", bucket.getName());
            assertEquals("cn-south-1", bucket.getRegion());
            assertNotNull(bucket.getCreationDate());
            assertTrue(bucket.isVersioning());
            assertTrue(bucket.isObjectLock());
            assertTrue(bucket.isRetention());
            assertEquals(1, bucket.getRetentionMode());
            assertEquals(365, bucket.getRetentionValidity());
            assertEquals(0, bucket.getRetentionValidityType());
        } finally {
            client.close();
            server.stop(0);
        }
    }

    @Test
    public void testHuaweiCrossRegionNoSuchBucket() throws Exception {
        AtomicInteger listRequests = new AtomicInteger();
        AtomicBoolean locationRequested = new AtomicBoolean();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            String query = exchange.getRequestURI().getQuery();
            int status = 200;
            String body;
            if (query != null && query.contains("location")) {
                locationRequested.set(true);
                body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                        + "<Location xmlns=\"http://obs.myhuaweicloud.com/doc/2015-06-30/\">"
                        + "cn-east-3</Location>";
            } else if ("/".equals(path)) {
                body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                        + "<ListAllMyBucketsResult xmlns=\"http://s3.amazonaws.com/doc/2006-03-01/\">"
                        + "<Owner><ID>test</ID><DisplayName>test</DisplayName></Owner>"
                        + "<Buckets><Bucket><Name>oyzh</Name>"
                        + "<CreationDate>2026-10-08T12:00:00.000Z</CreationDate></Bucket></Buckets>"
                        + "</ListAllMyBucketsResult>";
            } else if (query != null && query.contains("list-type=2")) {
                if (listRequests.incrementAndGet() == 1) {
                    status = 404;
                    body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                            + "<Error><Code>NoSuchBucket</Code>"
                            + "<Message>The specified bucket does not exist.</Message>"
                            + "<BucketName>oyzh</BucketName><RequestId>test</RequestId></Error>";
                } else {
                    body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                            + "<ListBucketResult xmlns=\"http://s3.amazonaws.com/doc/2006-03-01/\">"
                            + "<Name>oyzh</Name><Prefix></Prefix><KeyCount>1</KeyCount>"
                            + "<MaxKeys>1000</MaxKeys><IsTruncated>false</IsTruncated>"
                            + "<Contents><Key>file.bin</Key>"
                            + "<LastModified>2026-10-08T12:00:00.000Z</LastModified>"
                            + "<ETag>&quot;test&quot;</ETag><Size>4</Size>"
                            + "<StorageClass>STANDARD</StorageClass></Contents>"
                            + "</ListBucketResult>";
                }
            } else {
                status = 404;
                body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                        + "<Error><Code>ResourceNotFound</Code><Message>Not found</Message></Error>";
            }
            byte[] responseBytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/xml");
            exchange.sendResponseHeaders(status, responseBytes.length);
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(responseBytes);
            }
            exchange.close();
        });
        server.start();
        ShellConnect connect = new ShellConnect();
        connect.setName("EASY_SHELL_HUAWEI_CROSS_REGION");
        connect.setHost("http://127.0.0.1:" + server.getAddress().getPort());
        connect.setUser("test");
        connect.setPassword("test");
        connect.setS3Type("huawei");
        connect.setRegion("cn-north-4");
        connect.setConnectTimeOut(30);

        ShellS3ClientV2 client = new ShellS3ClientV2(connect);
        try {
            client.start(30_000);
            List<ShellS3File> files = new ArrayList<>();
            client.lsFileDynamic("/oyzh/", files::add);
            assertEquals(1, files.size());
            assertEquals("file.bin", files.get(0).getFileName());
            assertTrue(locationRequested.get());
            assertTrue(listRequests.get() >= 2);
        } finally {
            client.close();
            server.stop(0);
        }
    }

    @Test
    public void testAliyunCrossRegionBucket() throws Exception {
        String endpoint = System.getenv("EASY_SHELL_ALIYUN_CROSS_REGION_ENDPOINT");
        String accessKey = System.getenv("EASY_SHELL_ALIYUN_CROSS_REGION_ACCESS_KEY");
        String secretKey = System.getenv("EASY_SHELL_ALIYUN_CROSS_REGION_SECRET_KEY");
        Assume.assumeTrue("跳过未配置的阿里云跨区域测试",
                endpoint != null && accessKey != null && secretKey != null);

        String bucketName = System.getenv("EASY_SHELL_ALIYUN_CROSS_REGION_BUCKET");
        if (bucketName == null || bucketName.isBlank()) {
            bucketName = "oyzh6";
        }
        final String targetBucket = bucketName;

        ShellConnect connect = new ShellConnect();
        connect.setName("EASY_SHELL_ALIYUN_CROSS_REGION");
        connect.setHost(endpoint);
        connect.setUser(accessKey);
        connect.setPassword(secretKey);
        connect.setS3Type("alibaba");
        connect.setRegion("oss-cn-beijing");
        connect.setConnectTimeOut(30);

        ShellS3ClientV2 client = new ShellS3ClientV2(connect);
        try {
            client.start(30_000);
            assertTrue("跨区域桶应出现在桶列表中",
                    client.listBuckets().stream().anyMatch(item -> targetBucket.equals(item.getName())));
            assertTrue(client.exist("/" + targetBucket));
            assertNotNull(client.fileInfo("/" + targetBucket));
            List<ShellS3File> files = client.lsFile("/" + targetBucket);
            assertNotNull(files);
            for (ShellS3File file : files) {
                assertNotNull(file);
                break;
            }
        } finally {
            client.close();
        }
    }

    private void run(String prefix, String defaultType, String defaultRegion) throws Exception {
        String endpoint = System.getenv(prefix + "ENDPOINT");
        String accessKey = System.getenv(prefix + "ACCESS_KEY");
        String secretKey = System.getenv(prefix + "SECRET_KEY");
        Assume.assumeTrue("跳过未配置的 S3 集成测试", endpoint != null && accessKey != null && secretKey != null);

        ShellConnect connect = new ShellConnect();
        connect.setName(prefix + "ShellS3ClientV2");
        connect.setHost(endpoint);
        connect.setUser(accessKey);
        connect.setPassword(secretKey);
        connect.setS3Type(System.getenv(prefix + "TYPE") == null ? defaultType : System.getenv(prefix + "TYPE"));
        connect.setRegion(System.getenv(prefix + "REGION") == null ? defaultRegion : System.getenv(prefix + "REGION"));
        connect.setConnectTimeOut(30);

        ShellS3Bucket bucket = new ShellS3Bucket();
        bucket.setName("easyshell-v2-" + UUID.randomUUID().toString().replace("-", ""));
        bucket.setRegion(connect.getRegion());

        ShellS3ClientV2 client = new ShellS3ClientV2(connect);
        try {
            client.start(30_000);
            assertTrue(client.isConnected());
            client.createBucket(bucket);
            this.verifyClient(client, bucket.getName(), !connect.isMinioS3Type());
        } finally {
            try {
                client.deleteBucket(bucket, true);
            } catch (Exception ex) {
                System.err.println("清理测试桶失败: " + ex.getMessage());
                try {
                    client.lsFileDynamic("/" + bucket.getName() + "/",
                            item -> System.err.println("清理遗留对象: " + item.getFileKey()));
                } catch (Exception listError) {
                    System.err.println("列举清理遗留对象失败: " + listError.getMessage());
                }
            }
            client.close();
        }
    }

    private void verifyClient(ShellS3ClientV2 client, String bucketName, boolean strictDeletion) throws Exception {
        String root = "/" + bucketName;
        assertTrue(client.exist(root));
        assertNotNull(client.getBucket(bucketName));
        assertTrue(client.listBuckets().stream().anyMatch(item -> bucketName.equals(item.getName())));
        assertFalse(client.isBucketObjectLock(bucketName));
        assertNull(client.getBucketRetention(bucketName));
        try {
            client.setBucketVersioning(bucketName, true);
            assertTrue(client.isBucketVersioning(bucketName));
            client.setBucketVersioning(bucketName, false);
            assertFalse(client.isBucketVersioning(bucketName));
        } catch (RuntimeException ex) {
            System.err.println("当前 S3 服务不支持版本控制测试: " + ex.getMessage());
        }

        String directory = root + "/v2-dir";
        assertTrue(client.createDir(directory));
        assertTrue(client.exist(directory));
        assertNotNull(client.fileInfo(directory));

        byte[] payload = "minio-v2-compatible\n中文内容".getBytes(StandardCharsets.UTF_8);
        String objectPath = directory + "/data.bin";
        AtomicLong uploaded = new AtomicLong();
        try (InputStream input = new ByteArrayInputStream(payload)) {
            client.put(input, objectPath, value -> {
                uploaded.addAndGet(value);
                return true;
            });
        }
        assertEquals(payload.length, uploaded.get());
        assertTrue(client.exist(objectPath));

        ShellS3File remote = client.fileInfo(objectPath);
        assertNotNull(remote);
        assertEquals("data.bin", remote.getFileName());
        assertEquals("v2-dir/data.bin", remote.getFileKey());
        assertEquals(payload.length, remote.getFileSize());

        List<ShellS3File> files = client.lsFile(directory);
        assertTrue(files.stream().anyMatch(item -> "data.bin".equals(item.getFileName())));

        Path downloaded = Files.createTempFile("easyshell-s3-v2-", ".bin");
        try {
            AtomicLong downloadedBytes = new AtomicLong();
            client.get(remote, downloaded.toString(), value -> {
                downloadedBytes.addAndGet(value);
                return true;
            });
            assertArrayEquals(payload, Files.readAllBytes(downloaded));
            assertEquals(payload.length, downloadedBytes.get());
            try (InputStream input = client.getStream(remote, null)) {
                assertArrayEquals(payload, input.readAllBytes());
            }
        } finally {
            Files.deleteIfExists(downloaded);
        }

        String streamPath = directory + "/stream.bin";
        byte[] streamPayload = "stream-output".getBytes(StandardCharsets.UTF_8);
        try (OutputStream output = client.putStream(streamPath, null)) {
            output.write(streamPayload);
        }
        ShellS3File streamFile = client.fileInfo(streamPath);
        assertNotNull(streamFile);
        try (InputStream input = client.getStream(streamFile, null)) {
            assertArrayEquals(streamPayload, input.readAllBytes());
        }

        String deletePath = root + "/delete-root.bin";
        client.put(new ByteArrayInputStream(new byte[]{1}), deletePath, null);
        assertTrue(client.exist(deletePath));
        client.delete(deletePath);
        if (strictDeletion) {
            assertFalse(client.exist(deletePath));
        } else {
            System.err.println("当前 RustFS 端点删除后仍返回旧对象元数据，跳过删除后不存在断言");
        }

        String nestedDeletePath = directory + "/delete-me.txt";
        client.put(new ByteArrayInputStream(new byte[]{1}), nestedDeletePath, null);
        client.delete(nestedDeletePath);
        if (strictDeletion) {
            assertFalse(client.exist(nestedDeletePath));
        }

        client.createDirRecursive(directory + "/deep/level");
        assertTrue(client.exist(directory + "/deep/level"));

        assertTrue(client.rename(remote, "renamed.bin"));
        if (strictDeletion) {
            assertFalse(client.exist(objectPath));
        }
        String renamedPath = directory + "/renamed.bin";
        assertTrue(client.exist(renamedPath));
        assertArrayEquals(payload, this.download(client, client.fileInfo(renamedPath)));

        String presignedUrl = client.generatePresignedUrl(bucketName,
                "v2-dir/renamed.bin", Duration.ofMinutes(5));
        assertNotNull(presignedUrl);
        try (InputStream input = URI.create(presignedUrl).toURL().openStream()) {
            assertArrayEquals(payload, input.readAllBytes());
        }

        String nested = directory + "/nested";
        assertTrue(client.createDir(nested));
        client.touch(nested + "/empty.txt");
        assertTrue(client.exist(nested + "/empty.txt"));
        client.deleteDirRecursive(nested);
        if (strictDeletion) {
            assertFalse(client.exist(nested + "/empty.txt"));
        }

        ShellS3File directoryFile = client.fileInfo(directory);
        assertNotNull(directoryFile);
        assertTrue(client.rename(directoryFile, "v2-renamed-dir"));
        if (strictDeletion) {
            assertFalse(client.exist(directory));
        }
        assertTrue(client.exist(root + "/v2-renamed-dir/renamed.bin"));

        client.deleteDirRecursive(root + "/v2-renamed-dir");
        if (strictDeletion) {
            assertFalse(client.exist(root + "/v2-renamed-dir/renamed.bin"));
        }
    }

    private byte[] download(ShellS3ClientV2 client, ShellS3File file) throws Exception {
        Path path = Files.createTempFile("easyshell-s3-v2-copy-", ".bin");
        try {
            client.get(file, path.toString(), null);
            return Files.readAllBytes(path);
        } finally {
            Files.deleteIfExists(path);
        }
    }

}
