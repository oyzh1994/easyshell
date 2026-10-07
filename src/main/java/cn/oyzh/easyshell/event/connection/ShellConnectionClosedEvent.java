package cn.oyzh.easyshell.event.connection;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellBaseClient;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * 连接已关闭事件
 *
 * @author oyzh
 * @since 2025-03-03
 */
public class ShellConnectionClosedEvent extends Event<ShellBaseClient> implements EventFormatter {

    @Override
    public String eventFormat() {
        return String.format("[%s:%s closed] ", I18nHelper.connect(), this.data().connectName());
    }

    /**
     * 获取连接
     *
     * @return 连接
     */
    public ShellConnect connect() {
        return this.data().getShellConnect();
    }
}
