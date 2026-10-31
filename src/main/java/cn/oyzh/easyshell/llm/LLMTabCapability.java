package cn.oyzh.easyshell.llm;

import java.util.function.Supplier;

public interface LLMTabCapability {

    String getTabId();

    String getTabName();

    Supplier<? extends LLMTools> getLLMTools();

}

