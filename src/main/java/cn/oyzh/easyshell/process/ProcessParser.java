package cn.oyzh.easyshell.process;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 进程解析器
 *
 * @author oyzh
 * @since 25/03/29
 */
public class ProcessParser {

    public static List<ProcessInfo> ps(String output) {
        try {
            String[] lines = output.split("\n");
            List<ProcessInfo> list = new ArrayList<>();
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];
                String[] cols = line.split("\\s+");
                ProcessInfo info = new ProcessInfo();
                info.setUser(cols[0]);
                info.setPid(Integer.parseInt(cols[1]));
                info.setCpuUsage(Double.parseDouble(cols[2]));
                info.setMemUsage(Double.parseDouble(cols[3]));
                info.setStat(cols[7]);
                info.setStart(cols[8]);
                info.setTime(cols[9]);
                info.setCommand(cols[10]);
                list.add(info);
            }
            return list;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Collections.emptyList();
    }
}
