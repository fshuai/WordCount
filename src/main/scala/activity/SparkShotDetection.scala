package activity

import org.apache.spark.{SparkConf, SparkContext}
import shot.ShotCut
import scala.collection.JavaConversions._

/**
  * Created by root on 18-3-8.
  */
object SparkShotDetection {

  def main(args: Array[String]): Unit = {
    val conf=new SparkConf().setAppName("sparkshotdetection").setMaster("local[4]")
    val sc=new SparkContext(conf)
    //directory of input json

    val rdd=sc.textFile("/root/output/histinfo/").map(line=>{
      val fields=line.split(",")
      val key=fields(0).substring(1)
      val info=fields(1)
      val length=info.length
      (key,info.substring(0,length-1))
    }).mapPartitions(it=>{
      val shot=new ShotCut
      val input=it.toList.map(_._2)
      val res=shot.shotDetection(input)
      res.iterator}
      )

    rdd.saveAsTextFile("/root/output/tmphistinfo")
  }

}
