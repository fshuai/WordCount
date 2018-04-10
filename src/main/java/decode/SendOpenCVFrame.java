package decode;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

/**
 * Created by root on 18-1-17.
 */
public class SendOpenCVFrame {
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
//        props.put("batch.size","20971520");
//        props.put("linger.ms","5");
//        props.put("max.request.size","2097152");
        KafkaProducer<String,String> producer=new KafkaProducer<String, String>(props);
        String topic="frames";
        new SendOpencvKafkaThread(producer,topic,path).start();
    }

    public static void main(String[] args){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        sendFrames("/root/Videos/");
    }
}

class SendOpencvKafkaThread extends Thread{

    String path="";
    KafkaProducer<String,String> producer=null;
    String topic="";
    File file=null;
    String[] files=null;
    //Base64 base64=null;
    Mat input_mat=new Mat();

    public SendOpencvKafkaThread(KafkaProducer<String,String> producer,String topic,String path){
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
        Gson gson=new Gson();
        for(String f:files){
            if(f.endsWith(".jpg")){
                input_mat= Highgui.imread(path+f);
                int cols=input_mat.cols();
                int rows=input_mat.rows();
                int type=input_mat.type();
                byte[] data=new byte[(int)input_mat.total()*input_mat.channels()];
                input_mat.get(0,0,data);
                JsonObject obj=new JsonObject();
                obj.addProperty("rows",rows);
                obj.addProperty("cols",cols);
                obj.addProperty("type",type);
                //obj.addProperty("data", Base64.getEncoder().encodeToString(data));
                obj.addProperty("data",Base64.encodeBase64String(data));
                //System.out.println(Base64.encodeBase64String(data).getBytes().length);
                String json=gson.toJson(obj);
                ProducerRecord<String,String> record=new ProducerRecord<String, String>(topic,f,json);
                producer.send(record);
                //System.out.println(f+"rows:"+type);
                //System.out.println(input_mat.rows());
                System.out.println(f+":sent finished");
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

    }

}
