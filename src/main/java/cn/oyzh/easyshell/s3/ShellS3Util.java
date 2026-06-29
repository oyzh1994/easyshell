package cn.oyzh.easyshell.s3;

import cn.oyzh.common.security.SHA256Util;
import cn.oyzh.common.util.BooleanUtil;
import cn.oyzh.common.util.HexUtil;
import cn.oyzh.common.util.StringUtil;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectVersionsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectVersionsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * s3工具类
 *
 * @author oyzh
 * @since 2025-06-14
 */
public class ShellS3Util {

    /**
     * 转换为s3前缀
     *
     * @param fPath 文件路径
     * @return s3前缀
     */
    public static String toPrefix(String fPath) {
        String fPrefix;
        if ("/".equals(fPath)) {
            fPrefix = "";
        } else if (!fPath.endsWith("/")) {
            if (fPath.startsWith("/")) {
                fPrefix = fPath.substring(1) + "/";
            } else {
                fPrefix = fPath + "/";
            }
        } else {
            fPrefix = fPath;
        }
        return fPrefix;
    }


    /**
     * 删除非版本控制桶中的目录
     *
     * @param s3Client      s3客户端
     * @param bucketName    桶名称
     * @param directoryPath 路径
     * @return 删除数量
     */
    public static int deleteNonVersionedDirectory(S3Client s3Client,
                                                  String bucketName,
                                                  String directoryPath) {
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(directoryPath)
                .build();
        ListObjectsV2Response response;
        AtomicInteger deletedCount = new AtomicInteger();
        do {
            response = s3Client.listObjectsV2(listRequest);
            if (response == null) {
                break;
            }
            if (response.keyCount() > 0) {
                List<ObjectIdentifier> objectIdentifiers = new ArrayList<>();
                response.contents().forEach(item -> {
                    objectIdentifiers.add(ObjectIdentifier.builder()
                            .key(item.key())
                            .build());
                    deletedCount.getAndIncrement();
                });

                DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                        .bucket(bucketName)
                        .bypassGovernanceRetention(true) // 关键参数，绕过治理模式限制
                        .delete(Delete.builder().objects(objectIdentifiers).build())
                        .build();

                DeleteObjectsResponse deleteResponse = s3Client.deleteObjects(deleteRequest);

                // 检查删除结果
                if (!deleteResponse.deleted().isEmpty()) {
                    System.out.println("成功删除 " + deleteResponse.deleted().size() + " 个对象");
                }
                if (!deleteResponse.errors().isEmpty()) {
                    deleteResponse.errors().forEach(error -> {
                        System.err.println("删除失败: " + error.key() + " - " + error.message());
                    });
                }
            }

            listRequest = listRequest.toBuilder()
                    .continuationToken(response.nextContinuationToken())
                    .build();

        } while (BooleanUtil.isTrue(response.isTruncated()));
        System.out.println("非版本控制目录删除完成，共删除 " + deletedCount.get() + " 个对象");
        return deletedCount.get();
    }

    /**
     * 删除目录下所有对象的所有版本
     *
     * @param s3Client      s3客户端
     * @param bucketName    桶名称
     * @param directoryPath 对象路径
     * @return 删除数量
     */
    public static int deleteAllVersionsInDirectory(S3Client s3Client,
                                                   String bucketName,
                                                   String directoryPath) {
        ListObjectVersionsRequest listRequest = ListObjectVersionsRequest.builder()
                .bucket(bucketName)
                .prefix(directoryPath)
                .build();

        ListObjectVersionsResponse response;
        int totalDeleted = 0;

        do {
            response = s3Client.listObjectVersions(listRequest);
            if (response == null) {
                break;
            }
            System.out.println("发现 " + response.versions().size() + " 个对象版本 和 "
                    + response.deleteMarkers().size() + " 个删除标记");

            // 批量删除对象版本
            List<ObjectIdentifier> objectIdentifiers = new ArrayList<>();

            // 添加普通对象版本
            response.versions().forEach(version -> {
                objectIdentifiers.add(ObjectIdentifier.builder()
                        .key(version.key())
                        .versionId(version.versionId())
                        .build());
                System.out.println("准备删除版本: " + version.key() + " (v" + version.versionId() + ")");
            });

            // 添加删除标记
            response.deleteMarkers().forEach(marker -> {
                objectIdentifiers.add(ObjectIdentifier.builder()
                        .key(marker.key())
                        .versionId(marker.versionId())
                        .build());
                System.out.println("准备删除删除标记: " + marker.key() + " (v" + marker.versionId() + ")");
            });

            // 执行批量删除（每次最多1000个）
            if (!objectIdentifiers.isEmpty()) {
                for (int i = 0; i < objectIdentifiers.size(); i += 1000) {
                    List<ObjectIdentifier> batch = objectIdentifiers.subList(
                            i, Math.min(i + 1000, objectIdentifiers.size()));

                    DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                            .bucket(bucketName)
                            .bypassGovernanceRetention(true) // 关键参数，绕过治理模式限制
                            .delete(Delete.builder().objects(batch).build())
                            .build();

                    DeleteObjectsResponse deleteResponse = s3Client.deleteObjects(deleteRequest);

                    // 记录成功删除的数量
                    totalDeleted += deleteResponse.deleted().size();

                    // 检查失败项
                    if (!deleteResponse.errors().isEmpty()) {
                        deleteResponse.errors().forEach(error -> {
                            System.err.println("删除失败: " + error.key() + " (v" + error.versionId() + ") - " + error.message());
                        });
                    }
                }
            }

            // 处理分页
            listRequest = listRequest.toBuilder()
                    .keyMarker(response.nextKeyMarker())
                    .versionIdMarker(response.nextVersionIdMarker())
                    .build();

        } while (BooleanUtil.isTrue(response.isTruncated()));
        return totalDeleted;
    }

    /**
     * 删除普通文件
     *
     * @param s3Client   s3客户端
     * @param bucketName 桶名称
     * @param objectKey  对象键
     */
    public static void deleteNormalFile(S3Client s3Client,
                                        String bucketName,
                                        String objectKey) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .bypassGovernanceRetention(true) // 关键参数，绕过治理模式限制
                .build();

        s3Client.deleteObject(request);
    }

    /**
     * 删除带版本控制的文件的所有版本
     *
     * @param s3Client   s3客户端
     * @param bucketName 桶名称
     * @param objectKey  对象键
     */
    public static void deleteAllVersions(S3Client s3Client,
                                         String bucketName,
                                         String objectKey) {
        ListObjectVersionsRequest listRequest = ListObjectVersionsRequest.builder()
                .bucket(bucketName)
                .prefix(objectKey) // 精确匹配文件
                .build();

        List<ObjectIdentifier> versionsToDelete = new ArrayList<>();
        ListObjectVersionsResponse response;

        do {
            response = s3Client.listObjectVersions(listRequest);
            if (response == null) {
                break;
            }
            // 添加普通版本
            response.versions().stream()
                    .filter(version -> version.key().equals(objectKey))
                    .forEach(version -> {
                        versionsToDelete.add(ObjectIdentifier.builder()
                                .key(version.key())
                                .versionId(version.versionId())
                                .build());
                    });

            // 添加删除标记
            response.deleteMarkers().stream()
                    .filter(marker -> marker.key().equals(objectKey))
                    .forEach(marker -> {
                        versionsToDelete.add(ObjectIdentifier.builder()
                                .key(marker.key())
                                .versionId(marker.versionId())
                                .build());
                    });

            // 处理分页
            listRequest = listRequest.toBuilder()
                    .keyMarker(response.nextKeyMarker())
                    .versionIdMarker(response.nextVersionIdMarker())
                    .build();

        } while (BooleanUtil.isTrue(response.isTruncated()));

        // 批量删除所有版本
        if (!versionsToDelete.isEmpty()) {
            for (int i = 0; i < versionsToDelete.size(); i += 1000) {
                List<ObjectIdentifier> batch = versionsToDelete.subList(
                        i, Math.min(i + 1000, versionsToDelete.size()));

                DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                        .bucket(bucketName)
                        .bypassGovernanceRetention(true) // 关键参数，绕过治理模式限制
                        .delete(Delete.builder().objects(batch).build())
                        .build();

                s3Client.deleteObjects(deleteRequest);
            }
        }
    }

    /**
     * 删除版本控制的桶对象
     *
     * @param s3Client   s3客户端
     * @param bucketName 桶名称
     */
    public static void deleteVersionedBucketObjects(S3Client s3Client, String bucketName) {
        // 1. 删除所有对象版本（包括删除标记）
        ListObjectVersionsRequest listRequest = ListObjectVersionsRequest.builder()
                .bucket(bucketName)
                .build();

        ListObjectVersionsResponse response;
        do {
            response = s3Client.listObjectVersions(listRequest);
            if (response == null) {
                break;
            }
            // 删除普通版本
            if (!response.versions().isEmpty()) {
                List<ObjectIdentifier> versions = new ArrayList<>();
                response.versions().forEach(version -> {
                    versions.add(ObjectIdentifier.builder()
                            .key(version.key())
                            .versionId(version.versionId())
                            .build());
                });

                DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                        .bucket(bucketName)
                        // .bypassGovernanceRetention(true) // 关键参数，绕过治理模式限制
                        .delete(Delete.builder().objects(versions).build())
                        .build();

                s3Client.deleteObjects(deleteRequest);
            }

            // 删除删除标记
            if (!response.deleteMarkers().isEmpty()) {
                List<ObjectIdentifier> markers = new ArrayList<>();
                response.deleteMarkers().forEach(marker -> {
                    markers.add(ObjectIdentifier.builder()
                            .key(marker.key())
                            .versionId(marker.versionId())
                            .build());
                });

                DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                        .bucket(bucketName)
                        // .bypassGovernanceRetention(true) // 关键参数，绕过治理模式限制
                        .delete(Delete.builder().objects(markers).build())
                        .build();

                s3Client.deleteObjects(deleteRequest);
            }

            // 处理分页
            listRequest = listRequest.toBuilder()
                    .keyMarker(response.nextKeyMarker())
                    .versionIdMarker(response.nextVersionIdMarker())
                    .build();

        } while (BooleanUtil.isTrue(response.isTruncated()));
    }

    /**
     * 删除无版本控制的桶对象
     *
     * @param s3Client   s3客户端
     * @param bucketName 桶名称
     */
    public static void deleteNonVersionedBucketObjects(S3Client s3Client, String bucketName) {
        // 1. 清空Bucket中的所有对象
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();

        ListObjectsV2Response response;
        do {
            response = s3Client.listObjectsV2(listRequest);
            if (response == null) {
                break;
            }
            if (response.keyCount() > 0) {
                List<ObjectIdentifier> objects = new ArrayList<>();
                response.contents().forEach(item -> {
                    objects.add(ObjectIdentifier.builder()
                            .key(item.key())
                            .build());
                });

                DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                        .bucket(bucketName)
                        .bypassGovernanceRetention(true) // 关键参数，绕过治理模式限制
                        .delete(Delete.builder().objects(objects).build())
                        .build();

                s3Client.deleteObjects(deleteRequest);
            }

            listRequest = listRequest.toBuilder()
                    .continuationToken(response.nextContinuationToken())
                    .build();

        } while (BooleanUtil.isTrue(response.isTruncated()));
    }

    /**
     * 创建Region对象
     *
     * @param region 区域
     * @return Region
     */
    public static Region ofRegion(String region) {
        if (StringUtil.isBlank(region)) {
            return Region.US_EAST_1;
        }
        return Region.of(region);
    }

    /**
     * 解析区域
     *
     * @param host 地址
     * @return 区域
     */
    public static String parseRegion(String host) {
        // 阿里云
        if (host.contains("aliyuncs")) {
            String region = host.split("\\.")[0];
            region = region.split("://")[1];
            return region;
        }
        // 腾讯云
        if (host.contains("myqcloud")) {
            return host.split("\\.")[1];
        }
        // 华为云
        if (host.contains("myhuaweicloud")) {
            return host.split("\\.")[1];
        }
        return null;
    }

    /**
     * 解析文件key
     *
     * @param fKey 文件key
     * @return 处理后的文件key
     */
    public static String parseFileKey(String fKey) {
        if (StringUtil.startWith(fKey, "/")) {
            fKey = fKey.substring(1);
        }
        return fKey;
    }

    /**
     * 获取腾迅appId
     *
     * @param secretId  密钥id
     * @param secretKey 密钥key
     * @return 结果
     * @throws Exception 异常
     */
    public static String getAppId(String secretId, String secretKey) throws Exception {
        // 1. 准备请求参数
        String endpoint = "cam.tencentcloudapi.com";
        String service = "cam";
        String region = ""; // 该接口不需要 Region，留空
        String action = "GetUserAppId";
        String version = "2019-01-16";
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String date = new SimpleDateFormat("yyyy-MM-dd") {{
            setTimeZone(TimeZone.getTimeZone("UTC"));
        }}.format(new Date(Long.parseLong(timestamp) * 1000));

        // 2. 构建请求体 (Payload)
        // 对于 GetUserAppId 接口，没有业务参数，因此请求体为空 JSON 对象
        String payload = "{}";

        // 3. 构建待签名字符串
        // 3.1 规范请求串 (CanonicalRequest)
        String httpRequestMethod = "POST";
        String canonicalUri = "/";
        String canonicalQueryString = "";
        String canonicalHeaders = "content-type:application/json; charset=utf-8\nhost:" + endpoint + "\n";
        String signedHeaders = "content-type;host";
        String hashedPayload = SHA256Util.sha256Hex(payload);
        String canonicalRequest = httpRequestMethod + "\n" +
                canonicalUri + "\n" +
                canonicalQueryString + "\n" +
                canonicalHeaders + "\n" +
                signedHeaders + "\n" +
                hashedPayload;

        // 3.2 待签名字符串 (StringToSign)
        String algorithm = "TC3-HMAC-SHA256";
        String credentialScope = date + "/" + service + "/" + "tc3_request";
        String hashedCanonicalRequest = SHA256Util.sha256Hex(canonicalRequest);
        String stringToSign = algorithm + "\n" +
                timestamp + "\n" +
                credentialScope + "\n" +
                hashedCanonicalRequest;

        // 4. 计算签名 (Signature)
        byte[] secretDate = SHA256Util.hmacSha256(("TC3" + secretKey).getBytes(StandardCharsets.UTF_8), date);
        byte[] secretService = SHA256Util.hmacSha256(secretDate, service);
        byte[] secretSigning = SHA256Util.hmacSha256(secretService, "tc3_request");
        String signature = HexUtil.bytesToHex(SHA256Util.hmacSha256(secretSigning, stringToSign), false);

        // 5. 构建 Authorization 头
        String authorization = algorithm + " " +
                "Credential=" + secretId + "/" + credentialScope + ", " +
                "SignedHeaders=" + signedHeaders + ", " +
                "Signature=" + signature;

        // 6. 发送 HTTP 请求
        URL url = new URL("https://" + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setRequestProperty("Host", endpoint);
        conn.setRequestProperty("X-TC-Action", action);
        conn.setRequestProperty("X-TC-Timestamp", timestamp);
        conn.setRequestProperty("X-TC-Version", version);
        // Region 非必须，但如果你有指定可以加上
        // conn.setRequestProperty("X-TC-Region", region);
        conn.setRequestProperty("Authorization", authorization);
        conn.setDoOutput(true);

        // 写入请求体
        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        // 7. 读取响应
        int responseCode = conn.getResponseCode();
        InputStream inputStream = (responseCode >= 200 && responseCode < 300) ?
                conn.getInputStream() : conn.getErrorStream();

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }

        // 8. 解析响应，提取 AppId
        // 简单解析，生产环境建议使用 JSON 库如 Jackson 或 Gson
        String jsonResponse = response.toString();
        String appIdKey = "\"AppId\":";
        int startIndex = jsonResponse.indexOf(appIdKey);
        if (startIndex == -1) {
            throw new RuntimeException("Failed to get AppId from response: " + jsonResponse);
        }
        startIndex += appIdKey.length();
        int endIndex = jsonResponse.indexOf(",", startIndex);
        if (endIndex == -1) {
            endIndex = jsonResponse.indexOf("}", startIndex);
        }
        return jsonResponse.substring(startIndex, endIndex).trim();
    }
}
