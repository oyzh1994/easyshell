package cn.oyzh.easyshell.dto.redis;

import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.util.StringUtil;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * redis节点导出对象
 *
 * @author oyzh
 * @since 2023/06/20
 */
//@Slf4j
public class RedisNodeExport {

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

    public List<Map<String, Object>> getNodes() {
        return nodes;
    }

    public void setNodes(List<Map<String, Object>> nodes) {
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

    /**
     * 导出键数据
     */
    private List<Map<String, Object>> nodes;

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
        return JSONUtil.toPretty(this);
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
    public byte[] getDateBytes(String data, String charset) {
        if (StringUtil.isBlank(charset)) {
            charset = StandardCharsets.UTF_8.name();
        } else if ("跟随系统".equals(charset)) {
            charset = Charset.defaultCharset().name();
        }
        byte[] bytes;
        try {
            if (data == null) {
                bytes = new byte[]{};
            } else if (data.length() == 0) {
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
