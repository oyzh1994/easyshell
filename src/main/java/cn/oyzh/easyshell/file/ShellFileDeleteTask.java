package cn.oyzh.easyshell.file;

import cn.oyzh.common.util.IOUtil;

/**
 * 文件删除任务
 *
 * @author oyzh
 * @since 2025-04-28
 */
public class ShellFileDeleteTask {

    /**
     * 远程文件
     */
    private final ShellFile remoteFile;

    /**
     * 客户端
     */
    private final ShellFileClient client;

    public ShellFileDeleteTask(ShellFile remoteFile, ShellFileClient<?> client) {
        this.client = client.forkClient();
        this.remoteFile = remoteFile;
    }

    /**
     * 执行删除
     *
     * @throws Exception 异常
     */
    public void doDelete() throws Exception {
        try {
            this.remoteFile.startWaiting();
            // 执行删除
            if (this.remoteFile.isDirectory()) {
                this.client.deleteDirRecursive(this.remoteFile);
            } else {
                this.client.delete(this.remoteFile);
            }
        } finally {
            this.remoteFile.stopWaiting();
        }
        // 关闭子客户端
        if (this.client.isForked()) {
            IOUtil.close(this.client);
        }
    }

    public String getFilePath() {
        return this.remoteFile.getFilePath();
    }
}
