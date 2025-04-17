//package cn.oyzh.jeditermfx.terminal.model;
//
//import com.jediterm.terminal.StyledTextConsumer;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.NoSuchElementException;
//
///**
// * Base interface for storing terminal lines.
// * For the first line of the storage, we use the term top, for the last line - bottom.
// * Supports adding/removing lines from the top and bottom.
// * Also, accessing lines by index.
// */
//public interface LinesStorage extends Iterable<TerminalLine> {
//    /**
//     * Count of the available lines in the storage
//     */
//    int size();
//
//    /**
//     * Throws IndexOutOfBoundsException if index is negative.
//     * If index is greater than size(), storage should be filled with empty lines until index line.
//     */
//    TerminalLine get(int index);
//
//    /**
//     * @return -1 if there is no such line
//     */
//    int indexOf(TerminalLine line);
//
//    /**
//     * Adds a new line to the start of the storage.
//     * If the implementation limits the max capacity of the storage,
//     * and storage is full, then the line should not be added.
//     */
//    void addToTop(TerminalLine line);
//
//    /**
//     * Adds a new line to the end of the storage.
//     * If the implementation limits the max capacity of the storage
//     * and storage is full, the first line from the top should be removed.
//     */
//    void addToBottom(TerminalLine line);
//
//    /**
//     * Removes a single line from the start of the storage.
//     *
//     * @throws NoSuchElementException if storage is empty.
//     */
//    TerminalLine removeFromTop();
//
//    /**
//     * Removes a single line from the end of the storage.
//     *
//     * @throws NoSuchElementException if storage is empty.
//     */
//    TerminalLine removeFromBottom();
//
//    void clear();
//
//    int DEFAULT_MAX_LINES_COUNT = 5000;
//
//    static void addAllToTop(LinesStorage storage, List<TerminalLine> lines) {
//        for (int ind = lines.size() - 1; ind >= 0; ind--) {
//            storage.addToTop(lines.get(ind));
//        }
//    }
//
//    static void addAllToBottom(LinesStorage storage, List<TerminalLine> lines) {
//        for (TerminalLine line : lines) {
//            storage.addToBottom(line);
//        }
//    }
//
//    static List<TerminalLine> removeFromTop(LinesStorage storage, int count) {
//        return perform(storage, count, false, storage::removeFromTop);
//    }
//
//    static List<TerminalLine> removeFromBottom(LinesStorage storage, int count) {
//        return perform(storage, count, true, storage::removeFromBottom);
//    }
//
//    static List<TerminalLine> perform(LinesStorage storage, int count, boolean reverse, Operation operation) {
//        if (count < 0) {
//            throw new IllegalArgumentException("Count must be >= 0");
//        }
//        if (count == 0) {
//            return new ArrayList<>();
//        }
//        int actualCount = Math.min(count, storage.size());
//        if (actualCount == 0) {
//            return new ArrayList<>();
//        } else if (actualCount == 1) {
//            List<TerminalLine> result = new ArrayList<>();
//            result.add(operation.execute());
//            return result;
//        } else {
//            List<TerminalLine> result = new ArrayList<>(actualCount);
//            for (int i = 0; i < actualCount; i++) {
//                result.add(operation.execute());
//            }
//            if (reverse) {
//                java.util.Collections.reverse(result);
//            }
//            return result;
//        }
//    }
//
//    static int removeBottomEmptyLines(LinesStorage storage, int maxCount) {
//        int removedCount = 0;
//        int ind = storage.size() - 1;
//        while (removedCount < maxCount && ind >= 0 && storage.get(ind).isNulOrEmpty()) {
//            ind--;
//            removedCount++;
//        }
//        LinesStorage.removeFromBottom(storage, removedCount);
//        return removedCount;
//    }
//
//    /**
//     * @param startRow value passed as a corresponding parameter of the StyledTextConsumer methods.
//     */
//    static void processLines(LinesStorage storage, int yStart, int count, StyledTextConsumer consumer, int startRow) {
//        if (yStart < 0) {
//            throw new IllegalArgumentException("yStart is " + yStart + ", should be >0");
//        }
//        int maxY = Math.min(yStart + count, storage.size());
//        for (int y = yStart; y < maxY; y++) {
//            storage.get(y).process(y, consumer, startRow);
//        }
//    }
//
//    /**
//     * Performs a cyclic shift:
//     * adds count of lines at the position y, then removes count of lines from the end of [y, lastLine] range.
//     *
//     * @param y        index of the insertion point, the operation does not affect all lines before this line.
//     * @param count    number of lines to insert.
//     * @param lastLine the operation does not affect all lines after this line.
//     */
//    static void insertLines(LinesStorage storage, int y, int count, int lastLine, TerminalLine filler) {
//        // Implementation for insertLines method
//    }
//
//    interface Operation {
//        TerminalLine execute();
//    }
//}