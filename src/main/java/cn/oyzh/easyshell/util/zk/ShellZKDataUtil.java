package cn.oyzh.easyshell.util.zk;

import cn.oyzh.common.util.MD5Util;
import cn.oyzh.easyshell.dto.zk.ShellZKHistoryData;
import cn.oyzh.easyshell.zk.ShellZKClient;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * zk节点工具类
 *
 * @author oyzh
 * @since 2025/09/06
 */
public class ShellZKDataUtil {

    /**
     * 服务端路径
     */
    private static final String SERVER_PATH = "/_data_history/";

    /**
     * 加载数据历史
     *
     * @param path   zk路径
     * @param client zk客户端
     * @return 服务历史
     */
    public static List<ShellZKHistoryData> listHistory(String path, ShellZKClient client) {
        try {
            String dataPath = SERVER_PATH  + MD5Util.md5Hex(path);
            if (client.exists(dataPath)) {
                List<ShellZKHistoryData> histories = new ArrayList<>(24);
                for (String node : client.getChildren(dataPath)) {
                    ShellZKHistoryData history = new ShellZKHistoryData();
                    Stat stat = client.checkExists(dataPath + "/" + node);
                    history.setDataLength(stat.getDataLength());
                    try {
                        history.setSaveTime(Long.parseLong(node));
                    } catch (Exception ex) {
                        history.setSaveTime(stat.getMtime());
                    }
                    histories.add(history);
                }
                return histories.parallelStream().sorted(Comparator.comparingLong(ShellZKHistoryData::getSaveTime)).collect(Collectors.toList()).reversed();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * 添加数据历史
     *
     * @param path   路径
     * @param data   数据
     * @param client zk客户端
     * @throws Exception 异常
     */
    public static void addHistory(String path, byte[] data, ShellZKClient client) throws Exception {
        // 添加节点
        long saveTime = System.currentTimeMillis();
        String pPath = SERVER_PATH + MD5Util.md5Hex(path);
        String dataPath = pPath + "/" + saveTime;
        if (client.exists(dataPath)) {
            client.setData(dataPath, data);
        } else {
            // 权限读删
            int params = ShellZKACLUtil.toPermInt("rd");
            // 类型公开
            ACL acl = new ACL(params, ZooDefs.Ids.ANYONE_ID_UNSAFE);
            client.create(dataPath, data, List.of(acl), null, CreateMode.PERSISTENT, true);
        }
    }

    /**
     * 删除数据历史
     *
     * @param path     zk路径
     * @param saveTime 保存时间
     * @param client   zk客户端
     * @return 结果
     * @see cn.oyzh.easyshell.domain.ShellConnect
     */
    public static boolean deleteHistory(String path, long saveTime, ShellZKClient client) {
        try {
            String dataPath = SERVER_PATH + MD5Util.md5Hex(path) + "/" + saveTime;
            if (client.exists(dataPath)) {
                client.delete(dataPath);
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 获取历史数据
     *
     * @param path     zk路径
     * @param saveTime 保存时间
     * @param client   zk客户端
     * @return 服务数据
     */
    public static byte[] getHistory(String path, long saveTime, ShellZKClient client) {
        try {
            String dataPath = SERVER_PATH +MD5Util.md5Hex(path) + "/" + saveTime;
            if (client.exists(dataPath)) {
                return client.getData(dataPath);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

}
