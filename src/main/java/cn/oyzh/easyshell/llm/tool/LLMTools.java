package cn.oyzh.easyshell.llm.tool;

import cn.oyzh.easyshell.llm.LLMExecutionMode;

public interface LLMTools extends AutoCloseable {

    /**
     * Returns the execution scope represented by this tool group.
     *
     * @return tool scope
     */
    default LLMToolScope scope() {
        return LLMToolScope.SESSION;
    }

    String systemMessage(LLMExecutionMode executionMode);

    @Override
    default void close() {
    }
}
