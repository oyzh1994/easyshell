package cn.oyzh.easyshell.event.connection;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.internal.ShellBaseClient;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * 连接成功事件
 *
 * @author oyzh
 * @since 2023/9/18
 */
public class ShellConnectionConnectedEvent extends Event<ShellBaseClient> implements EventFormatter {

    @Override
    public String eventFormat() {
        return String.format("[%s:%s connected] " , I18nHelper.connect(), this.data().connectName());
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
