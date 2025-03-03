package cn.oyzh.easyssh.event.connection;

import cn.oyzh.easyssh.domain.SSHConnect;
import cn.oyzh.easyssh.ssh.SSHClient;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2023/9/19
 */
public class SSHConnectionLostEvent extends Event<SSHClient> implements EventFormatter {

    @Override
    public String eventFormat() {
        return String.format("[%s:%s lost] ", I18nHelper.connect(), this.data().connectName());
    }

    public SSHConnect connect() {
        return this.data().sshConnect();
    }
}
