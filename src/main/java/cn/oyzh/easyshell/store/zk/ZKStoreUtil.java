// package cn.oyzh.easyshell.store.zk;
//
// import cn.oyzh.common.SysConst;
// import cn.oyzh.common.file.FileUtil;
// import cn.oyzh.common.json.JSONUtil;
// import cn.oyzh.common.util.CollectionUtil;
// import cn.oyzh.common.util.StringUtil;
// import cn.oyzh.easyshell.domain.zk.ZKAuth;
// import cn.oyzh.easyshell.domain.zk.ZKCollect;
// import cn.oyzh.easyshell.domain.zk.ZKConnect;
// import cn.oyzh.easyshell.domain.zk.ZKFilter;
// import cn.oyzh.easyshell.domain.zk.ZKGroup;
// import cn.oyzh.easyshell.domain.zk.ZKSetting;
// import cn.oyzh.easyshell.terminal.zk.ZKTerminalHistory;
// import cn.oyzh.fx.plus.information.MessageBox;
// import cn.oyzh.i18n.I18nHelper;
// import cn.oyzh.store.jdbc.JdbcConst;
// import cn.oyzh.store.jdbc.JdbcDialect;
// import cn.oyzh.store.jdbc.JdbcManager;
// import com.alibaba.fastjson2.JSONArray;
// import com.alibaba.fastjson2.JSONObject;
//
// import java.io.File;
// import java.util.ArrayList;
// import java.util.List;
//
// /**
//  * zk存储工具类
//  *
//  * @author oyzh
//  * @since 2024-09-23
//  */
//
// public class ZKStoreUtil {
//
//     /**
//      * 执行初始化
//      */
//     public static void init() {
//         JdbcConst.dbCacheSize(1024);
//         JdbcConst.dbDialect(JdbcDialect.H2);
//         JdbcConst.dbFile(ZKConst.getStorePath() + "db");
//         try {
//             JdbcManager.takeoff();
//         } catch (Exception ex) {
//             if (StringUtil.containsAny(ex.getMessage(), "Database may be already in use")) {
//                 MessageBox.warn(I18nHelper.programTip1());
//             }
//         }
//     }
//
//     /**
//      * 加载旧版本分组数据
//      *
//      * @return 旧版本分组数据
//      */
//     public static List<ZKGroup> loadGroups() {
//         List<ZKGroup> groups = new ArrayList<>(24);
//         try {
//             String storePath = SysConst.storeDir();
//             String file = storePath + File.separator + "zk_group.json";
//             if (FileUtil.exist(file)) {
//                 String json = FileUtil.readUtf8String(file);
//                 JSONArray array = JSONUtil.parseArray(json);
//                 for (int i = 0; i < array.size(); i++) {
//                     JSONObject obj = array.getJSONObject(i);
//                     ZKGroup group = new ZKGroup();
//                     if (obj.containsKey("gid")) {
//                         group.setGid(obj.getString("gid"));
//                     }
//                     if (obj.containsKey("name")) {
//                         group.setName(obj.getString("name"));
//                     }
//                     if (obj.containsKey("expand")) {
//                         group.setExpand(obj.getBooleanValue("expand"));
//                     }
//                     groups.add(group);
//                 }
//             }
//         } catch (Exception ex) {
//             ex.printStackTrace();
//         }
//         return groups;
//     }
//
//     /**
//      * 加载旧版本连接数据
//      *
//      * @return 旧版本连接数据
//      */
//     public static List<ZKConnect> loadConnects() {
//         List<ZKConnect> connects = new ArrayList<>(24);
//         try {
//             String storePath = SysConst.storeDir();
//             String file = storePath + File.separator + "zk_info.json";
//             if (FileUtil.exist(file)) {
//                 String json = FileUtil.readUtf8String(file);
//                 JSONArray array = JSONUtil.parseArray(json);
//                 for (int i = 0; i < array.size(); i++) {
//                     JSONObject obj = array.getJSONObject(i);
//                     ZKConnect connect = new ZKConnect();
//                     if (obj.containsKey("id")) {
//                         connect.setId(obj.getString("id"));
//                     }
//                     if (obj.containsKey("name")) {
//                         connect.setName(obj.getString("name"));
//                     }
//                     if (obj.containsKey("host")) {
//                         connect.setHost(obj.getString("host"));
//                     }
//                     if (obj.containsKey("listen")) {
//                         connect.setListen(obj.getBooleanValue("listen"));
//                     }
// //                    if (obj.containsKey("sshForward")) {
// //                        connect.setSshForward(obj.getBooleanValue("sshForward"));
// //                    }
//                     if (obj.containsKey("collects")) {
//                         List<String> collects = obj.getList("collects", String.class);
//                         if (CollectionUtil.isNotEmpty(collects)) {
//                             List<ZKCollect> collectList = new ArrayList<>();
//                             for (String collect : collects) {
//                                 collectList.add(new ZKCollect(connect.getId(), collect));
//                             }
//                             connect.setCollects(collectList);
//                         }
// //                        connect.setCollects(obj.getBeanList("collects", String.class));
//                     }
//                     if (obj.containsKey("remark")) {
//                         connect.setRemark(obj.getString("remark"));
//                     }
//                     if (obj.containsKey("groupId")) {
//                         connect.setGroupId(obj.getString("groupId"));
//                     }
//                     if (obj.containsKey("readonly")) {
//                         connect.setReadonly(obj.getBooleanValue("readonly"));
//                     }
//                     if (obj.containsKey("compatibility")) {
//                         connect.setCompatibility(obj.getIntValue("compatibility"));
//                     }
//                     if (obj.containsKey("sessionTimeOut")) {
//                         connect.setSessionTimeOut(obj.getIntValue("sessionTimeOut"));
//                     }
//                     if (obj.containsKey("connectTimeOut")) {
//                         connect.setConnectTimeOut(obj.getIntValue("connectTimeOut"));
//                     }
// //                    if (obj.containsKey("sshInfo")) {
// //                        JSONObject object = obj.getJSONObject("sshInfo");
// //                        ZKSSHConfig sshConnect = new ZKSSHConfig();
// //                        if (object.containsKey("port")) {
// //                            sshConnect.setPort(object.getInt("port"));
// //                        }
// //                        if (object.containsKey("host")) {
// //                            sshConnect.setHost(object.getString("host"));
// //                        }
// //                        if (object.containsKey("user")) {
// //                            sshConnect.setUser(object.getString("user"));
// //                        }
// //                        if (object.containsKey("timeout")) {
// //                            sshConnect.setTimeout(object.getInt("timeout"));
// //                        }
// //                        if (object.containsKey("password")) {
// //                            sshConnect.setPassword(object.getString("password"));
// //                        }
// ////                        connect.setSshConfig(sshConnect);
// //                    }
//                     connects.add(connect);
//                 }
//             }
//         } catch (Exception ex) {
//             ex.printStackTrace();
//         }
//         return connects;
//     }
//
//     /**
//      * 加载旧版本过滤数据
//      *
//      * @return 旧版本过滤数据
//      */
//     public static List<ZKFilter> loadFilters() {
//         List<ZKFilter> filters = new ArrayList<>(24);
//         try {
//             String storePath = SysConst.storeDir();
//             String file = storePath + File.separator + "zk_filter.json";
//             if (FileUtil.exist(file)) {
//                 String json = FileUtil.readUtf8String(file);
//                 JSONArray array = JSONUtil.parseArray(json);
//                 for (int i = 0; i < array.size(); i++) {
//                     JSONObject obj = array.getJSONObject(i);
//                     ZKFilter filter = new ZKFilter();
//                     if (obj.containsKey("kw")) {
//                         filter.setKw(obj.getString("kw"));
//                     }
//                     if (obj.containsKey("uid")) {
//                         filter.setUid(obj.getString("uid"));
//                     }
//                     if (obj.containsKey("enable")) {
//                         filter.setEnable(obj.getBooleanValue("enable"));
//                     }
//                     if (obj.containsKey("partMatch")) {
//                         filter.setPartMatch(obj.getBooleanValue("partMatch"));
//                     }
//                     filters.add(filter);
//                 }
//             }
//         } catch (Exception ex) {
//             ex.printStackTrace();
//         }
//         return filters;
//     }
//
//     /**
//      * 加载旧版本认证数据
//      *
//      * @return 旧版本认证数据
//      */
//     public static List<ZKAuth> loadAuths() {
//         List<ZKAuth> auths = new ArrayList<>(24);
//         try {
//             String storePath = SysConst.storeDir();
//             String file = storePath + File.separator + "zk_auth.json";
//             if (FileUtil.exist(file)) {
//                 String json = FileUtil.readUtf8String(file);
//                 JSONArray array = JSONUtil.parseArray(json);
//                 for (int i = 0; i < array.size(); i++) {
//                     JSONObject obj = array.getJSONObject(i);
//                     ZKAuth auth = new ZKAuth();
//                     if (obj.containsKey("uid")) {
//                         auth.setUid(obj.getString("uid"));
//                     }
//                     if (obj.containsKey("user")) {
//                         auth.setUser(obj.getString("user"));
//                     }
//                     if (obj.containsKey("password")) {
//                         auth.setPassword(obj.getString("password"));
//                     }
//                     if (obj.containsKey("enable")) {
//                         auth.setEnable(obj.getBooleanValue("enable"));
//                     }
//                     auths.add(auth);
//                 }
//             }
//         } catch (Exception ex) {
//             ex.printStackTrace();
//         }
//         return auths;
//     }
//
//     /**
//      * 加载旧版本终端历史数据
//      *
//      * @return 旧版本终端历史数据
//      */
//     public static List<ZKTerminalHistory> loadTerminalHistory() {
//         List<ZKTerminalHistory> histories = new ArrayList<>(24);
//         try {
//             String storePath = SysConst.storeDir();
//             String file = storePath + File.separator + "zk_shell_history.json";
//             if (FileUtil.exist(file)) {
//                 String json = FileUtil.readUtf8String(file);
//                 JSONArray array = JSONUtil.parseArray(json);
//                 for (int i = 0; i < array.size(); i++) {
//                     JSONObject obj = array.getJSONObject(i);
//                     ZKTerminalHistory history = new ZKTerminalHistory();
//                     if (obj.containsKey("tid")) {
//                         history.setTid(obj.getString("tid"));
//                     }
//                     if (obj.containsKey("line")) {
//                         history.setLine(obj.getString("line"));
//                     }
//                     if (obj.containsKey("saveTime")) {
//                         history.setSaveTime(obj.getLongValue("saveTime"));
//                     }
//                     histories.add(history);
//                 }
//             }
//         } catch (Exception ex) {
//             ex.printStackTrace();
//         }
//         return histories;
//     }
//
//     /**
//      * 加载旧版本设置数据
//      *
//      * @return 旧版本设置数据
//      */
//     public static ZKSetting loadSetting() {
//         ZKSetting setting = new ZKSetting();
//         try {
//             String storePath = SysConst.storeDir();
//             String file = storePath + File.separator + "zk_setting.json";
//             if (FileUtil.exist(file)) {
//                 String json = FileUtil.readUtf8String(file);
//                 JSONObject object = JSONUtil.parseObject(json);
//                 if (object.containsKey("theme")) {
//                     setting.setTheme(object.getString("theme"));
//                 }
//                 if (object.containsKey("fgColor")) {
//                     setting.setFgColor(object.getString("fgColor"));
//                 }
//                 if (object.containsKey("bgColor")) {
//                     setting.setBgColor(object.getString("bgColor"));
//                 }
//                 if (object.containsKey("accentColor")) {
//                     setting.setAccentColor(object.getString("accentColor"));
//                 }
//                 if (object.containsKey("fontFamily")) {
//                     setting.setFontFamily(object.getString("fontFamily"));
//                 }
//                 if (object.containsKey("fontSize")) {
//                     setting.setFontSize(object.getByteValue("fontSize"));
//                 }
//                 if (object.containsKey("fontWeight")) {
//                     setting.setFontWeight(object.getShortValue("fontWeight"));
//                 }
//                 if (object.containsKey("locale")) {
//                     setting.setLocale(object.getString("locale"));
//                 }
//                 if (object.containsKey("exitMode")) {
//                     setting.setExitMode(object.getByteValue("exitMode"));
//                 }
//                 if (object.containsKey("loadMode")) {
//                     setting.setLoadMode(object.getByteValue("loadMode"));
//                 }
//                 if (object.containsKey("rememberPageSize")) {
//                     setting.setRememberPageSize(object.getByteValue("rememberPageSize"));
//                 }
//                 if (object.containsKey("rememberPageResize")) {
//                     setting.setRememberPageResize(object.getByteValue("rememberPageResize"));
//                 }
//                 if (object.containsKey("rememberPageLocation")) {
//                     setting.setRememberPageLocation(object.getByteValue("rememberPageLocation"));
//                 }
//                 if (object.containsKey("authMode")) {
//                     setting.setAuthMode(object.getByteValue("authMode"));
//                 }
//                 if (object.containsKey("opacity")) {
//                     setting.setOpacity(object.getFloatValue("opacity"));
//                 }
//             }
//             file = storePath + File.separator + "page_info.json";
//             if (FileUtil.exist(file)) {
//                 String json = FileUtil.readUtf8String(file);
//                 JSONObject object = JSONUtil.parseObject(json);
//                 if (object.containsKey("width")) {
//                     setting.setPageWidth(object.getDoubleValue("width"));
//                 }
//                 if (object.containsKey("height")) {
//                     setting.setPageHeight(object.getDoubleValue("height"));
//                 }
//                 if (object.containsKey("screenX")) {
//                     setting.setPageScreenX(object.getDoubleValue("screenX"));
//                 }
//                 if (object.containsKey("screenY")) {
//                     setting.setPageScreenY(object.getDoubleValue("screenY"));
//                 }
//                 if (object.containsKey("maximized")) {
//                     setting.setPageMaximized(object.getBooleanValue("maximized"));
//                 }
//                 if (object.containsKey("mainLeftWidth")) {
//                     setting.setPageLeftWidth(object.getFloatValue("mainLeftWidth"));
//                 }
//             }
//         } catch (Exception ex) {
//             ex.printStackTrace();
//         }
//         return setting;
//     }
//
//     /**
//      * 忽略迁移
//      */
//     public static void ignoreMigration() {
//         String storePath = SysConst.storeDir();
//         String ignore = storePath + File.separator + "ignore.data";
//         FileUtil.touch(ignore);
//     }
//
//     /**
//      * 完成迁移
//      */
//     public static void doneMigration() {
//         String storePath = SysConst.storeDir();
//         String done = storePath + File.separator + "done.data";
//         FileUtil.touch(done);
//     }
//
//     /**
//      * 检查旧版本
//      *
//      * @return 结果
//      */
//     public static boolean checkOlder() {
//         String storePath = SysConst.storeDir();
//         String file = storePath + File.separator + "zk_info.json";
//         String file1 = storePath + File.separator + "zk_group.json";
//         String done = storePath + File.separator + "done.data";
//         String ignore = storePath + File.separator + "ignore.data";
//         return (FileUtil.exist(file) || FileUtil.exist(file1)) && !(FileUtil.exist(done) || FileUtil.exist(ignore));
//     }
//
//     /**
//      * 销毁
//      */
//     public static void destroy() {
//         JdbcManager.destroy();
//     }
// }
