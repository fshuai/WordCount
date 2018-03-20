package javautils;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.utils.Converters;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by root on 18-1-8.
 */
public class FrameActivity {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private static OpticalFlowSeq opticalFlowSeq=new OpticalFlowSeq();

    public static List<Tuple2<String,Double>> getFrameActivity(List<Tuple2<Text,BytesWritable>> list){

        List<Tuple2<String,Double>> res=new ArrayList<>();
        for(int i=0;i<list.size()-1;i++){
            double result=opticalFlowSeq.opticalFlow(bytesWritable2Mat(list.get(i)._2),bytesWritable2Mat(list.get(i+1)._2));
            //System.out.println(i+":"+result);
            res.add(new Tuple2(i+"",result));
        }
        return res;
    }

    public static Mat bytesWritable2Mat(BytesWritable bytesWritable){
        byte[] imageFileBytes=bytesWritable.getBytes();
        Mat img=new Mat();
        Byte[] bigByteArray=new Byte[imageFileBytes.length];
        for(int i=0;i<imageFileBytes.length;i++){
            bigByteArray[i]=new Byte(imageFileBytes[i]);
        }
        List<Byte> mat_list= Arrays.asList(bigByteArray);
        img= Converters.vector_char_to_Mat(mat_list);
        img= Highgui.imdecode(img,Highgui.CV_LOAD_IMAGE_GRAYSCALE);
        return img;
    }
}
