package cn.oyzh.easyshell.trees.redis;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.easyshell.redis.ShellRedisKeyType;
import cn.oyzh.easyshell.redis.key.ShellRedisKey;
import cn.oyzh.easyshell.redis.key.ShellRedisKeyValue;
import cn.oyzh.easyshell.store.redis.RedisCollectStore;
import cn.oyzh.easyshell.util.redis.ShellRedisViewFactory;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author oyzh
 * @since 2023/6/30
 */
public abstract class ShellRedisKeyTreeItem extends RichTreeItem<ShellRedisKeyTreeItemValue> {

    /**
     * 单行最大值
     * 100kb
     */
    public static final int LINE_MAX = 100 * 1024;

    /**
     * 数据最大值
     * 20mb
     */
    public static final int DATA_MAX = 20 * 1024 * 1024;

    /**
     * db节点
     */
    private final ShellRedisDatabaseTreeItem dbItem;

    /**
     * redis键
     */
    protected ShellRedisKey value;

    public ShellRedisKey value() {
        return value;
    }

    /**
     * 设置键数据
     *
     * @param data 未键数据
     */
    public void data(Object data) {
        this.keyValue().setUnSavedValue(data);
        this.refresh();
    }

    /**
     * 获取键数据
     *
     * @return 键数据
     */
    public Object data() {
        Object data;
        if (this.isDataUnsaved()) {
            data = this.unsavedValue();
        } else {
            data = this.rawValue();
        }
        return data;
    }

    /**
     * 清除键数据
     */
    public void clearData() {
        this.keyValue().clearUnSavedValue();
        this.refresh();
    }

    /**
     * 数据是否未保存
     *
     * @return 结果
     */
    public Object unsavedValue() {
        ShellRedisKeyValue<?> keyValue = this.keyValue();
        return keyValue == null ? null : keyValue.getUnSavedValue();
    }

    /**
     * 数据是否未保存
     *
     * @return 结果
     */
    public boolean isDataUnsaved() {
        ShellRedisKeyValue<?> keyValue = this.keyValue();
        return keyValue != null && keyValue.hasUnSavedValue();
    }

    public ShellRedisKeyTreeItem(ShellRedisKey value, ShellRedisDatabaseTreeItem dbItem) {
        super(dbItem.getTreeView());
        this.value = value;
        this.dbItem = dbItem;
        super.setFilterable(true);
        this.setValue(new ShellRedisKeyTreeItemValue(this));
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>(8);
        FXMenuItem rename = MenuItemHelper.renameKey("12", this::rename);
        items.add(rename);
        FXMenuItem moveKey = MenuItemHelper.moveKey("12", this::moveKey);
        items.add(moveKey);
        FXMenuItem copyKey = MenuItemHelper.copyKey("12", this::copyKey);
        items.add(copyKey);
        FXMenuItem delete = MenuItemHelper.deleteKey("12", this::delete);
        items.add(delete);
        items.add(MenuItemHelper.separator());
        FXMenuItem updateTtl = MenuItemHelper.updateTtl("12", this::updateTtl);
        items.add(updateTtl);
        if (this.isCollect()) {
            FXMenuItem unCollectKey = MenuItemHelper.unCollectKey("12", this::unCollect);
            items.add(unCollectKey);
        } else {
            FXMenuItem collectKey = MenuItemHelper.collectKey("12", this::collect);
            items.add(collectKey);
        }
        return items;
    }

    /**
     * 修改ttl
     */
    private void updateTtl() {
        ShellRedisViewFactory.redisTtlKey(this);
    }

    /**
     * 移动键
     */
    private void moveKey() {
        StageAdapter adapter = ShellRedisViewFactory.redisMoveKey(this);
        if (adapter == null) {
            return;
        }
        Integer dbIndex = adapter.getProp("dbIndex");
        if (dbIndex == null) {
            return;
        }
        // 刷新父节点键数量
        this.parent().flushDbSize();
        // 移除此节点
        this.remove();
        // 键移动事件
        this.getTreeView().keyMoved(dbIndex);
    }

    /**
     * 复制键
     */
    private void copyKey() {
        StageAdapter adapter = ShellRedisViewFactory.redisCopyKey(this);
        if (adapter == null) {
            return;
        }
        Integer dbIndex = adapter.getProp("dbIndex");
        if (dbIndex == null) {
            return;
        }
        // 键移动事件
        this.getTreeView().keyCopied(dbIndex);
    }

    @Override
    public ShellRedisKeyTreeView getTreeView() {
        return (ShellRedisKeyTreeView) super.getTreeView();
    }

    /**
     * redis信息
     *
     * @return redis信息
     */
    public ShellConnect shellConnect() {
        return this.getTreeView().shellConnect();
    }

    /**
     * 获取redis连接名称
     *
     * @return redis连接名称
     */
    public String infoName() {
        return this.getTreeView().shellConnect().getName();
    }

    /**
     * 获取db索引
     *
     * @return db索引值
     */
    public int dbIndex() {
        return this.dbItem.dbIndex();
    }

    /**
     * 获取键名称
     *
     * @return 键名称
     */
    public String key() {
        return this.value.getKey();
    }

    /**
     * 获取键二进制名称
     *
     * @return 键二进制名称
     */
    public byte[] keyBinary() {
        return this.value.keyBinary();
    }

    /**
     * 获取redis客户端
     *
     * @return redis客户端
     */
    public ShellRedisClient client() {
        if (this.getTreeView() == null) {
            return null;
        }
        return this.getTreeView().getClient();
    }

    /**
     * 保存键值
     */
    public void saveKeyValue() {
    }

    /**
     * 设置键值
     *
     * @param value 值
     */
    protected void setKeyValue(Object value) {
    }

    /**
     * 刷新键值
     */
    public void refreshKeyValue() {
    }

    /**
     * 键是否被收藏
     */
    public boolean isCollect() {
        return RedisCollectStore.INSTANCE.exist(this.iid(), this.dbIndex(), this.key());
    }

    /**
     * 收藏键
     */
    public void collect() {
        RedisCollectStore.INSTANCE.replace(this.iid(), this.dbIndex(), this.key());
    }

    /**
     * 取消收藏键
     */
    public void unCollect() {
        RedisCollectStore.INSTANCE.delete(this.iid(), this.dbIndex(), this.key());
    }

    private String iid() {
        return this.shellConnect().getId();
    }

    @Override
    public void delete() {
        try {
            if (!MessageBox.confirm(I18nHelper.deleteKey() + " " + this.key())) {
                return;
            }
            // 删除此键
            this.client().del(this.dbIndex(), this.key());
            // 取消此键的收藏
            this.unCollect();
            // 刷新父节点键数量
            this.parent().flushDbSize();
            // 移除此键
            this.remove();
            // 清除选区
            this.clearSelection();
            // // 发送事件
            // ShellEventUtil.redisKeyDeleted(this.shellConnect(), this.key(), this.dbIndex());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    @Override
    public ShellRedisDatabaseTreeItem parent() {
        return (ShellRedisDatabaseTreeItem) super.parent();
    }

    @Override
    public void rename() {
        String newKey = MessageBox.prompt(I18nHelper.renameKey(), this.value.getKey());
        // 名称为空或者跟当前名称相同，则忽略
        if (StringUtil.isBlank(newKey) || Objects.equals(newKey, this.value.getKey())) {
            return;
        }
        // 键已存在
        if (this.client().exists(this.dbIndex(), newKey)) {
            MessageBox.warn(I18nHelper.contentAlreadyExists());
            return;
        }
        try {
            // String oldKey = this.key();
            String result = this.client().rename(this.dbIndex(), this.key(), newKey);
            if (StringUtil.equalsIgnoreCase(result, "OK")) {
                this.value.setKey(newKey);
                this.refresh();
                // ShellEventUtil.redisKeyRenamed(this, oldKey);
            } else {
                MessageBox.warn(I18nHelper.operationFail());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 获取当前ttl值
     *
     * @return 当前ttl值
     */
    public Long ttl() {
        try {
            this.value.setTtl(this.client().ttl(this.dbIndex(), this.key()));
            return this.value.getTtl();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
        return -1L;
    }

    /**
     * 键是否过期
     *
     * @return 结果
     */
    public boolean isExpire() {
        if (this.value.getTtl() == null) {
            this.ttl();
        }
        if (this.value.getTtl() == null) {
            return false;
        }
        return this.value.getTtl() == -2;
    }

    /**
     * 获取原始数据
     *
     * @return 原始数据
     */
    public abstract Object rawValue();

    /**
     * 获取键类型
     *
     * @return 键类型
     */
    public ShellRedisKeyType type() {
        return this.value.getType();
    }

    /**
     * 获取加载耗时
     *
     * @return 加载耗时
     */
    public short loadTime() {
        return this.value.getLoadTime() == 0 ? 1 : this.value.getLoadTime();
    }

    /**
     * 删除键，当键已过期
     */
    public void deleteByExpired() {
        try {
            this.unCollect();
            this.remove();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 获取内存占用
     *
     * @return 内存占用
     */
    public Long memoryUsage() {
        try {
            return this.client().memoryUsage(this.dbIndex(), this.key());
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1L;
        }
    }

    /**
     * 获取内存占用信息
     *
     * @return 内存占用信息
     */
    public String memoryUsageInfo() {
        Long memoryUsage = this.memoryUsage();
        if (memoryUsage == null || memoryUsage < 0) {
            return "N/A";
        }
        if (memoryUsage < 1024) {
            return memoryUsage + "bytes";
        }
        if (memoryUsage < 1024 * 1024) {
            return memoryUsage / 1024.0 + "KB";
        }
        if (memoryUsage < 1024 * 1024 * 1024) {
            return memoryUsage / 1024.0 / 1024 + "MB";
        }
        return memoryUsage / 1024.0 / 1024 / 1024 + "GB";
    }

    private StringProperty memoryUsageInfoProperty;

    public void flushMemoryUsage() {
        this.memoryUsageInfoProperty.set(I18nHelper.size() + " : " + this.memoryUsageInfo());
    }

    public StringProperty memoryUsageInfoProperty() {
        if (this.memoryUsageInfoProperty == null) {
            this.memoryUsageInfoProperty = new SimpleStringProperty();
        }
        return this.memoryUsageInfoProperty;
    }

    /**
     * 是否raw格式
     *
     * @return 结果
     */
    public boolean isRawEncoding() {
        return this.isRawEncoding(false);
    }

    /**
     * 是否raw格式
     *
     * @param flushEncoding 刷新编码
     * @return 结果
     */
    public boolean isRawEncoding(boolean flushEncoding) {
        if (this.value.getObjectedEncoding() == null || flushEncoding) {
            this.value.setObjectedEncoding(this.client().objectEncoding(this.dbIndex(), this.key()));
        }
        return this.value.isRawEncoding();
    }

    public ShellRedisKeyValue<?> keyValue() {
        return this.value.getValue();
    }

    public abstract Object rawData();

    public boolean keyEquals(ShellRedisKey redisKey) {
        if (redisKey != null) {
            return this.value.compareTo(redisKey) == 0;
        }
        return false;
    }

    public void keyCopy(ShellRedisKey redisKey) {
        if (redisKey != null) {
            this.value.copy(redisKey);
        }
    }

    public boolean isJsonKey() {
        return this.value.isJsonKey();
    }

    public boolean isStringKey() {
        return this.value.isStringKey();
    }

    public boolean isListKey() {
        return this.value.isListKey();
    }

    public boolean isStreamKey() {
        return this.value.isStreamKey();
    }

    public boolean isHashKey() {
        return this.value.isHashKey();
    }

    public boolean isSetKey() {
        return this.value.isSetKey();
    }

    public boolean isZSetKey() {
        return this.value.isZSetKey();
    }

    public String typeName() {
        return this.value.typeName();
    }
}
