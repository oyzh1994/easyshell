package cn.oyzh.easyshell.query.zk;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.query.ShellQueryUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * zk查询工具类
 *
 * @author oyzh
 * @since 2025/01/21
 */
public class ShellZKQueryUtil {

    /**
     * 节点列表
     */
    private static final Set<String> NODES = new HashSet<>();

    /**
     * 关键字
     */
    private static final Set<String> KEYWORDS = new HashSet<>();

    /**
     * 参数
     */
    private static final Set<String> PARAMS = new HashSet<>();

    static {
        // 关键字
        // 数据
        KEYWORDS.add("get");
        KEYWORDS.add("set");
        KEYWORDS.add("sync");
        // 节点
        KEYWORDS.add("rmr");
        KEYWORDS.add("delete");
        KEYWORDS.add("deleteall");
        KEYWORDS.add("create");
        // 权限
        KEYWORDS.add("setAcl");
        KEYWORDS.add("getAcl");
        // 子节点
        KEYWORDS.add("ls");
        KEYWORDS.add("ls2");
        KEYWORDS.add("stat");
        KEYWORDS.add("getEphemerals");
        KEYWORDS.add("getAllChildrenNumber");
        // 配额
        KEYWORDS.add("setquota");
        KEYWORDS.add("delquota");
        KEYWORDS.add("listquota");
        //  其他
        KEYWORDS.add("whoami");
        KEYWORDS.add("srvr");
        KEYWORDS.add("mntr");
        KEYWORDS.add("envi");
        KEYWORDS.add("conf");
        KEYWORDS.add("cons");
        KEYWORDS.add("ruok");
        KEYWORDS.add("crst");
        KEYWORDS.add("srst");
        KEYWORDS.add("stat4");
        KEYWORDS.add("wchc");
        KEYWORDS.add("wchs");
        KEYWORDS.add("wchp");
        KEYWORDS.add("dump");
        KEYWORDS.add("reqs");
        KEYWORDS.add("dirs");

        // 参数
        PARAMS.add("-s");
        PARAMS.add("-e");
        PARAMS.add("-c");
        PARAMS.add("-n");
        PARAMS.add("-b");
        PARAMS.add("-v");
    }

    public static Set<String> getKeywords() {
        return KEYWORDS;
    }

    public static Set<String> getNodes() {
        return NODES;
    }

    public static Set<String> getParams() {
        return PARAMS;
    }

    public static void setNodes(Collection<String> nodes) {
        NODES.clear();
        if (nodes != null) {
            NODES.addAll(nodes);
        }
    }


}
