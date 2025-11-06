package cn.oyzh.easyshell.mysql.database;


/**
 * db连接
 *
 * @author oyzh
 * @since 2023/8/10
 */
public class MysqlDatabase {

    /**
     * 名称
     */
    private String name;

    /**
     * 端口
     */
    private int port = 3306;

    /**
     * 用户
     */
    private String user;

    /**
     * 密码
     */
    private String password;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
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
}
