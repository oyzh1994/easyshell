package cn.oyzh.easyshell.llm;

import cn.oyzh.easyshell.llm.tool.LLMTools;

import java.util.function.Supplier;

public interface LLMTabCapability {

    String getTabId();

    String getTabName();

    Supplier<? extends LLMTools> getToolsFunc();

}

