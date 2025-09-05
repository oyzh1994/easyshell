package cn.oyzh.easyshell.util.zk;


/**
 * @author oyzh
 * @since 2025-01-02
 */
public class ShellZKClientActionArgument {

    private Object value;

    private String argument;

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getArgument() {
        return argument;
    }

    public void setArgument(String argument) {
        this.argument = argument;
    }

    public ShellZKClientActionArgument(String argument, Object value) {
        this.argument = argument;
        this.value = value;
    }

    public ShellZKClientActionArgument(String argument) {
        this.argument = argument;
    }

    public ShellZKClientActionArgument(Object value) {
        this.value = value;
    }

    public static ShellZKClientActionArgument ofArgument(Object value) {
        return new ShellZKClientActionArgument(value);
    }

    public static ShellZKClientActionArgument ofArgument(String argument) {
        return new ShellZKClientActionArgument(argument);
    }

    public static ShellZKClientActionArgument ofArgument(String arg, Object value) {
        return new ShellZKClientActionArgument(arg, value);
    }

}
