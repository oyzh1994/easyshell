package cn.oyzh.easyshell.dto.redis;


/**
 * 客户端项目
 *
 * @author oyzh
 * @since 2023/8/1
 */
public class ShellRedisClientItem {
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getFlags() {
        return flags;
    }

    public void setFlags(String flags) {
        this.flags = flags;
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getIdle() {
        return idle;
    }

    public void setIdle(String idle) {
        this.idle = idle;
    }

    /**
     * 编号
     */
    private int index;

    /**
     * 地址
     */
    private String addr;

    /**
     * 标记
     */
    private String flags;

    /**
     * 当前db
     */
    private String db;

    /**
     * 存活时间
     */
    private String age;

    /**
     * 空闲时间
     */
    private String idle;

    public static ShellRedisClientItem from(String l) {
        String[] arr = l.split(" ");
        ShellRedisClientItem item = new ShellRedisClientItem();
        for (String s : arr) {
            if (s.toLowerCase().startsWith("age")) {
                item.age = s.split("=")[1];
            } else if (s.toLowerCase().startsWith("addr")) {
                item.addr = s.split("=")[1];
            } else if (s.toLowerCase().startsWith("db")) {
                item.db = s.split("=")[1];
            } else if (s.toLowerCase().startsWith("flags")) {
                item.flags = s.split("=")[1];
            } else if (s.toLowerCase().startsWith("idle")) {
                item.idle = s.split("=")[1];
            }
        }
        return item;
    }
}
