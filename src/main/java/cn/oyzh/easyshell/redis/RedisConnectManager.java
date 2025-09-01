// package cn.oyzh.easyshell.redis;
//
// import cn.oyzh.common.util.CollectionUtil;
// import cn.oyzh.easyshell.domain.ShellConnect;
//
// import java.util.List;
// import java.util.stream.Collectors;
//
// /**
//  * 连接管理
//  *
//  * @author oyzh
//  * @since 2023/5/12
//  */
// public interface RedisConnectManager {
//
//     /**
//      * 添加连接
//      *
//      * @param redisConnect 连接信息
//      */
//     void addConnect( ShellConnect redisConnect);
//
//     /**
//      * 删除多个连接
//      *
//      * @param redisConnects 连接列表
//      */
//     default void addConnects(List<ShellConnect> redisConnects) {
//         if (CollectionUtil.isNotEmpty(redisConnects)) {
//             for (ShellConnect redisConnect : redisConnects) {
//                 this.addConnect(redisConnect);
//             }
//         }
//     }
//
//     /**
//      * 添加连接键
//      *
//      * @param item 连接键
//      */
//     void addConnectItem( RedisConnectTreeItem item);
//
//     /**
//      * 添加多个连接键
//      *
//      * @param items 连接键列表
//      */
//     void addConnectItems( List<RedisConnectTreeItem> items);
//
//     /**
//      * 删除连接键
//      *
//      * @param item 连接键
//      * @return 结果
//      */
//     boolean delConnectItem( RedisConnectTreeItem item);
//
//     /**
//      * 获取连接键
//      *
//      * @return 连接键
//      */
//     List<RedisConnectTreeItem> getConnectItems();
//
//     /**
//      * 获取已连接的连接节点
//      *
//      * @return 已连接的连接节点
//      */
//     default List<RedisConnectTreeItem> getConnectedItems() {
//         return this.getConnectItems().parallelStream().filter(RedisConnectTreeItem::isConnected).collect(Collectors.toList());
//     }
//
// }
