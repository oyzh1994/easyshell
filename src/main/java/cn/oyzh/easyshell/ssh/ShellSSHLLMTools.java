package cn.oyzh.easyshell.ssh;

import cn.oyzh.easyshell.llm.LLMExecutionMode;
import cn.oyzh.easyshell.llm.LLMTools;
import cn.oyzh.easyshell.llm.LLMUtil;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

public class ShellSSHLLMTools implements LLMTools {

    private ShellSSHClient client;

    public void setClient(ShellSSHClient client) {
        this.client = client;
    }

    @Tool("执行ssh命令")
    public String execCommand(
            @P("命令") String command) {
        return this.client.exec(command);
    }

    @Override
    public String systemMessage(LLMExecutionMode executionMode) {
        String base = LLMUtil.readLlmText("ssh_tool.txt");
        String languageRule = LLMUtil.languageSystemMessage();
        String executionModeText = LLMUtil.executionSystemMessage(executionMode);
        return base + "\n\n" + languageRule + "\n\n" + executionModeText;
    }
}

