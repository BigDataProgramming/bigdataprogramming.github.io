from __future__ import annotations
import pendulum
from airflow.decorators import dag, task
import numpy as np
import pickle
import sklearn
from sklearn.naive_bayes import GaussianNB
from sklearn.linear_model import LogisticRegression
from sklearn.tree import DecisionTreeClassifier
from sklearn.svm import SVC
from sklearn.neighbors import KNeighborsClassifier
from sklearn.datasets import load_breast_cancer as load_dataset
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score

# instantiate dag
@dag(
    schedule=None,
    start_date=pendulum.now(),
    catchup=False,
    tags=["big_data_book_examples"],
)
def ensemble_taskflow():
    # load and partition dataset into train and test
    @task(multiple_outputs=True)
    def partition():
        X,y = load_dataset(return_X_y=True)
        X_train, X_test, y_train, y_test = train_test_split(
            X, y, test_size=0.2, random_state=13
        )
        train_data = (X_train.tolist(), y_train.tolist())
        test_data = (X_test.tolist(), y_test.tolist())
        return {"train": train_data, "test": test_data}
    
    # fit a given classification models on train data
    @task
    def train(model:sklearn.base.BaseEstimator, train_data:tuple):
        X_train, y_train = train_data
        # fit the model and serialize it
        model.fit(X_train, y_train)
        model_bytes = pickle.dumps(model)
        model_str = model_bytes.decode("latin1")
        return model_str

    # perform ensemble classification on test data
    @task
    def vote(test_data:tuple, models:list):
        X_test, y_test = test_data
        pred_sum = np.array([0]*len(X_test))
        for model_str in models:
            # deserialize the model and predict
            model_bytes = model_str.encode("latin1")
            model = pickle.loads(model_bytes)
            pred_sum += model.predict(X_test)
        # prediction is set equal to the majority class
        n_models = len(models)
        threshold = np.ceil(n_models/2)
        preds = [int(s>=threshold) for s in pred_sum]
        print(f"Accuracy is: {accuracy_score(y_test, preds):.2f}")
    
   
    ### main flow ###
    # load and partition dataset
    partitioned_dataset = partition()
    train_data = partitioned_dataset["train"]
    test_data = partitioned_dataset["test"]
    # train in parallel 5 independent classifiers
    m1 = train(GaussianNB(), train_data)
    m2 = train(LogisticRegression(), train_data)
    m3 = train(DecisionTreeClassifier(), train_data)
    m4 = train(SVC(), train_data)
    m5 = train(KNeighborsClassifier(), train_data)
    # compute voting accuracy on test data
    vote(test_data, [m1, m2, m3, m4, m5])

# start DAG
ensemble_taskflow()
