package cn.oyzh.easyshell.event.dameng.connect;

import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.fx.db.DBDialect;

/**
 * @author oyzh
 * @since 2023/11/28
 */
public class DBConnectionClosedEvent extends Event<ShellDamengClient> implements EventFormatter {

    @Override
    public String eventFormat() {
        return String.format("[%s] 客户端已断开", this.data().connectName());
    }

    public ShellConnect shellConnect() {
        return this.data().getShellConnect();
    }

    public boolean isDamengType() {
        return this.data().dialect() == DBDialect.DAMENG;
    }
}
