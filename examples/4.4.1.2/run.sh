#! /bin/bash
INPUT=$(pwd)/data/data.txt

sudo chown $(whoami):$(id -gn) -R .
mpijavac --verbose -cp /opt/openmpi/lib/mpi.jar CharCountMPI.java && \
mpirun -v -np 3 --hostfile hostname java CharCountMPI $INPUT
