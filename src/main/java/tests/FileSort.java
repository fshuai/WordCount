package tests;

import java.io.File;
import java.util.Arrays;

/**
 * Created by root on 18-1-1.
 */
public class FileSort {
    public static void main(String[] args){
        File file=new File("/root/Videos");
        String[] files=file.list();
        Arrays.sort(files);
        for(String str:files){
            System.out.println(str);
        }
    }
}
