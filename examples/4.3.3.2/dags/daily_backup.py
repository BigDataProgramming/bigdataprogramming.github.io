from datetime import datetime
from airflow import DAG
from airflow.operators.bash import BashOperator
import pendulum

# Declaration of the DAG
with DAG(dag_id="daily_backup", start_date=datetime(2023, 1, 1), schedule="0 0 * * *", tags = ["big_data_book_examples"]) as dag:

    # Definition of four tasks performing bash commands
    # Note: Add a space after the script name when directly calling a .sh script with the bash_command argument – for example bash_command="my_script.sh ".
    # This is because Airflow tries to apply load this file and process it as a Jinja template to it ends with .sh, which will likely not be what most users want.
    task_A = BashOperator(task_id="task_A", bash_command="/opt/examples/4.3.3.2/scripts/task_A.sh ")
    task_B = BashOperator(task_id="task_B", bash_command="tar czf /opt/examples/4.3.3.2/dags.tgz /opt/examples/4.3.3.2/dags")
    task_C = BashOperator(task_id="task_C", bash_command="tar czf /opt/examples/4.3.3.2/dag_backup.tgz /opt/examples/4.3.3.2/dags/daily_backup.py")
    task_D = BashOperator(task_id="task_D", bash_command="echo backup completed")

    # Definition of task dependencies
    task_A >> [task_B, task_C]
    [task_B, task_C] >> task_D
