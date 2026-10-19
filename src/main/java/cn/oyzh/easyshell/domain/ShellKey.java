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
 * @since 2025-04-03
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

    /** 获取数据id */
    public String getId() {
        return id;
    }

    /** 设置数据id */
    public void setId(String id) {
        this.id = id;
    }

    /** 获取名称 */
    public String getName() {
        return name;
    }

    /** 设置名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** 获取类型 */
    public String getType() {
        return type;
    }

    /** 设置类型 */
    public void setType(String type) {
        this.type = type;
    }

    /** 获取长度 */
    public long getLength() {
        return length;
    }

    /** 设置长度 */
    public void setLength(long length) {
        this.length = length;
    }

    /** 获取公钥 */
    public String getPublicKey() {
        return publicKey;
    }

    /** 获取密码 */
    public String getPassword() {
        return password;
    }

    /** 设置密码 */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取公钥字节数组
     *
     * @return 公钥字节数组
     */
    @JSONField(serialize = false, deserialize = false)
    public byte[] getPublicKeyBytes() {
        return publicKey == null ? null : publicKey.getBytes();
    }

    /**
     * 获取密码字节数组
     *
     * @return 密码字节数组
     */
    @JSONField(serialize = false, deserialize = false)
    public byte[] getPasswordBytes() {
        return password == null ? null : password.getBytes();
    }

    /** 设置公钥 */
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * 获取私钥
     *
     * @return 私钥
     */
    public String getPrivateKey() {
        // 修复私钥不以\n结束的问题
        if (StringUtil.isNotBlank(privateKey) && !privateKey.endsWith("\n")) {
            this.privateKey = privateKey + "\n";
        }
        return privateKey;
    }

    /**
     * 获取私钥字节数组
     *
     * @return 私钥字节数组
     */
    @JSONField(serialize = false, deserialize = false)
    public byte[] getPrivateKeyBytes() {
        return privateKey == null ? null : privateKey.getBytes();
    }

    /**
     * 设置私钥
     *
     * @param privateKey 私钥
     */
    public void setPrivateKey(String privateKey) {
        // 修复私钥不以\n结束的问题
        if (StringUtil.isNotBlank(privateKey) && !privateKey.endsWith("\n")) {
            this.privateKey = privateKey + "\n";
        } else {
            this.privateKey = privateKey;
        }
    }
}
