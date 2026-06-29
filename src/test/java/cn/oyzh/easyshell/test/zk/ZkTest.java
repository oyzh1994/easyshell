package cn.oyzh.easyshell.test.zk;

import cn.oyzh.easyshell.trees.zk.other.ShellZKMoreTreeItem;
import cn.oyzh.easyshell.trees.zk.node.ShellZKNodeTreeItem;
import cn.oyzh.easyshell.trees.zk.other.ShellZKReturnTreeItem;
import cn.oyzh.easyshell.zk.ShellZKNode;
import cn.oyzh.fx.gui.tree.view.RichTreeItem;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author oyzh
 * @since 2026-05-25
 */
public class ZkTest {

    private class Obj implements Comparable<Obj> {
        private String name;

        public Obj(String name) {
            this.name = name;
        }

        @Override
        public int compareTo(Obj o) {
            // 1. a 永远最小（排最前）
            if (o instanceof ObjA) {
                return 1;
            }

            // 2. b 永远最大（排最后）
            if (o instanceof ObjB) {
                return -1;
            }
            return name.compareTo(o.name);
        }

        @Override
        public String toString() {
            return "Obj{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }

    private class ObjA extends Obj {

        public ObjA(String name) {
            super(name);
        }

        @Override
        public int compareTo(Obj o) {
            return -1;
        }
    }

    private class ObjB extends Obj {

        public ObjB(String name) {
            super(name);
        }

        @Override
        public int compareTo(Obj o) {
            return 1;
        }

    }

    private class ObjC extends Obj {

        public ObjC(String name) {
            super(name);
        }

    }

    @Test
    public void test() {
        ObjA a = new ObjA("first");
        ObjB b = new ObjB("last");
        ObjC c1 = new ObjC("obj-c1");
        ObjC c2 = new ObjC("obj-c2");
        ObjC c3 = new ObjC("obj-c3");

        List<Obj> list = new ArrayList<>();

        list.add(c1);
        list.add(c3);
        list.add(a);
        list.add(b);
        list.add(c2);
        Collections.shuffle(list);
        list.sort(Obj::compareTo);
        System.out.println(list.getFirst());
        System.out.println(list.getLast());

        list.sort(Comparator.reverseOrder());
        //                list.sort(Comparator.reverseOrder());
        //        list = list.reversed();
        System.out.println(list.getFirst());
        System.out.println(list.getLast());
        //
        //        Collections.shuffle(list);
        //        list.sort(Obj::compareTo);
        //        System.out.println(list.getFirst());
        //        System.out.println(list.getLast());


    }

    @Test
    public void test1() {
        ShellZKReturnTreeItem a = new ShellZKReturnTreeItem(null);
        ShellZKMoreTreeItem b = new ShellZKMoreTreeItem(null);
        ShellZKNode node1 = new ShellZKNode();
        node1.nodePath("/a1");
        ShellZKNodeTreeItem c1 = new ShellZKNodeTreeItem(node1, null);
        ShellZKNode node2 = new ShellZKNode();
        node2.nodePath("/a2");
        ShellZKNodeTreeItem c2 = new ShellZKNodeTreeItem(node2, null);
        ShellZKNode node3 = new ShellZKNode();
        node3.nodePath("/a3");
        ShellZKNodeTreeItem c3 = new ShellZKNodeTreeItem(node3, null);

        List<RichTreeItem<?>> list = new ArrayList<>();

        list.add(c1);
        list.add(c3);
        list.add(a);
        list.add(b);
        list.add(c2);
        Collections.shuffle(list);
        list.sort(RichTreeItem::compareTo);
        System.out.println(list.getFirst());
        System.out.println(list.getLast());

        Collections.shuffle(list);
        list.sort(Comparator.reverseOrder());
        System.out.println(list.getFirst());
        System.out.println(list.getLast());

        Collections.shuffle(list);
        list.sort(RichTreeItem::compareTo);
        System.out.println(list.getFirst());
        System.out.println(list.getLast());

    }
}
