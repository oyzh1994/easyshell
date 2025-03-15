package cn.oyzh.easyssh.dto;

import cn.oyzh.common.dto.Project;
import cn.oyzh.common.json.JSONObject;
import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.easyssh.domain.ShellConnect;
import cn.oyzh.easyssh.domain.SSHGroup;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * ssh连接导出对象
 *
 * @author oyzh
 * @since 2023/2/22
 */
@Getter
public class SSHConnectExport {

    /**
     * 导出程序版本号
     */
    private String version;

    /**
     * 平台
     */
    private String platform;

    /**
     * 连接
     */
    @Setter
    private List<SSHGroup> groups;

    /**
     * 连接
     */
    private List<ShellConnect> connects;

    /**
     * 从ssh连接数据生成
     *
     * @param shellConnects 连接列表
     * @return SSHConnectExport
     */
    public static SSHConnectExport fromConnects(@NonNull List<ShellConnect> shellConnects) {
        SSHConnectExport export = new SSHConnectExport();
        Project project = Project.load();
        export.version = project.getVersion();
        export.connects = shellConnects;
        export.platform = System.getProperty("os.name");
        return export;
    }

    /**
     * 从json对象数据生成
     *
     * @param json json字符串
     * @return SSHInfoExport
     */
    public static SSHConnectExport fromJSON(@NonNull String json) {
        JulLog.info("json: {}", json);
        JSONObject object = JSONUtil.parseObject(json);
        SSHConnectExport export = new SSHConnectExport();
        export.connects = new ArrayList<>(12);
        export.version = object.getString("version");
        export.platform = object.getString("platform");
        export.groups = object.getBeanList("groups", SSHGroup.class);
        export.connects = object.getBeanList("connects", ShellConnect.class);
        return export;
    }

    /**
     * 转成json字符串
     *
     * @return json字符串
     */
    public String toJSONString() {
        return JSONUtil.toJson(this);
    }
}
