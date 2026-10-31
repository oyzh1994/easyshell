package cn.oyzh.easyshell.llm;

import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

public interface LLMAssistant {

    String chat(@UserMessage String userMessage);

    TokenStream chatStream(@UserMessage String userMessage);
}