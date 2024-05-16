#!/bin/bash
docker build -t bigdata-book-base ./base
docker build -t bigdata-book-master ./master
docker build -t bigdata-book-worker ./worker
docker build -t bigdata-book-history ./history
docker build -t bigdata-book-jupyter ./jupyter
