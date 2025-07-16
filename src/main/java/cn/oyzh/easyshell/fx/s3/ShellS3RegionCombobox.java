// package cn.oyzh.easyshell.fx.s3;
//
// import cn.oyzh.easyshell.s3.ShellS3Util;
// import cn.oyzh.fx.plus.controls.combo.FXComboBox;
// import software.amazon.awssdk.regions.Region;
//
// /**
//  * @author oyzh
//  * @since 2025-06-16
//  */
// public class ShellS3RegionCombobox extends FXComboBox<Region> {
//
//     {
//         this.setItem(Region.regions());
//         this.select(Region.US_EAST_1);
//     }
//
//     public String getRegion() {
//         return this.getSelectedItem().id();
//     }
//
//     public void selectRegion(String region) {
//         this.select(ShellS3Util.ofRegion(region));
//     }
// }
