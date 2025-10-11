// package cn.oyzh.easyshell.test;
//
// import cn.oyzh.easyshell.domain.ShellKey;
// import cn.oyzh.easyshell.sync.ShellSyncManager;
// import org.junit.Test;
//
// import java.util.ArrayList;
// import java.util.List;
//
// /**
//  *
//  * @author oyzh
//  * @since 2025-10-11
//  */
// public class SyncTest {
//
//     @Test
//     public void test1(){
//
//         List<ShellKey> local = new ArrayList<>();
//         ShellKey l1 = new ShellKey();
//         l1.setId("1");
//         ShellKey l2 = new ShellKey();
//         l2.setId("2");
//         ShellKey l3 = new ShellKey();
//         l3.setId("3");
//         ShellKey l4 = new ShellKey();
//         l4.setId("4");
//
//         local.add(l1);
//         local.add(l2);
//         local.add(l3);
//         local.add(l4);
//
//         List<ShellKey> remote = new ArrayList<>();
//         ShellKey r1 = new ShellKey();
//         r1.setId("1");
//         ShellKey r2 = new ShellKey();
//         r2.setId("2");
//         ShellKey r3 = new ShellKey();
//         r3.setId("3");
//         remote.add(r1);
//         remote.add(r2);
//         remote.add(r3);
//
//         List<ShellKey> list=  ShellSyncManager.margeKeys(local, remote);
//
//
//         for (ShellKey shellKey : list) {
//             System.out.println(shellKey.getId());
//         }
//     }
// }
