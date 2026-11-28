package cn.oyzh.easyshell.h2.sequence;

/**
 * H2 序列。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class H2Sequence {

    private String schema;

    private String name;

    private String startValue;

    private String currentValue;

    private String minimumValue;

    private String maximumValue;

    private String increment;

    private String cacheSize;

    private boolean cycle;

    public String getSchema() {
        return this.schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStartValue() {
        return this.startValue;
    }

    public void setStartValue(String startValue) {
        this.startValue = startValue;
    }

    public String getCurrentValue() {
        return this.currentValue;
    }

    public void setCurrentValue(String currentValue) {
        this.currentValue = currentValue;
    }

    public String getMinimumValue() {
        return this.minimumValue;
    }

    public void setMinimumValue(String minimumValue) {
        this.minimumValue = minimumValue;
    }

    public String getMaximumValue() {
        return this.maximumValue;
    }

    public void setMaximumValue(String maximumValue) {
        this.maximumValue = maximumValue;
    }

    public String getIncrement() {
        return this.increment;
    }

    public void setIncrement(String increment) {
        this.increment = increment;
    }

    public String getCacheSize() {
        return this.cacheSize;
    }

    public void setCacheSize(String cacheSize) {
        this.cacheSize = cacheSize;
    }

    public boolean isCycle() {
        return this.cycle;
    }

    public void setCycle(boolean cycle) {
        this.cycle = cycle;
    }
}
