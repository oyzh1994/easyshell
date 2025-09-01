// package cn.oyzh.easyshell.domain.redis;
//
//
// import cn.oyzh.common.object.ObjectComparator;
// import cn.oyzh.fx.plus.domain.AppGroup;
// import cn.oyzh.store.jdbc.Table;
//
// import java.util.Objects;
//
// /**
//  * @author oyzh
//  * @since 2023/6/16
//  */
// @Table("t_group")
// public class RedisGroup extends AppGroup implements ObjectComparator<RedisGroup> {
//
//     public RedisGroup() {
//         super();
//     }
//
//     public RedisGroup(String gid, String name, boolean expand) {
//         super(gid, name, expand);
//     }
//
//     @Override
//     public boolean compare(RedisGroup t1) {
//         if (Objects.equals(this, t1)) {
//             return true;
//         }
//         return Objects.equals(t1.getName(), this.getName());
//     }
// }
