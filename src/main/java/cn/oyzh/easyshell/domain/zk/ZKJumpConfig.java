// package cn.oyzh.easyshell.domain.zk;
//
// import cn.oyzh.common.util.CollectionUtil;
// import cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch;
// import cn.oyzh.ssh.domain.SSHConnect;
// import cn.oyzh.store.jdbc.Column;
// import cn.oyzh.store.jdbc.PrimaryKey;
// import cn.oyzh.store.jdbc.Table;
// import com.alibaba.fastjson2.annotation.JSONField;
//
// import java.io.Serializable;
// import java.util.ArrayList;
// import java.util.Collections;
// import java.util.List;
//
// /**
//  * zk跳板配置
//  *
//  * @author oyzh
//  * @since 2025-05-19
//  */
// @Table("t_jump_config")
// public class ZKJumpConfig extends SSHConnect implements Serializable {
//
//     /**
//      * id
//      *
//      */
//     @Column
//     @PrimaryKey
//     private String id;
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
//      * 是否启用
//      */
//     @Column
//     private Boolean enabled;
//
//     public String getIid() {
//         return iid;
//     }
//
//     public void setIid(String iid) {
//         this.iid = iid;
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
//     public void setEnabled(boolean enabled) {
//         this.enabled = enabled;
//     }
//
//     public boolean isEnabled() {
//         return this.enabled == null || this.enabled;
//     }
//
//     @JSONField(serialize = false, deserialize = false)
//     public FXToggleSwitch getEnabledStatus() {
//         FXToggleSwitch toggleSwitch = new FXToggleSwitch();
//         toggleSwitch.setSelected(this.isEnabled());
//         toggleSwitch.selectedChanged((observable, oldValue, newValue) -> {
//             this.setEnabled(newValue);
//         });
//         return toggleSwitch;
//     }
//
//     public static List<ZKJumpConfig> clone(List<ZKJumpConfig> configs) {
//         if (CollectionUtil.isEmpty(configs)) {
//             return Collections.emptyList();
//         }
//         List<ZKJumpConfig> list = new ArrayList<>();
//         for (ZKJumpConfig config : configs) {
//             ZKJumpConfig jumpConfig = new ZKJumpConfig();
//             jumpConfig.copy(config);
//             list.add(jumpConfig);
//         }
//         return list;
//     }
// }
