CREATE DATABASE "metastore";
CREATE USER jupyter WITH ENCRYPTED PASSWORD 'jupyter';
GRANT ALL ON DATABASE metastore TO jupyter;

CREATE DATABASE "airflow";
GRANT ALL ON DATABASE airflow TO jupyter;
