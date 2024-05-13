# Spark Programming Examples
In this repository two examples about the basic use of Spark are presented.

The first example, **MarketBasketAnalysis.scala**, is based on the RDD abastraction and presents a simplified version of a
market basket analysis task that looks for products that frequently
appear together in a set of transactions. In particular, given a text
file of transactions (```data/transactions.txt```) represented as a set of items (one for each row)
delimited by a comma, the program outputs all pairs of items ordered
by the number of co-occurrences. 

Since Spark provides APIs for structured data and SQL-like
queries, we also illustrate another example that uses the DataFrame
abstraction. In particular, **DataFrameExample.scala** implements an application showing how to perform some queries using the methods provided by the DataFrame APIs. 

### Data description
The ```data/ITCompany_employess.json``` dataset contains information about employees within a company. Each entry represents a single employee and includes the following attributes:

- *id*: Unique identifier for each employee.
- *name*: First name of the employee.
- *surname*: Last name of the employee.
- *age*: Age of the employee.
- *department*: The department within the company where the employee works.
- *salary*: The salary of the employee.
- *skills*: A list of skills possessed by the employee, represented as an array of strings.

The ```data/ITCompany_projects.json``` dataset contains information about multiple projects within the company. Each entry represents a project and includes the following attributes:

- *id*: Unique identifier for each project.
- *name*: Name of the project.
- *description*: Description of the project.
- *budget*: Budget allocated for the project.
- *skills*: A list of skills required for the project, represented as an array of strings.
- *employees*: A list of employee IDs assigned to the project.

## How to run
The application comes with a script that automatically builds the 
application and runs it on the Spark cluster. To launch the application simply open a terminal in the _master_ container: 

```bash
docker exec -ti bigdata-book-master /bin/bash
```

Then, run the following commands in the container shell:
```bash
cd /opt/examples/4.3.1.4
bash ./run.sh
```