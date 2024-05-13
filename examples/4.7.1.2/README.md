# UPC++ Example: Monte Carlo estimation of &pi;

A [Linux Docker container with UPC++](https://hub.docker.com/r/upcxx/linux-amd64) is actively maintained 
by the Lawrence Berkeley National Laboratory. For more details on UPC++ please refer to the [official online guide](https://upcxx.lbl.gov/docs/html/guide.html).

## How to run
**From the official documentation of UPC++**

Assuming you have a Linux-compatible Docker environment, you can get a working UPC++ environment in seconds with the following command:

```console
docker run -it --rm upcxx/linux-amd64
```

The UPC++ commands inside the container are `upcxx` (compiler wrapper) and `upcxx-run` (run wrapper), 
and the home directory contains some example codes. Note this container is designed for test-driving UPC++ on a single node, and is not intended 
for long-term development or use in production environments. The container above supports the SMP and UDP backends, and does not include an MPI install. 

Then, run the following commands in the container shell:
```console
$ cd /home/upcxx/examples/compute-pi
$ make
$ upcxx-run -n 16 compute-pi 20000
```
Please note that the parameter `n` indicates the number of processes, 
while `20000` is the number of iteration for estimating Pi.
