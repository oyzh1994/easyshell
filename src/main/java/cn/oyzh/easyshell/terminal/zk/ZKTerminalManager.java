package cn.oyzh.easyshell.terminal.zk;

import cn.oyzh.easyshell.terminal.zk.basic.ZKConnectTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.zk.basic.ZKVersionTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.zk.cli.ZKAddAuthTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.zk.cli.ZKCloseTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.zk.cli.ZKCreateTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.zk.cli.ZKDelQuotaTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.zk.cli.ZKDeleteTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.zk.cli.ZKDeleteallTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.zk.cli.ZKGetAclTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.zk.cli.ZKGetAllChildrenNumberCommandHandler;
import cn.oyzh.easyshell.terminal.zk.cli.ZKGetConfigTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.zk.cli.ZKGetEphemeralsCommandHandler;
import cn.oyzh.easyshell.terminal.zk.cli.ZKGetTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.zk.cli.ZKListQuotaTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.zk.cli.ZKLs2TerminalCommandHandler;
import cn.oyzh.easyshell.terminal.zk.cli.ZKLsTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.zk.cli.ZKReconfigTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.zk.cli.ZKRemoveWatchesTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.zk.cli.ZKRmrTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.zk.cli.ZKSetAclTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.zk.cli.ZKSetQuotaTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.zk.cli.ZKSetTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.zk.cli.ZKStatTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.zk.cli.ZKSyncTerminalCommandHandler;
import cn.oyzh.easyshell.terminal.zk.cli.ZKWhoAmITerminalCommandHandler;
import cn.oyzh.easyshell.terminal.zk.fourletterword.ZKConfCommandHandler;
import cn.oyzh.easyshell.terminal.zk.fourletterword.ZKConsCommandHandler;
import cn.oyzh.easyshell.terminal.zk.fourletterword.ZKCrstCommandHandler;
import cn.oyzh.easyshell.terminal.zk.fourletterword.ZKDirsCommandHandler;
import cn.oyzh.easyshell.terminal.zk.fourletterword.ZKDumpCommandHandler;
import cn.oyzh.easyshell.terminal.zk.fourletterword.ZKEnviCommandHandler;
import cn.oyzh.easyshell.terminal.zk.fourletterword.ZKKillCommandHandler;
import cn.oyzh.easyshell.terminal.zk.fourletterword.ZKMntrCommandHandler;
import cn.oyzh.easyshell.terminal.zk.fourletterword.ZKReqsCommandHandler;
import cn.oyzh.easyshell.terminal.zk.fourletterword.ZKRuokCommandHandler;
import cn.oyzh.easyshell.terminal.zk.fourletterword.ZKSrstCommandHandler;
import cn.oyzh.easyshell.terminal.zk.fourletterword.ZKSrvrCommandHandler;
import cn.oyzh.easyshell.terminal.zk.fourletterword.ZKStatCommandHandler;
import cn.oyzh.easyshell.terminal.zk.fourletterword.ZKWchcCommandHandler;
import cn.oyzh.easyshell.terminal.zk.fourletterword.ZKWchpCommandHandler;
import cn.oyzh.easyshell.terminal.zk.fourletterword.ZKWchsCommandHandler;
import cn.oyzh.fx.terminal.standard.ClearTerminalCommandHandler;
import cn.oyzh.fx.terminal.standard.HelpTerminalCommandHandler;
import cn.oyzh.fx.terminal.util.TerminalManager;

/**
 * @author oyzh
 * @since 2024-12-30
 */

public class ZKTerminalManager {

    /**
     * 注册处理器
     */
    public static void registerHandlers() {
        // 标准命令
        TerminalManager.registerHandler(HelpTerminalCommandHandler.class);
        TerminalManager.registerHandler(ClearTerminalCommandHandler.class);

        // 基础命令
        TerminalManager.registerHandler(ZKConnectTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKVersionTerminalCommandHandler.class);

        // zk命令
        TerminalManager.registerHandler(ZKAddAuthTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKCloseTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKCreateTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKDeleteallTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKDeleteTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKDelQuotaTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKGetAclTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKGetAllChildrenNumberCommandHandler.class);
        TerminalManager.registerHandler(ZKGetConfigTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKGetEphemeralsCommandHandler.class);
        TerminalManager.registerHandler(ZKGetTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKListQuotaTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKLs2TerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKLsTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKReconfigTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKRemoveWatchesTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKRmrTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKSetAclTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKSetQuotaTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKSetTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKStatTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKSyncTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKWhoAmITerminalCommandHandler.class);

        // 四字命令
        TerminalManager.registerHandler(ZKConfCommandHandler.class);
        TerminalManager.registerHandler(ZKConsCommandHandler.class);
        TerminalManager.registerHandler(ZKCrstCommandHandler.class);
        TerminalManager.registerHandler(ZKDirsCommandHandler.class);
        TerminalManager.registerHandler(ZKDumpCommandHandler.class);
        TerminalManager.registerHandler(ZKEnviCommandHandler.class);
        TerminalManager.registerHandler(ZKKillCommandHandler.class);
        TerminalManager.registerHandler(ZKMntrCommandHandler.class);
        TerminalManager.registerHandler(ZKReqsCommandHandler.class);
        TerminalManager.registerHandler(ZKRuokCommandHandler.class);
        TerminalManager.registerHandler(ZKSrstCommandHandler.class);
        TerminalManager.registerHandler(ZKSrvrCommandHandler.class);
        TerminalManager.registerHandler(ZKStatCommandHandler.class);
        TerminalManager.registerHandler(ZKWchcCommandHandler.class);
        TerminalManager.registerHandler(ZKWchpCommandHandler.class);
        TerminalManager.registerHandler(ZKWchsCommandHandler.class);
    }
}
