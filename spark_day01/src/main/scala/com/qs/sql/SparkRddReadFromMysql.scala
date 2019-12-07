package com.qs.sql

import java.sql.{DriverManager, ResultSet}

import org.apache.spark.rdd.JdbcRDD
import org.apache.spark.{SparkConf, SparkContext}

object SparkRddReadFromMysql {

  def main(args: Array[String]): Unit = {

    val sc:SparkContext={
      val conf: SparkConf = new SparkConf()
        .setAppName(this.getClass.getSimpleName.stripSuffix("$"))
        .setMaster("local[2]")
        .set("spark.testing.memory", "512000000")
      val context: SparkContext = SparkContext.getOrCreate(conf)
      context
    }

    val result = new JdbcRDD[(String, Int)](
      sc,
      () => {
        Class.forName("com.mysql.jdbc.Driver")
        val conn = DriverManager.getConnection("jdbc:mysql://hadoop01:3306/test", "root", "mysql")
        conn
      },
      "select word,count from wc where count<? and count>?",
      11,
      5,
      1,
      (rs: ResultSet) => {
        val word: String = rs.getString("word")
        val count: Int = rs.getInt("count")
        (word, count)
      }

    )
    result.foreach(println)



    sc.stop()

  }

}
