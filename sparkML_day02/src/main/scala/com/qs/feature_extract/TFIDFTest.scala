package com.qs.feature_extract

import org.apache.spark.SparkConf
import org.apache.spark.ml.feature.{HashingTF, IDF, IDFModel, Tokenizer}
import org.apache.spark.sql.{DataFrame, SparkSession}

object TFIDFTest {

  def main(args: Array[String]): Unit = {

    val conf: SparkConf = new SparkConf()
      .setMaster(this.getClass.getSimpleName.stripSuffix("$"))
      .setMaster("local[2]")
      .set("spark.testing.memory", "512000000")
    val spark: SparkSession = SparkSession
      .builder()
      .config(conf)
      .getOrCreate()
    spark.sparkContext.setLogLevel("WARN")

    //准备数据
    val data: DataFrame = spark.createDataFrame(Seq(
      (0, "Hi I heard about Spark"),
      (0, "I wish Java could use case classes"),
      (1, "Logistic regression models are neat"))
    ).toDF("label", "words")
    //解析数据
    data.printSchema()
    //对文章进行分词
    val tokenizer: Tokenizer = new Tokenizer().setInputCol("words").setOutputCol("token_words")
    val rs: DataFrame = tokenizer.transform(data)
    rs.printSchema()
    rs.show(false)

    //    * 5-使用TF构建词频的模型
    //    (262144 id,[24417第1个桶id,
    //    49304第2个桶id,73197第3个桶id,
    //    91137第4个桶id,234657第5个桶id],[1.0,1.0,1.0,1.0,1.0])
    val hashTF: HashingTF = new HashingTF()
      .setInputCol("token_words").setOutputCol("hashingTokenWords")
    val hashRs: DataFrame = hashTF.transform(rs)
    hashRs.printSchema()
    hashRs.show(false)

    //    * 6-使用IDF构建逆文档频率模型
    val idf: IDF = new IDF().setInputCol("hashingTokenWords").setOutputCol("IDFWords")
    val model: IDFModel = idf.fit(hashRs)
    val result: DataFrame = model.transform(hashRs)
    result.show(false)

  }

}
