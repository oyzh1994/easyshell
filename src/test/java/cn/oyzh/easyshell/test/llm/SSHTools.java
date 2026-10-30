package cn.oyzh.easyshell.test.llm;

import cn.oyzh.easyshell.ssh.ShellSSHClient;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

public class SSHTools {

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
