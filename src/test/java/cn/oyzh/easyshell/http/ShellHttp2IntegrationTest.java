package cn.oyzh.easyshell.http;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.http.ShellHttpRequest;
import cn.oyzh.easyshell.domain.http.ShellHttpSetting;
import cn.oyzh.easyshell.internal.ShellPrototype;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import mockwebserver3.RecordedRequest;
import okhttp3.Protocol;
import okhttp3.tls.HandshakeCertificates;
import okhttp3.tls.HeldCertificate;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * HTTP/2 集成测试。
 *
 * @author oyzh
 * @since 2026-11-09
 */
public class ShellHttp2IntegrationTest {

    @Test
    public void negotiatesHttp2WithTls() throws Throwable {
        HeldCertificate certificate = new HeldCertificate.Builder()
                .addSubjectAlternativeName("localhost")
                .build();
        HandshakeCertificates certificates = new HandshakeCertificates.Builder()
                .heldCertificate(certificate)
                .build();
        try (MockWebServer server = new MockWebServer()) {
            server.useHttps(certificates.sslSocketFactory());
            server.setProtocols(List.of(Protocol.HTTP_2, Protocol.HTTP_1_1));
            server.enqueue(new MockResponse.Builder().body("http2-ok").build());
            server.start();

            ShellConnect connect = new ShellConnect();
            connect.setType(ShellPrototype.HTTP);
            connect.setName("HTTP2 Test");
            ShellHttpClient client = new ShellHttpClient(connect);
            client.start();
            try {
                ShellHttpWorkspace workspace = new ShellHttpWorkspace();
                workspace.getSettings().setVerifySsl(false);
                workspace.getSettings().setHttpVersion(ShellHttpSetting.VERSION_HTTP_1_1);
                ShellHttpRequest request = new ShellHttpRequest();
                request.setHttpVersion(ShellHttpSetting.VERSION_HTTP_2);
                request.setUrl(server.url("/api/http2").toString());
                ShellHttpResponse response = client.execute(request, workspace);
                assertEquals(200, response.getStatusCode());
                assertEquals("http2-ok", response.getBody());

                RecordedRequest recorded = server.takeRequest(5, TimeUnit.SECONDS);
                assertNotNull(recorded);
                assertEquals("HTTP/2", recorded.getVersion());
            } finally {
                client.close();
            }
        }
    }
}
