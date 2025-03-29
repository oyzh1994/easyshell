package cn.oyzh.easyshell.process;

import cn.oyzh.easyshell.shell.ShellClient;

import java.util.List;

/**
 * 进程执行器
 *
 * @author oyzh
 * @since 25/03/29
 */
public class ProcessExec implements AutoCloseable {

    private ShellClient client;

    public ProcessExec(ShellClient client) {
        this.client = client;
    }

    @Override
    public void close() throws Exception {
        this.client = null;
    }

    public List<ProcessInfo> ps() {
        String output = this.client.exec("ps -auxe");
        return ProcessParser.ps(output);
    }

    public String kill(int pid) {
        return this.client.exec("kill -9 " + pid);
    }
}
