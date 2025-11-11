package cn.oyzh.easyshell.event.mysql.sql;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;

/**
 * @author oyzh
 * @since 2024/06/29
 */
public class ShellPrintSqlEvent extends Event<String> implements EventFormatter {

    private ShellConnect connect;

    public void setConnect(ShellConnect connect) {
        this.connect = connect;
    }

    @Override
    public String eventFormat() {
        return " " + this.connect.getName() + " > " + this.data();
    }
}
