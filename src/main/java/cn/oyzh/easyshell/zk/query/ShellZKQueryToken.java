package cn.oyzh.easyshell.zk.query;

import cn.oyzh.easyshell.util.zk.ShellZKNodeUtil;
import cn.oyzh.fx.db.query.DBQueryToken;

/**
 * zk查询token
 *
 * @author oyzh
 * @since 2025/01/21
 */
public class ShellZKQueryToken extends DBQueryToken {

    public boolean isPossibilityKeyword() {
        return this.getToken() == null;
    }

    public boolean isPossibilityNode() {
        return this.getToken() != null && this.isNotEmpty() && this.getToken() == ' ';
    }

    public boolean isPossibilityParam() {
        return this.getToken() != null && this.getToken() == '-';
    }

    public String getPath() {
        if (this.getContent().startsWith("/")) {
            return ShellZKNodeUtil.getParentPath(this.getContent());
        }
        return null;
    }
}
