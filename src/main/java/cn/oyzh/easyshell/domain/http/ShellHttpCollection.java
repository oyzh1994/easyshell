package cn.oyzh.easyshell.domain.http;

import cn.oyzh.common.util.UUIDUtil;
import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;
import cn.oyzh.store.jdbc.Table;
import com.alibaba.fastjson2.annotation.JSONField;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * HTTP 请求集合。
 *
 * @author oyzh
 * @since 2026-11-07
 */
@Table("t_http_collection")
public class ShellHttpCollection implements Serializable {

    /**
     * 数据id
     */
    @Column
    @PrimaryKey
    private String id = UUIDUtil.uuidSimple();

    /**
     * 所属连接id
     *
     * @see cn.oyzh.easyshell.domain.ShellConnect
     */
    @Column
    private String iid;

    /**
     * 名称
     */
    @Column
    private String name = "";

    /**
     * 请求，运行期数据
     */
    @JSONField(serialize = false, deserialize = false)
    private transient List<ShellHttpRequest> requests = new ArrayList<>();

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ShellHttpRequest> getRequests() {
        if (requests == null) {
            requests = new ArrayList<>();
        }
        return requests;
    }

    public void setRequests(List<ShellHttpRequest> requests) {
        this.requests = requests;
    }

    /**
     * 获取请求数量。
     *
     * @return 请求数量
     */
    @JSONField(serialize = false, deserialize = false)
    public int requestCount() {
        return this.getRequests().size();
    }
}
