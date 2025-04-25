package cn.oyzh.easyshell.event.connection;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.ssh.ShellClient;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2023/9/18
 */
public class ShellConnectionClosedEvent extends Event<ShellClient> implements EventFormatter {

    @Override
    public String eventFormat() {
        return String.format("[%s:%s closed] ", I18nHelper.connect(), this.data().connectName());
    }

    public ShellConnect connect() {
        return this.data().getShellConnect();
    }
}
