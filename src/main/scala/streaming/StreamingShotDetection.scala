package streaming

import hist.Hist
import kafka.serializer.StringDecoder
import org.apache.commons.codec.binary.Base64
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka.KafkaUtils

import scala.collection.immutable.HashSet
import scalautils.LoggerLevels

/**
  * Created by root on 18-3-28.
  */
object StreamingShotDetection {

  def getHist(bytesWritable: Array[Byte]):String={
    val h=new Hist(bytesWritable)
    h.getAllHistInfo
  }


  def main(args: Array[String]): Unit = {
    LoggerLevels.setStreamingLogLevels()
    //val Array(brokers,topics,groupId)=args
    val brokers="master:9092,slave01:9092"
    val topics="streaming125"
    val groupId="test1211"

    //create context with 2 seconds batch interval
    val sparkconf=new SparkConf().setAppName("KafkaWordCount")
    sparkconf.setMaster("spark://master:7077")
    sparkconf.set("spark.streaming.kafka.maxRatePerPartition","5")
    sparkconf.set("spark.serializer","org.apache.spark.serializer.KryoSerializer")

    val ssc=new StreamingContext(sparkconf,Seconds(2))
    //set checkpoint
    ssc.checkpoint("")

    //create direct kafka stream with brokers and topics
    val topicSet=HashSet("streaming125")
    val kafkaParams=Map[String,String]("metadata.broker.list"->brokers)
    val messages=KafkaUtils.createDirectStream[String,String,StringDecoder,StringDecoder](ssc,kafkaParams,topicSet)
    //get each frame hist
    val histedDstream=messages.map(x=>(x._1,getHist(Base64.decodeBase64(x._2))))
  }

}
