package cn.oyzh.easyshell.zk;

import cn.oyzh.common.Const;
import cn.oyzh.common.dto.FriendlyInfo;
import cn.oyzh.i18n.I18nManager;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/**
 * zk状态解析器
 *
 * @author oyzh
 * @since 2020/3/26
 */
public class ShellZKStatParser implements Function<Stat, List<FriendlyInfo<Stat>>> {

    /**
     * 当前实例
     */
    public final static ShellZKStatParser INSTANCE = new ShellZKStatParser();

    /**
     * 为Stat对象应用FriendlyInfo
     *
     * @param stat Stat对象
     * @return 包含Stat对象各个属性的FriendlyInfo列表
     */
    @Override
    public List<FriendlyInfo<Stat>> apply( Stat stat) {
        List<FriendlyInfo<Stat>> statInfos = new ArrayList<>();

        // 创建节点事务ID
        FriendlyInfo<Stat> czxid = new FriendlyInfo<>();
        czxid.name("czxid");
        czxid.value(stat.getCzxid());
        if (I18nManager.currentLocale() == Locale.SIMPLIFIED_CHINESE || I18nManager.currentLocale() == Locale.PRC) {
            czxid.friendlyName("创建节点事务ID");
        } else if (I18nManager.currentLocale() == Locale.TRADITIONAL_CHINESE || I18nManager.currentLocale() == Locale.TAIWAN) {
            czxid.friendlyName("創建節點事務ID");
        } else {
            czxid.friendlyName("czxid");
        }
        czxid.friendlyValue(czxid.value());

        // 最后修改节点事务ID
        FriendlyInfo<Stat> mzxid = new FriendlyInfo<>();
        mzxid.name("mzxid");
        mzxid.value(stat.getMzxid());
        if (I18nManager.currentLocale() == Locale.SIMPLIFIED_CHINESE || I18nManager.currentLocale() == Locale.PRC) {
            mzxid.friendlyName("最后修改节点事务ID");
        } else if (I18nManager.currentLocale() == Locale.TRADITIONAL_CHINESE || I18nManager.currentLocale() == Locale.TAIWAN) {
            mzxid.friendlyName("最後修改節點事務ID");
        } else {
            mzxid.friendlyName("mzxid");
        }
        mzxid.friendlyValue(mzxid.value());

        // 最后修改子节点事务ID
        FriendlyInfo<Stat> pzxid = new FriendlyInfo<>();
        pzxid.name("pzxid");
        pzxid.value(stat.getPzxid());
        if (I18nManager.currentLocale() == Locale.SIMPLIFIED_CHINESE || I18nManager.currentLocale() == Locale.PRC) {
            pzxid.friendlyName("最后修改子节点事务ID");
        } else if (I18nManager.currentLocale() == Locale.TRADITIONAL_CHINESE || I18nManager.currentLocale() == Locale.TAIWAN) {
            pzxid.friendlyName("最後修改子節點事務ID");
        } else {
            pzxid.friendlyName("pzxid");
        }
        pzxid.friendlyValue(pzxid.value());

        // 创建时间
        FriendlyInfo<Stat> ctime = new FriendlyInfo<>();
        ctime.name("ctime");
        ctime.value(stat.getCtime());
        if (I18nManager.currentLocale() == Locale.SIMPLIFIED_CHINESE || I18nManager.currentLocale() == Locale.PRC) {
            ctime.friendlyName("创建时间");
        } else if (I18nManager.currentLocale() == Locale.TRADITIONAL_CHINESE || I18nManager.currentLocale() == Locale.TAIWAN) {
            ctime.friendlyName("創建時間");
        } else {
            ctime.friendlyName("ctime");
        }
        if (ctime.value().equals(0L)) {
            ctime.friendlyValue("");
        } else {
            ctime.friendlyValue(Const.DATE_FORMAT.format(ctime.value()));
        }

        // 最后修改时间
        FriendlyInfo<Stat> mtime = new FriendlyInfo<>();
        mtime.name("mtime");
        mtime.value(stat.getMtime());
        if (I18nManager.currentLocale() == Locale.SIMPLIFIED_CHINESE || I18nManager.currentLocale() == Locale.PRC) {
            mtime.friendlyName("最后修改时间");
        } else if (I18nManager.currentLocale() == Locale.TRADITIONAL_CHINESE || I18nManager.currentLocale() == Locale.TAIWAN) {
            mtime.friendlyName("最後修改時間");
        } else {
            mtime.friendlyName("mtime");
        }
        if (mtime.value().equals(0L)) {
            mtime.friendlyValue("");
        } else {
            mtime.friendlyValue(Const.DATE_FORMAT.format(mtime.value()));
        }

        // 数据版本号
        FriendlyInfo<Stat> version = new FriendlyInfo<>();
        version.name("version");
        version.value(stat.getVersion());
        if (I18nManager.currentLocale() == Locale.SIMPLIFIED_CHINESE || I18nManager.currentLocale() == Locale.PRC) {
            version.friendlyName("数据版本号");
        } else if (I18nManager.currentLocale() == Locale.TRADITIONAL_CHINESE || I18nManager.currentLocale() == Locale.TAIWAN) {
            version.friendlyName("數據版本號");
        } else {
            version.friendlyName("version");
        }
        version.friendlyValue(version.value());

        // 权限版本号
        FriendlyInfo<Stat> aversion = new FriendlyInfo<>();
        aversion.name("aversion");
        aversion.value(stat.getAversion());
        if (I18nManager.currentLocale() == Locale.SIMPLIFIED_CHINESE || I18nManager.currentLocale() == Locale.PRC) {
            aversion.friendlyName("权限版本号");
        } else if (I18nManager.currentLocale() == Locale.TRADITIONAL_CHINESE || I18nManager.currentLocale() == Locale.TAIWAN) {
            aversion.friendlyName("權限版本號");
        } else {
            aversion.friendlyName("aversion");
        }
        aversion.friendlyValue(aversion.value());

        // 子节点版本号
        FriendlyInfo<Stat> cversion = new FriendlyInfo<>();
        cversion.name("cversion");
        cversion.value(stat.getCversion());
        if (I18nManager.currentLocale() == Locale.SIMPLIFIED_CHINESE || I18nManager.currentLocale() == Locale.PRC) {
            cversion.friendlyName("子节点版本号");
        } else if (I18nManager.currentLocale() == Locale.TRADITIONAL_CHINESE || I18nManager.currentLocale() == Locale.TAIWAN) {
            cversion.friendlyName("子節點版本號");
        } else {
            cversion.friendlyName("cversion");
        }
        cversion.friendlyValue(cversion.value());

        // 子节点数量
        FriendlyInfo<Stat> numChildren = new FriendlyInfo<>();
        numChildren.name("numChildren");
        numChildren.value(stat.getNumChildren());
        if (I18nManager.currentLocale() == Locale.SIMPLIFIED_CHINESE || I18nManager.currentLocale() == Locale.PRC) {
            numChildren.friendlyName("子节点数量");
        } else if (I18nManager.currentLocale() == Locale.TRADITIONAL_CHINESE || I18nManager.currentLocale() == Locale.TAIWAN) {
            numChildren.friendlyName("子節點數量");
        } else {
            numChildren.friendlyName("numChildren");
        }
        numChildren.friendlyValue(numChildren.value());

        // 数据长度
        FriendlyInfo<Stat> dataLength = new FriendlyInfo<>();
        dataLength.name("dataLength");
        dataLength.value(stat.getDataLength());
        if (I18nManager.currentLocale() == Locale.SIMPLIFIED_CHINESE || I18nManager.currentLocale() == Locale.PRC) {
            dataLength.friendlyName("数据长度");
        } else if (I18nManager.currentLocale() == Locale.TRADITIONAL_CHINESE || I18nManager.currentLocale() == Locale.TAIWAN) {
            dataLength.friendlyName("數據長度");
        } else {
            dataLength.friendlyName("dataLength");
        }
        dataLength.friendlyValue(dataLength.value());

        // 会话ID
        FriendlyInfo<Stat> ephemeralOwner = new FriendlyInfo<>();
        ephemeralOwner.name("ephemeralOwner");
        ephemeralOwner.value(stat.getEphemeralOwner());
        if (I18nManager.currentLocale() == Locale.SIMPLIFIED_CHINESE || I18nManager.currentLocale() == Locale.PRC) {
            ephemeralOwner.friendlyName("会话ID");
        } else if (I18nManager.currentLocale() == Locale.TRADITIONAL_CHINESE || I18nManager.currentLocale() == Locale.TAIWAN) {
            ephemeralOwner.friendlyName("會話ID");
        } else {
            ephemeralOwner.friendlyName("ephemeralOwner");
        }
        ephemeralOwner.friendlyValue(ephemeralOwner.value());

        // 将各个属性的FriendlyInfo添加到statInfos列表中
        statInfos.add(czxid);
        statInfos.add(mzxid);
        statInfos.add(pzxid);
        statInfos.add(ctime);
        statInfos.add(mtime);
        statInfos.add(version);
        statInfos.add(aversion);
        statInfos.add(cversion);
        statInfos.add(dataLength);
        statInfos.add(numChildren);
        statInfos.add(ephemeralOwner);

        // 返回包含Stat对象各个属性的FriendlyInfo列表
        return statInfos;
    }
}
