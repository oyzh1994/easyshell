package cn.oyzh.easyshell.query.redis;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.query.ShellQueryUtil;
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

import static redis.clients.jedis.Protocol.Command.APPEND;
import static redis.clients.jedis.Protocol.Command.BITCOUNT;
import static redis.clients.jedis.Protocol.Command.BITFIELD;
import static redis.clients.jedis.Protocol.Command.BITFIELD_RO;
import static redis.clients.jedis.Protocol.Command.BITOP;
import static redis.clients.jedis.Protocol.Command.BITPOS;
import static redis.clients.jedis.Protocol.Command.BLMOVE;
import static redis.clients.jedis.Protocol.Command.BLMPOP;
import static redis.clients.jedis.Protocol.Command.BLPOP;
import static redis.clients.jedis.Protocol.Command.BRPOP;
import static redis.clients.jedis.Protocol.Command.BRPOPLPUSH;
import static redis.clients.jedis.Protocol.Command.BZMPOP;
import static redis.clients.jedis.Protocol.Command.BZPOPMAX;
import static redis.clients.jedis.Protocol.Command.BZPOPMIN;
import static redis.clients.jedis.Protocol.Command.CONFIG;
import static redis.clients.jedis.Protocol.Command.DECR;
import static redis.clients.jedis.Protocol.Command.DECRBY;
import static redis.clients.jedis.Protocol.Command.DISCARD;
import static redis.clients.jedis.Protocol.Command.EXEC;
import static redis.clients.jedis.Protocol.Command.EXPIRE;
import static redis.clients.jedis.Protocol.Command.EXPIREAT;
import static redis.clients.jedis.Protocol.Command.EXPIRETIME;
import static redis.clients.jedis.Protocol.Command.GEOADD;
import static redis.clients.jedis.Protocol.Command.GEODIST;
import static redis.clients.jedis.Protocol.Command.GEOHASH;
import static redis.clients.jedis.Protocol.Command.GEOPOS;
import static redis.clients.jedis.Protocol.Command.GEORADIUS;
import static redis.clients.jedis.Protocol.Command.GEORADIUSBYMEMBER;
import static redis.clients.jedis.Protocol.Command.GEORADIUSBYMEMBER_RO;
import static redis.clients.jedis.Protocol.Command.GEORADIUS_RO;
import static redis.clients.jedis.Protocol.Command.GEOSEARCH;
import static redis.clients.jedis.Protocol.Command.GEOSEARCHSTORE;
import static redis.clients.jedis.Protocol.Command.GETBIT;
import static redis.clients.jedis.Protocol.Command.GETRANGE;
import static redis.clients.jedis.Protocol.Command.GETSET;
import static redis.clients.jedis.Protocol.Command.HDEL;
import static redis.clients.jedis.Protocol.Command.HEXISTS;
import static redis.clients.jedis.Protocol.Command.HEXPIRE;
import static redis.clients.jedis.Protocol.Command.HEXPIREAT;
import static redis.clients.jedis.Protocol.Command.HEXPIRETIME;
import static redis.clients.jedis.Protocol.Command.HGET;
import static redis.clients.jedis.Protocol.Command.HGETALL;
import static redis.clients.jedis.Protocol.Command.HINCRBY;
import static redis.clients.jedis.Protocol.Command.HINCRBYFLOAT;
import static redis.clients.jedis.Protocol.Command.HKEYS;
import static redis.clients.jedis.Protocol.Command.HLEN;
import static redis.clients.jedis.Protocol.Command.HMGET;
import static redis.clients.jedis.Protocol.Command.HMSET;
import static redis.clients.jedis.Protocol.Command.HPERSIST;
import static redis.clients.jedis.Protocol.Command.HPEXPIRE;
import static redis.clients.jedis.Protocol.Command.HPEXPIREAT;
import static redis.clients.jedis.Protocol.Command.HPEXPIRETIME;
import static redis.clients.jedis.Protocol.Command.HPTTL;
import static redis.clients.jedis.Protocol.Command.HRANDFIELD;
import static redis.clients.jedis.Protocol.Command.HSET;
import static redis.clients.jedis.Protocol.Command.HSETNX;
import static redis.clients.jedis.Protocol.Command.HSTRLEN;
import static redis.clients.jedis.Protocol.Command.HTTL;
import static redis.clients.jedis.Protocol.Command.HVALS;
import static redis.clients.jedis.Protocol.Command.INCR;
import static redis.clients.jedis.Protocol.Command.INCRBY;
import static redis.clients.jedis.Protocol.Command.INCRBYFLOAT;
import static redis.clients.jedis.Protocol.Command.INFO;
import static redis.clients.jedis.Protocol.Command.LCS;
import static redis.clients.jedis.Protocol.Command.LINDEX;
import static redis.clients.jedis.Protocol.Command.LINSERT;
import static redis.clients.jedis.Protocol.Command.LLEN;
import static redis.clients.jedis.Protocol.Command.LMOVE;
import static redis.clients.jedis.Protocol.Command.LMPOP;
import static redis.clients.jedis.Protocol.Command.LPOP;
import static redis.clients.jedis.Protocol.Command.LPOS;
import static redis.clients.jedis.Protocol.Command.LPUSH;
import static redis.clients.jedis.Protocol.Command.LPUSHX;
import static redis.clients.jedis.Protocol.Command.LRANGE;
import static redis.clients.jedis.Protocol.Command.LREM;
import static redis.clients.jedis.Protocol.Command.LSET;
import static redis.clients.jedis.Protocol.Command.LTRIM;
import static redis.clients.jedis.Protocol.Command.MGET;
import static redis.clients.jedis.Protocol.Command.MONITOR;
import static redis.clients.jedis.Protocol.Command.MSET;
import static redis.clients.jedis.Protocol.Command.MSETNX;
import static redis.clients.jedis.Protocol.Command.MULTI;
import static redis.clients.jedis.Protocol.Command.PEXPIRE;
import static redis.clients.jedis.Protocol.Command.PEXPIREAT;
import static redis.clients.jedis.Protocol.Command.PEXPIRETIME;
import static redis.clients.jedis.Protocol.Command.PFADD;
import static redis.clients.jedis.Protocol.Command.PFCOUNT;
import static redis.clients.jedis.Protocol.Command.PFMERGE;
import static redis.clients.jedis.Protocol.Command.PSETEX;
import static redis.clients.jedis.Protocol.Command.PTTL;
import static redis.clients.jedis.Protocol.Command.RPOP;
import static redis.clients.jedis.Protocol.Command.RPOPLPUSH;
import static redis.clients.jedis.Protocol.Command.RPUSH;
import static redis.clients.jedis.Protocol.Command.RPUSHX;
import static redis.clients.jedis.Protocol.Command.SADD;
import static redis.clients.jedis.Protocol.Command.SCARD;
import static redis.clients.jedis.Protocol.Command.SDIFF;
import static redis.clients.jedis.Protocol.Command.SDIFFSTORE;
import static redis.clients.jedis.Protocol.Command.SETBIT;
import static redis.clients.jedis.Protocol.Command.SETEX;
import static redis.clients.jedis.Protocol.Command.SETNX;
import static redis.clients.jedis.Protocol.Command.SETRANGE;
import static redis.clients.jedis.Protocol.Command.SHUTDOWN;
import static redis.clients.jedis.Protocol.Command.SINTER;
import static redis.clients.jedis.Protocol.Command.SINTERCARD;
import static redis.clients.jedis.Protocol.Command.SINTERSTORE;
import static redis.clients.jedis.Protocol.Command.SISMEMBER;
import static redis.clients.jedis.Protocol.Command.SMEMBERS;
import static redis.clients.jedis.Protocol.Command.SMISMEMBER;
import static redis.clients.jedis.Protocol.Command.SMOVE;
import static redis.clients.jedis.Protocol.Command.SORT;
import static redis.clients.jedis.Protocol.Command.SORT_RO;
import static redis.clients.jedis.Protocol.Command.SPOP;
import static redis.clients.jedis.Protocol.Command.SRANDMEMBER;
import static redis.clients.jedis.Protocol.Command.SREM;
import static redis.clients.jedis.Protocol.Command.STRLEN;
import static redis.clients.jedis.Protocol.Command.SUBSTR;
import static redis.clients.jedis.Protocol.Command.SUNION;
import static redis.clients.jedis.Protocol.Command.SUNIONSTORE;
import static redis.clients.jedis.Protocol.Command.TTL;
import static redis.clients.jedis.Protocol.Command.TYPE;
import static redis.clients.jedis.Protocol.Command.UNWATCH;
import static redis.clients.jedis.Protocol.Command.WATCH;
import static redis.clients.jedis.Protocol.Command.XACK;
import static redis.clients.jedis.Protocol.Command.XADD;
import static redis.clients.jedis.Protocol.Command.XAUTOCLAIM;
import static redis.clients.jedis.Protocol.Command.XCLAIM;
import static redis.clients.jedis.Protocol.Command.XDEL;
import static redis.clients.jedis.Protocol.Command.XGROUP;
import static redis.clients.jedis.Protocol.Command.XINFO;
import static redis.clients.jedis.Protocol.Command.XLEN;
import static redis.clients.jedis.Protocol.Command.XPENDING;
import static redis.clients.jedis.Protocol.Command.XRANGE;
import static redis.clients.jedis.Protocol.Command.XREAD;
import static redis.clients.jedis.Protocol.Command.XREADGROUP;
import static redis.clients.jedis.Protocol.Command.XREVRANGE;
import static redis.clients.jedis.Protocol.Command.XTRIM;
import static redis.clients.jedis.Protocol.Command.ZADD;
import static redis.clients.jedis.Protocol.Command.ZCARD;
import static redis.clients.jedis.Protocol.Command.ZCOUNT;
import static redis.clients.jedis.Protocol.Command.ZDIFF;
import static redis.clients.jedis.Protocol.Command.ZDIFFSTORE;
import static redis.clients.jedis.Protocol.Command.ZINCRBY;
import static redis.clients.jedis.Protocol.Command.ZINTER;
import static redis.clients.jedis.Protocol.Command.ZINTERCARD;
import static redis.clients.jedis.Protocol.Command.ZINTERSTORE;
import static redis.clients.jedis.Protocol.Command.ZLEXCOUNT;
import static redis.clients.jedis.Protocol.Command.ZMPOP;
import static redis.clients.jedis.Protocol.Command.ZMSCORE;
import static redis.clients.jedis.Protocol.Command.ZPOPMAX;
import static redis.clients.jedis.Protocol.Command.ZPOPMIN;
import static redis.clients.jedis.Protocol.Command.ZRANDMEMBER;
import static redis.clients.jedis.Protocol.Command.ZRANGE;
import static redis.clients.jedis.Protocol.Command.ZRANGEBYLEX;
import static redis.clients.jedis.Protocol.Command.ZRANGEBYSCORE;
import static redis.clients.jedis.Protocol.Command.ZRANGESTORE;
import static redis.clients.jedis.Protocol.Command.ZRANK;
import static redis.clients.jedis.Protocol.Command.ZREM;
import static redis.clients.jedis.Protocol.Command.ZREMRANGEBYLEX;
import static redis.clients.jedis.Protocol.Command.ZREMRANGEBYRANK;
import static redis.clients.jedis.Protocol.Command.ZREMRANGEBYSCORE;
import static redis.clients.jedis.Protocol.Command.ZREVRANGE;
import static redis.clients.jedis.Protocol.Command.ZREVRANGEBYLEX;
import static redis.clients.jedis.Protocol.Command.ZREVRANGEBYSCORE;
import static redis.clients.jedis.Protocol.Command.ZREVRANK;
import static redis.clients.jedis.Protocol.Command.ZSCORE;
import static redis.clients.jedis.Protocol.Command.ZUNION;
import static redis.clients.jedis.Protocol.Command.ZUNIONSTORE;

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

    // public static double clacCorr(String str, String text) {
    //     str = str.toUpperCase();
    //     text = text.toUpperCase();
    //     if (!str.contains(text) && !text.contains(str)) {
    //         return 0.d;
    //     }
    //     double corr = StringUtil.similarity(str, text);
    //     if (str.startsWith(text)) {
    //         corr += 0.3;
    //     } else if (str.contains(text)) {
    //         corr += 0.2;
    //     } else if (str.endsWith(text)) {
    //         corr += 0.1;
    //     }
    //     return corr;
    // }

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
                double corr = ShellQueryUtil.clacCorr(keyword, text);
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
                double corr = ShellQueryUtil.clacCorr(param, text);
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
                double corr = ShellQueryUtil.clacCorr(key, text);
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
