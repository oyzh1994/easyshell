package cn.oyzh.easyshell.domain;

import cn.oyzh.common.object.ObjectComparator;
import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;
import cn.oyzh.store.jdbc.Table;
import com.alibaba.fastjson2.annotation.JSONField;

import java.io.Serializable;

/**
 * 密钥
 *
 * @author oyzh
 * @since 2023/6/16
 */
@Table("t_key")
public class ShellKey implements ObjectComparator<ShellKey>, Serializable, ObjectCopier<ShellKey> {

    /**
     * 数据id
     */
    @Column
    @PrimaryKey
    private String id;

    /**
     * 名称
     */
    @Column
    private String name;

    /**
     * 类型
     */
    @Column
    private String type;

    /**
     * 长度
     */
    @Column
    private long length;

    /**
     * 密码
     */
    @Column
    private String password;

    /**
     * 公钥
     */
    @Column
    private String publicKey;

    /**
     * 密钥
     */
    @Column
    private String privateKey;

    @Override
    public void copy(ShellKey shellKey) {
        this.name = shellKey.name;
        this.type = shellKey.type;
        this.length = shellKey.length;
        this.publicKey = shellKey.publicKey;
        this.privateKey = shellKey.privateKey;
    }

    @Override
    public boolean compare(ShellKey t1) {
        if (t1 == null) {
            return false;
        }
        return StringUtil.equals(this.privateKey, t1.privateKey) && StringUtil.equals(this.publicKey, t1.publicKey);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @JSONField(serialize = false, deserialize = false)
    public byte[] getPublicKeyBytes() {
        return publicKey == null ? null : publicKey.getBytes();
    }

    @JSONField(serialize = false, deserialize = false)
    public byte[] getPasswordBytes() {
        return password == null ? null : password.getBytes();
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        // 修复私钥不以\n结束的问题
        if (StringUtil.isNotBlank(privateKey) && !privateKey.endsWith("\n")) {
            this.privateKey = privateKey + "\n";
        }
        return privateKey;
    }

    @JSONField(serialize = false, deserialize = false)
    public byte[] getPrivateKeyBytes() {
        return privateKey == null ? null : privateKey.getBytes();
    }

    public void setPrivateKey(String privateKey) {
        // 修复私钥不以\n结束的问题
        if (StringUtil.isNotBlank(privateKey) && !privateKey.endsWith("\n")) {
            this.privateKey = privateKey + "\n";
        } else {
            this.privateKey = privateKey;
        }
    }
}
