package cn.oyzh.easyshell.util;


import cn.oyzh.common.dto.Project;
import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.easyshell.dto.redis.RedisNodeExport;
import cn.oyzh.easyshell.redis.key.RedisKey;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * redis导出工具类
 *
 * @author oyzh
 * @since 2023/07/07
 */
//@Slf4j

public class RedisExportUtil {

    /**
     * redis键数据生成json字符串
     *
     * @param nodes        redis键数据
     * @param charset      字符集
     * @param prettyFormat 美化
     * @return 数据json字符串
     */
    public static String nodesToJSON(List<RedisKey> nodes, String charset, boolean prettyFormat) {
        Project project = Project.load();
        String version = project.getVersion();
        String platform = OSUtil.getOSType();
        RedisNodeExport export = new RedisNodeExport();
        // 元信息
        export.setNodes(new ArrayList<>(4));
        export.setVersion(version);
        export.setCharset(charset);
        export.setPlatform(platform);
        // 拼接数据
        for (RedisKey n : nodes) {
            Map<String, Object> node = new HashMap<>();
            node.put("key", n.getKey());
            node.put("type", n.getType().toString());
            node.put("dbIndex", n.getDbIndex());
            if (n.getTtl() != null) {
                node.put("ttl", n.getTtl());
            }
            String value = RedisKeyUtil.serializeNode(n);
            if (value != null) {
                node.put("value", value);
            }
            export.getNodes().add(node);
        }
        return export.toJSONString(prettyFormat);
    }

    /**
     * 从文件生成
     *
     * @param file 文件
     * @return RedisNodeExport
     */
    public static RedisNodeExport fromFile( File file) {
        String text = FileUtil.readUtf8String(file);
        return fromJSON(text);
    }

    /**
     * 从json对象数据生成
     *
     * @param json json字符串
     * @return RedisNodeExport
     */
    public static RedisNodeExport fromJSON( String json) {
        JulLog.info("json: {}", json);
        JSONObject object = JSONUtil.parseObject(json);
        RedisNodeExport export = new RedisNodeExport();
        export.setNodes(new ArrayList<>(4));
        export.setVersion(object.getString("version"));
        export.setPlatform(object.getString("platform"));
        JSONArray nodes = object.getJSONArray("nodes");
        for (Object n : nodes) {
            JSONObject o = (JSONObject) n;
            Map<String, Object> node = new HashMap<>();
            node.put("ttl", o.getLong("ttl"));
            node.put("key", o.getString("key"));
            node.put("type", o.getString("type"));
            node.put("value", o.getString("value"));
            node.put("dbIndex", o.getIntValue("dbIndex"));
            export.getNodes().add(node);
        }
        return export;
    }
}


