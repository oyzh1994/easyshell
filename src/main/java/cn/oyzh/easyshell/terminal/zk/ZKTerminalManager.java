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
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, HelpTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ClearTerminalCommandHandler.class);

        // 基础命令
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKConnectTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKVersionTerminalCommandHandler.class);

        // zk命令
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKAddAuthTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKCloseTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKCreateTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKDeleteallTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKDeleteTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKDelQuotaTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKGetAclTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKGetAllChildrenNumberCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKGetConfigTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKGetEphemeralsCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKGetTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKListQuotaTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKLs2TerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKLsTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKReconfigTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKRemoveWatchesTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKRmrTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKSetAclTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKSetQuotaTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKSetTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKStatTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKSyncTerminalCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKWhoAmITerminalCommandHandler.class);

        // 四字命令
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKConfCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKConsCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKCrstCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKDirsCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKDumpCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKEnviCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKKillCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKMntrCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKReqsCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKRuokCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKSrstCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKSrvrCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKStatCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKWchcCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKWchpCommandHandler.class);
        TerminalManager.registerHandler(ZKTerminalPane.TERMINAL_NAME, ZKWchsCommandHandler.class);
    }
}
