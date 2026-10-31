package cn.oyzh.easyshell.test.llm;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.llm.LLMMessage;
import cn.oyzh.easyshell.fx.llm.LLMView;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.fx.editor.incubator.Editor;
import cn.oyzh.fx.plus.ext.FXApplication;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.Themes;
import cn.oyzh.fx.plus.window.StageManager;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class LLMTest extends FXApplication {

    public static void main(String[] args) throws URISyntaxException {
        launch(LLMTest.class, args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        ThemeManager.apply(Themes.PRIMER_LIGHT);
        StageManager.setPrimaryStage(primaryStage);
        try {
            //test1(primaryStage);
            //test2(primaryStage);
            //test3(primaryStage);
            //test4(primaryStage);
            test5(primaryStage);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private String sk = "deepseek-sk";

    private String sessionId = "test";

    public void test1(Stage stage) {
        StreamingChatModel streamingModel = OpenAiStreamingChatModel.builder()
                //.apiKey("sk-beee474324574e2dbe47c529a24152ca")
                //.apiKey("sk-f5985088a1074af794d88636163df7d2")
                //.apiKey("sk-81f56ffe044446bea929c43235e7407c")
                .apiKey(System.getenv(sk))
                .modelName("deepseek-flash")
                .baseUrl("https://api.deepseek.com")
                .build();

        Editor chatArea = new Editor();
        stage.setScene(new Scene(chatArea));

        List<ChatMessage> userMessage = new ArrayList<>();

        UserMessage message = new UserMessage(new TextContent("你好"));
        userMessage.add(message);

// 在后台线程发起，不阻塞 JavaFX Application Thread
        new Thread(() -> {
            streamingModel.chat(userMessage, new StreamingChatResponseHandler() {

                @Override
                public void onPartialResponse(String partialResponse) {
                    Platform.runLater(() ->
                            chatArea.appendText(partialResponse));
                }

                @Override
                public void onCompleteResponse(ChatResponse completeResponse) {
                    // 流式结束，可做收尾
                }

                @Override
                public void onError(Throwable error) {
                    Platform.runLater(() ->
                            chatArea.appendText("\n[错误] " + error.getMessage()));
                }
            });
        }).start();

        stage.setWidth(600);
        stage.setHeight(800);
        stage.show();
    }

    public void test2(Stage stage) throws Throwable {
        OpenAiChatModel.OpenAiChatModelBuilder builder = new OpenAiChatModel.OpenAiChatModelBuilder()
                .apiKey(System.getenv(this.sk))
                .modelName("deepseek-flash")
                .baseUrl("https://api.deepseek.com");

        SSHTools tools = new SSHTools();

        ShellConnect connect = new ShellConnect();
        connect.setHost("192.168.3.21:22");
        connect.setUser("root");
        connect.setPassword("user@147");
        ShellSSHClient sshClient = new ShellSSHClient(connect);
        sshClient.start();
        tools.setClient(sshClient);
        OpenAiChatModel model = new OpenAiChatModel(builder);
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .tools(tools)   // 注册工具实例
                .build();

        Editor chatArea = new Editor();
        stage.setScene(new Scene(chatArea));

// 在后台线程发起，不阻塞 JavaFX Application Thread
        new Thread(() -> {
            String msg = assistant.chat(sessionId, "帮我看下服务器是否启动了docker");
            chatArea.appendText(msg);
        }).start();

        stage.setWidth(600);
        stage.setHeight(800);
        stage.show();
    }

    public void test3(Stage stage) throws Throwable {
        OpenAiChatModel.OpenAiChatModelBuilder builder = new OpenAiChatModel.OpenAiChatModelBuilder()
                .apiKey(System.getenv(this.sk))
                .modelName("deepseek-flash")
                .baseUrl("https://api.deepseek.com");

        SSHTools tools = new SSHTools();

        ShellConnect connect = new ShellConnect();
        connect.setHost("192.168.3.21:22");
        connect.setUser("root");
        connect.setPassword("user@147");
        ShellSSHClient sshClient = new ShellSSHClient(connect);
        sshClient.start();
        tools.setClient(sshClient);
        OpenAiChatModel model = new OpenAiChatModel(builder);
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .tools(tools)   // 注册工具实例
                .build();

        LLMView chatArea = new LLMView();
        chatArea.setFlexWidth("100%");
        chatArea.setFlexHeight("100%");
        stage.setScene(new Scene(chatArea));

        chatArea.setSendHandler(message -> {
            ThreadUtil.startVirtual(() -> {
                String resp = assistant.chat(sessionId, message.getMessage());
                chatArea.addAssistantMessage(resp);
            });
        });

        stage.setWidth(600);
        stage.setHeight(800);
        stage.show();
    }

    public void test4(Stage stage) throws Throwable {
        OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder builder = new OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder()
                .apiKey(System.getenv(this.sk))
                .modelName("deepseek-flash")
                .baseUrl("https://api.deepseek.com");

        SSHTools tools = new SSHTools();

        ShellConnect connect = new ShellConnect();
        connect.setHost("192.168.3.21:22");
        connect.setUser("root");
        connect.setPassword("user@147");
        ShellSSHClient sshClient = new ShellSSHClient(connect);
        sshClient.start();
        tools.setClient(sshClient);
        OpenAiStreamingChatModel model = new OpenAiStreamingChatModel(builder);
        Assistant assistant = AiServices.builder(Assistant.class)
                .streamingChatModel(model)
                .tools(tools)   // 注册工具实例
                .build();

        LLMView chatArea = new LLMView();
        chatArea.setFlexWidth("100%");
        chatArea.setFlexHeight("100%");
        stage.setScene(new Scene(chatArea));

        chatArea.setSendHandler(message -> {
            ThreadUtil.startVirtual(() -> {
                LLMMessage message1 = chatArea.addAssistantMessage("思考中...");
                AtomicBoolean first = new AtomicBoolean(true);
                //AtomicReference<LLMMessage> ref = new AtomicReference<>();
                assistant.chatStream(sessionId, message.getMessage())
                        .onPartialResponse(token -> {
                            //if (ref.get() == null) {
                            //    ref.set(chatArea.addAssistantMessage(token));
                            //} else {
                            //    chatArea.appendMessage(ref.get(), token);
                            //}
                            if (first.get()) {
                                first.set(false);
                                chatArea.replaceMessage(message1, token);
                            } else {
                                chatArea.appendMessage(message1, token);
                            }
                        })
                        .onToolExecuted(response -> { /* 流式结束 */ })
                        .onError(error -> {
                            chatArea.addErrorMessage(error.getMessage());
                        })
                        .start();
            });
        });

        stage.setWidth(600);
        stage.setHeight(800);
        stage.show();
    }

    public void test5(Stage stage) throws Throwable {
        OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder builder = new OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder()
                .apiKey(System.getenv(this.sk))
                .modelName("deepseek-flash")
                .baseUrl("https://api.deepseek.com");

        SSHTools tools = new SSHTools();

        ShellConnect connect = new ShellConnect();
        connect.setHost("192.168.3.21:22");
        connect.setUser("root");
        connect.setPassword("user@147");
        ShellSSHClient sshClient = new ShellSSHClient(connect);
        sshClient.start();
        tools.setClient(sshClient);
        OpenAiStreamingChatModel model = new OpenAiStreamingChatModel(builder);

        ChatMemoryStore store = new ChatMemoryStore() {
            @Override
            public List<ChatMessage> getMessages(Object memoryId) {
                return List.of();
            }

            @Override
            public void updateMessages(Object memoryId, List<ChatMessage> messages) {

                System.out.println("----");
            }

            @Override
            public void deleteMessages(Object memoryId) {

            }
        };

        Assistant assistant = AiServices.builder(Assistant.class)
                .streamingChatModel(model)
                .tools(tools)   // 注册工具实例
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .maxMessages(20)                    // 送给 LLM 的记忆窗口
                        .chatMemoryStore(store)             // 挂载持久化存储
                        .build())
                .build();

        LLMView chatArea = new LLMView();
        chatArea.setFlexWidth("100%");
        chatArea.setFlexHeight("100%");
        stage.setScene(new Scene(chatArea));

        chatArea.setSendHandler(message -> {
            ThreadUtil.startVirtual(() -> {
                LLMMessage message1 = chatArea.addAssistantMessage("思考中...");
                AtomicBoolean first = new AtomicBoolean(true);
                assistant.chatStream(sessionId, message.getMessage())
                        .onPartialResponse(token -> {
                            if (first.get()) {
                                first.set(false);
                                chatArea.replaceMessage(message1, token);
                            } else {
                                chatArea.appendMessage(message1, token);
                            }
                        })
                        .onToolExecuted(response -> { /* 流式结束 */ })
                        .onError(error -> {
                            chatArea.addErrorMessage(error.getMessage());
                        })
                        .start();
            });
        });

        stage.setWidth(600);
        stage.setHeight(800);
        stage.show();
    }

    public static class LLMTestStarter {

        public static void main(String[] args) throws URISyntaxException {
            LLMTest.main(args);
        }

    }

}



