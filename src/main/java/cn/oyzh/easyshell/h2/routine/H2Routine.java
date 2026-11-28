package cn.oyzh.easyshell.h2.routine;

/**
 * H2 例程（函数或过程）。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class H2Routine {

    private String schema;

    private String name;

    private String type;

    private String dataType;

    private String body;

    private String definition;

    private String externalName;

    private boolean deterministic;

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

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isFunction() {
        return "FUNCTION".equalsIgnoreCase(this.type);
    }

    public boolean isProcedure() {
        return "PROCEDURE".equalsIgnoreCase(this.type);
    }

    public String getDataType() {
        return this.dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getBody() {
        return this.body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getDefinition() {
        return this.definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getExternalName() {
        return externalName;
    }

    public void setExternalName(String externalName) {
        this.externalName = externalName;
    }

    public boolean isDeterministic() {
        return deterministic;
    }

    public void setDeterministic(boolean deterministic) {
        this.deterministic = deterministic;
    }

    public H2Routine copy() {
        H2Routine routine = new H2Routine();
        routine.setSchema(this.schema);
        routine.setName(this.name);
        routine.setType(this.type);
        routine.setDataType(this.dataType);
        routine.setBody(this.body);
        routine.setDefinition(this.definition);
        routine.setExternalName(this.externalName);
        routine.setDeterministic(this.deterministic);
        return routine;
    }
}
