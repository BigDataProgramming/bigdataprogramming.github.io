REGISTER $jarPath;
DEFINE PROCESS it.unical.dimes.scalab.pig.Processing;
-- Load data from HDFS
reviews = LOAD '$dataset' USING PigStorage('\t') AS (id:int, text:chararray);
-- Load the dictionary of word sentiment
dictionary = LOAD '$dict' USING PigStorage('\t') AS (word:chararray,rating:int);
-- Tokenize and process the text of each review
words = FOREACH reviews GENERATE id,text, FLATTEN(TOKENIZE(PROCESS(text))) AS word;
--DUMP words;
-- Join each word with the dictionary and assign a score sentiment
matching = JOIN words BY word LEFT OUTER, dictionary BY word;
matches_rating = FOREACH matching GENERATE words::id AS id, words::text AS text, dictionary::rating AS rate;
-- Group and compute the average rating for a review
group_rating = GROUP matches_rating BY(id,text);
avg_ratings = FOREACH group_rating GENERATE group,AVG($1.$2) AS rate;
--DUMP avg_ratings;
-- Store the results
STORE avg_ratings INTO '$outputPath' USING PigStorage(',','-schema');
