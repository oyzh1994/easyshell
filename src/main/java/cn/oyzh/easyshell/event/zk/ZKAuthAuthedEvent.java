package cn.oyzh.easyshell.event.zk;

import cn.oyzh.easyshell.domain.zk.ShellZKAuth;
import cn.oyzh.easyshell.trees.zk.ShellZKNodeTreeItem;
import cn.oyzh.easyshell.zk.ShellZKClient;
import cn.oyzh.event.Event;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2023/9/18
 */
public class ZKAuthAuthedEvent extends Event<ShellZKNodeTreeItem> implements EventFormatter {

    private String user;

    private String password;

    private boolean success;

    public ShellZKAuth auth() {
        return new ShellZKAuth(this.data().client().iid(), this.user, this.password);
    }

    public ShellZKClient client() {
        return this.data().client();
    }

    @Override
    public String eventFormat() {
        return String.format(
                "[%s:%s authed %s, user:%s password:%s] ",
                I18nHelper.connect(), this.data().connectName(), this.success ? I18nHelper.success() : I18nHelper.fail(), this.user, this.password
        );
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
