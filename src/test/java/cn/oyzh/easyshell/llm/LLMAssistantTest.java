package cn.oyzh.easyshell.llm;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.AiServices;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class LLMAssistantTest {

    @Test
    public void returnsThinkingAndTextFromNonStreamingResponse() {
        ChatModel model = new ChatModel() {
            @Override
            public ChatResponse doChat(ChatRequest request) {
                return ChatResponse.builder()
                        .aiMessage(AiMessage.builder()
                                .thinking("check the result")
                                .text("the result is ready")
                                .build())
                        .build();
            }
        };
        LLMAssistant assistant = AiServices.builder(LLMAssistant.class)
                .chatModel(model)
                .build();

        ChatResponse response = assistant.chatResponse(List.of(TextContent.from("run")));

        assertEquals("check the result", LLMResponseUtil.finalThinking(response));
        assertEquals("the result is ready", LLMResponseUtil.finalText(response));
    }
}
