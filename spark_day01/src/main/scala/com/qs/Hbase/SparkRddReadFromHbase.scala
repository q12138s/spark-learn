package com.qs.Hbase

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.{Cell, CellUtil, HBaseConfiguration}
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object SparkRddReadFromHbase {

  def main(args: Array[String]): Unit = {

    val sc:SparkContext={
      val conf: SparkConf = new SparkConf()
        .setAppName(this.getClass.getSimpleName.stripSuffix("$"))
        .setMaster("local[2]")
        .set("spark.testing.memory", "512000000")
      val context: SparkContext = SparkContext.getOrCreate(conf)
      context
    }

    val conf: Configuration = HBaseConfiguration.create()
    conf.set("hbase.zookeeper.quorum","hadoop01:2181,hadoop02:2181,hadoop03:2181")
    //配置读取的表
    conf.set(TableInputFormat.INPUT_TABLE,"hbase_score")
    val inputRdd: RDD[(ImmutableBytesWritable, Result)] = sc.newAPIHadoopRDD(
      conf,
      classOf[TableInputFormat],
      classOf[ImmutableBytesWritable],
      classOf[Result]
    )
    val temp= inputRdd
      .map(tuple => tuple._2)
//        .take(1)            //会报错
    temp.foreach(
      rs => {
        val rawCells: Array[Cell] = rs.rawCells()
        rawCells.foreach(cell => {
           println( Bytes.toString(CellUtil.cloneFamily(cell))+"\t"+          //列族
              Bytes.toString(CellUtil.cloneQualifier(cell))+"\t"+     //列
              Bytes.toString(CellUtil.cloneValue(cell))+"\t"+
              cell.getTimestamp()
           )
        })
      }
    )


    sc.stop()

  }

}
