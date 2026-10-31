package cn.oyzh.easyshell.llm;

import dev.langchain4j.data.message.Content;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

import java.util.List;

public interface LLMAssistant {

    String chat(@UserMessage String userMessage);

    String chat(@UserMessage List<Content> contents);

    TokenStream chatStream(@UserMessage String userMessage);

    TokenStream chatStream(@UserMessage List<Content> contents);
}
