from airflow import DAG
from airflow.providers.http.operators.http import HttpOperator
from airflow.operators.python_operator import PythonOperator
from airflow.operators.email import EmailOperator
import pendulum, json

endpoint="v1/forecast"
location = "latitude=39.30&longitude=16.25"
parameters= "temperature_2m_max,temperature_2m_min,precipitation_sum,sunrise,sunset,windspeed_10m_max,winddirection_10m_dominant&timezone=Europe/Berlin"
today = pendulum.now().strftime("%Y-%m-%d")
weather_query = endpoint+"?"+location+"&daily="+parameters+"&start_date="+today+"&end_date="+today

def build_body(**context):
    query_result = context['ti'].xcom_pull('submit_query')
    weather_info = json.loads(query_result)
    daily_info = weather_info["daily"].items()
    units = weather_info["daily_units"].values()
    list_info = [f"{k}:{v[0]} {unit}" for (k,v),unit in zip(daily_info, units)]
    body_mail = ", ".join(list_info)
    return body_mail

# The SimpleHttpOperator used for the submit_query task is now deprecated and it has been replaced with the HttpOperator
with DAG(dag_id="weather_mail", start_date=pendulum.now(), schedule=None, tags = ["big_data_book_examples"]) as dag_weather:
    submit_query = HttpOperator(task_id="submit_query", http_conn_id='api_weather', endpoint=weather_query, method="GET", headers={})
    prepare_email = PythonOperator(task_id='prepare_email', python_callable=build_body, dag=dag_weather)
    send_email = EmailOperator(task_id="send_email", to="user@example.com", subject="Weather today in Cosenza", html_content="{{ti.xcom_pull('prepare_email')}}")
    
    submit_query >> prepare_email >> send_email
