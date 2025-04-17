//package cn.oyzh.jeditermfx.terminal.model;
//
//import org.jetbrains.annotations.ApiStatus;
//import java.util.List;
//
//// 标记为实验性 API
//@ApiStatus.Experimental
//// 定义一个接口，用于监听文本缓冲区内容变化的详细事件
//public interface TextBufferChangesListener {
//    /**
//     * 当索引为 [fromIndex] 的行以及可能其后的一些行发生变化时调用此方法。
//     *
//     * @param fromIndex 文本缓冲区中的行索引。正数表示屏幕上的行，负数表示历史记录中的行。
//     * 例如，索引为 0 的行是屏幕上的第一行，索引为 -1 的行是历史记录中的最后一行。
//     */
//    default void linesChanged(int fromIndex) {
//    }
//
//    /**
//     * 当历史缓冲区容量超出限制，文本缓冲区不得不从历史记录开头丢弃一些行时调用此方法。
//     *
//     * @param lines 被丢弃的行。
//     */
//    default void linesDiscardedFromHistory(List<TerminalLine> lines) {
//    }
//
//    /**
//     * 当所有行从历史缓冲区中被移除时调用此方法。
//     * 例如，在执行 `clear`（ED - Erase in Display）或 RIS（Reset to the Initial State）转义序列后。
//     */
//    default void historyCleared() {
//    }
//
//    /**
//     * 当文本缓冲区的宽度发生变化时调用此方法。
//     */
//    default void widthResized() {
//    }
//}