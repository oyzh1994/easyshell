package cn.oyzh.jeditermfx.terminal.ui.input;

import cn.oyzh.jeditermfx.terminal.emulator.mouse.MouseButtonCodes;
import cn.oyzh.jeditermfx.terminal.emulator.mouse.MouseButtonModifierFlags;
import cn.oyzh.jeditermfx.core.input.MouseWheelEvent;
import javafx.scene.input.ScrollEvent;
import org.jetbrains.annotations.NotNull;

public final class FxMouseWheelEvent extends MouseWheelEvent {

    private static int createButtonCode(@NotNull ScrollEvent fxMouseEvent) {
        if (fxMouseEvent.getDeltaY() > 0) {
            return MouseButtonCodes.SCROLLUP;
        } else {
            return MouseButtonCodes.SCROLLDOWN;
        }
    }

    private static int getModifierKeys(@NotNull ScrollEvent fxMouseEvent) {
        int modifier = 0;
        if (fxMouseEvent.isControlDown()) {
            modifier |= MouseButtonModifierFlags.MOUSE_BUTTON_CTRL_FLAG;
        }
        if (fxMouseEvent.isShiftDown()) {
            modifier |= MouseButtonModifierFlags.MOUSE_BUTTON_SHIFT_FLAG;
        }
        if (fxMouseEvent.isMetaDown()) {
            modifier |= MouseButtonModifierFlags.MOUSE_BUTTON_META_FLAG;
        }
        return modifier;
    }

    private final ScrollEvent myFxMouseWheelEvent;

    public FxMouseWheelEvent(@NotNull ScrollEvent fxMouseWheelEvent) {
        super(createButtonCode(fxMouseWheelEvent), getModifierKeys(fxMouseWheelEvent));
        myFxMouseWheelEvent = fxMouseWheelEvent;
    }

    @Override
    public String toString() {
        return myFxMouseWheelEvent.toString();
    }
}
