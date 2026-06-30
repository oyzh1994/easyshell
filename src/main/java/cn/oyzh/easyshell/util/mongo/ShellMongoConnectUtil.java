package cn.oyzh.easyshell.util.mongo;


import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.dto.mongo.ShellMongoConnectInfo;

/**
 * mongodb连接工具类
 *
 * @author oyzh
 * @since 2022/8/26
 */

public class ShellMongoConnectUtil {

    /**
     * 解析连接
     *
     * @param input 输入内容
     * @return 连接
     */
    public static ShellMongoConnectInfo parse(String input) {
        if (input != null) {
            try {
                String[] words = input.split(" ");
                ShellMongoConnectInfo connect = new ShellMongoConnectInfo();
                connect.setInput(input);
                for (int i = 0; i < words.length; i++) {
                    String word = words[i];
                    int type;
                    if (word.equalsIgnoreCase("-server")) {
                        type = 0;
                    } else if (word.equalsIgnoreCase("-timeout")) {
                        type = 1;
                    } else if (word.equalsIgnoreCase("-r")) {
                        type = 2;
                    } else {
                        type = -1;
                    }
                    if (type == 0) {
                        String[] strings = words[i + 1].trim().split(":");
                        if (strings.length > 0) {
                            connect.setHost(strings[0]);
                        }
                        if (strings.length > 1) {
                            connect.setPort(Integer.parseInt(strings[1]));
                        }
                    } else if (type == 1) {
                        connect.setTimeout(Integer.parseInt(words[i + 1]) / 1000);
                    } else if (type == 2) {
                        connect.setReadonly(true);
                    }
                }
                return connect;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 复制连接
     *
     * @param connectInfo 连接信息
     * @param connect     连接
     */
    public static void copyConnect(ShellMongoConnectInfo connectInfo, ShellConnect connect) {
        if (connectInfo != null && connect != null) {
            connect.setReadonly(connectInfo.isReadonly());
            connect.setConnectTimeOut(connectInfo.getTimeout());
            connect.setHost(connectInfo.getHost() + ":" + connectInfo.getPort());
        }
    }
}
