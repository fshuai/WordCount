package decode;

import hist.Hist;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.util.ReflectionUtils;

import java.io.IOException;
import java.net.URI;


/**
 * Created by root on 18-1-1.
 */
public class ReadSeq {

    public static void main(String[] args) throws IOException {
        String uri="hdfs://master:9000/user/fshuai/img.seq";
        Configuration conf=new Configuration();
        FileSystem fs=FileSystem.get(URI.create(uri), conf);
        Path path=new Path(uri);

        SequenceFile.Reader reader=null;
        reader=new SequenceFile.Reader(fs, path,conf);
        Writable key=(Writable) ReflectionUtils.newInstance(reader.getKeyClass(), conf);
        BytesWritable value=(BytesWritable)ReflectionUtils.newInstance(reader.getValueClass(), conf);
        long position=reader.getPosition();
        while(reader.next(key,value)){
            System.out.println("key:"+key);
            String name=key.toString();
            byte[] imageFileBytes=value.getBytes();
            Hist hist=new Hist(imageFileBytes);
            System.out.println("hist:"+hist.getHist());
        }
        IOUtils.closeStream(reader);
    }
}
