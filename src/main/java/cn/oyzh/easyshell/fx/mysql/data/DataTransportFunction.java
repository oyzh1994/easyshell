package cn.oyzh.easyshell.fx.mysql.data;

/**
 * @author oyzh
 * @since 2024-09-06
 */
public class DataTransportFunction {

    /**
     * 函数名称
     */
    private String name;

    /**
     * 是否选中
     */
    private boolean selected = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
