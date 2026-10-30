package cn.oyzh.easyshell.test.llm;

import dev.langchain4j.service.TokenStream;

interface Assistant {
    String chat(String userMessage);

    TokenStream chatStream(String userMessage);
}