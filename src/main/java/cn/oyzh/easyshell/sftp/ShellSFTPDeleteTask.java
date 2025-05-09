//package cn.oyzh.easyshell.sftp;
//
//import cn.oyzh.common.exception.ExceptionUtil;
//import cn.oyzh.common.thread.ThreadUtil;
//
///**
// * @author oyzh
// * @since 2025-04-28
// */
//public class ShellSFTPDeleteTask {
//
//    /**
//     * 远程文件
//     */
//    private final ShellSFTPFile remoteFile;
//
//    /**
//     * 客户端
//     */
//    private final ShellSFTPClient client;
//
//    public ShellSFTPDeleteTask(ShellSFTPFile remoteFile, ShellSFTPClient client) {
//        this.client = client;
//        this.remoteFile = remoteFile;
//    }
//
//    /**
//     * 执行删除
//     *
//     * @throws Exception 异常
//     */
//    public void doDelete() throws Exception {
//        try {
//            this.remoteFile.startWaiting();
//            // 执行删除
//            if (this.remoteFile.isDirectory()) {
//                this.client.deleteDirRecursive(this.remoteFile);
//            } else {
//                this.client.delete(this.remoteFile);
//            }
//        } catch (Exception ex) {// 其他
//            // 忽略中断异常
//            if (!ExceptionUtil.hasMessage(ex, "InterruptedIOException")) {
//                throw ex;
//            }
//        } finally {
//            this.remoteFile.stopWaiting();
//        }
//    }
//
//    /**
//     * 取消
//     */
//    public void cancel() {
//        ThreadUtil.interrupt(this.worker);
//    }
//
//    public String getFilePath() {
//        return this.remoteFile.getFilePath();
//    }
//
//    private Thread worker;
//
//    public void setWorker(Thread worker) {
//        this.worker = worker;
//    }
//}
