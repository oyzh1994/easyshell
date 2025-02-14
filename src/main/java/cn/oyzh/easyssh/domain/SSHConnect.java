package cn.oyzh.easyssh.domain;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2023/6/16
 */
public class SSHConnect implements Comparable<SSHConnect>, Serializable {

    /**
     * 数据id
     */
    @Getter
    @Setter
    private String id;

    /**
     * 连接地址
     */
    @Getter
    @Setter
    private String host;

    /**
     * 名称
     */
    @Getter
    @Setter
    private String name;

    /**
     * 备注信息
     */
    @Getter
    @Setter
    private String remark;

    /**
     * 分组id
     */
    @Getter
    @Setter
    private String groupId;

    /**
     * 认证用户
     */
    @Getter
    @Setter
    private String user;

    /**
     * 认证密码
     */
    @Getter
    @Setter
    private String password;

    /**
     * 收藏的键
     */
    @Getter
    @Setter
    private List<String> collects;

    /**
     * 连接超时时间
     */
    @Setter
    private Integer connectTimeOut;

    /**
     * 复制对象
     *
     * @param info ssh信息
     * @return 当前对象
     */
    public SSHConnect copy(@NonNull SSHConnect info) {
        this.id = info.id;
        this.name = info.name;
        this.host = info.host;
        this.user = info.user;
        this.remark = info.remark;
        this.groupId = info.groupId;
        this.collects = info.collects;
        this.password = info.password;
        this.connectTimeOut = info.connectTimeOut;
        return this;
    }

    /**
     * 是否被收藏
     *
     * @param dbIndex 数据库索引
     * @param key     键
     * @return 结果
     */
    public boolean isCollect(int dbIndex, @NonNull String key) {
        return CollectionUtil.isNotEmpty(this.collects) && this.collects.contains(this.getCollectName(dbIndex, key));
    }

    /**
     * 添加收藏
     *
     * @param dbIndex 数据库索引
     * @param key     键
     */
    public void addCollect(int dbIndex, @NonNull String key) {
        if (this.collects == null) {
            this.collects = new ArrayList<>();
        }
        String name = this.getCollectName(dbIndex, key);
        if (!this.collects.contains(name)) {
            this.collects.add(name);
        }
    }

    /**
     * 取消收藏
     *
     * @param dbIndex 数据库索引
     * @param key     键
     * @return 结果
     */
    public boolean removeCollect(int dbIndex, @NonNull String key) {
        if (this.collects != null) {
            return this.collects.remove(this.getCollectName(dbIndex, key));
        }
        return false;
    }

    /**
     * 获取收藏名称
     *
     * @param dbIndex db索引
     * @param key     键名称
     * @return 收藏名称
     */
    private String getCollectName(int dbIndex, String key) {
        return dbIndex + "_@coll@_" + key;
    }

    /**
     * 获取连接超时
     *
     * @return 连接超时
     */
    public Integer getConnectTimeOut() {
        return this.connectTimeOut == null || this.connectTimeOut < 3 ? 5 : this.connectTimeOut;
    }

    /**
     * 获取连接超时毫秒值
     *
     * @return 连接超时毫秒值
     */
    public int connectTimeOutMs() {
        return this.getConnectTimeOut() * 1000;
    }

    @Override
    public int compareTo(SSHConnect o) {
        if (o == null) {
            return 1;
        }
        return this.name.compareToIgnoreCase(o.getName());
    }

    /**
     * 获取连接ip
     *
     * @return 连接ip
     */
    public String hostIp() {
        if (StringUtil.isBlank(this.host)) {
            return "";
        }
        return this.host.split(":")[0];
    }

    /**
     * 获取连接端口
     *
     * @return 连接端口
     */
    public int hostPort() {
        if (StringUtil.isBlank(this.host)) {
            return -1;
        }
        try {
            return Integer.parseInt(this.host.split(":")[1]);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return -1;
    }
}
