package com.jediterm.terminal.ui.input;

import com.jediterm.core.input.MouseWheelEvent;
import com.jediterm.terminal.emulator.mouse.MouseButtonCodes;
import com.jediterm.terminal.emulator.mouse.MouseButtonModifierFlags;
import javafx.scene.input.ScrollEvent;
import org.jetbrains.annotations.NotNull;

public final class FXMouseWheelEvent extends MouseWheelEvent {
    private final ScrollEvent myFxMouseWheelEvent;

    public FXMouseWheelEvent(@NotNull ScrollEvent fxMouseWheelEvent) {
        super(createButtonCode(fxMouseWheelEvent), getModifierKeys(fxMouseWheelEvent));
        myFxMouseWheelEvent = fxMouseWheelEvent;
    }

    @Override
    public String toString() {
        return myFxMouseWheelEvent.toString();
    }

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
}
