// package cn.oyzh.easyshell.domain.zk;
//
// import cn.oyzh.common.object.ObjectComparator;
// import cn.oyzh.common.object.ObjectCopier;
// import cn.oyzh.common.util.BooleanUtil;
// import cn.oyzh.common.util.CollectionUtil;
// import cn.oyzh.common.util.StringUtil;
// import cn.oyzh.store.jdbc.Column;
// import cn.oyzh.store.jdbc.PrimaryKey;
// import cn.oyzh.store.jdbc.Table;
// import com.alibaba.fastjson2.annotation.JSONField;
//
// import java.io.Serializable;
// import java.util.ArrayList;
// import java.util.Collections;
// import java.util.List;
// import java.util.Objects;
//
// /**
//  * zk连接
//  *
//  * @author oyzh
//  * @since 2020/3/6
//  */
// @Table("t_connect")
// public class ShellConnect implements Comparable<ShellConnect>, ObjectComparator<ShellConnect>, ObjectCopier<ShellConnect>, Serializable {
//
//     /**
//      * 数据id
//      */
//     @Column
//     @PrimaryKey
//     private String id;
//
//     /**
//      * 连接地址
//      */
//     @Column
//     private String host;
//
//     /**
//      * 名称
//      */
//     @Column
//     private String name;
//
//     /**
//      * 备注信息
//      */
//     @Column
//     private String remark;
//
//     /**
//      * 只读模式
//      */
//     @Column
//     private Boolean readonly;
//
//     /**
//      * 分组id
//      */
//     @Column
//     private String groupId;
//
//     /**
//      * 兼容模式
//      * null: 无
//      * 1: 兼容3.4.x版本
//      */
//     @Column
//     private Integer compatibility;
//
//     /**
//      * 监听节点
//      * false: 否
//      * null|true: 是
//      */
//     @Column
//     private Boolean listen;
//
//     /**
//      * 认证列表
//      */
//     private List<ZKAuth> auths;
//
//     /**
//      * 收藏的节点
//      */
//     private List<ZKCollect> collects;
//
//     /**
//      * 过滤列表
//      */
//     private List<ZKFilter> filters;
//
//     /**
//      * 会话超时时间
//      */
//     @Column
//     private Integer sessionTimeOut;
//
//     /**
//      * 连接超时时间
//      */
//     @Column
//     private Integer connectTimeOut;
//
// //    /**
// //     * 是否开启代理转发
// //     */
// //    @Column
// //    private Boolean enableProxy;
//
// //    /**
// //     * 代理配置
// //     */
// //    private ZKProxyConfig proxyConfig;
//
//     /**
//      * 跳板信息
//      */
//     private List<ZKJumpConfig> jumpConfigs;
//
// //    /**
// //     * 是否开启ssh转发
// //     */
// //    @Column
// //    private Boolean sshForward;
// //
// //    /**
// //     * ssh信息
// //     */
// //    private ZKSSHConfig sshConfig;
//
//     /**
//      * 是否开启ssh转发
//      */
//     @Column
//     private Boolean saslAuth;
//
//     /**
//      * ssh信息
//      */
//     private ZKSASLConfig saslConfig;
//
//     @Override
//     public void copy(ShellConnect zkConnect) {
//         this.name = zkConnect.name;
//         this.host = zkConnect.host;
//         this.remark = zkConnect.remark;
//         this.listen = zkConnect.listen;
//         this.groupId = zkConnect.groupId;
//         this.readonly = zkConnect.readonly;
//         this.compatibility = zkConnect.compatibility;
//         this.sessionTimeOut = zkConnect.sessionTimeOut;
//         this.connectTimeOut = zkConnect.connectTimeOut;
//         // 认证
//         this.auths = ZKAuth.clone(zkConnect.auths);
//         // 过滤
//         this.filters = ZKFilter.clone(zkConnect.filters);
//         // 收藏
//         this.collects = ZKCollect.clone(zkConnect.collects);
//         // sasl
//         this.saslAuth = zkConnect.saslAuth;
//         this.saslConfig = ZKSASLConfig.clone(zkConnect.saslConfig);
//         // 跳板机
//         this.jumpConfigs = ZKJumpConfig.clone(zkConnect.jumpConfigs);
//     }
//
//     /**
//      * 是否开启sasl认证
//      *
//      * @return 结果
//      */
//     public boolean isSASLAuth() {
//         return BooleanUtil.isTrue(this.saslAuth);
//     }
//
//     /**
//      * 是否只读模式
//      *
//      * @return 结果
//      */
//     public boolean isReadonly() {
//         return BooleanUtil.isTrue(this.readonly);
//     }
//
//     /**
//      * 获取会话超时
//      *
//      * @return 会话超时时间
//      */
//     public Integer getSessionTimeOut() {
//         return this.sessionTimeOut == null || this.sessionTimeOut < 1 ? 30 : this.sessionTimeOut;
//     }
//
//     /**
//      * 获取会话超时毫秒值
//      *
//      * @return 会话超时时间毫秒值
//      */
//     public int sessionTimeOutMs() {
//         return this.getSessionTimeOut() * 60 * 1000;
//     }
//
//     /**
//      * 获取连接超时
//      *
//      * @return 连接超时时间
//      */
//     public Integer getConnectTimeOut() {
//         return this.connectTimeOut == null || this.connectTimeOut < 1 ? 5 : this.connectTimeOut;
//     }
//
//     /**
//      * 是否监听节点
//      *
//      * @return 结果
//      */
//     public Boolean getListen() {
//         return this.listen == null || this.listen;
//     }
//
//     /**
//      * 获取连接超时毫秒值
//      *
//      * @return 连接超时时间毫秒值
//      */
//     public int connectTimeOutMs() {
//         return this.getConnectTimeOut() * 1000;
//     }
//
//     @Override
//     public int compareTo(ShellConnect o) {
//         if (o == null) {
//             return 1;
//         }
//         return this.name.compareToIgnoreCase(o.getName());
//     }
//
//     /**
//      * 是否兼容3.4.x版本
//      *
//      * @return 结果
//      */
//     public boolean compatibility34() {
//         return Objects.equals(1, this.compatibility);
//     }
//
//     /**
//      * 获取连接ip
//      *
//      * @return 连接ip
//      */
//     public String hostIp() {
//         if (StringUtil.isNotBlank(this.host) && this.host.contains(":")) {
//             return this.host.split(":")[0];
//         }
//         return "";
//     }
//
//     /**
//      * 获取连接端口
//      *
//      * @return 连接端口
//      */
//     public int hostPort() {
//         try {
//             if (StringUtil.isNotBlank(this.host) && !this.host.contains(",") && this.host.contains(":")) {
//                 return Integer.parseInt(this.host.split(":")[1]);
//             }
//         } catch (Exception ex) {
//             ex.printStackTrace();
//         }
//         return -1;
//     }
//
//     @Override
//     public boolean compare(ShellConnect t1) {
//         if (t1 == null) {
//             return false;
//         }
//         return StringUtil.equals(this.name, t1.name);
//     }
//
//     public String getId() {
//         return id;
//     }
//
//     public void setId(String id) {
//         this.id = id;
//     }
//
//     public String getHost() {
//         return host;
//     }
//
//     public void setHost(String host) {
//         this.host = host;
//     }
//
//     public String getName() {
//         return name;
//     }
//
//     public void setName(String name) {
//         this.name = name;
//     }
//
//     public String getRemark() {
//         return remark;
//     }
//
//     public void setRemark(String remark) {
//         this.remark = remark;
//     }
//
//     public Boolean getReadonly() {
//         return readonly;
//     }
//
//     public void setReadonly(Boolean readonly) {
//         this.readonly = readonly;
//     }
//
//     public String getGroupId() {
//         return groupId;
//     }
//
//     public void setGroupId(String groupId) {
//         this.groupId = groupId;
//     }
//
//     public Integer getCompatibility() {
//         return compatibility;
//     }
//
//     public void setCompatibility(Integer compatibility) {
//         this.compatibility = compatibility;
//     }
//
//     public void setListen(Boolean listen) {
//         this.listen = listen;
//     }
//
//     public List<ZKAuth> getAuths() {
//         return auths;
//     }
//
//     public void setAuths(List<ZKAuth> auths) {
//         this.auths = auths;
//     }
//
//     public void addAuth(ZKAuth auth) {
//         if (auth == null) {
//             return;
//         }
//         if (this.auths == null) {
//             this.auths = new ArrayList<>();
//         } else {
//             for (ZKAuth zkAuth : auths) {
//                 if (zkAuth.compare(auth)) {
//                     return;
//                 }
//             }
//         }
//         this.auths.add(auth);
//
//     }
//
//     public List<ZKCollect> getCollects() {
//         return collects;
//     }
//
//     public void setCollects(List<ZKCollect> collects) {
//         this.collects = collects;
//     }
//
//     public List<ZKFilter> getFilters() {
//         return filters;
//     }
//
//     public void setFilters(List<ZKFilter> filters) {
//         this.filters = filters;
//     }
//
//     public void setSessionTimeOut(Integer sessionTimeOut) {
//         this.sessionTimeOut = sessionTimeOut;
//     }
//
//     public void setConnectTimeOut(Integer connectTimeOut) {
//         this.connectTimeOut = connectTimeOut;
//     }
//
//     public Boolean getSaslAuth() {
//         return saslAuth;
//     }
//
//     public void setSaslAuth(Boolean saslAuth) {
//         this.saslAuth = saslAuth;
//     }
//
//     public ZKSASLConfig getSaslConfig() {
//         return saslConfig;
//     }
//
//     public void setSaslConfig(ZKSASLConfig saslConfig) {
//         this.saslConfig = saslConfig;
//     }
//
//     public List<ZKJumpConfig> getJumpConfigs() {
//         return jumpConfigs;
//     }
//
//     public void setJumpConfigs(List<ZKJumpConfig> jumpConfigs) {
//         this.jumpConfigs = jumpConfigs;
//     }
//
//     /**
//      * 是否开启跳板
//      *
//      * @return 结果
//      */
//     @JSONField(serialize = false, deserialize = false)
//     public boolean isEnableJump() {
//         // 初始化跳板配置
//         List<ZKJumpConfig> jumpConfigs = this.getJumpConfigs();
//         // 过滤配置
//         jumpConfigs = jumpConfigs == null ? Collections.emptyList() : jumpConfigs.stream().filter(ZKJumpConfig::isEnabled).toList();
//         return CollectionUtil.isNotEmpty(jumpConfigs);
//     }
// }
