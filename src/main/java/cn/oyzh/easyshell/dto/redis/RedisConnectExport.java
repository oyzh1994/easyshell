// package cn.oyzh.easyshell.dto.redis;
//
// import cn.oyzh.common.dto.Project;
// import cn.oyzh.common.json.JSONUtil;
// import cn.oyzh.common.log.JulLog;
// import cn.oyzh.easyshell.domain.ShellConnect;
// import cn.oyzh.easyshell.domain.redis.RedisGroup;
// import com.alibaba.fastjson2.JSONObject;
//
// import java.util.ArrayList;
// import java.util.List;
//
// /**
//  * redis连接导出对象
//  *
//  * @author oyzh
//  * @since 2023/06/22
//  */
// //@Slf4j
// public class RedisConnectExport {
//
//     /**
//      * 导出程序版本号
//      */
//     private String version;
//
//     /**
//      * 平台
//      */
//     private String platform;
//
//     /**
//      * 分组
//      */
//     private List<RedisGroup> groups;
//
//     /**
//      * 连接
//      */
//     private List<ShellConnect> connects;
//
//     public String getVersion() {
//         return version;
//     }
//
//     public void setVersion(String version) {
//         this.version = version;
//     }
//
//     public String getPlatform() {
//         return platform;
//     }
//
//     public void setPlatform(String platform) {
//         this.platform = platform;
//     }
//
//     public List<RedisGroup> getGroups() {
//         return groups;
//     }
//
//     public void setGroups(List<RedisGroup> groups) {
//         this.groups = groups;
//     }
//
//     public List<ShellConnect> getConnects() {
//         return connects;
//     }
//
//     public void setConnects(List<ShellConnect> connects) {
//         this.connects = connects;
//     }
//
//     /**
//      * 从redis连接数据生成
//      *
//      * @param redisConnects 连接列表
//      * @return RedisInfoExport
//      */
//     public static RedisConnectExport fromConnects( List<ShellConnect> redisConnects) {
//         RedisConnectExport export = new RedisConnectExport();
//         Project project = Project.load();
//         export.version = project.getVersion();
//         export.connects = redisConnects;
//         export.platform = System.getProperty("os.name");
//         return export;
//     }
//
//     /**
//      * 从json对象数据生成
//      *
//      * @param json json字符串
//      * @return RedisInfoExport
//      */
//     public static RedisConnectExport fromJSON( String json) {
//         JulLog.info("json: {}", json);
//         JSONObject object = JSONUtil.parseObject(json);
//         RedisConnectExport export = new RedisConnectExport();
//         export.connects = new ArrayList<>(4);
//         export.version = object.getString("version");
//         export.platform = object.getString("platform");
//         export.groups = object.getList("groups", RedisGroup.class);
//         export.connects = object.getList("connects", ShellConnect.class);
//         return export;
//     }
//
//     /**
//      * 转成json字符串
//      *
//      * @return json字符串
//      */
//     public String toJSONString() {
//         return JSONUtil.toJson(this);
//     }
// }
