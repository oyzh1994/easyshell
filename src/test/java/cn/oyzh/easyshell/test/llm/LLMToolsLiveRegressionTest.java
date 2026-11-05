package cn.oyzh.easyshell.test.llm;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.dameng.ShellDamengLLMTools;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.easyshell.mariadb.ShellMariadbLLMTools;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.mongo.ShellMongoLLMTools;
import cn.oyzh.easyshell.mongo.record.MongoRecord;
import cn.oyzh.easyshell.mysql.ShellMysqlLLMTools;
import cn.oyzh.easyshell.query.mongo.ShellMongoExecuteResult;
import cn.oyzh.easyshell.redis.ShellRedisLLMTools;
import cn.oyzh.easyshell.sqlite.ShellSqliteLLMTools;
import cn.oyzh.easyshell.ssh.ShellSSHLLMTools;
import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.easyshell.zk.ShellZKLLMTools;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import org.apache.zookeeper.ZooKeeper;
import org.bson.Document;
import org.junit.Assume;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Live regression tests for LLM database tools.
 *
 * <p>Run MongoDB tests with {@code -Dllm.live=true -Dmongo.password=...}.</p>
 */
public class LLMToolsLiveRegressionTest {

    @Test
    public void exposesValidToolSpecifications() {
        List<ToolSpecification> mongoSpecs = ToolSpecifications.toolSpecificationsFrom(new ShellMongoLLMTools());
        List<ToolSpecification> zkSpecs = ToolSpecifications.toolSpecificationsFrom(new ShellZKLLMTools());

        ToolSpecifications.validateSpecifications(mongoSpecs);
        ToolSpecifications.validateSpecifications(zkSpecs);
        mongoSpecs.forEach(spec -> System.out.println("Mongo tool spec: " + spec.toJson()));
        zkSpecs.forEach(spec -> System.out.println("ZooKeeper tool spec: " + spec.toJson()));
        String mongoSchema = mongoSpecs.stream()
                .filter(spec -> spec.name().equals("executeScript"))
                .findFirst().orElseThrow().toJson();
        String mongoRunCommandSchema = mongoSpecs.stream()
                .filter(spec -> spec.name().equals("runCommand"))
                .findFirst().orElseThrow().toJson();
        String zkSchema = zkSpecs.stream()
                .filter(spec -> spec.name().equals("executeCommand"))
                .findFirst().orElseThrow().toJson();
        assertTrue(mongoSchema, mongoSchema.contains("\"database\""));
        assertTrue(mongoSchema, mongoSchema.contains("\"script\""));
        assertTrue(mongoRunCommandSchema, mongoRunCommandSchema.contains("\"database\""));
        assertTrue(mongoRunCommandSchema, mongoRunCommandSchema.contains("\"command\""));
        assertTrue(zkSchema, zkSchema.contains("\"command\""));
        assertFalse(mongoSchema, mongoSchema.contains("\"arg0\""));
        assertFalse(mongoRunCommandSchema, mongoRunCommandSchema.contains("\"arg0\""));
        assertFalse(zkSchema, zkSchema.contains("\"arg0\""));
        Set<String> mongoNames = Set.copyOf(mongoSpecs.stream().map(ToolSpecification::name).toList());
        Set<String> zkNames = Set.copyOf(zkSpecs.stream().map(ToolSpecification::name).toList());
        assertTrue(mongoNames.containsAll(Set.of(
                "executeScript", "runCommand", "mongoExamples", "listDatabases", "listCollections",
                "listBuckets", "listFunctions", "listUsers", "getServerVersion", "pingDatabase", "countCollection")));
        assertTrue(zkNames.containsAll(Set.of(
                "executeCommand", "getNodeData", "listChildren", "checkNodeExists",
                "getNodeStat", "getNodeAcl", "getAllChildrenCount")));
    }

    @Test
    public void allLlmToolsUseExplicitParameterNames() {
        List<Object> tools = List.of(
                new ShellMongoLLMTools(),
                new ShellZKLLMTools(),
                new ShellRedisLLMTools(),
                new ShellMysqlLLMTools(),
                new ShellMariadbLLMTools(),
                new ShellDamengLLMTools(),
                new ShellSSHLLMTools()
        );
        for (Object tool : tools) {
            List<ToolSpecification> specs = ToolSpecifications.toolSpecificationsFrom(tool);
            ToolSpecifications.validateSpecifications(specs);
            for (ToolSpecification spec : specs) {
                String json = spec.toJson();
                for (int index = 0; index < 10; index++) {
                    assertFalse(tool.getClass().getSimpleName() + ": " + json,
                            json.contains("\"arg" + index + "\""));
                }
            }
        }
    }

    @Test
    public void rawJdbcResultSetsAreSafelyExposedAsTextTools() {
        List<Object> tools = List.of(
                new ShellSqliteLLMTools(),
                new ShellMysqlLLMTools(),
                new ShellMariadbLLMTools(),
                new ShellDamengLLMTools()
        );
        for (Object tool : tools) {
            Set<String> names = Set.copyOf(ToolSpecifications.toolSpecificationsFrom(tool).stream()
                    .map(ToolSpecification::name)
                    .toList());
            assertTrue(tool.getClass().getSimpleName(), names.contains("executeSqlRaw"));
        }
    }

    @Test
    public void executesToolsThroughAiServices() {
        RecordingTool tool = new RecordingTool();
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(new ForcedToolChatModel())
                .tools(tool)
                .build();

        assertEquals("tool-result: hello", assistant.chat("run"));
        assertEquals(1, tool.calls.get());
        assertEquals("hello", tool.lastValue.get());
    }

    @Test
    public void executesToolsThroughStreamingAiServices() throws Exception {
        RecordingTool tool = new RecordingTool();
        Assistant assistant = AiServices.builder(Assistant.class)
                .streamingChatModel(new ForcedToolStreamingChatModel())
                .tools(tool)
                .build();
        CountDownLatch complete = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();

        TokenStream stream = assistant.chatStream("run");
        stream.onToolExecuted(execution -> {
                    tool.streamToolExecuted.set(true);
                })
                .onCompleteResponse(response -> complete.countDown())
                .onError(ex -> {
                    error.set(ex);
                    complete.countDown();
                })
                .start();

        assertTrue("streaming tool call timed out", complete.await(5, TimeUnit.SECONDS));
        assertNull(error.get());
        assertEquals(1, tool.calls.get());
        assertEquals("hello", tool.lastValue.get());
        assertTrue(tool.streamToolExecuted.get());
    }

    @Test
    public void mongoToolReflectsRealDatabaseChanges() throws Throwable {
        Assume.assumeTrue("Enable with -Dllm.live=true", Boolean.getBoolean("llm.live"));
        String host = System.getProperty("mongo.host", "127.0.0.1");
        int port = Integer.parseInt(System.getProperty("mongo.port", "27017"));
        String user = System.getProperty("mongo.user", "admin");
        String password = System.getProperty("mongo.password");
        String authDatabase = System.getProperty("mongo.authDatabase", "admin");
        Assume.assumeTrue("MongoDB endpoint is not reachable", isReachable(host, port));
        Assume.assumeTrue("mongo.password is required", password != null && !password.isBlank());

        ShellConnect connect = new ShellConnect();
        connect.setId("llm-live-mongo-" + UUID.randomUUID());
        connect.setName("llm-live-mongo");
        connect.setType(ShellPrototype.MONGO);
        connect.setHost(host + ":" + port);
        connect.setUser(user);
        connect.setPassword(password);
        connect.setMongoAuthDatabase(authDatabase);
        connect.setReadonly(false);
        connect.setConnectTimeOut(5);

        ShellMongoClient client = new ShellMongoClient(connect);
        String database = "easyshell_llm_test_" + UUID.randomUUID().toString().replace("-", "");
        try {
            client.start(5_000);
            assertTrue(client.isConnected());
            ShellMongoLLMTools tools = new ShellMongoLLMTools();
            tools.setClient(client);

            String insert = tools.executeScript(database, """
                    db.getCollection('users').insertMany([
                        {name: 'Alice', status: 'active'},
                        {name: 'Bob', status: 'inactive'}
                    ]);
                    """);
            System.out.println("Mongo insert result: " + insert);
            assertTrue(insert, insert.contains("2 rows affected"));
            MongoRecord alice = findMongoRecord(client, database, "{name: 'Alice'}");
            assertNotNull(alice);
            assertEquals("active", alice.getValue("status"));

            String query = tools.executeScript(database, """
                    db.getCollection('users').find({status: 'active'});
                    """);
            System.out.println("Mongo query result: " + query);
            assertTrue(query, query.contains("Alice"));
            assertFalse(query, query.contains("Bob"));

            String update = tools.executeScript(database, """
                    db.getCollection('users').updateOne(
                        {name: 'Alice'},
                        {$set: {verified: true}}
                    );
                    """);
            System.out.println("Mongo update result: " + update);
            assertTrue(update, update.contains("1 rows affected"));
            alice = findMongoRecord(client, database, "{name: 'Alice'}");
            assertNotNull(alice);
            assertEquals(Boolean.TRUE, alice.getValue("verified"));

            String ping = tools.runCommand(database, "{\"ping\":1}");
            System.out.println("Mongo runCommand ping result: " + ping);
            assertEquals(1.0, ((Number) Document.parse(ping).get("ok")).doubleValue(), 0.0);

            String commandInsert = tools.runCommand(database, """
                    {
                      "insert": "command_items",
                      "documents": [
                        {"name": "Charlie", "active": true},
                        {"name": "Dora", "active": false}
                      ]
                    }
                    """);
            System.out.println("Mongo runCommand insert result: " + commandInsert);
            assertEquals(2, ((Number) Document.parse(commandInsert).get("n")).intValue());

            String commandQuery = tools.runCommand(database, """
                    {
                      "find": "command_items",
                      "filter": {"active": true},
                      "limit": 10
                    }
                    """);
            System.out.println("Mongo runCommand find result: " + commandQuery);
            assertTrue(commandQuery, commandQuery.contains("Charlie"));
            assertFalse(commandQuery, commandQuery.contains("Dora"));

            String commandUpdate = tools.runCommand(database, """
                    {
                      "update": "command_items",
                      "updates": [
                        {
                          "q": {"name": "Charlie"},
                          "u": {"$set": {"verified": true}},
                          "multi": false
                        }
                      ]
                    }
                    """);
            System.out.println("Mongo runCommand update result: " + commandUpdate);
            assertEquals(1, ((Number) Document.parse(commandUpdate).get("nModified")).intValue());

            String commandCount = tools.runCommand(database, """
                    {
                      "count": "command_items",
                      "query": {"verified": true}
                    }
                    """);
            System.out.println("Mongo runCommand count result: " + commandCount);
            assertEquals(1, ((Number) Document.parse(commandCount).get("n")).intValue());

            String commandDelete = tools.runCommand(database, """
                    {
                      "delete": "command_items",
                      "deletes": [
                        {"q": {"name": "Dora"}, "limit": 1}
                      ]
                    }
                    """);
            System.out.println("Mongo runCommand delete result: " + commandDelete);
            assertEquals(1, ((Number) Document.parse(commandDelete).get("n")).intValue());

            String invalidCommand = tools.runCommand(database, "{\"notARealCommand\":1}");
            System.out.println("Mongo runCommand invalid result: " + invalidCommand);
            assertTrue(invalidCommand, invalidCommand.startsWith("Execution failed:"));

            String invalidScript = tools.executeScript(database, "use users; show collections;");
            System.out.println("Mongo invalid script result: " + invalidScript);
            assertTrue(invalidScript, invalidScript.startsWith("Execution failed: No executable MongoDB statement"));

            String runtimeError = tools.executeScript(database,
                    "db.getCollection('users').notARealMethod();");
            System.out.println("Mongo runtime error result: " + runtimeError);
            assertTrue(runtimeError, runtimeError.startsWith("ERROR:"));
            assertFalse(runtimeError, runtimeError.contains("ERROR: ok"));
        } finally {
            try {
                client.dropDatabase(database);
            } catch (Exception ignored) {
            }
            client.close();
        }
    }

    @Test
    public void zookeeperToolReflectsRealNodeChanges() throws Throwable {
        Assume.assumeTrue("Enable with -Dllm.live=true", Boolean.getBoolean("llm.live"));
        String host = System.getProperty("zk.host", "127.0.0.1");
        int port = Integer.parseInt(System.getProperty("zk.port", "2181"));
        Assume.assumeTrue("ZooKeeper endpoint is not reachable", isReachable(host, port));

        ShellConnect connect = new ShellConnect();
        connect.setId("llm-live-zk-" + UUID.randomUUID());
        connect.setName("llm-live-zk");
        connect.setType(ShellPrototype.ZOOKEEPER);
        connect.setHost(host + ":" + port);
        connect.setReadonly(false);
        connect.setConnectTimeOut(5);

        ShellZKClient client = new ShellZKClient(connect);
        String root = "/easyshell-llm-live-" + UUID.randomUUID().toString().replace("-", "");
        String child = root + "/child";
        try {
            client.start(5_000);
            assertTrue(client.isConnected());
            ZooKeeper zooKeeper = client.getZooKeeper();
            assertNotNull(zooKeeper);
            ShellZKLLMTools tools = new ShellZKLLMTools();
            tools.setClient(client);

            String initialData = "initial-" + root.substring(root.length() - 8);
            String create = tools.executeCommand("create " + root + " '" + initialData + "'");
            System.out.println("ZK create result: " + create);
            assertTrue(create, create.contains("Created"));
            assertNotNull(zooKeeper.exists(root, false));
            assertEquals(initialData,
                    new String(zooKeeper.getData(root, false, null), StandardCharsets.UTF_8));

            String updatedData = "updated-" + root.substring(root.length() - 8);
            String set = tools.executeCommand("set " + root + " '" + updatedData + "'");
            System.out.println("ZK set result: " + set);
            assertEquals("OK", set);
            assertEquals(updatedData,
                    new String(zooKeeper.getData(root, false, null), StandardCharsets.UTF_8));

            String get = tools.executeCommand("get " + root);
            System.out.println("ZK get result: " + get);
            assertTrue(get, get.contains(updatedData));

            String createChild = tools.executeCommand("create " + child + " 'child';");
            System.out.println("ZK create child with semicolon result: " + createChild);
            assertTrue(createChild, createChild.contains("Created"));
            assertNotNull(zooKeeper.exists(child, false));

            String list = tools.executeCommand("ls " + root);
            System.out.println("ZK list result: " + list);
            assertTrue(list, list.contains("child"));

            String stat = tools.executeCommand("stat " + root);
            System.out.println("ZK stat result: " + stat);
            assertTrue(stat, stat.contains("cversion = "));

            String deleteChild = tools.executeCommand("delete " + child);
            System.out.println("ZK delete child result: " + deleteChild);
            assertEquals("OK", deleteChild);
            assertNull(zooKeeper.exists(child, false));

            String invalid = tools.executeCommand("notARealZooKeeperCommand");
            System.out.println("ZK invalid command result: " + invalid);
            assertTrue(invalid, invalid.startsWith("Execution failed: Command not found"));

            String ruok = tools.executeCommand("ruok");
            System.out.println("ZK ruok result: " + ruok);
            assertFalse(ruok, ruok.isBlank());

            String deleteRoot = tools.executeCommand("rmr " + root);
            System.out.println("ZK rmr result: " + deleteRoot);
            assertEquals("OK", deleteRoot);
            assertNull(zooKeeper.exists(root, false));
        } finally {
            try {
                if (client.isConnected()) {
                    client.delete(root, -1, true);
                }
            } catch (Exception ignored) {
            }
            client.close();
        }
    }

    private static boolean isReachable(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 1_000);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private static MongoRecord findMongoRecord(ShellMongoClient client, String database, String filter) {
        ShellMongoExecuteResult result = client.executeSingleScript(database,
                "db.getCollection('users').findOne(" + filter + ");");
        assertTrue(result.isSuccess());
        if (result.getRecords() == null || result.getRecords().isEmpty()) {
            return null;
        }
        return result.getRecords().getFirst();
    }

    private interface Assistant {

        String chat(String message);

        TokenStream chatStream(String message);
    }

    private static class RecordingTool {

        private final AtomicInteger calls = new AtomicInteger();

        private final AtomicReference<String> lastValue = new AtomicReference<>();

        private final java.util.concurrent.atomic.AtomicBoolean streamToolExecuted =
                new java.util.concurrent.atomic.AtomicBoolean();

        @Tool("Record the supplied value")
        public String record(@P(name = "value", value = "Value to record") String value) {
            this.calls.incrementAndGet();
            this.lastValue.set(value);
            return "tool-result: " + value;
        }
    }

    private abstract static class ForcedToolModelSupport {

        protected static ChatResponse toolCallResponse() {
            ToolExecutionRequest request = ToolExecutionRequest.builder()
                    .id("call-1")
                    .name("record")
                    .arguments("{\"value\":\"hello\"}")
                    .build();
            return ChatResponse.builder()
                    .aiMessage(AiMessage.from(request))
                    .build();
        }

        protected static ChatResponse finalResponse() {
            return ChatResponse.builder()
                    .aiMessage(AiMessage.from("tool-result: hello"))
                    .build();
        }

        protected static boolean hasToolResult(ChatRequest request) {
            return request.messages().stream().anyMatch(ToolExecutionResultMessage.class::isInstance);
        }
    }

    private static class ForcedToolChatModel extends ForcedToolModelSupport implements ChatModel {

        @Override
        public ChatResponse doChat(ChatRequest request) {
            return hasToolResult(request) ? finalResponse() : toolCallResponse();
        }
    }

    private static class ForcedToolStreamingChatModel extends ForcedToolModelSupport implements StreamingChatModel {

        @Override
        public void doChat(ChatRequest request, StreamingChatResponseHandler handler) {
            if (hasToolResult(request)) {
                handler.onPartialResponse("tool-result: hello");
                handler.onCompleteResponse(finalResponse());
            } else {
                handler.onCompleteResponse(toolCallResponse());
            }
        }
    }
}
