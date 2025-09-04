package cn.oyzh.easyshell.query.redis;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.StringUtil;
import redis.clients.jedis.Protocol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static redis.clients.jedis.Protocol.Command.*;

/**
 * @author oyzh
 * @since 2025/01/21
 */

public class ShellRedisQueryUtil {

    /**
     * 关键字
     */
    private static final Set<String> KEYWORDS = new HashSet<>();

    /**
     * 参数
     */
    private static final Set<String> PARAMS = new HashSet<>();

    /**
     * 键
     */
    private static final Set<String> KEYS = new HashSet<>();

    static {
        // 关键字
        for (Protocol.Command command : Protocol.Command.values()) {
            KEYWORDS.add(command.toString());
        }
        for (Protocol.Keyword keyword : Protocol.Keyword.values()) {
            KEYWORDS.add(keyword.toString());
        }
        for (Protocol.ClusterKeyword keyword : Protocol.ClusterKeyword.values()) {
            KEYWORDS.add(keyword.toString());
        }
        for (Protocol.ResponseKeyword keyword : Protocol.ResponseKeyword.values()) {
            KEYWORDS.add(keyword.toString());
        }
        for (Protocol.SentinelKeyword keyword : Protocol.SentinelKeyword.values()) {
            KEYWORDS.add(keyword.toString());
        }
        // 参数
        for (Protocol.Keyword keyword : Protocol.Keyword.values()) {
            PARAMS.add(keyword.toString());
        }
        for (Protocol.ClusterKeyword keyword : Protocol.ClusterKeyword.values()) {
            PARAMS.add(keyword.toString());
        }
        for (Protocol.ResponseKeyword keyword : Protocol.ResponseKeyword.values()) {
            PARAMS.add(keyword.toString());
        }
        for (Protocol.SentinelKeyword keyword : Protocol.SentinelKeyword.values()) {
            PARAMS.add(keyword.toString());
        }
    }

    public static Set<String> getKeywords() {
        return KEYWORDS;
    }

    public static Set<String> getParams() {
        return PARAMS;
    }

    public static Set<String> getKeys() {
        return KEYS;
    }

    public static void setKeys(Collection<String> keys) {
        KEYS.clear();
        if (keys != null) {
            KEYS.addAll(keys);
        }
    }

    public static double clacCorr(String str, String text) {
        str = str.toUpperCase();
        text = text.toUpperCase();
        if (!str.contains(text) && !text.contains(str)) {
            return 0.d;
        }
        double corr = StringUtil.similarity(str, text);
        if (str.startsWith(text)) {
            corr += 0.3;
        } else if (str.contains(text)) {
            corr += 0.2;
        } else if (str.endsWith(text)) {
            corr += 0.1;
        }
        return corr;
    }

    /**
     * 初始化提示词
     *
     * @param token   提示词
     * @param minCorr 最低相关度
     * @return 结果
     */
    public static List<ShellRedisQueryPromptItem> initPrompts(ShellRedisQueryToken token, float minCorr) {
        if (token == null) {
            return Collections.emptyList();
        }
        // 当前提示词
        String text = token.getContent().toUpperCase();
        // 提示词列表
        final List<ShellRedisQueryPromptItem> items = new CopyOnWriteArrayList<>();
        // 任务列表
        List<Runnable> tasks = new ArrayList<>();
        // 关键字
        if (token.isPossibilityKeyword()) {
            tasks.add(() -> getKeywords().parallelStream().forEach(keyword -> {
                // 计算相关度
                double corr = clacCorr(keyword, text);
                if (corr > minCorr) {
                    ShellRedisQueryPromptItem item = new ShellRedisQueryPromptItem();
                    item.setType((byte) 1);
                    item.setContent(keyword);
                    item.setCorrelation(corr);
                    items.add(item);
                }
            }));
        }
        // 参数
        if (token.isPossibilityParam()) {
            tasks.add(() -> getParams().parallelStream().forEach(param -> {
                // 计算相关度
                double corr = clacCorr(param, text);
                if (corr > minCorr) {
                    ShellRedisQueryPromptItem item = new ShellRedisQueryPromptItem();
                    item.setType((byte) 2);
                    item.setContent(param);
                    item.setCorrelation(corr);
                    items.add(item);
                }
            }));
        }
        // 键
        if (token.isPossibilityKey()) {
            tasks.add(() -> getKeys().parallelStream().forEach(key -> {
                // 计算相关度
                double corr = clacCorr(key, text);
                if (corr > minCorr) {
                    ShellRedisQueryPromptItem item = new ShellRedisQueryPromptItem();
                    item.setType((byte) 3);
                    item.setContent(key);
                    item.setCorrelation(corr);
                    items.add(item);
                }
            }));
        }
        // 执行任务
        ThreadUtil.submitVirtual(tasks);
        // 根据相关度排序
        return items.parallelStream()
                .sorted(Comparator.comparingDouble(ShellRedisQueryPromptItem::getCorrelation))
                .collect(Collectors.toList())
                .reversed();
    }

    /**
     * 获取键相关的命令
     *
     * @return 结果
     */
    public static List<Protocol.Command> keyCommands() {
        List<Protocol.Command> commands = new ArrayList<>();
        // key
        Protocol.Command[] keyCommands = new Protocol.Command[]{
                EXPIRE, EXPIREAT, EXPIRETIME, PEXPIRE, PEXPIREAT, PEXPIRETIME, TTL, PTTL
        };
        // string
        Protocol.Command[] stringCommands = new Protocol.Command[]{
                MULTI, DISCARD, EXEC, WATCH, UNWATCH, SORT, SORT_RO, INFO, SHUTDOWN, MONITOR, CONFIG, LCS, //
                GETSET, MGET, SETNX, SETEX, PSETEX, MSET, MSETNX, DECR, DECRBY, INCR, INCRBY, INCRBYFLOAT,
                STRLEN, APPEND, SUBSTR
        };
        // bit
        Protocol.Command[] bitCommands = new Protocol.Command[]{
                SETBIT, GETBIT, BITPOS, SETRANGE, GETRANGE, BITCOUNT, BITOP, BITFIELD, BITFIELD_RO
        };
        // hash
        Protocol.Command[] hashCommands = new Protocol.Command[]{
                HSET, HGET, HSETNX, HMSET, HMGET, HINCRBY, HEXISTS, HDEL, HLEN, HKEYS, HVALS, HGETALL, HSTRLEN,
                HEXPIRE, HPEXPIRE, HEXPIREAT, HPEXPIREAT, HTTL, HPTTL, HEXPIRETIME, HPEXPIRETIME, HPERSIST,
                HRANDFIELD, HINCRBYFLOAT
        };
        // list
        Protocol.Command[] listCommands = new Protocol.Command[]{
                RPUSH, LPUSH, LLEN, LRANGE, LTRIM, LINDEX, LSET, LREM, LPOP, RPOP, BLPOP, BRPOP, LINSERT, LPOS,
                RPOPLPUSH, BRPOPLPUSH, BLMOVE, LMOVE, LMPOP, BLMPOP, LPUSHX, RPUSHX
        };
        // set
        Protocol.Command[] setCommands = new Protocol.Command[]{
                SADD, SMEMBERS, SREM, SPOP, SMOVE, SCARD, SRANDMEMBER, SINTER, SINTERSTORE, SUNION, SUNIONSTORE,
                SDIFF, SDIFFSTORE, SISMEMBER, SMISMEMBER, SINTERCARD
        };
        // zset
        Protocol.Command[] zsetCommands = new Protocol.Command[]{
                ZADD, ZDIFF, ZDIFFSTORE, ZRANGE, ZREM, ZINCRBY, ZRANK, ZREVRANK, ZREVRANGE, ZRANDMEMBER, ZCARD,
                ZSCORE, ZPOPMAX, ZPOPMIN, ZCOUNT, ZUNION, ZUNIONSTORE, ZINTER, ZINTERSTORE, ZRANGEBYSCORE,
                ZREVRANGEBYSCORE, ZREMRANGEBYRANK, ZREMRANGEBYSCORE, ZLEXCOUNT, ZRANGEBYLEX, ZREVRANGEBYLEX,
                ZREMRANGEBYLEX, ZMSCORE, ZRANGESTORE, ZINTERCARD, ZMPOP, BZMPOP, BZPOPMIN, BZPOPMAX
        };
        // geo
        Protocol.Command[] geoCommands = new Protocol.Command[]{
                GEOADD, GEODIST, GEOHASH, GEOPOS, GEORADIUS, GEORADIUS_RO, GEOSEARCH, GEOSEARCHSTORE,
                GEORADIUSBYMEMBER, GEORADIUSBYMEMBER_RO
        };
        // hyper log
        Protocol.Command[] hyperLogCommands = new Protocol.Command[]{
                PFADD, PFCOUNT, PFMERGE
        };
        // stream
        Protocol.Command[] streamCommands = new Protocol.Command[]{
                XADD, XLEN, XDEL, XTRIM, XRANGE, XREVRANGE, XREAD, XACK, XGROUP, XREADGROUP, XPENDING, XCLAIM,
                XAUTOCLAIM, XINFO
        };
        // other
        Protocol.Command[] otherCommands = new Protocol.Command[]{
                TYPE
        };
        commands.addAll(List.of(keyCommands));
        commands.addAll(List.of(stringCommands));
        commands.addAll(List.of(bitCommands));
        commands.addAll(List.of(hashCommands));
        commands.addAll(List.of(listCommands));
        commands.addAll(List.of(setCommands));
        commands.addAll(List.of(zsetCommands));
        commands.addAll(List.of(geoCommands));
        commands.addAll(List.of(hyperLogCommands));
        commands.addAll(List.of(streamCommands));
        commands.addAll(List.of(otherCommands));
        return commands;
    }
}
