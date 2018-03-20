package decode;


import hist.Hist;

import java.io.*;

/**
 * Created by root on 17-12-27.
 */
public class ReadFile {

    public static byte[] file2Bytes(String filePath){
        byte[] buffer=null;
        try {
            File file=new File(filePath);
            FileInputStream fis=new FileInputStream(file);
            ByteArrayOutputStream bos=new ByteArrayOutputStream();
            byte[] b=new byte[1024];
            int n=0;
            while((n=fis.read(b))!=-1){
                bos.write(b,0,n);
            }
            fis.close();
            bos.close();
            buffer=bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return buffer;
    }

    public static void main(String[] args){
        byte[] res=file2Bytes("/root/Videos/frame00001.jpg");
        Hist h=new Hist(res);
        System.out.println(h.getHist().toString());
    }
}
