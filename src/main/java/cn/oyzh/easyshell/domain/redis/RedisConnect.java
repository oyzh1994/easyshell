// package cn.oyzh.easyshell.domain.redis;
//
// import cn.oyzh.common.object.ObjectComparator;
// import cn.oyzh.common.util.BooleanUtil;
// import cn.oyzh.common.util.CollectionUtil;
// import cn.oyzh.common.util.StringUtil;
// import cn.oyzh.store.jdbc.Column;
// import cn.oyzh.store.jdbc.PrimaryKey;
// import cn.oyzh.store.jdbc.Table;
// import com.alibaba.fastjson2.annotation.JSONField;
//
// import java.io.Serializable;
// import java.util.Collections;
// import java.util.List;
// import java.util.Objects;
//
// /**
//  * @author oyzh
//  * @since 2023/6/16
//  */
// @Table("t_connect")
// public class ShellConnect implements Comparable<ShellConnect>, ObjectComparator<ShellConnect>, Serializable {
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
//      * 分组id
//      */
//     @Column
//     private String groupId;
//
//     /**
//      * 认证用户
//      */
//     @Column
//     private String user;
//
//     /**
//      * 认证密码
//      */
//     @Column
//     private String password;
//
//     /**
//      * 只读模式
//      */
//     @Column
//     private Boolean readonly;
//
//     /**
//      * 收藏的键
//      */
//     private List<RedisCollect> collects;
//
//     /**
//      * 过滤列表
//      */
//     private List<RedisFilter> filters;
//
//     /**
//      * 连接超时时间
//      */
//     @Column
//     private Integer connectTimeOut;
//
//     /**
//      * 执行超时时间
//      */
//     @Column
//     private Integer executeTimeOut;
//
//     /**
//      * 跳板信息
//      */
//     private List<RedisJumpConfig> jumpConfigs;
//
//     /**
//      * 复制对象
//      *
//      * @param redisConnect redis连接
//      * @return 当前对象
//      */
//     public ShellConnect copy( ShellConnect redisConnect) {
//         this.name = redisConnect.name;
//         this.host = redisConnect.host;
//         this.user = redisConnect.user;
//         this.remark = redisConnect.remark;
//         this.groupId = redisConnect.groupId;
//         this.readonly = redisConnect.readonly;
//         this.password = redisConnect.password;
//         this.connectTimeOut = redisConnect.connectTimeOut;
//         // 过滤
//         this.filters = RedisFilter.clone(redisConnect.filters);
//         // 收藏
//         this.collects = RedisCollect.clone(redisConnect.collects);
//         // 跳板机
//         this.jumpConfigs = RedisJumpConfig.clone(redisConnect.jumpConfigs);
//         return this;
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
//      * 获取连接超时
//      *
//      * @return 连接超时
//      */
//     public Integer getConnectTimeOut() {
//         return this.connectTimeOut == null || this.connectTimeOut < 3 ? 5 : this.connectTimeOut;
//     }
//
//     /**
//      * 获取连接超时毫秒值
//      *
//      * @return 连接超时毫秒值
//      */
//     public int connectTimeOutMs() {
//         return this.getConnectTimeOut() * 1000;
//     }
//
//     /**
//      * 获取执行超时
//      *
//      * @return 执行超时
//      */
//     public Integer getExecuteTimeOut() {
//         return this.executeTimeOut == null || this.executeTimeOut < 3 ? 5 : this.executeTimeOut;
//     }
//
//     /**
//      * 获取执行超时毫秒值
//      *
//      * @return 执行超时毫秒值
//      */
//     public int executeTimeOutMs() {
//         return this.getExecuteTimeOut() * 1000;
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
//     public String getGroupId() {
//         return groupId;
//     }
//
//     public void setGroupId(String groupId) {
//         this.groupId = groupId;
//     }
//
//     public String getUser() {
//         return user;
//     }
//
//     public void setUser(String user) {
//         this.user = user;
//     }
//
//     public String getPassword() {
//         return password;
//     }
//
//     public void setPassword(String password) {
//         this.password = password;
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
//     public List<RedisCollect> getCollects() {
//         return collects;
//     }
//
//     public void setCollects(List<RedisCollect> collects) {
//         this.collects = collects;
//     }
//
//     public List<RedisFilter> getFilters() {
//         return filters;
//     }
//
//     public void setFilters(List<RedisFilter> filters) {
//         this.filters = filters;
//     }
//
//     public void setConnectTimeOut(Integer connectTimeOut) {
//         this.connectTimeOut = connectTimeOut;
//     }
//
//     public void setExecuteTimeOut(Integer executeTimeOut) {
//         this.executeTimeOut = executeTimeOut;
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
//      * 获取连接ip
//      *
//      * @return 连接ip
//      */
//     public String hostIp() {
//         if (StringUtil.isBlank(this.host)) {
//             return "";
//         }
//         return this.host.split(":")[0];
//     }
//
//     /**
//      * 获取连接端口
//      *
//      * @return 连接端口
//      */
//     public int hostPort() {
//         if (StringUtil.isBlank(this.host)) {
//             return -1;
//         }
//         try {
//             return Integer.parseInt(this.host.split(":")[1]);
//         } catch (Exception ex) {
//             ex.printStackTrace();
//         }
//         return -1;
//     }
//
//     @Override
//     public boolean compare(ShellConnect t1) {
//         if (Objects.equals(this, t1)) {
//             return true;
//         }
//         return Objects.equals(t1.name, this.name);
//     }
//
//     /**
//      * 获取认证方式
//      *
//      * @return 0:无需认证 1:密码认证 2:用户密码认证
//      */
//     @JSONField(serialize = false, deserialize = false)
//     public int getAuthType() {
//         if (StringUtil.isNotBlank(this.user) && StringUtil.isNotBlank(this.password)) {
//             return 2;
//         }
//         if (StringUtil.isNotBlank(this.password)) {
//             return 1;
//         }
//         return 0;
//     }
//
//     public List<RedisJumpConfig> getJumpConfigs() {
//         return jumpConfigs;
//     }
//
//     public void setJumpConfigs(List<RedisJumpConfig> jumpConfigs) {
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
//         List<RedisJumpConfig> jumpConfigs = this.getJumpConfigs();
//         // 过滤配置
//         jumpConfigs = jumpConfigs == null ? Collections.emptyList() : jumpConfigs.stream().filter(RedisJumpConfig::isEnabled).toList();
//         return CollectionUtil.isNotEmpty(jumpConfigs);
//     }
// }
