package cn.oyzh.easyshell.test.llm;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

interface Assistant {
    String chat(@MemoryId String sessionId, @UserMessage String userMessage);

    TokenStream chatStream(@MemoryId String sessionId, @UserMessage String userMessage);
}