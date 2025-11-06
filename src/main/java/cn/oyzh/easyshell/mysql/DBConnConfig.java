package cn.oyzh.easyshell.mysql;

/**
 * @author oyzh
 * @since 2024-09-06
 */
public class DBConnConfig {

    private String host;

    private Integer port;

    private String sid;

    private String username;

    private String serviceName;

    public String getConnectionString(DBDialect dialect) {
        return "jdbc:mysql://" + this.host + ":" + this.port + "/";
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }


}
