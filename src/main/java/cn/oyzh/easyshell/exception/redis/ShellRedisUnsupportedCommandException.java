package cn.oyzh.easyshell.exception.redis;

import cn.oyzh.easyshell.exception.ShellException;
import cn.oyzh.i18n.I18nHelper;

/**
 * Redis不支持的命令异常
 *
 * @author oyzh
 * @since 2023/7/31
 */
public class ShellRedisUnsupportedCommandException extends ShellException {

    /**
     * 构造函数
     *
     * @param serverVersion    服务版本
     * @param supportedVersion 支持命令的最低服务版本
     * @param command          命令
     */
    public ShellRedisUnsupportedCommandException(String serverVersion, String supportedVersion, String command) {
        super(I18nHelper.cmd() + " [" + command + "] " + I18nHelper.notSupport());
        // super("指令:" + command + " 不支持，服务版本为:" + serverVersion + " 最低支持命令的服务版本为:" + supportedVersion);
    }
}
