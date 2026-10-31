package cn.oyzh.easyshell.ssh;

import cn.oyzh.easyshell.llm.LLMTools;
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
}

