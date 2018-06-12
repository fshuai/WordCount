package activity

import javautils.{FrameActivity, OpticalFlowSeq}

import hist.Hist
import org.apache.hadoop.io.{BytesWritable, Text}
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ArrayBuffer
import scalautils.LoggerLevels

/**
  * Created by root on 18-1-1.
  */
object SparkMotionActivity {

  //hist is used in shotdetection
  def getHist(bytesWritable: BytesWritable):String={
    val h=new Hist(bytesWritable.getBytes)
    h.getHist.toString
  }

  def main(args: Array[String]): Unit = {
    LoggerLevels.setStreamingLogLevels()
    val conf=new SparkConf().setAppName("SparkMotionActivity").setMaster("local[4]")
    //setMaster("spark://master:7077").setJars(List("/root/work/intellij/WordCount/target/WordCount-1.0-SNAPSHOT.jar"))
    val sc=new SparkContext(conf)
    //val rdd=sc.sequenceFile("hdfs://master:9000/user/fshuai/img.seq",classOf[Text],classOf[BytesWritable])
    val rdd=sc.sequenceFile("/root/input/image.seq",classOf[Text],classOf[BytesWritable])
      //.map{case (x,y) => (x.toString,getHist(y).toString)}
    val rdd1=rdd.mapPartitions(it => {
      val optical=new OpticalFlowSeq
      var input=new ArrayBuffer[(String,BytesWritable)]()
      var result=new ArrayBuffer[(String,Double)]()

      while(it.hasNext){
        val current=it.next()
        val imageFileBytes=current._2.getBytes
        val tmp:Array[Byte]=new Array[Byte](imageFileBytes.length)
        for(j <- 0 to imageFileBytes.length-1){
          tmp(j)=imageFileBytes(j)
        }
        val re=new BytesWritable(tmp)
        input.append((current._1.toString,re))
      }
      for(i<- 0 until input.length-1){
        val res=optical.opticalFlow(FrameActivity.bytesWritable2Mat(input(i)._2),
          FrameActivity.bytesWritable2Mat(input(i+1)._2))
        result.append((input(i)._1,res))
      }
      result.iterator
    })
    //rdd1.saveAsTextFile("hdfs://master:9000/user/fshuai/out20")
    rdd1.saveAsTextFile("/root/output/activity1")
    sc.stop()
  }

}
