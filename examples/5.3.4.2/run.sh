#! /bin/bash
INPUT=$(pwd)/data/ANN.txt

sudo chown $(whoami):$(id -gn) -R .

mpijavac --verbose -cp /opt/openmpi/lib/mpi.jar TextRankMPI.java && \
mpirun -v -np 3 --hostfile hostname java TextRankMPI $INPUT
