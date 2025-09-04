// package cn.oyzh.easyshell.domain.zk;
//
// import cn.oyzh.store.jdbc.Column;
// import cn.oyzh.store.jdbc.PrimaryKey;
// import cn.oyzh.store.jdbc.Table;
//
// import java.io.Serializable;
//
// /**
//  * zk查询
//  *
//  * @author oyzh
//  * @since 2025-01-20
//  */
// @Table("t_zk_query")
// public class ZKQuery implements Serializable {
//
//     /**
//      * 连接id
//      *
//      * @see ShellConnect
//      */
//     @Column
//     private String iid;
//
//     /**
//      * 主键
//      */
//     @Column
//     @PrimaryKey
//     private String uid;
//
//     /**
//      * 名称
//      */
//     @Column
//     private String name;
//
//     /**
//      * 内容
//      */
//     @Column
//     private String content;
//
//     public String getIid() {
//         return iid;
//     }
//
//     public void setIid(String iid) {
//         this.iid = iid;
//     }
//
//     public String getUid() {
//         return uid;
//     }
//
//     public void setUid(String uid) {
//         this.uid = uid;
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
//     public String getContent() {
//         return content;
//     }
//
//     public void setContent(String content) {
//         this.content = content;
//     }
// }
