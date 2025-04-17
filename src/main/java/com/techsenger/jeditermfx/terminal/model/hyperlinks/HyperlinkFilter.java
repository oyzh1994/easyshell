package com.techsenger.jeditermfx.terminal.model.hyperlinks;

import org.jetbrains.annotations.Nullable;

/**
 * @author traff
 */
public interface HyperlinkFilter {

    @Nullable
    LinkResult apply(String line);
}
