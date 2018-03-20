package imageserver;

import metadata.ImageKey;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * Created by root on 17-12-12.
 */
public class ImageProducer {

    public static void main(String[] args){
        Properties props=new Properties();
        props.put("bootstrap.servers","master:9092");
        props.put("metadata.broker.list","master:9092,slave01:9092");
        props.put("key.serializer","utils.ObjectEncoder");
        props.put("value.serializer","org.apache.kafka.common.serialization.StringSerializer");
        KafkaProducer<ImageKey,String> producer=new KafkaProducer<ImageKey, String>(props);
        String topic="streaming1221";
        new SendThread(producer,topic).start();
    }

}

class SendThread extends Thread{

    private KafkaProducer<ImageKey,String> producer;
    private String topic;

    public SendThread(KafkaProducer<ImageKey,String> producer,String topic){
        this.producer=producer;
        this.topic=topic;
    }

    public void run(){
        int messageNo=0;
        while (messageNo<1000){
            String messageStr="messageStr_"+messageNo;
            ImageKey key=new ImageKey(messageNo+"",messageStr);
            ProducerRecord<ImageKey,String> record=new ProducerRecord<ImageKey, String>(topic,key,messageStr);
            producer.send(record);
            try{
                Thread.sleep(500);
            } catch (InterruptedException e){
                e.printStackTrace();
            }
            messageNo++;
        }

    }
}
