package tests;

import org.apache.hadoop.io.BytesWritable;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.utils.Converters;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * Created by root on 18-1-2.
 */
public class JavaCV {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    public String getCol(BytesWritable bytesWritable){
        Mat mat=bytesWritable2Mat(bytesWritable);
        return mat.cols()+"";
    }

    private Mat bytesWritable2Mat(BytesWritable bytesWritable){
        byte[] imgFileBytes=bytesWritable.getBytes();
        Mat img=new Mat();
        Byte[] bigByteArray=new Byte[imgFileBytes.length];
        for(int i=0;i<imgFileBytes.length;i++){
            bigByteArray[i]=new Byte(imgFileBytes[i]);
        }
        List<Byte> matList= Arrays.asList(bigByteArray);
        img= Converters.vector_char_to_Mat(matList);
        img= Highgui.imdecode(img,Highgui.CV_LOAD_IMAGE_COLOR);
        return img;
    }

}
