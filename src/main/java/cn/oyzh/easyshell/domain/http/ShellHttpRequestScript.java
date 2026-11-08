package cn.oyzh.easyshell.domain.http;

import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;
import cn.oyzh.store.jdbc.Table;
import cn.oyzh.common.util.UUIDUtil;

import java.io.Serializable;

/**
 * HTTP 请求脚本实体。
 *
 * @author oyzh
 * @since 2026-11-08
 */
@Table("t_http_request_script")
public class ShellHttpRequestScript implements Serializable {

    /**
     * 数据id
     */
    @Column
    @PrimaryKey
    private String id = UUIDUtil.uuidSimple();

    /**
     * 所属连接id
     */
    @Column
    private String iid;

    /**
     * 请求id
     */
    @Column
    private String requestId;

    /**
     * 请求前脚本
     */
    @Column
    private String preRequestScript = "";

    /**
     * 测试脚本
     */
    @Column
    private String testScript = "";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIid() {
        return iid;
    }

    public void setIid(String iid) {
        this.iid = iid;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getPreRequestScript() {
        return preRequestScript == null ? "" : preRequestScript;
    }

    public void setPreRequestScript(String preRequestScript) {
        this.preRequestScript = preRequestScript == null ? "" : preRequestScript;
    }

    public String getTestScript() {
        return testScript == null ? "" : testScript;
    }

    public void setTestScript(String testScript) {
        this.testScript = testScript == null ? "" : testScript;
    }

    /**
     * 复制脚本
     *
     * @param requestId 新请求id
     * @return 脚本
     */
    public ShellHttpRequestScript copy(String requestId) {
        ShellHttpRequestScript copy = new ShellHttpRequestScript();
        copy.setIid(this.iid);
        copy.setRequestId(requestId);
        copy.setPreRequestScript(this.getPreRequestScript());
        copy.setTestScript(this.getTestScript());
        return copy;
    }
}
