package activity

import hist.Hist
import org.apache.hadoop.io.{BytesWritable, Text}
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.JavaConversions._
import shot.ShotCut

import scala.collection.mutable.ListBuffer

/**
  * Created by root on 18-3-28.
  * shot detection in one class
  */
object SparkOneShot {

  def getHist(bytesWritable: BytesWritable):String={
    val h=new Hist(bytesWritable.getBytes)
    h.getAllHistInfo
  }

  def frame2Int(str:String):Int={
    val in=str.substring(5,10)
    in.toInt
  }

  def main(args: Array[String]): Unit = {
    val conf=new SparkConf().setAppName("sparkoneshot").setMaster("local[4]")
    val sc=new SparkContext(conf)

    val rdd=sc.sequenceFile("/root/input/image.seq",classOf[Text],classOf[BytesWritable])
    val histRdd=rdd.map(x=>(x._1.toString,getHist(x._2).toString))
    //histRdd : (frame00001.jpg, xxxxx)
    //res.saveAsTextFile("/root/output/histinfo")
    val res=histRdd.mapPartitions(it => {
      val shot=new ShotCut
      val inputlist=it.toList
      val firstFrame=inputlist(0)._1
      val begin=frame2Int(firstFrame) //the first frame in each partition
      val input=inputlist.map(_._2)
      val r1=shot.shotDetection(input)
      val r=r1.map(_ + begin) //r is still the original one
      val scores = new ListBuffer[Double]
      for(i <- r.indices){
        var score:Double=0
        if(i-1<0){
          score=0  //Math.pow(Math.E,((1-r(i))/300d))
        }
        else {
          score=Math.pow(Math.E,(1-(r(i)-r(i-1)))/300d)
        }
        scores+=score
      }
      val fres=r.zip(scores)
      fres.iterator}
    )
    res.saveAsTextFile("/root/output/oneshot58")
    sc.stop()
  }

}
