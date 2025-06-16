package cn.oyzh.easyshell.s3;

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

import java.util.ArrayList;
import java.util.List;
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
    public static int deleteNonVersionedDirectory(S3Client s3Client, String bucketName, String directoryPath) {
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(directoryPath)
                .build();

        ListObjectsV2Response response;
        AtomicInteger deletedCount = new AtomicInteger();

        do {
            response = s3Client.listObjectsV2(listRequest);

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

        } while (response.isTruncated());
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
    public static int deleteAllVersionsInDirectory(S3Client s3Client, String bucketName, String directoryPath) {
        ListObjectVersionsRequest listRequest = ListObjectVersionsRequest.builder()
                .bucket(bucketName)
                .prefix(directoryPath)
                .build();

        ListObjectVersionsResponse response;
        int totalDeleted = 0;

        do {
            response = s3Client.listObjectVersions(listRequest);
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

        } while (response.isTruncated());
        return totalDeleted;
    }

    /**
     * 删除普通文件
     *
     * @param s3Client   s3客户端
     * @param bucketName 桶名称
     * @param objectKey  对象键
     */
    public static void deleteNormalFile(S3Client s3Client, String bucketName, String objectKey) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
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
    public static void deleteAllVersions(S3Client s3Client, String bucketName, String objectKey) {
        ListObjectVersionsRequest listRequest = ListObjectVersionsRequest.builder()
                .bucket(bucketName)
                .prefix(objectKey) // 精确匹配文件
                .build();

        List<ObjectIdentifier> versionsToDelete = new ArrayList<>();
        ListObjectVersionsResponse response;

        do {
            response = s3Client.listObjectVersions(listRequest);

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

        } while (response.isTruncated());

        // 批量删除所有版本
        if (!versionsToDelete.isEmpty()) {
            for (int i = 0; i < versionsToDelete.size(); i += 1000) {
                List<ObjectIdentifier> batch = versionsToDelete.subList(
                        i, Math.min(i + 1000, versionsToDelete.size()));

                DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                        .bucket(bucketName)
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
                        .delete(Delete.builder().objects(markers).build())
                        .build();

                s3Client.deleteObjects(deleteRequest);
            }

            // 处理分页
            listRequest = listRequest.toBuilder()
                    .keyMarker(response.nextKeyMarker())
                    .versionIdMarker(response.nextVersionIdMarker())
                    .build();

        } while (response.isTruncated());
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

            if (response.keyCount() > 0) {
                List<ObjectIdentifier> objects = new ArrayList<>();
                response.contents().forEach(item -> {
                    objects.add(ObjectIdentifier.builder()
                            .key(item.key())
                            .build());
                });

                DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                        .bucket(bucketName)
                        .delete(Delete.builder().objects(objects).build())
                        .build();

                s3Client.deleteObjects(deleteRequest);
            }

            listRequest = listRequest.toBuilder()
                    .continuationToken(response.nextContinuationToken())
                    .build();

        } while (response.isTruncated());
    }

    /**
     * 创建Region对象
     * @param region 区域
     * @return Region
     */
    public static Region ofRegion(String region) {
        if (StringUtil.isBlank(region)) {
            return Region.US_EAST_1;
        }
        return Region.of(region);
    }
}
