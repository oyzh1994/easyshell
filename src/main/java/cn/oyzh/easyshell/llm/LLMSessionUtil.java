package cn.oyzh.easyshell.llm;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.UUIDUtil;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import com.alibaba.fastjson2.JSON;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * LLM会话工具类
 *
 * @author oyzh
 * @since 2026-10-31
 */
public final class LLMSessionUtil {

    private LLMSessionUtil() {
    }

    /**
     * 新建会话
     *
     * @param tab 绑定的Tab
     * @return 会话
     */
    public static LLMSession newSession(LLMSessionTab tab) {
        LLMSession session = new LLMSession();
        session.setUid(UUIDUtil.uuidSimple());
        session.setSessionTabId(tab == null ? null : tab.getId());
        session.setTitle(tab == null || StringUtil.isBlank(tab.getName()) ? untitled() : tab.getName());
        return session;
    }

    /**
     * 获取会话显示标题
     *
     * @param session 会话
     * @return 显示标题
     */
    public static String getDisplayTitle(LLMSession session) {
        if (session == null || StringUtil.isBlank(session.getTitle())) {
            return untitled();
        }
        return session.getTitle();
    }

    /**
     * 获取未命名会话标题
     *
     * @return 未命名会话标题
     */
    public static String untitled() {
        return i18n("shell.llm.untitled", "Untitled");
    }

    /**
     * 获取会话过期提示
     *
     * @return 会话过期提示
     */
    public static String sessionExpired() {
        return i18n("shell.llm.sessionExpired", "Session expired");
    }

    /**
     * 获取会话历史标题
     *
     * @return 会话历史标题
     */
    public static String sessionHistory() {
        return i18n("shell.llm.sessionHistory", i18n("base.history", "History"));
    }

    /**
     * 获取新建会话确认提示
     *
     * @return 确认提示
     */
    public static String newSessionConfirm() {
        return i18n("shell.llm.newSessionConfirm", "Create a new session? The current session will be saved.");
    }

    private static String i18n(String key, String defaultValue) {
        try {
            if (I18nResourceBundle.containsI18nKey(key)) {
                String value = I18nResourceBundle.i18nString(key);
                if (StringUtil.isNotBlank(value)) {
                    return value;
                }
            }
        } catch (Exception ignored) {
        }
        return defaultValue;
    }

    /**
     * 将消息列表序列化为JSON
     *
     * @param messages 消息列表
     * @return JSON
     */
    public static String toJson(List<LLMMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return "[]";
        }
        List<LLMSessionMessage> items = new ArrayList<>(messages.size());
        for (LLMMessage message : messages) {
            if (message == null) {
                continue;
            }
            LLMSessionMessage item = new LLMSessionMessage();
            item.setMessageId(message.getMessageId());
            item.setType(message.getType());
            item.setTime(message.getTime());
            item.setMessage(message.getMessage());
            item.setModel(message.getModel());
            File file = message.getFile();
            if (file != null) {
                item.setFilePath(file.getAbsolutePath());
            }
            items.add(item);
        }
        return JSON.toJSONString(items);
    }

    /**
     * 从JSON恢复消息列表
     *
     * @param json JSON
     * @return 消息列表
     */
    public static List<LLMMessage> fromJson(String json) {
        if (StringUtil.isBlank(json)) {
            return List.of();
        }
        try {
            List<LLMSessionMessage> items = JSON.parseArray(json, LLMSessionMessage.class);
            if (items == null || items.isEmpty()) {
                return List.of();
            }
            List<LLMMessage> messages = new ArrayList<>(items.size());
            for (LLMSessionMessage item : items) {
                if (item == null || item.getType() == null) {
                    continue;
                }
                LLMMessage message = new LLMMessage(item.getType(), item.getTime());
                if (StringUtil.isNotBlank(item.getMessageId())) {
                    message.setMessageId(item.getMessageId());
                }
                message.setMessage(item.getMessage());
                message.setModel(item.getModel());
                if (StringUtil.isNotBlank(item.getFilePath())) {
                    message.setFile(new File(item.getFilePath()));
                }
                messages.add(message);
            }
            return messages;
        } catch (Exception ex) {
            JulLog.warn("parse llm session messages fail", ex);
            return List.of();
        }
    }
}
