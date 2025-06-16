package cn.oyzh.easyshell.file;

import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.IOUtil;

import java.util.function.Consumer;

/**
 * 文件删除任务
 *
 * @author oyzh
 * @since 2025-04-28
 */
public class ShellFileDeleteTask {

    /**
     * 工作线程
     */
    private Thread worker;

    /**
     * 远程文件
     */
    private final ShellFile remoteFile;

    /**
     * 客户端
     */
    private ShellFileClient client;

    /**
     * 状态
     */
    private transient ShellFileStatus status;

    public ShellFileDeleteTask(ShellFile remoteFile, ShellFileClient<?> client) {
        this.client = client;
        this.remoteFile = remoteFile;
        this.status = ShellFileStatus.IN_PREPARATION;
    }

    /**
     * 执行删除
     *
     * @param finishCallback 结束回调
     * @param errorCallback  错误回调
     */
    public void doDelete(Runnable finishCallback, Consumer<Exception> errorCallback) {
        this.worker = ThreadUtil.start(() -> {
            try {
                this.client = this.client.forkClient();
                this.doDelete();
            } catch (Exception ex) {
                errorCallback.accept(ex);
            } finally {
                finishCallback.run();
            }
        });
    }

    /**
     * 执行删除
     *
     * @throws Exception 异常
     */
    private void doDelete() throws Exception {
        try {
            this.status = ShellFileStatus.EXECUTE_ING;
            this.remoteFile.startWaiting();
            // 执行删除
            if (this.remoteFile.isDirectory()) {
                this.client.deleteDirRecursive(this.remoteFile);
            } else {
                this.client.delete(this.remoteFile);
            }
        } catch (Exception ex) {
            if (this.status != ShellFileStatus.CANCELED && !ExceptionUtil.isInterrupt(ex)) {
                this.status = ShellFileStatus.FAILED;
            }
            throw ex;
        } finally {
            this.remoteFile.stopWaiting();
        }
        if (this.status != ShellFileStatus.CANCELED && this.status != ShellFileStatus.FAILED) {
            this.status = ShellFileStatus.FINISHED;
        }
        // 关闭子客户端
        if (this.client.isForked()) {
            IOUtil.close(this.client);
        }
    }

    public String getFilePath() {
        return this.remoteFile.getFilePath();
    }

    /**
     * 取消
     */
    public void cancel() {
        this.status = ShellFileStatus.CANCELED;
        ThreadUtil.interrupt(this.worker);
    }

    /**
     * 是否失败
     *
     * @return 结果
     */
    public boolean isFailed() {
        return this.status == ShellFileStatus.FAILED;
    }

    /**
     * 是否取消
     *
     * @return 结果
     */
    public boolean isCanceled() {
        return this.status == ShellFileStatus.CANCELED;
    }
}
