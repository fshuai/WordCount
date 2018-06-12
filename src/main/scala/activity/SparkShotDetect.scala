package activity

import hist.Hist
import org.apache.hadoop.io.{BytesWritable, Text}
import org.apache.spark.{SparkConf, SparkContext}
import scala.collection.JavaConversions._
import shot.ShotCut

import scala.collection.mutable.ListBuffer

/**
  * Created by root on 18-5-11.
  */
object SparkShotDetect {

  def getHist(bytesWritable: BytesWritable):String={
    val h=new Hist(bytesWritable.getBytes)
    h.getAllHistInfo
  }

  def frame2Int(str:String):Int={
    val in=str.substring(5,10)
    in.toInt
  }

  def main(args: Array[String]): Unit = {
    val conf=new SparkConf().setAppName("sparkshotdetect").setMaster("local[4]")
    val sc=new SparkContext(conf)

    val rdd=sc.sequenceFile("/root/input/image.seq",classOf[Text],classOf[BytesWritable])
    val histRdd=rdd.map(x=>(x._1.toString,getHist(x._2)))
    //histRdd : (frame00001.jpg, xxxxx)
    //res.saveAsTextFile("/root/output/histinfo")
    val res=histRdd.mapPartitions(it => {
      val shot=new ShotCut
      val inputlist=it.toList
      val firstFrame=inputlist(0)._1
      val begin=frame2Int(firstFrame) //the first frame in each partition
      val input=inputlist.map(_._2)
      val r1=shot.shotDetection(input)
      val firstshot=r1.get(1) //
      val r=r1.map(_ + begin) //r is still the original one
      val scores = new ListBuffer[String]
      var lastFrame = -1
      for(i <- 0 to r.length-2){
        var score:Double=0
        if(i-1<0){
          score=0  //Math.pow(Math.E,((1-r(i))/300d))
        }
        else {
          score=Math.pow(Math.E,(1-(r(i)-r(i-1)))/300d)
        }
        scores+=score.toString
      }
      lastFrame=r(r.length-2)
      var curFrame=begin
      val fres=r.zip(scores)
      val fresult=new ListBuffer[(Int,String)]
      for(i <- 0 to firstshot){
        fresult += ((curFrame,inputlist(i)._2))
        curFrame+=1
      }
      curFrame=lastFrame
      fresult++=fres
      for(i <- lastFrame to r.length-1){
        fresult += ((curFrame,inputlist(i)._2))
        curFrame += 1
      }
      fresult.iterator //List[(Int,Double)]
    }
    )
    val col=res.collect()
    res.saveAsTextFile("/root/output/oneshot59")
    sc.stop()
  }

}
