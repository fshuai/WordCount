package decode;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hist.Hist;
import org.apache.commons.codec.binary.Base64;
import org.apache.hadoop.conf.Configuration;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Test;

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
        props.put("value.serializer","org.apache.kafka.common.serialization.StringSerializer");
        KafkaProducer<String,String> producer=new KafkaProducer<String, String>(props);
        String topic="frame44";
        new SendKafkaThread(producer,topic,path).start();
    }

    public static void main(String[] args){
        //getFrames("/root/Videos/test.mp4","/root/Videos/");
        sendFrames("/root/Videos/");
    }

    @Test
    public void testJson(){
        String path="/root/Videos/frame00001.jpg";
        JsonObject obj=new JsonObject();
        Gson gson=new Gson();
        obj.addProperty("frameId","frame00001.jpg");
        byte[] data=ReadFile.file2Bytes(path);
        Hist hist=new Hist(data);
        String info=hist.getAllHistInfo();
        obj.addProperty("data",info);
        System.out.println(info);
        String json=gson.toJson(obj);
        System.out.println(json);

        JsonParser parser=new JsonParser();
        JsonObject obj1=parser.parse(json).getAsJsonObject();
        System.out.println(obj1.get("frameId"));
    }

}

class SendKafkaThread extends Thread{

    String path="";
    KafkaProducer<String,String> producer=null;
    String topic="";
    File file=null;
    String[] files=null;
    Gson gson=null;
    Hist hist=null;

    public SendKafkaThread(KafkaProducer<String,String> producer,String topic,String path){
        this.producer=producer;
        this.path=path;
        this.topic=topic;
        file=new File(path);
        files=file.list();
        Arrays.sort(files);  //sort the frames
        gson=new Gson();
    }

    //modified in 2018.4.2
    //key:videoId,value:frameBytes
    @Override
    public void run(){
        for(String f:files){
            if(f.endsWith(".jpg")){
                JsonObject obj=new JsonObject();
                obj.addProperty("frameId",f);
                byte[] data=ReadFile.file2Bytes(path+f);
                hist=new Hist(data);
                String info=hist.getAllHistInfo();
                obj.addProperty("data",info);
                System.out.println(info);
                String json=gson.toJson(obj);
                ProducerRecord<String,String> record=new ProducerRecord<String, String>
                        (topic,"videoId", json);
                producer.send(record);
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(f+" sent");
            }
        }
    }

}
