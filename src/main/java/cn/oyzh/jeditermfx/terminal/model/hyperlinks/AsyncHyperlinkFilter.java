//package cn.oyzh.jeditermfx.terminal.model.hyperlinks;
//
//import java.util.concurrent.CompletableFuture;
//
//// 定义异步超链接过滤器接口
//public interface AsyncHyperlinkFilter {
//    /**
//     * 在给定的行中查找链接。
//     *
//     * @param lineInfo 要应用过滤器的一行文本的信息。
//     * @return 一个 CompletableFuture 实例，当找到链接时异步解析。
//     *         如果未找到链接或 lineInfo.line 为 null，则可以用 null 或异常完成。
//     *         同样，永远不完成返回的实例也是可以的。
//     */
//    CompletableFuture<LinkResult> apply(LineInfo lineInfo);
//
//    // 定义行信息接口
//    interface LineInfo {
//        /**
//         * @return 行字符串；如果为 null，则应跳过查找链接
//         *         它可以从任何线程访问。
//         */
//        String getLine();
//    }
//}