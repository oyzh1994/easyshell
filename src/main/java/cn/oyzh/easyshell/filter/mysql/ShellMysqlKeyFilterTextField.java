package cn.oyzh.easyshell.filter.mysql;

import cn.oyzh.fx.gui.text.field.LimitTextField;
import cn.oyzh.fx.plus.event.AnonymousEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Skin;

/**
 * mysql过滤文本域
 *
 * @author oyzh
 * @since 2025/11/07
 */
public class ShellMysqlKeyFilterTextField extends LimitTextField {

    public EventHandler<AnonymousEvent<Object>> getOnSearch() {
        return onSearch;
    }

    public void setOnSearch(EventHandler<AnonymousEvent<Object>> onSearch) {
        this.onSearch = onSearch;
    }

    /**
     * 搜索事件处理
     */
    private EventHandler<AnonymousEvent<Object>> onSearch;

    /**
     * 当前皮肤
     *
     * @return 皮肤
     */
    public ShellMysqlKeyFilterTextFieldSkin skin() {
        return (ShellMysqlKeyFilterTextFieldSkin) this.getSkin();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ShellMysqlKeyFilterTextFieldSkin(this) {
            @Override
            public void onSearch(String text) {
                super.onSearch(text);
                if (onSearch != null) {
                    onSearch.handle(AnonymousEvent.of(text));
                }
            }
        };
    }

    /**
     * 获取过滤参数
     *
     * @return 过滤参数
     */
    public ShellMysqlKeyFilterParam filterParam() {
        return this.skin().filterParam();
    }

    /**
     * 获取过滤模式
     *
     * @return 过滤模式
     */
    public byte filterMode() {
        ShellMysqlKeyFilterParam filterParam = this.filterParam();
        if (filterParam.isMatchCase() && filterParam.isMatchFull()) {
            return 3;
        }
        if (filterParam.isMatchFull()) {
            return 2;
        }
        if (filterParam.isMatchCase()) {
            return 1;
        }
        return 0;
    }
}
