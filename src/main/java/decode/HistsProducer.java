package decode;

import hist.Hist;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Test;
import scala.collection.mutable.Publisher$class;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

/**
 * Created by root on 18-6-9.
 */
public class HistsProducer {

    public static void sendFrames(String path){
        Properties props=new Properties();
        props.put("bootstrap.servers","master:9092");
        props.put("metadata.broker.list","master:9092,slave01:9092");
        props.put("key.serializer","org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer","org.apache.kafka.common.serialization.StringSerializer");
//        props.put("batch.size","20971520");
//        props.put("linger.ms","5");
//        props.put("max.request.size","2097152");
        KafkaProducer<String,String> producer=new KafkaProducer<String, String>(props);
        String topic="frames72";
        new HistsThread(producer,topic,path).start();
    }

    public static void main(String[] args){
        sendFrames("/root/Videos/");
    }

    @Test
    public void testSub(){
        String s="frame00001.jpg 75080 4019 763 1007 1512 4241 260032 16720 4828 10452 5703 4496 7446 26582 2873 " +
                "2406 74507 3476 7899 9325 11870 24481 213749 67107 2973 2143 1466 1014 955 2185 1159 3851 73776 " +
                "1142 494 2115 2526 7548 10651 270235 7041 5198 5065 10495 9173 11053 4168 7480 0.6590526905829597 " +
                "960 446";
        System.out.println(s.substring(0,14));
        System.out.println(s.substring(15));

    }


}

class HistsThread extends Thread{

    String path="";
    KafkaProducer<String,String> producer=null;
    String topic="";
    File file=null;
    String[] files=null;
    //Base64 base64=null;

    public HistsThread(KafkaProducer<String,String> producer,String topic,String path){
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        this.producer=producer;
        this.path=path;
        this.topic=topic;
        //base64=new Base64();
        file=new File(path);
        files=file.list();
        Arrays.sort(files);  //sort the frames
    }

    @Override
    public void run() {
        for(String f:files){
            if(f.endsWith(".jpg")){
                BufferedImage img=null;
                try {
                    img= ImageIO.read(new File(path+f));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Hist hist=new Hist(img,f);
                String h=hist.getAllHistInfo();

                ProducerRecord<String,String> record=new ProducerRecord<String, String>(topic,"video1",f+" "+h);
                //ProducerRecord<String,String> record=new ProducerRecord<String, String>(topic,"video1",i+"");
                producer.send(record);
                System.out.println(f+" sent");
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

    }

}
