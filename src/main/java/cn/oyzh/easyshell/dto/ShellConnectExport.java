package cn.oyzh.easyshell.dto;

import cn.oyzh.common.dto.Project;
import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellGroup;
import cn.oyzh.easyshell.domain.ShellKey;
import cn.oyzh.easyshell.domain.ShellSnippet;
import com.alibaba.fastjson2.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * shell连接导出对象
 *
 * @author oyzh
 * @since 2023/2/22
 */
public class ShellConnectExport {

    /**
     * 导出程序版本号
     */
    private String version;

    /**
     * 平台
     */
    private String platform;

    /**
     * 密钥
     */
    private List<ShellKey> keys;

    /**
     * 分组
     */
    private List<ShellGroup> groups;

    /**
     * 片段
     */
    private List<ShellSnippet> snippets;

    /**
     * 连接
     */
    private List<ShellConnect> connects;

    /**
     * 从shell连接数据生成
     *
     * @param shellConnects 连接列表
     * @return ShellConnectExport
     */
    public static ShellConnectExport fromConnects(List<ShellConnect> shellConnects) {
        ShellConnectExport export = new ShellConnectExport();
        Project project = Project.load();
        export.version = project.getVersion();
        export.connects = shellConnects;
        export.platform = System.getProperty("os.name");
        return export;
    }

    /**
     * 生成导出对象
     *
     * @return ShellConnectExport
     */
    public static ShellConnectExport of() {
        ShellConnectExport export = new ShellConnectExport();
        Project project = Project.load();
        export.version = project.getVersion();
        export.platform = System.getProperty("os.name");
        return export;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public List<ShellKey> getKeys() {
        return keys;
    }

    public void setKeys(List<ShellKey> keys) {
        this.keys = keys;
    }

    public List<ShellSnippet> getSnippets() {
        return snippets;
    }

    public void setSnippets(List<ShellSnippet> snippets) {
        this.snippets = snippets;
    }

    public List<ShellGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<ShellGroup> groups) {
        this.groups = groups;
    }

    public List<ShellConnect> getConnects() {
        return connects;
    }

    public void setConnects(List<ShellConnect> connects) {
        this.connects = connects;
    }

    /**
     * 从json对象数据生成
     *
     * @param json json字符串
     * @return ShellConnectExport
     */
    public static ShellConnectExport fromJSON(String json) {
        if (JulLog.isInfoEnabled()) {
            JulLog.info("json: {}", json);
        }
        JSONObject object = JSONUtil.parseObject(json);
        ShellConnectExport export = new ShellConnectExport();
        export.connects = new ArrayList<>(12);
        export.version = object.getString("version");
        export.platform = object.getString("platform");
        export.keys = object.getList("keys", ShellKey.class);
        export.groups = object.getList("groups", ShellGroup.class);
        export.connects = object.getList("connects", ShellConnect.class);
        export.snippets = object.getList("snippets", ShellSnippet.class);
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
