// package cn.oyzh.easyshell.domain;
//
// import cn.oyzh.common.object.ObjectCopier;
// import cn.oyzh.common.util.StringUtil;
// import cn.oyzh.store.jdbc.Column;
// import cn.oyzh.store.jdbc.PrimaryKey;
// import cn.oyzh.store.jdbc.Table;
// import com.alibaba.fastjson2.annotation.JSONField;
//
// import java.io.Serializable;
//
// /**
//  * 同步
//  *
//  * @author oyzh
//  * @since 2025/10/11
//  */
// @Table("t_sync")
// public class ShellSync implements Serializable, ObjectCopier<ShellSync> {
//
//     /**
//      * 数据id
//      */
//     @Column
//     @PrimaryKey
//     private String id;
//
//     /**
//      * gitee更新id
//      */
//     @Column
//     private String giteeId;
//
//     /**
//      * gitee令牌
//      */
//     @Column
//     private String giteeToken;
//
//     /**
//      * github更新id
//      */
//     @Column
//     private String githubId;
//
//     /**
//      * github令牌
//      */
//     @Column
//     private String githubToken;
//
//     /**
//      * 更新时间
//      */
//     @Column
//     private Long syncTime;
//
//     /**
//      * 同步类型
//      * gitee
//      * github
//      */
//     @Column
//     private String syncType;
//
//     /**
//      * 同步密钥
//      */
//     @Column
//     private boolean syncKey;
//
//     /**
//      * 同步分组
//      */
//     @Column
//     private boolean syncGroup;
//
//     /**
//      * 同步片段
//      */
//     @Column
//     private boolean syncSnippet;
//
//     /**
//      * 同步连接
//      */
//     @Column
//     private boolean syncConnect;
//
//     @Override
//     public void copy(ShellSync shellKey) {
//         this.syncTime = shellKey.syncTime;
//         this.giteeId = shellKey.giteeId;
//         this.giteeToken = shellKey.giteeToken;
//         this.githubId = shellKey.githubId;
//         this.githubToken = shellKey.githubToken;
//         this.syncType = shellKey.syncType;
//         this.syncKey = shellKey.syncKey;
//         this.syncGroup = shellKey.syncGroup;
//         this.syncSnippet = shellKey.syncSnippet;
//         this.syncConnect = shellKey.syncConnect;
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
//     public String getGiteeId() {
//         return giteeId;
//     }
//
//     public void setGiteeId(String giteeId) {
//         this.giteeId = giteeId;
//     }
//
//     public String getGithubId() {
//         return githubId;
//     }
//
//     public void setGithubId(String githubId) {
//         this.githubId = githubId;
//     }
//
//     public Long getSyncTime() {
//         return syncTime;
//     }
//
//     public void setSyncTime(Long syncTime) {
//         this.syncTime = syncTime;
//     }
//
//     public String getGiteeToken() {
//         return giteeToken;
//     }
//
//     public void setGiteeToken(String giteeToken) {
//         this.giteeToken = giteeToken;
//     }
//
//     public String getGithubToken() {
//         return githubToken;
//     }
//
//     public void setGithubToken(String githubToken) {
//         this.githubToken = githubToken;
//     }
//
//     public String getSyncType() {
//         return syncType;
//     }
//
//     public void setSyncType(String syncType) {
//         this.syncType = syncType;
//     }
//
//     public boolean isSyncKey() {
//         return syncKey;
//     }
//
//     public void setSyncKey(boolean syncKey) {
//         this.syncKey = syncKey;
//     }
//
//     public boolean isSyncGroup() {
//         return syncGroup;
//     }
//
//     public void setSyncGroup(boolean syncGroup) {
//         this.syncGroup = syncGroup;
//     }
//
//     public boolean isSyncSnippet() {
//         return syncSnippet;
//     }
//
//     public void setSyncSnippet(boolean syncSnippet) {
//         this.syncSnippet = syncSnippet;
//     }
//
//     public boolean isSyncConnect() {
//         return syncConnect;
//     }
//
//     public void setSyncConnect(boolean syncConnect) {
//         this.syncConnect = syncConnect;
//     }
//
//     @JSONField(serialize = false,deserialize = false)
//     public boolean isGiteeType() {
//         return syncType == null || StringUtil.equalsIgnoreCase(syncType,"gitee");
//     }
//
//     @JSONField(serialize = false,deserialize = false)
//     public boolean isGithubType() {
//         return StringUtil.equalsIgnoreCase(syncType,"github");
//     }
// }
