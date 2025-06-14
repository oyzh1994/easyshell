package cn.oyzh.easyshell.s3;

public class ShellS3Util {



    public static String[] parse(String path){
        int index = path.indexOf("/");
        String bucket=path.substring(0,index);
        String filePath=path.substring(index);
        return new String[]{bucket,filePath};
    }
}
