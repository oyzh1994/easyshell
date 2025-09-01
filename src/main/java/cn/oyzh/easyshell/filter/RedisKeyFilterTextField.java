package cn.oyzh.easyshell.filter;

import cn.oyzh.fx.gui.text.field.LimitTextField;
import cn.oyzh.fx.plus.event.AnonymousEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Skin;

/**
 * 过滤文本域
 *
 * @author oyzh
 * @since 2025/01/23
 */
public class RedisKeyFilterTextField extends LimitTextField {

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
    public RedisKeyFilterTextFieldSkin skin() {
        return (RedisKeyFilterTextFieldSkin) this.getSkin();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new RedisKeyFilterTextFieldSkin(this) {
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
    public RedisKeyFilterParam filterParam() {
        return this.skin().filterParam();
    }

    /**
     * 获取过滤模式
     *
     * @return 过滤模式
     */
    public byte filterMode() {
        RedisKeyFilterParam filterParam = this.filterParam();
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

    /**
     * 获取过滤范围
     *
     * @return 过滤范围
     */
    public byte filterScope() {
        RedisKeyFilterParam filterParam = this.filterParam();
        if (filterParam.isSearchData() && filterParam.isSearchKey()) {
            return 2;
        }
        if (filterParam.isSearchData()) {
            return 1;
        }
        if (filterParam.isSearchKey()) {
            return 0;
        }
        return -1;
    }
}
