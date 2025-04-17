package com.techsenger.jeditermfx.terminal.model;

import java.util.Iterator;

// 假设 LinesStorage 接口存在，这里给出一个简单的定义
interface LinesStorage {
    int getSize();

    TerminalLine get(int index);

    int indexOf(TerminalLine line);

    void addToTop(TerminalLine line);

    void addToBottom(TerminalLine line);

    TerminalLine removeFromTop();

    TerminalLine removeFromBottom();

    void clear();

    Iterator<TerminalLine> iterator();
}
