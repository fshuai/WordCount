package decode;

import org.apache.hadoop.conf.Configuration;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

/**
 * Created by root on 17-12-25.
 */
public class FrameExtract {

    public static void getFrames(String srcPath,String destPath){
        Runtime rt=Runtime.getRuntime();
        String command="ffmpeg -loglevel quiet -i "+srcPath+" "+destPath+"frame%05d.jpg";
        Process p=null;
        try {
            System.out.println(command);
            p=rt.exec(command);
            p.waitFor();
            System.out.println("decode ended,do sth else");
            //send the images

        } catch (Exception e) {
            e.printStackTrace();
            try {
                p.getErrorStream().close();
                p.getInputStream().close();
                p.getOutputStream().close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public static void sendFrames(String path){
        Properties props=new Properties();
        props.put("bootstrap.servers","master:9092");
        props.put("metadata.broker.list","master:9092,slave01:9092");
        props.put("key.serializer","org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer","org.apache.kafka.common.serialization.ByteArraySerializer");
        KafkaProducer<String,byte[]> producer=new KafkaProducer<String, byte[]>(props);
        String topic="frames1228";
        new SendKafkaThread(producer,topic,path).start();
    }

    public static void main(String[] args){
        //getFrames("/root/Videos/test.mp4","/root/Videos/");
        sendFrames("/root/Videos/");
    }

}

class SendKafkaThread extends Thread{

    String path="";
    KafkaProducer<String,byte[]> producer=null;
    String topic="";
    File file=null;
    String[] files=null;

    public SendKafkaThread(KafkaProducer<String,byte[]> producer,String topic,String path){
        this.producer=producer;
        this.path=path;
        this.topic=topic;
        file=new File(path);
        files=file.list();
        Arrays.sort(files);  //sort the frames
    }
    @Override
    public void run() {

        for(String f:files){
            if(f.endsWith(".jpg")){
                byte[] value=ReadFile.file2Bytes(path+f);
                ProducerRecord<String,byte[]> record=new ProducerRecord<String, byte[]>(topic,f,value);
                producer.send(record);
                System.out.println(f+":sent finished");
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

        }

    }
}
