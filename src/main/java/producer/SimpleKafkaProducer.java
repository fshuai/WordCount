package producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 * Created by root on 17-12-5.
 */
public class SimpleKafkaProducer {

    public static void main(String[] args){
        String topic="streaming125";
        Properties props=new Properties();
        props.put("bootstrap.servers","master:9092");
        props.put("client.id","SimpleKafkaProducer");
        props.put("key.serializer","org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer","org.apache.kafka.common.serialization.StringSerializer");
        KafkaProducer<String,String> producer=new KafkaProducer<String, String>(props);
        new ProducerThread(1000,producer,topic).start();
    }

}

class ProducerThread extends Thread{

    int num;
    int messageNo;
    String topic;
    KafkaProducer<String,String> producer=null;
    Random r=null;
    public ProducerThread(int messageNum,KafkaProducer<String,String> producer,String topic){
        num=messageNum;
        messageNo=0;
        this.producer=producer;
        r=new Random();
        this.topic=topic;
    }
    @Override
    public void run() {
        while(messageNo<num){
            String messageStr=r.nextInt(100)+"";
            try {
                producer.send(new ProducerRecord<String, String>(topic,messageNo+"",messageStr)).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            messageNo++;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
