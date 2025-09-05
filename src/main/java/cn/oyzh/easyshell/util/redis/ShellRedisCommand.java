package cn.oyzh.easyshell.util.redis;


/**
 * @author oyzh
 * @since 2024/5/29
 */
public class ShellRedisCommand {

    private String desc;

    private String args;

    private String command;

    private String available;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getAvailable() {
        return available;
    }

    public void setAvailable(String available) {
        this.available = available;
    }
}
