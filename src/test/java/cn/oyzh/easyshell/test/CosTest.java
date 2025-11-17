package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.s3.ShellS3MD5Interceptor;
import org.junit.Before;
import org.junit.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.net.URI;
import java.util.List;

/**
 * s3协议客户端
 *
 * @author oyzh
 * @since 2025-06-14
 */
public class CosTest {

    /**
     * s3客户端
     */
    private S3Client s3Client;

    /**
     * 获取区域
     *
     * @return 区域
     */
    public Region region() {
        return Region.of("ap-guangzhou");
    }

    /**
     * 签名提供者
     */
    private StaticCredentialsProvider credentialsProvider;

    /**
     * 初始化客户端
     */
    private void initClient() {
        String endpoint = "http://cos.ap-guangzhou.myqcloud.com";
        String accessKey = "";
        String secretKey = "";
        // 创建凭证提供者
        this.credentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));

        // http客户端
        S3Configuration s3Configuration = S3Configuration.builder().build();

        UrlConnectionHttpClient httpClient = (UrlConnectionHttpClient) UrlConnectionHttpClient.builder()
                .build();

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

    @Before
    public void before() {
        this.initClient();
    }

    @Test
    public void listBuckets() {
        ListBucketsRequest request = ListBucketsRequest.builder().build();
        ListBucketsResponse response = this.s3Client.listBuckets(request);
        List<Bucket> list = response.buckets();
        for (Bucket bucket : list) {
            System.out.println(bucket);
        }
    }

    @Test
    public void listObjects() {
        // String fPrefix = "*";
        String bucketName = "test4-1259210924";
        ListObjectsRequest request = ListObjectsRequest.builder()
                .bucket(bucketName)
                // .prefix(fPrefix)
                .delimiter("/")
                .build();
        ListObjectsResponse response = this.s3Client.listObjects(request);
        List<S3Object> list = response.contents();
        for (S3Object s3Object : list) {
            System.out.println(s3Object);
        }
    }

}
