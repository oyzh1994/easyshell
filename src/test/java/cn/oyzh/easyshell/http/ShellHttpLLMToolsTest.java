package cn.oyzh.easyshell.http;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.http.ShellHttpCollection;
import cn.oyzh.easyshell.domain.http.ShellHttpEnvironment;
import cn.oyzh.easyshell.domain.http.ShellHttpRequest;
import cn.oyzh.easyshell.domain.http.ShellHttpRequestScript;
import cn.oyzh.easyshell.domain.http.ShellHttpSetting;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.llm.LLMExecutionMode;
import cn.oyzh.easyshell.store.http.ShellHttpCollectionStore;
import cn.oyzh.easyshell.store.http.ShellHttpEnvironmentStore;
import cn.oyzh.easyshell.store.http.ShellHttpRequestScriptStore;
import cn.oyzh.easyshell.store.http.ShellHttpRequestStore;
import cn.oyzh.easyshell.store.http.ShellHttpSettingStore;
import cn.oyzh.easyshell.store.http.ShellHttpWorkspaceStore;
import cn.oyzh.store.jdbc.JdbcConst;
import cn.oyzh.store.jdbc.JdbcDialect;
import cn.oyzh.store.jdbc.JdbcManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ShellHttpLLMToolsTest {

    private static final String IID = "http-llm-test";

    private ShellHttpMockServer server;

    private ShellHttpClient client;

    private ShellHttpWorkspace workspace;

    private ShellHttpLLMTools tools;

    private ShellHttpCollection collection;

    private ShellHttpRequest request;

    private ShellHttpEnvironment environment;

    private static Path workDirectory;

    @BeforeClass
    public static void beforeClass() throws Exception {
        workDirectory = Files.createTempDirectory("easyshell-http-llm-");
        Path database = workDirectory.resolve("db");
        JdbcManager.destroy();
        JdbcConst.dbFile(database.toString());
        JdbcConst.dbDialect(JdbcDialect.SQLITE);
        JdbcManager.dialect = JdbcDialect.SQLITE;
    }

    @AfterClass
    public static void afterClass() throws Exception {
        JdbcManager.destroy();
        if (workDirectory != null) {
            try (Stream<Path> paths = Files.walk(workDirectory)) {
                paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (Exception ignored) {
                    }
                });
            }
        }
    }

    @Before
    public void setUp() throws Throwable {
        this.server = new ShellHttpMockServer();
        this.server.start();

        ShellConnect connect = new ShellConnect();
        connect.setType(ShellPrototype.HTTP);
        connect.setName("Mock HTTP");
        this.client = new ShellHttpClient(connect);
        this.client.start();

        this.workspace = new ShellHttpWorkspace();
        this.workspace.setIid(IID);
        this.workspace.getSettings().setIid(IID);
        this.workspace.getSettings().setTimeoutSeconds(10);

        this.collection = new ShellHttpCollection();
        this.collection.setIid(IID);
        this.collection.setName("Public API");
        this.workspace.getCollections().add(this.collection);

        this.request = new ShellHttpRequest();
        this.request.setIid(IID);
        this.request.setCollectionId(this.collection.getId());
        this.request.setName("JSON data");
        this.request.setMethod(ShellHttpRequest.METHOD_GET);
        this.request.setUrl(this.server.baseUrl() + "/api/json?token=secret-value&q=1");
        this.request.getHeaders().add(new ShellHttpKeyValue("Authorization", "Bearer stored-secret"));
        this.request.getHeaders().add(new ShellHttpKeyValue("X-Test", "visible-value"));
        this.request.setAuthType(ShellHttpRequest.AUTH_BEARER);
        this.request.setAuthToken("super-secret-token");
        this.collection.getRequests().add(this.request);

        this.environment = new ShellHttpEnvironment();
        this.environment.setIid(IID);
        this.environment.setName("Local");
        this.environment.getValues().add(new ShellHttpKeyValue("baseUrl", this.server.baseUrl()));
        this.environment.getValues().add(new ShellHttpKeyValue("apiToken", "environment-secret"));
        this.workspace.getEnvironments().add(this.environment);
        this.workspace.setActiveEnvironmentId(this.environment.getId());

        assertTrue(ShellHttpCollectionStore.INSTANCE.insert(this.collection));
        assertTrue(ShellHttpRequestStore.INSTANCE.insert(this.request));
        assertTrue(ShellHttpEnvironmentStore.INSTANCE.insert(this.environment));
        assertTrue(ShellHttpWorkspaceStore.INSTANCE.saveSetting(this.workspace));

        this.tools = new ShellHttpLLMTools();
        this.tools.setClient(this.client);
        this.tools.setWorkspace(this.workspace);
    }

    @After
    public void tearDown() {
        if (this.client != null) {
            this.client.close();
        }
        if (this.server != null) {
            this.server.close();
        }
    }

    @Test
    public void readsCompleteDefinitions() {
        String collections = this.tools.listCollections(null, 20);
        assertTrue(collections.contains("Public API"));
        assertTrue(collections.contains("requests=1"));

        String requests = this.tools.listRequests(null, null, 20);
        assertTrue(requests.contains(this.request.getId()));
        assertTrue(requests.contains("token=secret-value"));
        assertFalse(requests.contains("super-secret-token"));

        String details = this.tools.getRequestDetails(this.request.getId());
        assertTrue(details.contains("Bearer stored-secret"));
        assertTrue(details.contains("super-secret-token"));
        assertTrue(details.contains("token=secret-value"));
        assertTrue(details.contains("authType"));
        assertTrue(details.contains("preRequestScript"));

        String environments = this.tools.listEnvironments();
        assertTrue(environments.contains("apiToken"));
        assertTrue(environments.contains("environment-secret"));
        assertTrue(environments.contains(this.server.baseUrl()));
        assertTrue(environments.contains("\"active\":true"));

        String settings = this.tools.getSettings();
        assertTrue(settings.contains("timeoutSeconds"));
        assertTrue(settings.contains("activeEnvironmentId"));
    }

    @Test
    public void createsUpdatesAndDeletesCollectionAndRequest() {
        String createdCollectionMessage = this.tools.createCollection("Created API");
        assertTrue(createdCollectionMessage.contains("Created collection"));
        ShellHttpCollection createdCollection = this.workspace.getCollections().stream()
                .filter(value -> "Created API".equals(value.getName()))
                .findFirst().orElse(null);
        assertNotNull(createdCollection);
        assertTrue(ShellHttpCollectionStore.INSTANCE.exist(createdCollection.getId()));

        String requestJson = "{"
                + "\"name\":\"Created request\","
                + "\"method\":\"POST\","
                + "\"url\":\"" + this.server.baseUrl() + "/api/echo\","
                + "\"headers\":[{\"key\":\"Authorization\",\"value\":\"Bearer created-secret\"}],"
                + "\"bodyType\":\"raw\","
                + "\"bodyContentType\":\"text/plain\","
                + "\"body\":\"hello\","
                + "\"authType\":\"basic\","
                + "\"authUser\":\"user\","
                + "\"authPassword\":\"password\","
                + "\"preRequestScript\":\"// pre\","
                + "\"testScript\":\"// test\""
                + "}";
        String createdRequestMessage = this.tools.createRequest(createdCollection.getId(), requestJson);
        assertTrue(createdRequestMessage.contains("Created request"));
        ShellHttpRequest createdRequest = createdCollection.getRequests().stream()
                .filter(value -> "Created request".equals(value.getName()))
                .findFirst().orElse(null);
        assertNotNull(createdRequest);
        assertEquals(ShellHttpRequest.METHOD_POST, createdRequest.getMethod());
        assertEquals("Bearer created-secret", createdRequest.getHeaders().getFirst().getValue());
        assertEquals("password", createdRequest.getAuthPassword());
        ShellHttpRequestScript script = ShellHttpRequestScriptStore.INSTANCE
                .getByRequestId(createdRequest.getId());
        assertNotNull(script);
        assertEquals("// pre", script.getPreRequestScript());
        assertEquals("// test", script.getTestScript());

        String updateJson = "{"
                + "\"method\":\"GET\","
                + "\"url\":\"" + this.server.baseUrl() + "/api/headers\","
                + "\"headers\":[{\"key\":\"X-Test\",\"value\":\"updated-value\"}],"
                + "\"authPassword\":\"new-password\","
                + "\"timeoutSeconds\":12,"
                + "\"preRequestScript\":\"// pre-updated\""
                + "}";
        String updatedRequestMessage = this.tools.updateRequest(createdRequest.getId(), updateJson);
        assertTrue(updatedRequestMessage.contains("Updated request"));
        ShellHttpRequest updatedRequest = this.workspace.findRequest(createdRequest.getId());
        assertNotNull(updatedRequest);
        assertEquals(ShellHttpRequest.METHOD_GET, updatedRequest.getMethod());
        assertEquals("updated-value", updatedRequest.getHeaders().getFirst().getValue());
        assertEquals("new-password", updatedRequest.getAuthPassword());
        assertEquals(Integer.valueOf(12), updatedRequest.getTimeoutSeconds());
        assertEquals("hello", updatedRequest.getBody());
        ShellHttpRequestScript updatedScript = ShellHttpRequestScriptStore.INSTANCE
                .getByRequestId(updatedRequest.getId());
        assertEquals("// pre-updated", updatedScript.getPreRequestScript());
        assertEquals("// test", updatedScript.getTestScript());

        String execution = this.tools.executeRequest(updatedRequest.getId());
        assertTrue(execution.contains("statusCode: 200"));
        assertTrue(execution.contains("updated-value"));

        assertTrue(this.tools.deleteRequest(updatedRequest.getId()).contains("Deleted request"));
        assertNull(this.workspace.findRequest(updatedRequest.getId()));
        assertFalse(ShellHttpRequestStore.INSTANCE.exist(updatedRequest.getId()));
        assertNull(ShellHttpRequestScriptStore.INSTANCE.getByRequestId(updatedRequest.getId()));

        assertTrue(this.tools.deleteCollection(createdCollection.getId()).contains("Deleted collection"));
        assertNull(this.workspace.getCollections().stream()
                .filter(value -> createdCollection.getId().equals(value.getId()))
                .findFirst().orElse(null));
        assertFalse(ShellHttpCollectionStore.INSTANCE.exist(createdCollection.getId()));
    }

    @Test
    public void managesEnvironmentsSettingsAndActiveEnvironment() {
        String environmentJson = "{\"name\":\"Created env\",\"values\":["
                + "{\"key\":\"baseUrl\",\"value\":\"" + this.server.baseUrl() + "\"},"
                + "{\"key\":\"token\",\"value\":\"created-secret\"}]}";
        assertTrue(this.tools.createEnvironment(environmentJson).contains("Created environment"));
        ShellHttpEnvironment createdEnvironment = this.workspace.getEnvironments().stream()
                .filter(value -> "Created env".equals(value.getName()))
                .findFirst().orElse(null);
        assertNotNull(createdEnvironment);
        assertEquals("created-secret", createdEnvironment.getValues().get(1).getValue());

        assertTrue(this.tools.setActiveEnvironment(createdEnvironment.getId()).contains("Created env"));
        assertEquals(createdEnvironment.getId(), this.workspace.getActiveEnvironmentId());
        assertEquals(createdEnvironment.getId(),
                ShellHttpSettingStore.INSTANCE.getByIid(IID).getActiveEnvironmentId());

        String environmentUpdate = "{\"values\":[{\"key\":\"token\",\"value\":\"changed-secret\"}]}";
        assertTrue(this.tools.updateEnvironment(createdEnvironment.getId(), environmentUpdate)
                .contains("Updated environment"));
        assertEquals("changed-secret", createdEnvironment.getValues().getFirst().getValue());

        String settingsJson = "{"
                + "\"timeoutSeconds\":7,"
                + "\"httpVersion\":\"HTTP/2\","
                + "\"verifySsl\":false,"
                + "\"followRedirects\":false,"
                + "\"maxResponseSizeMb\":12,"
                + "\"activeEnvironmentId\":\"" + createdEnvironment.getId() + "\""
                + "}";
        assertTrue(this.tools.updateSettings(settingsJson).contains("Updated settings"));
        assertEquals(7, this.workspace.getSettings().getTimeoutSeconds());
        assertEquals(ShellHttpSetting.VERSION_HTTP_2, this.workspace.getSettings().getHttpVersion());
        assertFalse(this.workspace.getSettings().isVerifySsl());
        assertFalse(this.workspace.getSettings().isFollowRedirects());
        assertEquals(12, this.workspace.getSettings().getMaxResponseSizeMb());
        assertEquals(createdEnvironment.getId(), this.workspace.getActiveEnvironmentId());

        assertTrue(this.tools.deleteEnvironment(createdEnvironment.getId())
                .contains("Deleted environment"));
        assertNull(this.workspace.getActiveEnvironmentId());
        assertNull(ShellHttpSettingStore.INSTANCE.getByIid(IID).getActiveEnvironmentId());
        assertFalse(ShellHttpEnvironmentStore.INSTANCE.exist(createdEnvironment.getId()));
    }

    @Test
    public void executesSavedRequestAndTemporaryRequestWithSensitiveHeaders() {
        String saved = this.tools.executeRequest(this.request.getId());
        assertTrue(saved.contains("statusCode: 200"));
        assertTrue(saved.contains("successful: true"));
        assertTrue(saved.contains("EasyShell"));
        assertTrue(saved.contains("durationMs:"));
        assertFalse(saved.contains("super-secret-token"));

        String temporary = this.tools.executeHttpRequest(ShellHttpRequest.METHOD_GET,
                this.server.baseUrl() + "/api/headers",
                "{\"Authorization\":\"Bearer temporary-secret\",\"X-Test\":\"temporary-value\"}",
                null, null);
        assertTrue(temporary.contains("statusCode: 200"));
        assertTrue(temporary.contains("temporary-value"));
        assertTrue(temporary.contains("temporary-secret"));
    }

    @Test
    public void returnsClearErrorsAndSystemMessage() {
        assertTrue(this.tools.executeRequest("missing").contains("Request not found"));
        assertTrue(this.tools.listRequests("missing", null, 10).contains("Collection not found"));
        assertTrue(this.tools.createRequest("missing", "{\"name\":\"x\"}")
                .contains("Collection not found"));

        String message = this.tools.systemMessage(LLMExecutionMode.APPROVE);
        assertTrue(message.contains("createRequest"));
        assertTrue(message.contains("updateSettings"));
        assertTrue(message.contains("deleteCollection"));
        assertTrue(message.contains("HTTP"));
    }
}
