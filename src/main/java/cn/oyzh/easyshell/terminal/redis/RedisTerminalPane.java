package cn.oyzh.easyshell.terminal.redis;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ExecutorUtil;
import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.dto.redis.RedisConnectInfo;
import cn.oyzh.easyshell.exception.redis.RedisExceptionParser;
import cn.oyzh.easyshell.redis.RedisClient;
import cn.oyzh.easyshell.redis.RedisConnState;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.util.RedisConnectUtil;
import cn.oyzh.fx.plus.font.FontManager;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import cn.oyzh.fx.terminal.TerminalPane;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.value.ChangeListener;
import javafx.scene.text.Font;

/**
 * redis终端文本域
 *
 * @author oyzh
 * @since 2023/7/21
 */
public class RedisTerminalPane extends TerminalPane {

    {
        this.keyHandler(RedisTerminalKeyHandler.INSTANCE);
        this.helpHandler(RedisTerminalHelpHandler.INSTANCE);
        this.mouseHandler(RedisTerminalMouseHandler.INSTANCE);
        this.historyHandler(RedisTerminalHistoryHandler.INSTANCE);
        this.completeHandler(RedisTerminalCompleteHandler.INSTANCE);
    }

//     @Override
//     public void initNode() {
//         super.initNode();
//         super.initPrompts();
//     }
//
//     @Override
//     protected Font initFont() {
// //        // 禁用字体管理
// //        super.disableFont();
//         // 初始化字体
//         RedisSetting setting = RedisSettingStore.SETTING;
// //        this.setFontSize(setting.getTerminalFontSize());
// //        this.setFontFamily(setting.getTerminalFontFamily());
// //        this.setFontWeight2(setting.getTerminalFontWeight());
//         return FontManager.toFont(setting.terminalFontConfig());
//     }

    @Override
    protected Font getEditorFont() {
        if (super.getEditorFont() == null) {
            ShellSetting setting = ShellSettingStore.SETTING;
            Font font = FontManager.toFont(setting.terminalFontConfig());
            super.setEditorFont(font);
        }
        return super.getEditorFont();
    }

    //@Override
    //public void changeFont(Font font) {
    //    //RedisSetting setting = RedisSettingStore.SETTING;
    //    //Font font1 = FontManager.toFont(setting.terminalFontConfig());
    //    super.changeFont(font);
    //    //super.applyEditorFont();
    //}

    /**
     * redis客户端
     */
    private RedisClient client;

    public RedisClient getClient() {
        return client;
    }

    /**
     * redis连接
     */
    private RedisConnectInfo connectInfo;

    /**
     * redis客户端连接状态监听器
     */
    private ChangeListener<RedisConnState> stateChangeListener;

    /**
     * db索引
     */
    private Integer dbIndex;

    public Integer getDbIndex() {
        return dbIndex;
    }

    @Override
    public void flushPrompt() {
        String str;
        if (this.isTemporary()) {
            str = "redis " + I18nHelper.connection();
        } else {
            str = this.client.connectName();
        }
        if (this.redisConnect().getHost() != null) {
            str += "@" + this.redisConnect().getHost();
        }
        if (this.isConnecting()) {
            str += "(" + I18nHelper.connectIng() + this.getDbName() + ")> ";
        } else if (this.isConnected()) {
            if (this.client.isReadonly()) {
                str += "(" + I18nHelper.connected() + "/" + I18nHelper.readonlyMode() + this.getDbName() + ")> ";
            } else {
                str += "(" + I18nHelper.connected() + this.getDbName() + ")> ";
            }
        } else {
            str += this.getDbName() + "> ";
        }
        this.prompt(str);
    }

    private String getDbName() {
        return this.dbIndex == null ? "" : "@db" + this.dbIndex;
    }

    /**
     * 初始化
     *
     * @param client  redis客户端
     * @param dbIndex db索引
     */
    public void init(RedisClient client, Integer dbIndex) {
        this.client = client;
        this.dbIndex = dbIndex;
        this.disableInput();
        this.outputLine(I18nResourceBundle.i18nString("redis.home.welcome"));
        this.outputLine("Powered By oyzh(2023-2025).");
        this.flushPrompt();
        if (this.isTemporary()) {
            this.initByTemporary();
        } else {
            this.initByPermanent();
        }
    }

    /**
     * 是否临时连接
     *
     * @return 结果
     */
    public boolean isTemporary() {
        return this.client.iid() == null;
    }

    @Override
    public void outputPrompt() {
        if (!this.client.isConnecting()) {
            super.outputPrompt();
        }
    }

    /**
     * 是否已连接
     *
     * @return 结果
     */
    public boolean isConnected() {
        return this.client != null && this.client.isConnected();
    }

    /**
     * 是否连接中
     *
     * @return 结果
     */
    public boolean isConnecting() {
        return this.client != null && this.client.isConnecting();
    }

    /**
     * 是否已关闭
     *
     * @return 结果
     */
    public boolean isClosed() {
        return this.client != null && this.client.isClosed();
    }

    /**
     * 执行连接
     *
     * @param input 输入内容
     */
    public void connect(String input) {
        this.connectInfo = RedisConnectUtil.parse(input);
        if (this.connectInfo != null) {
            this.disable();
            RedisConnectUtil.copyConnect(this.connectInfo, this.redisConnect());
            this.start(this.connectInfo.getDb());
        }
    }

    /**
     * 临时连接处理
     */
    private void initByTemporary() {
        this.outputLine("connect [-timeout timeout] -h host [-p port] [-u user] [-a password] [-n db] [-r]");
        this.outputLine("-timeout " + I18nResourceBundle.i18nString("base.unit", "base.ms"));
        this.outputLine("-h " + I18nHelper.host());
        this.outputLine("-p " + I18nHelper.port());
        this.outputLine("-u " + I18nHelper.userName());
        this.outputLine("-a " + I18nHelper.password());
        this.outputLine("-n " + I18nHelper.database());
        this.outputLine("-r " + I18nHelper.readonlyMode());
        this.appendByPrompt("connect -timeout 3000 -h 127.0.0.1 -p 6379 -n 0");
        this.enableInput();
        this.flushAndMoveCaretEnd();
    }

    /**
     * 常驻连接处理
     */
    private void initByPermanent() {
//        this.start(0);
        this.flushPrompt();
        this.appendByPrompt("");
        this.enableInput();
        this.flushAndMoveCaretEnd();
    }

    /**
     * 开始连接
     */
    private void start(int db) {
        TaskManager.startSync(() -> {
            try {
                this.initStatListener();
                this.client.startDatabase(db);
            } catch (Exception ex) {
                this.onError(RedisExceptionParser.INSTANCE.apply(ex));
            } finally {
                this.enable();
            }
        });
    }

    /**
     * 刷新光标并移动到尾部
     */
    private void flushAndMoveCaretEnd() {
        ExecutorUtil.start(() -> {
            this.flushCaret();
            this.moveCaretEnd();
        }, 50);
    }

    /**
     * 初始化连接状态监听器
     */
    private void initStatListener() {
        if (this.stateChangeListener == null) {
            this.stateChangeListener = (observableValue, state, t1) -> {
                this.flushPrompt();
                // 获取连接
                String host = this.client.redisConnect().getHost();
                if (t1 == RedisConnState.CONNECTED) {
                    this.outputLine(host + I18nHelper.connectSuccess() + " .");
                    this.outputLine(I18nHelper.terminalTip2());
                    this.outputLine(I18nHelper.terminalTip1());
                    this.outputPrompt();
                    this.flushCaret();
                    super.enableInput();
                } else if (t1 == RedisConnState.CLOSED) {
                    this.outputLine(host + " " + I18nHelper.connectionClosed() + " .");
                    this.enableInput();
                } else if (t1 == RedisConnState.CONNECTING) {
                    this.outputLine(host + " " + I18nHelper.connectionConnecting() + " .");
                    this.disableInput();
                } else if (t1 == RedisConnState.BROKEN) {
                    this.outputLine(host + " " + I18nHelper.connectionBroken() + " .");
                    this.enableInput();
                } else if (t1 == RedisConnState.FAILED) {
                    this.outputLine(host + " " + I18nHelper.connectFail() + " .");
                    if (this.connectInfo != null) {
                        this.appendByPrompt(this.connectInfo.getInput());
                    }
                    this.flushAndMoveCaretEnd();
                    this.enableInput();
                }
                JulLog.info("connState={}", t1);
            };
            this.getClient().addStateListener(this.stateChangeListener);
        }
    }

    @Override
    public void enableInput() {
        if (this.isConnecting()) {
            return;
        }
        if (this.isConnected() || (!this.isConnected() && this.isTemporary())) {
            super.enableInput();
        }
    }

    public ShellConnect redisConnect() {
        return this.getClient().redisConnect();
    }

    @Override
    public void fontSizeIncr() {
        super.fontSizeIncr();
        this.saveFontSize();
    }

    @Override
    public void fontSizeDecr() {
        //double fSize= super.getFontSize();
        // this.editorFont = this.getEditorFont();
        // this.getEditorFont();
        super.fontSizeDecr();
        this.saveFontSize();
    }

    /**
     * 保存字体大小
     */
    private void saveFontSize() {
        //System.out.println(this.getFontSize());
        ShellSetting setting = ShellSettingStore.SETTING;
        setting.setTerminalFontSize((byte) this.getFontSize());
        ShellSettingStore.INSTANCE.replace(setting);
        ////刷新字体
        //this.editorFont = null;
        //this.getEditorFont();
        //System.out.println(this.getEditorFont());
    }
}
