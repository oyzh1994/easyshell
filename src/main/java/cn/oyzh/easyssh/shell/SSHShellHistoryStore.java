//package cn.oyzh.easyssh.shell;
//
//import cn.oyzh.easyshell.histroy.ShellHistoryStore;
//import cn.oyzh.easyssh.SSHConst;
//import lombok.extern.slf4j.Slf4j;
//
///**
// * ssh终端命令历史
// *
// * @author oyzh
// * @since 2023/7/21
// */
//public class SSHShellHistoryStore extends ShellHistoryStore {
//
//    /**
//     * 当前实例
//     */
//    public static final SSHShellHistoryStore INSTANCE = new SSHShellHistoryStore();
//
//    {
//        this.filePath(SSHConst.STORE_PATH + "ssh_shell_history.json");
//        JulLog.info("SSHShellHistoryStore filePath:{} charset:{} init {}.", this.filePath(), this.charset(), super.init() ? "success" : "fail");
//    }
//
//}
