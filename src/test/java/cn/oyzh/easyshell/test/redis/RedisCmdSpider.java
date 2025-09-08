package cn.oyzh.easyshell.test.redis;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.terminal.redis.RedisTerminalManager;
import cn.oyzh.easyshell.terminal.redis.RedisTerminalPane;
import cn.oyzh.easyshell.util.redis.ShellRedisCommand;
import cn.oyzh.easyshell.util.redis.ShellRedisCommandUtil;
import cn.oyzh.fx.terminal.command.TerminalCommandHandler;
import cn.oyzh.fx.terminal.util.TerminalManager;
import com.alibaba.fastjson2.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author oyzh
 * @since 2024/5/29
 */
public class RedisCmdSpider {

    private final String descUrl = "https://redis.io/docs/latest/commands/";

    private final String detailUrl = "https://redis.io/docs/latest/commands/";

    private final String filePath = "D:\\Workspaces\\OYZH\\easyredis\\src\\main\\resources\\redis_commands.json";

    private void fetch() throws IOException {
        System.out.println("fetch start---------->");
        RedisTerminalManager.registerHandlers();
        Collection<TerminalCommandHandler<?, ?>> list = TerminalManager.listHandler(RedisTerminalPane.TERMINAL_NAME);
        List<ShellRedisCommand> list1 = new ArrayList<>();
        Document document = Jsoup.connect(descUrl).get();
        Elements articles = document.getElementsByTag("article");
        int count = list.size();
        int sum = 0;
        for (TerminalCommandHandler value : list) {
            try {
                String cmdName = value.commandFullName();
                // if (StrUtil.isNotBlank(value.commandSubName())) {
                //     cmdName = cmdName + " " + value.commandSubName();
                // }
                if (!this.isNeedFetch(cmdName)) {
                    System.out.println("command:" + cmdName + " skip.");
                    continue;
                }
                System.out.println("fetch command:" + cmdName);
                ShellRedisCommand command = new ShellRedisCommand();
                command.setCommand(cmdName);
                for (Element article : articles) {
                    // System.out.println(article);
                    String attr = article.attr("data-name");
                    if (cmdName.toUpperCase().equalsIgnoreCase(attr)) {
                        Elements p = article.getElementsByTag("p");
                        String text = p.text();
                        command.setDesc(text);
                        System.out.println(text);
                        System.out.println("-------------------------------------->");
                        break;
                    }
                }
                this.getDetail(cmdName, command);
                list1.add(command);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                System.out.println("count:" + count + " ,sum:" + ++sum);
            }
        }

        List<ShellRedisCommand> list2 = new ArrayList<>(list1);
        for (ShellRedisCommand command : ShellRedisCommandUtil.getCommands()) {
            Optional<ShellRedisCommand> optional = list1.parallelStream().filter(c -> c.getCommand().equalsIgnoreCase(command.getCommand())).findAny();
            if (optional.isEmpty()) {
                list2.add(command);
            }
        }
        String json = JSONObject.toJSONString(list2);
        FileUtil.writeString(json, filePath, CharsetUtil.UTF_8);
        System.out.println("fetch finish---------->");
    }

    private boolean isNeedFetch(String cmdName) {
        if (StringUtil.equalsAnyIgnoreCase(cmdName, "clear", "help", "connect")) {
            return false;
        }
        ShellRedisCommand command = ShellRedisCommandUtil.getCommand(cmdName);
        if (command == null || StrUtil.isBlank(command.getAvailable()) || StrUtil.isBlank(command.getArgs())) {
            return true;
        }
        // if (command == null || StrUtil.isBlank(command.getDesc()) || StrUtil.isBlank(command.getAvailable())
        //         || StrUtil.isBlank(command.getArgs())) {
        //     return true;
        // }
        return false;
    }

    private void getDetail(String cmdName, ShellRedisCommand redisCommand) throws Exception {
        String name = cmdName.replaceAll(" ", "-");
        Document document = Jsoup.connect(this.detailUrl + name + "/").get();
        // System.out.println(document.html());
        Elements args = document.getElementsByClass("command-syntax");
        String argsText = args.text();
        redisCommand.setArgs(argsText);
        Elements available = document.getElementsByClass("prose").getFirst().getElementsByTag("dd");
        String availableText = available.getFirst().text();
        redisCommand.setAvailable(availableText);

        System.out.println(argsText);
        System.out.println(availableText);
        System.out.println("-------------------------------------->");
    }

    public static void main(String... args) throws Exception {
        RedisCmdSpider spider = new RedisCmdSpider();
        spider.fetch();
    }
}
