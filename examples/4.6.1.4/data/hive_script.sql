USE DEFAULT;

CREATE TABLE IF NOT EXISTS data (userid INT,
                   movieid INT,
                   rating INT,
                   timestamp_ INT)
ROW FORMAT DELIMITED
FIELDS TERMINATED BY '\t';

LOAD DATA INPATH '/user/custom/4.6.1.4/hive-data'
OVERWRITE INTO TABLE data;

SELECT userid, movieid, rating, timestamp_ FROM data LIMIT 10;

SELECT COUNT(*)
FROM data;

-- Save query results to HDFS
INSERT OVERWRITE DIRECTORY '/user/custom/4.6.1.4/hive-results'
ROW FORMAT DELIMITED
FIELDS TERMINATED BY ','
SELECT movieid, COUNT(rating) AS num_ratings
FROM data
GROUP BY movieid
ORDER BY num_ratings DESC;

CREATE TABLE IF NOT EXISTS data_new (userid INT,
                       movieid INT,
                       rating INT,
                       week STRING)
ROW FORMAT DELIMITED
FIELDS TERMINATED BY '\t';

add FILE hdfs://master/user/custom/4.6.1.4/week_mapper.py;

INSERT OVERWRITE TABLE data_new
SELECT
    TRANSFORM (userid, movieid, rating, timestamp_)
    USING 'python week_mapper.py'
    AS (userid, movieid, rating, week)
FROM data;

SELECT userid, movieid, rating, week FROM data_new LIMIT 10;

SELECT week, COUNT(*)
FROM data_new
GROUP BY week;