package cn.oyzh.easyshell.mongo.database;

/**
 *
 * @author oyzh
 * @since 2026-06-01
 */
public class MongoDatabase {

    private String name;

    private Double sizeOnDisk;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getSizeOnDisk() {
        return sizeOnDisk;
    }

    public void setSizeOnDisk(Double sizeOnDisk) {
        this.sizeOnDisk = sizeOnDisk;
    }
}
