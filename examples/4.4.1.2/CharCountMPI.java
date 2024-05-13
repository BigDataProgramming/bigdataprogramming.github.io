import mpi.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.FileWriter;
import java.io.BufferedWriter;

public class CharCountMPI {
    public static void main(String[] args) throws MPIException, IOException {
        int tag = 42;
		// Initialize the message array for each process and result array for master
        int[] partial_counter = new int[26];
        int[] res = new int[26];
        int[] split_size = new int[1];
        int[] remainingBytes = new int[1];
        String fileName = args[0];
        MPI.Init(args);
        Comm comm = MPI.COMM_WORLD;
        int rank = comm.getRank();
        int master_rank = 0;
        int ntasks = comm.getSize();
        if (rank == master_rank) { // The master computes the split size
            Path path = Paths.get(fileName);
            long bytes = Files.size(path);
            split_size[0] = (int) (bytes / ntasks);
            remainingBytes[0] = (int) (bytes % ntasks); // Get the remaining bytes
        }
		// The split size is broadcast to all processes
        comm.bcast(split_size, split_size.length, MPI.INT, 0);
        comm.bcast(remainingBytes, remainingBytes.length, MPI.INT, 0);

        int size = split_size[0];
        if (rank == ntasks - 1) {// Last process gets the remaining bytes
            size += remainingBytes[0];
        }
        byte[] readBytes = new byte[size];
        try (InputStream inputStream = new FileInputStream(fileName)) {
		    // Each process determines the chunk in which to work
            int start = rank * size;
            if (rank == ntasks - 1) { // Adjust the start position for the last process
                start += remainingBytes[0];
            }
            inputStream.skip(start);
            inputStream.read(readBytes, 0, size);
			// Count and store all the characters of the chunk
            for (byte b : readBytes) {
                char c = (char) b;
                if (Character.isLetter(c)) {
                    int index = (int) Character.toLowerCase(c) - (int) 'a';
                    partial_counter[index] += 1;
                }
            }
            // Each process communicates the partial counter to the master
            comm.send(partial_counter, partial_counter.length, MPI.INT, master_rank, tag);
        }
        if (rank == master_rank) {
			// The master aggregates the partial results
            for (int i = 0; i < ntasks; i++) {
                Status status = comm.recv(partial_counter,
                        partial_counter.length, MPI.INT, MPI.ANY_SOURCE, tag);
                for (int j = 0; j < partial_counter.length; j++)
                    res[j] += partial_counter[j];
            }
            saveResults(res);
        }
        MPI.Finalize();
    }

    private static String getCharForNumber(int i) {
        return i >= 0 && i <= 26 ? String.valueOf((char)(i + 65)) : null;
    }

    private static void saveResults(int[] res) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"))) {
            for (int j = 0; j < res.length; j++) {
                writer.write(getCharForNumber(j) + ": " + res[j] + "\n");
            }
        }
    }
}