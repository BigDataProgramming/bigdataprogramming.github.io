USE DEFAULT;

DROP TABLE IF EXISTS posts;

CREATE EXTERNAL TABLE posts
(
    dateposted  STRING,
    description STRING,
    latitude    DECIMAL(6, 6),
    longitude   DECIMAL(6, 6)
)
    ROW FORMAT SERDE 'com.amazon.ionhiveserde.IonHiveSerDe'
    STORED AS
        INPUTFORMAT 'com.amazon.ionhiveserde.formats.IonInputFormat'
        OUTPUTFORMAT 'com.amazon.ionhiveserde.formats.IonOutputFormat'
    LOCATION '/user/custom/5.3.3/hive-data';



DROP FUNCTION IF EXISTS geodata;
DROP FUNCTION IF EXISTS geodbscan;

ADD JAR hdfs://master/user/custom/5.3.3/HiveUDF-1.0-SNAPSHOT-jar-with-dependencies.jar;
ADD JAR hdfs://master/user/custom/5.3.3/ion-hive3-serde-all-1.2.0.jar;

CREATE TEMPORARY FUNCTION geodata AS 'it.unical.dimes.scalab.hive.udf.GeoDataUDF';
CREATE TEMPORARY FUNCTION geodbscan AS 'it.unical.dimes.scalab.hive.udf.DbScanUDAF';

SELECT geodbscan(longitude, latitude, 150, 50) AS cluster
FROM posts
WHERE geodata(description,
              'colosseo,colis,collis,collos,Amphiteatrum Flavium,Amphitheatrum Flavium,An Colasaem,Coliseo,Coliseo,Coliseo de Roma,Coliseo de Roma,Coliseu de Roma,Coliseum,Coliseum,Coliseum,Coliseus,Colloseum,Coloseu,Colosseo,Colosseo,Colosseo,Colosseu,Colosseum,Colosseum,Colosseum,Colosseum,Colosseum,Colosseum,Colosseum,Colosseum,Colosseum,Colosseum,Culusseu,Kolezyum,Koliseoa,Kolize,Kolizejs,Kolizey,Kolizey,Koliziejus,Kolosej,Kolosej,Koloseo,Koloseo,Koloseum,Koloseum,Koloseum,Koloseum,Koloseum,Koloseum,Koloseum,Koloseumi,Kolosseum,Kolosseum,Kolosseum,Kolosseum') IS NOT NULL
  AND latitude > 0
  AND longitude > 0;
