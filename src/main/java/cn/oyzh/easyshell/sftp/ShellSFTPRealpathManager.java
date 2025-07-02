// package cn.oyzh.easyshell.sftp;
//
// import cn.oyzh.common.thread.ThreadUtil;
// import cn.oyzh.common.util.CollectionUtil;
// import com.jcraft.jsch.SftpATTRS;
//
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Map;
// import java.util.concurrent.ConcurrentHashMap;
// import java.util.concurrent.atomic.AtomicBoolean;
//
// /**
//  * sftp链接文件管理器
//  *
//  * @author oyzh
//  * @since 2025-06-07
//  */
// @Deprecated
// public class ShellSFTPRealpathManager implements AutoCloseable {
//
//     /**
//      * sftp客户端
//      */
//     private ShellSFTPClient client;
//
//     /**
//      * 路径缓存
//      * key: 路径 value: 链接路径
//      */
//     private final Map<String, String> pathCache = new ConcurrentHashMap<>();
//
//     /**
//      * 属性缓存
//      * key: 路径:修改时间 value: 文件属性
//      */
//     private final Map<String, SftpATTRS> attrsCache = new ConcurrentHashMap<>();
//
//     /**
//      * 文件队列
//      */
//     private final List<ShellSFTPFile> queue = new ArrayList<>();
//
//     /**
//      * 运行中标志位
//      */
//     private final AtomicBoolean running = new AtomicBoolean(false);
//
//     public ShellSFTPRealpathManager(ShellSFTPClient client) {
//         this.client = client;
//     }
//
//     /**
//      * 添加文件
//      *
//      * @param files 文件列表
//      */
//     public void put(List<ShellSFTPFile> files) {
//         this.queue.addAll(files);
//         this.doRealpath();
//     }
//
//     /**
//      * 读取链接
//      */
//     private void doRealpath() {
//         if (this.running.get()) {
//             return;
//         }
//         this.running.set(true);
//         List<Runnable> tasks = new ArrayList<>();
//         // 按指定份数切割
//         List<List<ShellSFTPFile>> list = CollectionUtil.splitIntoParts(this.queue, 3);
//         // 提交到任务
//         for (List<ShellSFTPFile> files : list) {
//             Runnable task = () -> {
//                 for (ShellSFTPFile file : files) {
//                     if (!this.running.get()) {
//                         break;
//                     }
//                     if (this.client.isClosed()) {
//                         break;
//                     }
//                     try {
//                         String filePath = file.getFilePath();
//                         String linkPath = this.pathCache.get(filePath);
//                         String attrKey = filePath + ":" + file.getMTime();
//                         SftpATTRS attrs = this.attrsCache.get(attrKey);
//                         // 缓存处理
//                         if (linkPath != null) {
//                             if (attrs == null) {
//                                 attrs = this.client.stat(linkPath);
//                                 file.setLinkAttrs(attrs);
//                                 if (attrs != null) {
//                                     this.attrsCache.put(attrKey, attrs);
//                                 }
//                             } else {
//                                 file.setLinkAttrs(attrs);
//                             }
//                         } else {// 正常处理
//                             linkPath = ShellSFTPUtil.realpath(file, this.client);
//                             attrs = file.getLinkAttrs();
//                             if (linkPath != null) {
//                                 this.pathCache.put(filePath, linkPath);
//                             }
//                             if (attrs != null) {
//                                 this.attrsCache.put(attrKey, attrs);
//                             }
//                         }
//                     } catch (Exception ex) {
//                         ex.printStackTrace();
//                     }
//                 }
//             };
//             tasks.add(task);
//         }
//         // 执行任务
//         ThreadUtil.submitVirtual(tasks);
//         // 清除队列
//         this.queue.clear();
//         this.running.set(false);
//     }
//
//     /**
//      * 等待完成
//      */
//     public void waitComplete() {
//         while (this.running.get()) {
//             ThreadUtil.sleep(5);
//         }
//     }
//
//     @Override
//     public void close() throws Exception {
//         this.running.set(false);
//         this.queue.clear();
//         this.pathCache.clear();
//         this.attrsCache.clear();
//         this.client = null;
//     }
// }
