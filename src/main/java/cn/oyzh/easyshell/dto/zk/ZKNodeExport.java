package cn.oyzh.easyshell.dto.zk;

import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.util.StringUtil;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * zk节点导出对象
 *
 * @author oyzh
 * @since 2023/2/20
 */
public class ZKNodeExport {

    /**
     * 脚本前缀，txt格式专用
     */
    private String prefix;

    /**
     * 导出程序版本号
     */
    private String version;

    /**
     * 平台
     */
    private String platform;

    /**
     * 字符集
     */
    private String charset;

    public List<Map<String, String>> getNodes() {
        return nodes;
    }

    public void setNodes(List<Map<String, String>> nodes) {
        this.nodes = nodes;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * 导出节点数据
     */
    private List<Map<String, String>> nodes;

    /**
     * 转成json字符串
     *
     * @param prettyFormat 美化
     * @return json字符串
     */
    public String toJSONString(boolean prettyFormat) {
        if (prettyFormat) {
            return JSONUtil.toPretty(this);
        }
        return JSONUtil.toJson(this);
    }

    /**
     * 获取数据总数
     *
     * @return 数据总数
     */
    public int counts() {
        return this.nodes == null ? 0 : this.nodes.size();
    }

    /**
     * 获取数据字节数组
     *
     * @param data    数据
     * @param charset 字符集
     * @return 数据字节数组
     */
    public byte[] getDateBytes(String data,  Charset charset) {
        byte[] bytes;
        try {
            if (data == null) {
                bytes = new byte[]{};
            } else if (data.isEmpty()) {
                bytes = "".getBytes(charset);
            } else {
                bytes = data.getBytes(charset);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            bytes = new byte[]{};
        }
        return bytes;
    }

    /**
     * 是否有前缀
     *
     * @return 结果
     */
    public boolean hasPrefix() {
        return StringUtil.isNotBlank(this.prefix);
    }

    public String version() {
        return this.version == null ? "未知" : this.version;
    }

    public String platform() {
        return this.platform == null ? "未知" : this.platform;
    }

    public String charset() {
        return this.charset == null ? "未知" : this.charset;
    }
}
