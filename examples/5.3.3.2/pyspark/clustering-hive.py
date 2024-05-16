import findspark
findspark.init('/opt/spark')
from pyspark.sql import SparkSession

sparkMaster = "spark://master:7077"

spark = SparkSession.builder \
    .master(sparkMaster) \
    .appName("Clustering with Hive") \
    .enableHiveSupport() \
    .getOrCreate()

spark.sql("ADD JAR hdfs://master/user/custom/5.3.3/ion-hive3-serde-all-1.2.0.jar")
spark.sql("DROP TABLE IF EXISTS posts")
flickrData=spark.read.json("hdfs://master/user/custom/5.3.3/data/allFlickrRome.json")

# Create Hive Internal table
flickrData.write.mode("overwrite").saveAsTable("posts")
spark.sql("DROP FUNCTION IF EXISTS GeoData")
spark.sql("DROP FUNCTION IF EXISTS GeoDbscan")
spark.sql("ADD JAR hdfs://master/user/custom/5.3.3/HiveUDF-1.0-SNAPSHOT-jar-with-dependencies.jar")
spark.sql("CREATE FUNCTION GeoData AS 'it.unical.dimes.scalab.hive.udf.GeoDataUDF'")
spark.sql("CREATE FUNCTION GeoDbscan AS 'it.unical.dimes.scalab.hive.udf.DbScanUDAF'")

keywords = "colosseo,colis,collis,collos,Amphiteatrum Flavium,Amphitheatrum Flavium,An Colasaem,Coliseo,Coliseo,Coliseo de Roma,Coliseo de Roma,Coliseu de Roma,Coliseum,Coliseum,Coliseum,Coliseus,Colloseum,Coloseu,Colosseo,Colosseo,Colosseo,Colosseu,Colosseum,Colosseum,Colosseum,Colosseum,Colosseum,Colosseum,Colosseum,Colosseum,Colosseum,Colosseum,Culusseu,Kolezyum,Koliseoa,Kolize,Kolizejs,Kolizey,Kolizey,Koliziejus,Kolosej,Kolosej,Koloseo,Koloseo,Koloseum,Koloseum,Koloseum,Koloseum,Koloseum,Koloseum,Koloseum,Koloseumi,Kolosseum,Kolosseum,Kolosseum,Kolosseum"

spark.sql("SELECT GeoDbscan(longitude, latitude, 150, 50) AS cluster FROM posts WHERE \
          GeoData(description,'"+keywords+"') IS NOT NULL AND latitude > 0 AND longitude > 0").show(truncate=False)
