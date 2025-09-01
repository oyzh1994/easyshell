// package cn.oyzh.easyshell.domain.redis;
//
//
// import cn.oyzh.fx.plus.domain.AppSetting;
// import cn.oyzh.store.jdbc.Column;
// import cn.oyzh.store.jdbc.Table;
//
// /**
//  * redis设置
//  *
//  * @author oyzh
//  * @since 2023/6/16
//  */
// @Table("t_setting")
// public class RedisSetting extends AppSetting {
//
//     /**
//      * 键加载上限
//      */
//     @Column
//     private Integer keyLoadLimit;
//
//     public Integer getKeyLoadLimit() {
//         return keyLoadLimit;
//     }
//
//     public void setKeyLoadLimit(Integer keyLoadLimit) {
//         this.keyLoadLimit = keyLoadLimit;
//     }
//
//     public int keyLoadLimit() {
//         return this.keyLoadLimit == null ? 0 : this.keyLoadLimit;
//     }
//
//     /**
//      * 行页码限制
//      */
//     @Column
//     private Integer rowPageLimit;
//
//     public void setRowPageLimit(Integer rowPageLimit) {
//         if (rowPageLimit == null || rowPageLimit <= 0) {
//             this.rowPageLimit = 100;
//         } else {
//             this.rowPageLimit = rowPageLimit;
//         }
//     }
//
//     public Integer getRowPageLimit() {
//         if (this.rowPageLimit == null || this.rowPageLimit <= 0) {
//             return 100;
//         }
//         return this.rowPageLimit;
//     }
//
//     @Override
//     public void copy(Object o) {
//         super.copy(o);
//         if (o instanceof RedisSetting setting) {
//             this.keyLoadLimit = setting.keyLoadLimit;
//         }
//     }
// }
