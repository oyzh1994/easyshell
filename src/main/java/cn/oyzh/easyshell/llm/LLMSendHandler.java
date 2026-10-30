package cn.oyzh.easyshell.llm;

@FunctionalInterface
public interface LLMSendHandler {
    void send(LLMMessage message);
}