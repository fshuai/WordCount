package tests;

import hist.Hist;
import javautils.OpticalFlowSeq;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.util.ReflectionUtils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.utils.Converters;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by root on 18-1-2.
 */
public class RedSeqCols {

    public static void main(String[] args) throws IOException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        String uri="/root/Seqs/img.seq";
        Configuration conf=new Configuration();
        FileSystem fs=FileSystem.get(URI.create(uri), conf);
        Path path=new Path(uri);
        OpticalFlowSeq opticalFlowSeq=new OpticalFlowSeq();

        SequenceFile.Reader reader=null;
        reader=new SequenceFile.Reader(fs, path,conf);
        Text key=(Text) ReflectionUtils.newInstance(reader.getKeyClass(), conf);
        BytesWritable value=(BytesWritable)ReflectionUtils.newInstance(reader.getValueClass(), conf);
        long position=reader.getPosition();
        ArrayList<BytesWritable> values=new ArrayList<BytesWritable>();
        ArrayList<String> keys=new ArrayList<String>();
        while(reader.next(key,value)){
            //System.out.println("key:"+key);
            String name=key.toString();
            byte[] imageFileBytes=value.getBytes();
            byte[] tmp=new byte[imageFileBytes.length];
            for(int i=0;i<imageFileBytes.length;i++){
                tmp[i]=imageFileBytes[i];
            }
            BytesWritable res=new BytesWritable(tmp);
            //System.out.println(tmp);
            keys.add(name);
            values.add(res);
        }
        for(int i=0;i<values.size()-1;i++){
            double result=opticalFlowSeq.opticalFlow(bytesWritable2Mat(values.get(i)),bytesWritable2Mat(values.get(i+1)));
            System.out.println(i+":"+result);
        }
//        for(int i=0;i<keys.size();i++){
//            System.out.println(i+":"+keys.get(i));
//        }
//        System.out.println(values.get(0).getLength());
//        Highgui.imwrite("testresult1.jpg",bytesWritable2Mat(values.get(0)));
//        Highgui.imwrite("testresult2.jpg",bytesWritable2Mat(values.get(1)));
        System.out.println(values.size());
        IOUtils.closeStream(reader);
    }

    private static Mat bytesWritable2Mat(BytesWritable bytesWritable){
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
