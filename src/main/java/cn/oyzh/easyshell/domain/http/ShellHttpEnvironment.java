package cn.oyzh.easyshell.domain.http;

import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.UUIDUtil;
import cn.oyzh.easyshell.http.ShellHttpKeyValue;
import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;
import cn.oyzh.store.jdbc.Table;
import com.alibaba.fastjson2.annotation.JSONField;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * HTTP 环境变量实体。
 *
 * @author oyzh
 * @since 2026-11-07
 */
@Table("t_http_environment")
public class ShellHttpEnvironment implements Serializable {

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
     * 名称
     */
    @Column
    private String name = "";

    /**
     * 环境变量 JSON
     */
    @Column
    private String valuesJson;

    /**
     * 环境变量，运行期数据
     */
    @JSONField(serialize = false, deserialize = false)
    private transient List<ShellHttpKeyValue> values;

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

    public String getValuesJson() {
        return valuesJson;
    }

    public void setValuesJson(String valuesJson) {
        this.valuesJson = valuesJson;
        this.values = null;
    }

    @JSONField(serialize = false, deserialize = false)
    public List<ShellHttpKeyValue> getValues() {
        if (this.values == null) {
            if (StringUtil.isBlank(this.valuesJson)) {
                this.values = new ArrayList<>();
            } else {
                try {
                    List<ShellHttpKeyValue> list = JSONUtil.toList(this.valuesJson, ShellHttpKeyValue.class);
                    this.values = list == null ? new ArrayList<>() : list;
                } catch (Exception ex) {
                    this.values = new ArrayList<>();
                }
            }
        }
        return this.values;
    }

    public void setValues(List<ShellHttpKeyValue> values) {
        this.values = values;
        this.valuesJson = JSONUtil.toJson(values);
    }

    /**
     * 同步 JSON 字段。
     */
    public void syncJson() {
        this.valuesJson = JSONUtil.toJson(this.getValues());
    }

    /**
     * 复制环境。
     *
     * @return 环境副本
     */
    public ShellHttpEnvironment copy() {
        ShellHttpEnvironment copy = new ShellHttpEnvironment();
        copy.iid = this.iid;
        copy.name = this.name;
        copy.values = ShellHttpRequest.copyRows(this.getValues());
        copy.syncJson();
        return copy;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
