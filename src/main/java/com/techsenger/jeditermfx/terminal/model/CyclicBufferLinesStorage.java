package com.techsenger.jeditermfx.terminal.model;

import java.util.ArrayDeque;
import java.util.Iterator;

// 定义 CyclicBufferLinesStorage 类，实现 LinesStorage 接口
class CyclicBufferLinesStorage implements LinesStorage {
    // 最大存储行数，-1 表示无限制
    private final int maxCapacity;
    // 存储 TerminalLine 对象的双端队列
    private final ArrayDeque<TerminalLine> lines;
    // 表示容量是否受限
    private final boolean isCapacityLimited;

    // 构造函数，初始化最大容量和队列
    public CyclicBufferLinesStorage(int maxCapacity) {
        this.maxCapacity = maxCapacity;
        this.lines = new ArrayDeque<>();
        this.isCapacityLimited = maxCapacity >= 0;
    }

    // 获取存储的行数
    @Override
    public int getSize() {
        return lines.size();
    }

    // 根据索引获取 TerminalLine 对象
    @Override
    public TerminalLine get(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Negative index: " + index);
        }
        if (index >= getSize()) {
            for (int i = 0; i <= index - getSize(); i++) {
                addToBottom(TerminalLine.createEmpty());
            }
        }
        // Java 中 ArrayDeque 没有直接通过索引访问的方法，需要转换为数组
        TerminalLine[] lineArray = lines.toArray(new TerminalLine[0]);
        return lineArray[index];
    }

    // 查找指定 TerminalLine 对象的索引
    @Override
    public int indexOf(TerminalLine line) {
        int index = 0;
        for (TerminalLine currentLine : lines) {
            if (currentLine.equals(line)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    // 在顶部添加一个 TerminalLine 对象
    @Override
    public void addToTop(TerminalLine line) {
        if (isCapacityLimited && lines.size() == maxCapacity) {
            return;
        }
        lines.addFirst(line);
    }

    // 在底部添加一个 TerminalLine 对象
    @Override
    public void addToBottom(TerminalLine line) {
        lines.addLast(line);
        if (isCapacityLimited && lines.size() > maxCapacity) {
            lines.removeFirst();
        }
    }

    // 从顶部移除一个 TerminalLine 对象
    @Override
    public TerminalLine removeFromTop() {
        return lines.removeFirst();
    }

    // 从底部移除一个 TerminalLine 对象
    @Override
    public TerminalLine removeFromBottom() {
        return lines.removeLast();
    }

    // 清空存储的所有 TerminalLine 对象
    @Override
    public void clear() {
        lines.clear();
    }

    // 获取存储的 TerminalLine 对象的迭代器
    @Override
    public Iterator<TerminalLine> iterator() {
        return lines.iterator();
    }
}

