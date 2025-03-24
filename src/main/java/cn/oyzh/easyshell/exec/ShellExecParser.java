package cn.oyzh.easyshell.exec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-03-18
 */
public class ShellExecParser {

    public static List<DiskInfo> disk(String output, boolean isMacos) {
        try {
            String[] lines = output.split("\n");
            List<DiskInfo> list = new ArrayList<>();
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];
                String[] cols = line.split("\\s+");
                DiskInfo info = new DiskInfo();
                info.setFileSystem(cols[0]);
                info.setSize(cols[1]);
                info.setUsed(cols[2]);
                info.setAvail(cols[3]);
                if (isMacos) {
                    info.setUse(cols[7]);
                    info.setMountedOn(cols[8]);
                } else {
                    info.setUse(cols[4]);
                    info.setMountedOn(cols[5]);
                }
                list.add(info);
            }
            return list;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Collections.emptyList();
    }
}
