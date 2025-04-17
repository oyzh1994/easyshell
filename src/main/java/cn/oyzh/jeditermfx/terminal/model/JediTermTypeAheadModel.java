package cn.oyzh.jeditermfx.terminal.model;

import cn.oyzh.common.util.ReflectUtil;
import cn.oyzh.jeditermfx.terminal.ui.settings.SettingsProvider;
import com.jediterm.core.typeahead.TypeAheadTerminalModel;
import com.jediterm.terminal.Terminal;
import com.jediterm.terminal.model.CharBuffer;
import com.jediterm.terminal.TextStyle;
import com.jediterm.terminal.model.TerminalLine;
import com.jediterm.terminal.model.TerminalModelListener;
import com.jediterm.terminal.model.TerminalTextBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class JediTermTypeAheadModel implements TypeAheadTerminalModel {

    private final @NotNull Terminal myTerminal;

    private final @NotNull TerminalTextBuffer myTerminalTextBuffer;

    private final @NotNull SettingsProvider mySettingsProvider;

    private @NotNull TypeAheadTerminalModel.ShellType myShellType = ShellType.Unknown;

    private boolean isPredictionsApplied = false;

    private final List<TerminalModelListener> myTypeAheadListeners = new CopyOnWriteArrayList<>();

    public JediTermTypeAheadModel(@NotNull Terminal terminal, @NotNull TerminalTextBuffer textBuffer,
                                  @NotNull SettingsProvider settingsProvider) {
        myTerminal = terminal;
        myTerminalTextBuffer = textBuffer;
        mySettingsProvider = settingsProvider;
    }

    @Override
    public void insertCharacter(char ch, int index) {
        isPredictionsApplied = true;
        TerminalLine typeAheadLine = getTypeAheadLine();
        TextStyle typeAheadStyle = mySettingsProvider.getTypeAheadSettings().getTypeAheadStyle();
        typeAheadLine.insertString(index, new CharBuffer(ch, 1), typeAheadStyle);
        setTypeAheadLine(typeAheadLine);
    }

    @Override
    public void removeCharacters(int from, int count) {
        isPredictionsApplied = true;
        TerminalLine typeAheadLine = getTypeAheadLine();
        typeAheadLine.deleteCharacters(from, count, TextStyle.EMPTY);
        setTypeAheadLine(typeAheadLine);
    }

    public void forceRedraw() {
//        myTerminalTextBuffer.fireTypeAheadModelChangeEvent();
        this.fireTypeAheadModelChangeEvent();
    }

    @Override
    public void moveCursor(int index) {
    }

    @Override
    public void clearPredictions() {
        if (isPredictionsApplied) {
            myTerminalTextBuffer.clearTypeAheadPredictions();
        }
        isPredictionsApplied = false;
    }

    @Override
    public void lock() {
        myTerminalTextBuffer.lock();
    }

    @Override
    public void unlock() {
        myTerminalTextBuffer.unlock();
    }

    @Override
    public boolean isUsingAlternateBuffer() {
        return myTerminalTextBuffer.isUsingAlternateBuffer();
    }

    @Override
    public boolean isTypeAheadEnabled() {
        return mySettingsProvider.getTypeAheadSettings().isEnabled();
    }

    @Override
    public long getLatencyThreshold() {
        return mySettingsProvider.getTypeAheadSettings().getLatencyThreshold();
    }

    @Override
    public @NotNull ShellType getShellType() {
        return myShellType;
    }

    public void setShellType(ShellType shellType) {
        myShellType = shellType;
    }

    @Override
    public @NotNull TypeAheadTerminalModel.LineWithCursorX getCurrentLineWithCursor() {
        TerminalLine terminalLine = myTerminalTextBuffer.getLine(myTerminal.getCursorY() - 1);
        return new LineWithCursorX(new StringBuffer(terminalLine.getText()), myTerminal.getCursorX() - 1);
    }

    @Override
    public int getTerminalWidth() {
        return myTerminal.getTerminalWidth();
    }

    private @NotNull TerminalLine getTypeAheadLine() {
        TerminalLine terminalLine = myTerminalTextBuffer.getLine(myTerminal.getCursorY() - 1);
        TerminalLine typeAheadLine=  ReflectUtil.getFieldValue(terminalLine, "myTypeAheadLine");
        if (typeAheadLine != null) {
            terminalLine = typeAheadLine;
        }
//        if (terminalLine.getTypeAheadLine() != null) {
//            terminalLine = terminalLine.getTypeAheadLine();
//        }
        return terminalLine.copy();
    }

    private void setTypeAheadLine(@NotNull TerminalLine typeAheadTerminalLine) {
        TerminalLine terminalLine = myTerminalTextBuffer.getLine(myTerminal.getCursorY() - 1);
//        terminalLine.setTypeAheadLine(typeAheadTerminalLine);
          ReflectUtil.setFieldValue("myTypeAheadLine",typeAheadTerminalLine,terminalLine);
    }

    public void addTypeAheadModelListener(@NotNull TerminalModelListener listener) {
        myTypeAheadListeners.add(listener);
    }

    public void removeTypeAheadModelListener(@NotNull TerminalModelListener listener) {
        myTypeAheadListeners.remove(listener);
    }

    private void fireTypeAheadModelChangeEvent() {
        for (TerminalModelListener typeAheadListener : myTypeAheadListeners) {
            typeAheadListener.modelChanged();
        }
    }
}
