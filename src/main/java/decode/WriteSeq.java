package decode;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

import java.io.*;
import java.util.Arrays;

/**
 * Created by root on 18-1-1.
 */
public class WriteSeq {

    public static void main(String[] args) throws IOException {
        String input="/root/Videos/";
        File inputDir=new File(input);
        if(!inputDir.isDirectory()){
            throw new RuntimeException("invalid input dir");
        }
        String[] files=inputDir.list();
        Arrays.sort(files);
        String uri="hdfs://master:9000/user/fshuai/img.seq";
        //String uri="/root/input/image.seq";
        Configuration conf=new Configuration();
        FileInputStream fis=null;
        Path path=new Path(uri);
        Text key;
        BytesWritable value=new BytesWritable();
        SequenceFile.Writer writer=SequenceFile.createWriter(conf,SequenceFile.Writer.file(path),
                SequenceFile.Writer.keyClass(Text.class),SequenceFile.Writer.valueClass(BytesWritable.class));
        for(String file:files){
            if(!file.endsWith(".jpg")){
                continue;
            }
            byte[] buffer=null;
            try {
                File f=new File(input+file);
                fis=new FileInputStream(f);
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
            //System.out.println(buffer.toString());
            writer.append(new Text(file), new BytesWritable(buffer));
        }

        IOUtils.closeStream(fis);
        IOUtils.closeStream(writer);
    }
}
