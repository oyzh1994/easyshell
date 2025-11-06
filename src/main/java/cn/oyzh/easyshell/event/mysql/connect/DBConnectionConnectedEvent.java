package cn.oyzh.easyshell.event.mysql.connect;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.mysql.MysqlClient;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;

/**
 * @author oyzh
 * @since 2023/11/28
 */
public class DBConnectionConnectedEvent extends Event<MysqlClient> implements  EventFormatter {

    @Override
    public String eventFormat() {
        return String.format("[%s] 客户端已连接", this.data().connectName());
    }

    public ShellConnect dbConnect() {
        return this.data().getDbConnect();
    }
}
