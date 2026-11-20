package cn.oyzh.easyshell.llm.tool;

import cn.oyzh.easyshell.llm.LLMExecutionMode;

public interface LLMTools extends AutoCloseable {

    String systemMessage(LLMExecutionMode executionMode);

    @Override
    default void close() {
    }
}
