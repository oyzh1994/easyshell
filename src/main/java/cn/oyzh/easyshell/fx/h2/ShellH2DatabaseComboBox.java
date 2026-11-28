package cn.oyzh.easyshell.fx.h2;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.h2.schema.H2Schema;
import cn.oyzh.easyshell.h2.ShellH2Client;
import cn.oyzh.fx.plus.controls.combo.FXComboBox;

import java.util.List;

/**
 * H2 模式下拉框。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellH2DatabaseComboBox extends FXComboBox<String> {

    /**
     * 初始化数据库列表。
     *
     * @param client H2 客户端
     */
    public void init(ShellH2Client client) {
        this.init(client, null);
    }

    /**
     * 初始化数据库列表并选中指定数据库。
     *
     * @param client H2 客户端
     * @param dbName 数据库名称
     */
    public void init(ShellH2Client client, String dbName) {
        this.clearItems();
        List<H2Schema> schemas = client.selectSchemas();
        if (CollectionUtil.isNotEmpty(schemas)) {
            this.setItem(schemas.stream().map(H2Schema::getName).toList());
        }
        if (StringUtil.isNotBlank(dbName)) {
            this.select(dbName);
        }
    }
}
