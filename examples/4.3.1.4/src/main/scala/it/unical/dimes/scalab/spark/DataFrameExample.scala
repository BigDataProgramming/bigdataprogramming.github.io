package it.unical.dimes.scalab.spark

import org.apache.spark.SparkContext
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.sql.functions.{explode, max, mean, min}
import org.apache.spark.sql.types.{ArrayType, IntegerType, StringType, StructType}

object DataFrameExample {
  def main(args: Array[String]): Unit = {

    val fileEmpl = args(0)
    val fileProj = args(1)
    val spark = SparkSession
      .builder.master("spark://master:7077")
      .appName("DataFrameExample")
      .getOrCreate()

    val sc: SparkContext = spark.sparkContext
    import spark.implicits._

    val schemaEmpl = new StructType()
      .add("id", IntegerType)
      .add("name", StringType)
      .add("surname", StringType)
      .add("age", IntegerType)
      .add("department", StringType)
      .add("salary", IntegerType)
      .add("skills", ArrayType(StringType))
    val dfEmpl = spark.read.schema(schemaEmpl).json(fileEmpl)
    dfEmpl.printSchema()
    dfEmpl.show()

    val schemaProj = new StructType()
      .add("id", IntegerType)
      .add("name", StringType)
      .add("description", StringType)
      .add("budget", IntegerType)
      .add("skills", ArrayType(StringType))
      .add("employees", ArrayType(IntegerType))
    val dfProj = spark.read.schema(schemaProj).json(fileProj)
    dfProj.printSchema()
    dfProj.show()

    dfEmpl.groupBy("department").count().show()

    dfEmpl.agg(min("age"), max("age"), mean("age")).show()

    dfEmpl.filter("age >= 30 AND age <40").agg(mean("salary")).show()
    dfEmpl.filter("age >= 40 AND age <50").agg(mean("salary")).show()

    dfEmpl.createOrReplaceTempView("itcompany")
    spark.sql("SELECT * FROM itcompany").show()
    spark.sql("SELECT department, COUNT(*) FROM itcompany GROUP BY department").show()

    //Using Join expression
    val dfProjExpl = dfProj.filter(dfProj("id") === "0").withColumn("employee", explode($"employees"))
    dfProjExpl.show()
    dfEmpl.join(dfProjExpl, dfEmpl("id") === dfProjExpl("employee")).show()
    dfEmpl.join(dfProjExpl, dfEmpl("id") === dfProjExpl("employee")).groupBy("department").count().show()
  }
}

