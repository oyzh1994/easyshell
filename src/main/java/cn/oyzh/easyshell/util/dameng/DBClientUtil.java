//package cn.oyzh.easyshell.util.dameng;
//
//import cn.oyzh.common.util.StringUtil;
//import cn.oyzh.easyshell.dameng.ShellDamengClient;
//import cn.oyzh.easydameng.domain.DamengConnect;
//import cn.oyzh.fx.db.DBDialect;
//
///**
// * db客户端封装
// *
// * @author oyzh
// * @since 2020/6/8
// */
//public class DBClientUtil {
//
//    public static ShellDamengClient newClient(DamengConnect info) {
//        if (StringUtil.isBlank(info.getType()) || DBDialect.valueOf(info.getType()) == DBDialect.DAMENG) {
//            return new ShellDamengClient(info);
//        }
//        if (DBDialect.valueOf(info.getType()) == DBDialect.DAMENG) {
//            return new ShellDamengClient(info);
//        }
//        return null;
//    }
//
//}
