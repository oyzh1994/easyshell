package cn.oyzh.jeditermfx.terminal.ui;

import cn.oyzh.jeditermfx.core.compatibility.Point;
import cn.oyzh.jeditermfx.terminal.model.CharBuffer;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public interface FindResult {

    public static interface FindItem {

        @NotNull
        public String getText();

        /**
         * Returns one-based index in the result list.
         * @return
         */
        public int getIndex();

        public Point getStart();

        public Point getEnd();
    }

    public @Nullable List<Pair<Integer, Integer>> getRanges(CharBuffer characters);

    public @NotNull List<FindItem> getItems();

    public @NotNull FindItem selectedItem();

    public @NotNull FindItem nextFindItem();

    public @NotNull FindItem prevFindItem();
}
