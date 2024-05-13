#!/bin/bash
echo "Starting SSH service name node..."
sudo service ssh start

echo "Starting jupyter notebook..."
jupyter notebook --ip=0.0.0.0 --port=8888 --no-browser --NotebookApp.token='' --notebook-dir=/notebook