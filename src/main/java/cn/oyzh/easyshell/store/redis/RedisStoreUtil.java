// package cn.oyzh.easyshell.store.redis;
//
// import cn.oyzh.common.SysConst;
// import cn.oyzh.common.file.FileUtil;
// import cn.oyzh.common.json.JSONUtil;
// import cn.oyzh.common.util.CollectionUtil;
// import cn.oyzh.common.util.StringUtil;
// import cn.oyzh.easyshell.ShellConst;
// import cn.oyzh.easyshell.domain.redis.ShellRedisCollect;
// import cn.oyzh.easyshell.domain.ShellConnect;
// import cn.oyzh.easyshell.domain.redis.RedisFilter;
// import cn.oyzh.easyshell.domain.redis.RedisGroup;
// import cn.oyzh.easyshell.domain.redis.ShellRedisKeyFilterHistory;
// import cn.oyzh.easyshell.domain.redis.RedisSetting;
// import cn.oyzh.easyshell.terminal.redis.RedisTerminalHistory;
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
//  * @author oyzh
//  * @since 2024-09-23
//  */
//
// public class RedisStoreUtil {
//
//     /**
//      * 执行初始化
//      */
//     public static void init() {
//         JdbcConst.dbCacheSize(65535);
//         JdbcConst.dbPageSize(1024);
//         JdbcConst.dbDialect(JdbcDialect.H2);
//         JdbcConst.dbFile(ShellConst.getStorePath() + "db");
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
//     public static List<RedisGroup> loadGroups() {
//         List<RedisGroup> groups = new ArrayList<>(24);
//         String storePath = SysConst.storeDir();
//         String file = storePath + File.separator + "redis_group.json";
//         try {
//             if (FileUtil.exist(file)) {
//                 String json = FileUtil.readUtf8String(file);
//                 JSONArray array = JSONUtil.parseArray(json);
//                 for (int i = 0; i < array.size(); i++) {
//                     JSONObject obj = array.getJSONObject(i);
//                     RedisGroup group = new RedisGroup();
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
//     public static List<ShellConnect> loadConnects() {
//         List<ShellConnect> connects = new ArrayList<>(24);
//         String storePath = SysConst.storeDir();
//         String file = storePath + File.separator + "redis_info.json";
//         try {
//             if (FileUtil.exist(file)) {
//                 String json = FileUtil.readUtf8String(file);
//                 JSONArray array = JSONUtil.parseArray(json);
//                 for (int i = 0; i < array.size(); i++) {
//                     JSONObject obj = array.getJSONObject(i);
//                     ShellConnect connect = new ShellConnect();
//                     if (obj.containsKey("id")) {
//                         connect.setId(obj.getString("id"));
//                     }
//                     if (obj.containsKey("name")) {
//                         connect.setName(obj.getString("name"));
//                     }
//                     if (obj.containsKey("host")) {
//                         connect.setHost(obj.getString("host"));
//                     }
// //                    if (obj.containsKey("sshForward")) {
// //                        connect.setSshForward(obj.getBooleanValue("sshForward"));
// //                    }
//                     if (obj.containsKey("collects")) {
//                         List<String> collects = obj.getList("collects", String.class);
//                         if (CollectionUtil.isNotEmpty(collects)) {
//                             List<ShellRedisCollect> collectList = new ArrayList<>();
//                             for (String collect : collects) {
//                                 collectList.add(new ShellRedisCollect(connect.getId(), 0, collect));
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
//                     if (obj.containsKey("connectTimeOut")) {
//                         connect.setConnectTimeOut(obj.getIntValue("connectTimeOut"));
//                     }
// //                    if (obj.containsKey("sshInfo")) {
// //                        JSONObject object = obj.getJSONObject("sshInfo");
// //                        RedisSSHConfig sshConfig = new RedisSSHConfig();
// //                        if (object.containsKey("port")) {
// //                            sshConfig.setPort(object.getInt("port"));
// //                        }
// //                        if (object.containsKey("host")) {
// //                            sshConfig.setHost(object.getString("host"));
// //                        }
// //                        if (object.containsKey("user")) {
// //                            sshConfig.setUser(object.getString("user"));
// //                        }
// //                        if (object.containsKey("timeout")) {
// //                            sshConfig.setTimeout(object.getInt("timeout"));
// //                        }
// //                        if (object.containsKey("password")) {
// //                            sshConfig.setPassword(object.getString("password"));
// //                        }
// //                        connect.setSshConfig(sshConfig);
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
//     public static List<RedisFilter> loadFilters() {
//         List<RedisFilter> filters = new ArrayList<>();
//         try {
//             String storePath = SysConst.storeDir();
//             String file = storePath + File.separator + "redis_filter.json";
//             if (FileUtil.exist(file)) {
//                 String json = FileUtil.readUtf8String(file);
//                 JSONArray array = JSONUtil.parseArray(json);
//                 for (int i = 0; i < array.size(); i++) {
//                     JSONObject obj = array.getJSONObject(i);
//                     RedisFilter filter = new RedisFilter();
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
//      * 加载旧版本终端历史数据
//      *
//      * @return 旧版本终端历史数据
//      */
//     public static List<RedisTerminalHistory> loadTerminalHistory() {
//         List<RedisTerminalHistory> histories = new ArrayList<>(24);
//         try {
//             String storePath = SysConst.storeDir();
//             String file = storePath + File.separator + "redis_shell_history.json";
//             if (FileUtil.exist(file)) {
//                 String json = FileUtil.readUtf8String(file);
//                 JSONArray array = JSONUtil.parseArray(json);
//                 for (int i = 0; i < array.size(); i++) {
//                     JSONObject obj = array.getJSONObject(i);
//                     RedisTerminalHistory history = new RedisTerminalHistory();
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
//      * 加载旧版本键过滤历史数据
//      *
//      * @return 旧版本键过滤历史数据
//      */
//     public static List<ShellRedisKeyFilterHistory> loadKeyFilterHistory() {
//         List<ShellRedisKeyFilterHistory> histories = new ArrayList<>(24);
//         try {
//             String storePath = SysConst.storeDir();
//             String file = storePath + File.separator + "redis_key_filter_history.json";
//             if (FileUtil.exist(file)) {
//                 String json = FileUtil.readUtf8String(file);
//                 JSONArray array = JSONUtil.parseArray(json);
//                 for (int i = 0; i < array.size(); i++) {
//                     JSONObject obj = array.getJSONObject(i);
//                     ShellRedisKeyFilterHistory history = new ShellRedisKeyFilterHistory();
//                     if (obj.containsKey("uid")) {
//                         history.setUid(obj.getString("uid"));
//                     }
//                     if (obj.containsKey("pattern")) {
//                         history.setPattern(obj.getString("pattern"));
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
//     public static RedisSetting loadSetting() {
//         RedisSetting setting = new RedisSetting();
//         String storePath = SysConst.storeDir();
//         String file = storePath + File.separator + "redis_setting.json";
//         try {
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
//                 if (object.containsKey("rememberPageSize")) {
//                     setting.setRememberPageSize(object.getByteValue("rememberPageSize"));
//                 }
//                 if (object.containsKey("rememberPageResize")) {
//                     setting.setRememberPageResize(object.getByteValue("rememberPageResize"));
//                 }
//                 if (object.containsKey("rememberPageLocation")) {
//                     setting.setRememberPageLocation(object.getByteValue("rememberPageLocation"));
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
//         String file = storePath + File.separator + "redis_info.json";
//         String file1 = storePath + File.separator + "redis_group.json";
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
