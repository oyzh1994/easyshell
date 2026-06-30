package cn.oyzh.easyshell.data;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.CollectionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author oyzh
 * @since 2026-06-30
 */
public interface ShellBatchInsertable<D> {

    /**
     * 获取批量限制
     *
     * @return 结果
     */
    int getBatchLimit();

    /**
     * 获取插入限制
     *
     * @return 结果
     */
    int getInsertLimit();

    /**
     * 插入数据列表
     *
     * @return 结果
     */
    List<D> getInsertList();

    /**
     * 添加插入数据
     *
     * @param insert 插入数据
     */
    default void addInsert(D insert) throws Exception {
        if (insert != null) {
            if (this.getInsertList() == null) {
                throw new NullPointerException("getInsertList");
            }
            this.getInsertList().add(insert);
            if (this.getInsertList().size() >= this.getInsertLimit()) {
                this.doBatchInsert();
            }
        }
    }

    /**
     * 添加插入数据
     *
     * @param inserts 插入数据列表
     */
    default void addInsert(List<D> inserts) throws Exception {
        if (inserts != null) {
            for (D insert : inserts) {
                this.addInsert(insert);
            }
        }
    }

    /**
     * 执行批量插入
     */
    default void doBatchInsert() throws Exception {
        if (CollectionUtil.isNotEmpty(this.getInsertList())) {
            try {
                if (this.getInsertList().size() <= this.getBatchLimit()) {
                    this.doBatchInsert(this.getInsertList(), false);
                } else {
                    AtomicReference<Exception> exceptionRef = new AtomicReference<>();
                    List<List<D>> lists = CollectionUtil.split(this.getInsertList(), this.getBatchLimit());
                    List<Runnable> tasks = new ArrayList<>();
                    for (List<D> list : lists) {
                        tasks.add(() -> {
                            try {
                                this.doBatchInsert(list, true);
                            } catch (Exception ex) {
                                exceptionRef.set(ex);
                            }
                        });
                    }
                    ThreadUtil.submit(tasks);
                    if (exceptionRef.get() != null) {
                        throw exceptionRef.get();
                    }
                }
            } finally {
                this.getInsertList().clear();
            }
        }
    }

    /**
     * 执行批量插入
     *
     * @param list     数据列表
     * @param parallel 是否并行
     * @throws Exception 异常
     */
    void doBatchInsert(List<D> list, boolean parallel) throws Exception;
}
