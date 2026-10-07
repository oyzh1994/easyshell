package cn.oyzh.easyshell.dto.redis;


/**
 * 客户端项目
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellRedisClientItem {

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

    /** 获取编号 */
    public int getIndex() {
        return index;
    }

    /** 设置编号 */
    public void setIndex(int index) {
        this.index = index;
    }

    /** 获取地址 */
    public String getAddr() {
        return addr;
    }

    /** 设置地址 */
    public void setAddr(String addr) {
        this.addr = addr;
    }

    /** 获取标记 */
    public String getFlags() {
        return flags;
    }

    /** 设置标记 */
    public void setFlags(String flags) {
        this.flags = flags;
    }

    /** 获取当前db */
    public String getDb() {
        return db;
    }

    /** 设置当前db */
    public void setDb(String db) {
        this.db = db;
    }

    /** 获取存活时间 */
    public String getAge() {
        return age;
    }

    /** 设置存活时间 */
    public void setAge(String age) {
        this.age = age;
    }

    /** 获取空闲时间 */
    public String getIdle() {
        return idle;
    }

    /** 设置空闲时间 */
    public void setIdle(String idle) {
        this.idle = idle;
    }

    /**
     * 从客户端信息字符串解析
     *
     * @param l 客户端信息字符串
     * @return 客户端项目
     */
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
