package cn.oyzh.easyshell.query.redis;

import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.redis.ShellRedisClient;
import cn.oyzh.easyshell.redis.ShellRedisKeyUtil;
import cn.oyzh.fx.plus.controls.popup.FXPopup;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.thread.RenderService;
import cn.oyzh.fx.plus.util.FXUtil;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * 查询弹框
 *
 * @author oyzh
 * @since 2025/01/21
 */
public class RedisQueryPromptPopup extends FXPopup {

    /**
     * 提示字符
     */
    private final static List<KeyCode> PROMPT_CODES = new ArrayList<>();

    /**
     * 更新字符
     */
    private final static List<KeyCode> UPDATE_CODES = new ArrayList<>();

    static {
        // 字母
        PROMPT_CODES.add(KeyCode.A);
        PROMPT_CODES.add(KeyCode.B);
        PROMPT_CODES.add(KeyCode.C);
        PROMPT_CODES.add(KeyCode.D);
        PROMPT_CODES.add(KeyCode.E);
        PROMPT_CODES.add(KeyCode.F);
        PROMPT_CODES.add(KeyCode.G);
        PROMPT_CODES.add(KeyCode.H);
        PROMPT_CODES.add(KeyCode.I);
        PROMPT_CODES.add(KeyCode.J);
        PROMPT_CODES.add(KeyCode.K);
        PROMPT_CODES.add(KeyCode.L);
        PROMPT_CODES.add(KeyCode.M);
        PROMPT_CODES.add(KeyCode.N);
        PROMPT_CODES.add(KeyCode.O);
        PROMPT_CODES.add(KeyCode.P);
        PROMPT_CODES.add(KeyCode.Q);
        PROMPT_CODES.add(KeyCode.R);
        PROMPT_CODES.add(KeyCode.S);
        PROMPT_CODES.add(KeyCode.T);
        PROMPT_CODES.add(KeyCode.U);
        PROMPT_CODES.add(KeyCode.V);
        PROMPT_CODES.add(KeyCode.W);
        PROMPT_CODES.add(KeyCode.X);
        PROMPT_CODES.add(KeyCode.Y);
        PROMPT_CODES.add(KeyCode.Z);
        // 小键盘数字
        PROMPT_CODES.add(KeyCode.NUMPAD0);
        PROMPT_CODES.add(KeyCode.NUMPAD1);
        PROMPT_CODES.add(KeyCode.NUMPAD2);
        PROMPT_CODES.add(KeyCode.NUMPAD3);
        PROMPT_CODES.add(KeyCode.NUMPAD4);
        PROMPT_CODES.add(KeyCode.NUMPAD5);
        PROMPT_CODES.add(KeyCode.NUMPAD6);
        PROMPT_CODES.add(KeyCode.NUMPAD7);
        PROMPT_CODES.add(KeyCode.NUMPAD8);
        PROMPT_CODES.add(KeyCode.NUMPAD9);
        // 软盘数字
        PROMPT_CODES.add(KeyCode.SOFTKEY_0);
        PROMPT_CODES.add(KeyCode.SOFTKEY_1);
        PROMPT_CODES.add(KeyCode.SOFTKEY_2);
        PROMPT_CODES.add(KeyCode.SOFTKEY_3);
        PROMPT_CODES.add(KeyCode.SOFTKEY_4);
        PROMPT_CODES.add(KeyCode.SOFTKEY_5);
        PROMPT_CODES.add(KeyCode.SOFTKEY_6);
        PROMPT_CODES.add(KeyCode.SOFTKEY_7);
        PROMPT_CODES.add(KeyCode.SOFTKEY_8);
        PROMPT_CODES.add(KeyCode.SOFTKEY_9);
        // 数字
        PROMPT_CODES.add(KeyCode.DIGIT0);
        PROMPT_CODES.add(KeyCode.DIGIT1);
        PROMPT_CODES.add(KeyCode.DIGIT2);
        PROMPT_CODES.add(KeyCode.DIGIT3);
        PROMPT_CODES.add(KeyCode.DIGIT4);
        PROMPT_CODES.add(KeyCode.DIGIT5);
        PROMPT_CODES.add(KeyCode.DIGIT6);
        PROMPT_CODES.add(KeyCode.DIGIT7);
        PROMPT_CODES.add(KeyCode.DIGIT8);
        PROMPT_CODES.add(KeyCode.DIGIT9);
        // 其他字符
        PROMPT_CODES.add(KeyCode.MINUS);
        PROMPT_CODES.add(KeyCode.SLASH);

        // 更新字符
        UPDATE_CODES.add(KeyCode.BACK_SPACE);
        UPDATE_CODES.add(KeyCode.DELETE);
        UPDATE_CODES.add(KeyCode.SPACE);
    }

    /**
     * 选中事件
     */
    protected Consumer<RedisQueryPromptItem> onItemSelected;

    public RedisQueryPromptPopup() {
        this.setAutoFix(true);
        this.setAutoHide(true);
        this.initContent();
        this.changeTheme(ThemeManager.currentTheme());
    }

    /**
     * 初始化内容组件
     */
    protected void initContent() {
        RedisQueryPromptListView listView = this.listView();
        if (listView == null) {
            listView = new RedisQueryPromptListView();
            this.getContent().setAll(listView);
            listView.setFontSize(12.0);
            listView.setCursor(Cursor.HAND);
            listView.setOnItemPicked(() -> {
                this.pickItem();
                this.hide();
            });
        }
    }

    /**
     * 列表组件
     *
     * @return 列表组件
     */
    public RedisQueryPromptListView listView() {
        return (RedisQueryPromptListView) CollectionUtil.getFirst(this.getContent());
    }

    /**
     * 初始化提示词
     *
     * @param token       提示词
     * @param redisClient redis客户端
     * @param dbIndex     db索引
     * @return 结果
     */
    public synchronized boolean initPrompts(RedisQueryToken token, ShellRedisClient redisClient, int dbIndex) {
        // 初始化提示的键列表
        if (token.isPossibilityKey()) {
            try {
                List<String> keys = ShellRedisKeyUtil.scanKeys(dbIndex, redisClient, "*", 30);
                RedisQueryUtil.setKeys(keys);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            RedisQueryUtil.setKeys(null);
        }
        // 提示词列表
        List<RedisQueryPromptItem> items = RedisQueryUtil.initPrompts(token, 0.5f);
        // 初始化数据
        this.listView().init(items);
        // 判断是否为空
        return !this.listView().isItemEmpty();
    }

    /**
     * token
     */
    private RedisQueryToken token;

    /**
     * 提示词标志位
     */
    private final AtomicInteger promptFlag = new AtomicInteger();

    public Consumer<RedisQueryPromptItem> getOnItemSelected() {
        return onItemSelected;
    }

    public void setOnItemSelected(Consumer<RedisQueryPromptItem> onItemSelected) {
        this.onItemSelected = onItemSelected;
    }

    public RedisQueryToken getToken() {
        return token;
    }

    public void setToken(RedisQueryToken token) {
        this.token = token;
    }

    /**
     * 执行提示
     *
     * @param area  文本域
     * @param event 键盘按键事件
     */
    public void prompt(RedisQueryEditor area, KeyEvent event) {
        // 常规按键不处理
        if (this.isGeneralKeyEvent(event)) {
            this.hide();
            return;
        }
        KeyCode code = event.getCode();
        // 已显示
        if (this.isShowing()) {
            // 按键下
            if (code == KeyCode.DOWN) {
                this.listView().pickNext();
                return;
            }
            // 选中内容清空下才处理
            if (this.listView().hasPicked()) {
                // 按键上
                if (code == KeyCode.UP) {
                    this.listView().pickPrev();
                    return;
                }
                // 按键回车
                if (code == KeyCode.ENTER) {
                    this.pickItem();
                    this.hide();
                    return;
                }
            }
        }
        // 更新按键
        if (UPDATE_CODES.contains(code)) {
            this.hide();
            return;
        }
        // 如果是控制型按键、或者非提示词列表，则隐藏提示组件
        if (event.isShortcutDown() || !PROMPT_CODES.contains(code)) {
            this.hide();
            return;
        }
        // 光标位置
        int cartPos = area.caretPosition();
        // 文本内容
        String content = area.getText();
        // 获取token
        this.token = RedisQueryTokenAnalyzer.INSTANCE.currentToken(content, cartPos);
        // 处理token
        if (this.token != null && (this.token.isPossibilityParam() || this.token.isNotEmpty())) {
            // 生成标志位
            int promptFlagVal = this.promptFlag.incrementAndGet();
            // 延迟显示提示词
            TaskManager.startDelay(() -> {
                // 初始化提示词
                if (this.promptFlag.get() == promptFlagVal) {
                    if (this.initPrompts(this.token, area.getClient(), area.getDbIndex())) {
                        this.showPrompt(area);
                    } else {
                        this.hide();
                    }
                }
            }, 30);
            return;
        }
        // 隐藏组件
        this.hide();
    }

    /**
     * 显示提示词组件
     *
     * @param area 文本域
     */
    private void showPrompt(RedisQueryEditor area) {
        RenderService.submitFXLater(() -> {
            Optional<Bounds> optional = area.getCaretBounds();
            // 显示提示词
            optional.ifPresent(value -> this.show(area, value.getCenterX() - 20, value.getCenterY() + 5));
        });
    }

    @Override
    public void hide() {
        if (this.isShowing()) {
            FXUtil.runWait(super::hide);
        }
        this.token = null;
    }

    /**
     * 执行自动完成
     *
     * @param editor 编辑器
     * @param item   提示内容
     */
    public void autoComplete(RedisQueryEditor editor, RedisQueryPromptItem item) {
        try {
            if (this.token != null) {
                editor.replaceText(this.token.getStartIndex(), this.token.getEndIndex(), item.getContent());
                editor.positionCaret(this.token.getEndIndex() + item.getContent().length() - 1);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 选中提示词
     */
    private void pickItem() {
        if (this.onItemSelected != null && this.isShowing()) {
            RedisQueryPromptItem pickedItem = this.listView().getPickedItem();
            if (pickedItem != null) {
                this.onItemSelected.accept(pickedItem);
            }
        }
    }

    /**
     * 是否常规按键事件
     *
     * @param event 按键事件
     * @return 结果
     */
    private boolean isGeneralKeyEvent(KeyEvent event) {
        KeyCode code = event.getCode();
        // 保存
        if (KeyboardUtil.isSave(event)) {
            return true;
        }
        // 剪切
        if (KeyboardUtil.isCut(event)) {
            return true;
        }
        // 粘贴
        if (KeyboardUtil.isPaste(event)) {
            return true;
        }
        // 复制
        if (KeyboardUtil.isCopy(event)) {
            return true;
        }
        // 全选
        if (KeyboardUtil.isSelectAll(event)) {
            return true;
        }
        // 撤销
        if (KeyboardUtil.isUndo(event)) {
            return true;
        }
        // 重做
        if (KeyboardUtil.isRedo(event)) {
            return true;
        }
//        // 注释
//        if (event.isControlDown() && KeyCode.SLASH == code) {
//            return true;
//        }
        return false;
    }
}
