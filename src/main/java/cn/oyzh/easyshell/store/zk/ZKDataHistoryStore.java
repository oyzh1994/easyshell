package cn.oyzh.easyshell.store.zk;

import cn.oyzh.common.exception.InvalidDataException;
import cn.oyzh.common.util.MD5Util;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.zk.ZKDataHistory;
import cn.oyzh.easyshell.util.zk.ZKACLUtil;
import cn.oyzh.easyshell.zk.ZKClient;
import cn.oyzh.store.jdbc.DeleteParam;
import cn.oyzh.store.jdbc.JdbcStandardStore;
import cn.oyzh.store.jdbc.OrderByParam;
import cn.oyzh.store.jdbc.QueryParam;
import cn.oyzh.store.jdbc.SelectParam;
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
 * zk数据历史存储
 *
 * @author oyzh
 * @since 2024/12/16
 */
public class ZKDataHistoryStore extends JdbcStandardStore<ZKDataHistory> {

    /**
     * 最大历史数量
     */
    public static int Max_Size = 50;

    /**
     * 当前实例
     */
    public static final ZKDataHistoryStore INSTANCE = new ZKDataHistoryStore();

    /**
     * 服务端路径
     */
    private static final String SERVER_PATH = "/_data_history/";

    /**
     * 加载本地历史
     *
     * @param iid  zk连接id
     * @param path zk路径
     * @return 本地历史
     */
    public List<ZKDataHistory> listLocal(String iid, String path) {
        SelectParam param = new SelectParam();
        param.addQueryParam(new QueryParam("iid", iid));
        param.addQueryParam(new QueryParam("path", MD5Util.md5Hex(path)));
        param.addQueryColumn("iid");
        param.addQueryColumn("saveTime");
        param.addQueryColumn("dataLength");
        List<ZKDataHistory> histories = super.selectList(param).reversed();
        return histories.parallelStream().sorted(Comparator.comparingLong(ZKDataHistory::getSaveTime)).collect(Collectors.toList()).reversed();
    }

    /**
     * 加载服务历史
     *
     * @param iid    zk连接id
     * @param path   zk路径
     * @param client zk客户端
     * @return 服务历史
     */
    public List<ZKDataHistory> listServer(String iid, String path, ZKClient client) {
        try {
            String dataPath = SERVER_PATH + iid + "/" + MD5Util.md5Hex(path);
            if (client.exists(dataPath)) {
                List<ZKDataHistory> histories = new ArrayList<>(24);
                for (String node : client.getChildren(dataPath)) {
                    ZKDataHistory history = new ZKDataHistory();
                    history.setIid(iid);
                    Stat stat = client.checkExists(dataPath + "/" + node);
                    history.setDataLength(stat.getDataLength());
                    try {
                        history.setSaveTime(Long.parseLong(node));
                    } catch (Exception ex) {
                        history.setSaveTime(stat.getMtime());
                    }
                    histories.add(history);
                }
                return histories.parallelStream().sorted(Comparator.comparingLong(ZKDataHistory::getSaveTime)).collect(Collectors.toList()).reversed();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * 替换
     *
     * @param model  模型
     * @param client zk客户端
     * @return 结果
     */
    public boolean replace(ZKDataHistory model, ZKClient client) {
        String iid = model.getIid();
        String path = model.getPath();
        if (StringUtil.isBlank(path) || StringUtil.isBlank(iid)) {
            throw new InvalidDataException("path", "iid");
        }
        try {
            model.setPath(MD5Util.md5Hex(path));
            this.doLocalReplace(model);
            this.doServerReplace(model, client);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * 替换本地历史
     *
     * @param model 模型
     */
    private void doLocalReplace(ZKDataHistory model) {
        // 新增数据
        boolean result = super.insert(model);
        if (result) {
            // 查询超出部分
            SelectParam selectParam = new SelectParam();
            selectParam.setLimit(1L);
            selectParam.setOffset((long) Max_Size - 1);
            selectParam.addQueryColumns("iid", "path", "saveTime");
            selectParam.addQueryParam(new QueryParam("iid", model.getIid()));
            selectParam.addQueryParam(new QueryParam("path", model.getPath()));
            selectParam.addOrderByParam(new OrderByParam("saveTime", "asc"));
            ZKDataHistory data = super.selectOne(selectParam);
            // 删除超出限制的数据
            if (data != null) {
                this.deleteLocalInner(data.getIid(), data.getPath(), data.getSaveTime());
            }
        }
    }

    /**
     * 替换服务历史
     *
     * @param model  模型
     * @param client zk客户端
     * @throws Exception 异常
     */
    private void doServerReplace(ZKDataHistory model, ZKClient client) throws Exception {
        String iid = model.getIid();
        String path = model.getPath();
        // 添加节点
        long saveTime = model.getSaveTime();
        String pPath = SERVER_PATH + iid + "/" + path;
        String dataPath = pPath + "/" + saveTime;
        if (client.exists(dataPath)) {
            client.setData(dataPath, model.getData());
        } else {
            // 权限读删
            int params = ZKACLUtil.toPermInt("rd");
            // 类型公开
            ACL acl = new ACL(params, ZooDefs.Ids.ANYONE_ID_UNSAFE);
            client.create(dataPath, model.getData(), List.of(acl), null, CreateMode.PERSISTENT, true);
        }
        // 删除超出限制的节点
        Stat stat = client.checkExists(pPath);
        if (stat != null && stat.getNumChildren() > Max_Size) {
            long minTime = -1;
            String beDelNodeNode = null;
            // 便利字节的
            for (String node : client.getChildren(dataPath)) {
                // 获取保存时间
                long nodeSaveTime;
                try {
                    nodeSaveTime = Long.parseLong(node);
                } catch (Exception ex) {
                    Stat stat1 = client.checkExists(pPath + "/" + node);
                    nodeSaveTime = stat1.getMtime();
                }
                // 更新删除节点
                if (nodeSaveTime < minTime) {
                    minTime = nodeSaveTime;
                    beDelNodeNode = node;
                }
            }
            // 删除节点
            if (beDelNodeNode != null) {
                client.delete(pPath + "/" + beDelNodeNode, null, true);
            }
        }
    }

    /**
     * 删除本地历史
     *
     * @param iid      zk连接id
     * @param path     zk路径
     * @param saveTime 保存时间
     * @return 结果
     * @see cn.oyzh.easyshell.domain.ShellConnect
     */
    public boolean deleteLocal(String iid, String path, long saveTime) {
        return this.deleteLocalInner(iid, MD5Util.md5Hex(path), saveTime);
    }

    /**
     * 删除本地历史
     *
     * @param iid      zk连接id
     * @param path     zk路径
     * @param saveTime 保存时间
     * @return 结果
     * @see cn.oyzh.easyshell.domain.ShellConnect
     */
    private boolean deleteLocalInner(String iid, String path, long saveTime) {
        DeleteParam deleteParam = new DeleteParam();
        deleteParam.addQueryParam(new QueryParam("iid", iid));
        deleteParam.addQueryParam(new QueryParam("saveTime", saveTime));
        deleteParam.addQueryParam(new QueryParam("path", path));
        deleteParam.setLimit(1L);
        return super.delete(deleteParam);
    }

    /**
     * 删除服务历史
     *
     * @param iid      zk连接id
     * @param path     zk路径
     * @param saveTime 保存时间
     * @param client   zk客户端
     * @return 结果
     * @see cn.oyzh.easyshell.domain.ShellConnect
     */
    public boolean deleteServer(String iid, String path, long saveTime, ZKClient client) {
        try {
            String dataPath = SERVER_PATH + iid + "/" + MD5Util.md5Hex(path) + "/" + saveTime;
            if (client.exists(dataPath)) {
                client.delete(dataPath);
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    protected Class<ZKDataHistory> modelClass() {
        return ZKDataHistory.class;
    }

    /**
     * 获取本地数据
     *
     * @param iid      zk连接id
     * @param path     zk路径
     * @param saveTime 保存时间
     * @return 本地数据
     */
    public byte[] getLocalData(String iid, String path, long saveTime) {
        SelectParam param = new SelectParam();
        param.addQueryParam(new QueryParam("iid", iid));
        param.addQueryParam(new QueryParam("saveTime", saveTime));
        param.addQueryParam(new QueryParam("path", MD5Util.md5Hex(path)));
        param.addQueryColumn("data");
        ZKDataHistory history = super.selectOne(param);
        return history == null ? null : history.getData();
    }

    /**
     * 获取服务数据
     *
     * @param iid      zk连接id
     * @param path     zk路径
     * @param saveTime 保存时间
     * @param client   zk客户端
     * @return 服务数据
     */
    public byte[] getServerData(String iid, String path, long saveTime, ZKClient client) {
        try {
            String dataPath = SERVER_PATH + iid + "/" + MD5Util.md5Hex(path) + "/" + saveTime;
            if (client.exists(dataPath)) {
                return client.getData(dataPath);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
