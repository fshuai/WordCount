package streaming

import kafka.serializer.StringDecoder
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.collection.immutable.HashSet
import scalautils.LoggerLevels

/**
  * Created by root on 18-1-12.
  * calculate each frame's hist from kafka
  * using direct
  */
object StreamingHist {

  def main(args: Array[String]): Unit = {
    LoggerLevels.setStreamingLogLevels()

    val brokers="master:9092,slave01:9092"
    val topics="frames"
    val groupId="testshot"

    val sparkConf=new SparkConf().setAppName("streaminghist").setMaster("local[4]")
    sparkConf.set("spark.streaming.kafka.maxRatePerPartition","5")
    sparkConf.set("spark.serializer","org.apache.spark.serializer.KryoSerializer")

    val ssc=new StreamingContext(sparkConf,Seconds(2))
    //create direct kafka stream
    val topicSet=HashSet(topics)
    val kafkaParams=Map[String,String]("metadata.broker.list" -> brokers)
    val messages=KafkaUtils.createDirectStream[String,String,StringDecoder,StringDecoder](ssc,kafkaParams,topicSet)

    val sc=new SparkContext(sparkConf)
  }

}

case class FrameInfo(rows:Int,cols:Int,frameType:Int,histInfo:String)