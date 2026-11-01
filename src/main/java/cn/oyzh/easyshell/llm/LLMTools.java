package cn.oyzh.easyshell.llm;

public interface LLMTools extends AutoCloseable {

    String systemMessage(LLMExecutionMode executionMode);

    @Override
    default void close() {
    }
}
