package streaming

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scalautils.LoggerLevels

/**
  * Created by root on 18-3-14.
  */
object StreamingHdfs {

  def main(args: Array[String]): Unit = {
    LoggerLevels.setStreamingLogLevels()
    val conf=new SparkConf().setAppName("streaminghdfs").setMaster("local[4]")
    val ssc=new StreamingContext(conf,Seconds(2))
    val lines=ssc.textFileStream("")
    //shotdetection and return the acitivity level
    //()
    val lists=lines.map(x=>{
      val id=x.split(",")(0)
      val info=x.split(",")(1)
      (id,info)
    })
    lists.mapPartitions(it=>{
      val input=it.toList
      val inputInfo=input.map(x=>x._2)

      it.toIterator
    })
    ssc.awaitTermination()
  }

}
