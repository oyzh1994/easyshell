package cn.oyzh.easyssh.dto;

import cn.oyzh.common.dto.Project;
import cn.oyzh.common.util.SpringUtil;
import cn.oyzh.easyssh.domain.SSHInfo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * ssh连接导出对象
 *
 * @author oyzh
 * @since 2023/06/22
 */
@Slf4j
public class SSHInfoExport {

    /**
     * 导出程序版本号
     */
    @Getter
    private String version;

    /**
     * 平台
     */
    @Getter
    private String platform;

    /**
     * 导出连接数据
     */
    @Getter
    private List<SSHInfo> connects;

    /**
     * 从ssh连接数据生成
     *
     * @param sshInfos 连接列表
     * @return SSHInfoExport
     */
    public static SSHInfoExport fromConnects(@NonNull List<SSHInfo> sshInfos) {
        SSHInfoExport export = new SSHInfoExport();
        Project project = SpringUtil.getBean(Project.class);
        export.version = project.getVersion();
        export.connects = sshInfos;
        export.platform = System.getProperty("os.name");
        return export;
    }

    /**
     * 从json对象数据生成
     *
     * @param json json字符串
     * @return SSHInfoExport
     */
    public static SSHInfoExport fromJSON(@NonNull String json) {
        log.info("json: {}", json);
        JSONObject object = JSONObject.parseObject(json);
        SSHInfoExport export = new SSHInfoExport();
        export.connects = new ArrayList<>();
        export.version = object.getString("version");
        JSONArray nodes = object.getJSONArray("connects");
        export.connects = nodes.toJavaList(SSHInfo.class);
        return export;
    }

    /**
     * 转成json字符串
     *
     * @return json字符串
     */
    public String toJSONString() {
        return JSONObject.toJSONString(this);
    }
}
