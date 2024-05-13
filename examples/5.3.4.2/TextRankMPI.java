import mpi.*;
import java.io.*;
import java.util.*;
import java.util.stream.*;

public class TextRankMPI {

	private static final double DAMPING_FACTOR = 0.85;
	private static final double CONVERGENCE_THRESHOLD = 0.001;

	public static void main(String[] args) throws MPIException {
		// Initialize the MPI execution environment
		MPI.Init(args);

		// Get the size and rank of the current process
		int size = MPI.COMM_WORLD.getSize();
		int rank = MPI.COMM_WORLD.getRank();

		// Read the input file
		List<String> allSentences = readInputFile(args[0]);

		// Build the graph
		Map<Integer, Map<String, ArrayList<Integer>>> graph = buildGraph(allSentences);

		// Compute the initial PageRank value for each vertex
		int numVertices = graph.keySet().size();
		double[] pageranks = new double[numVertices];
		Arrays.fill(pageranks, 1.0 / numVertices);

		// Compute the number of vertices that each process will handle
		int verticesPerProcess = numVertices / size;
		int remainder = numVertices % size;

		// Compute the starting and ending indices for this process
		int startIndex = rank * verticesPerProcess;
		int endIndex = startIndex + verticesPerProcess;
		if (remainder > 0 && rank == size - 1) {
			endIndex += remainder;
			verticesPerProcess += remainder;
		}

		double[] globalPageranks = new double[numVertices];

		// Iterate until convergence
		boolean converged = false;
		while (!converged) {
			// Compute the PageRank values for the vertices assigned to this process
			double[] localPageranks = new double[verticesPerProcess];
			double sum_out_sim = 0.0;
			double in_weight = 0.0;
			for (int i = startIndex; i < endIndex; i++) {
				for (int in_neighbor : graph.get(i).get("in_neigh")) {
					in_weight = sentenceSimilarity(allSentences.get(in_neighbor), allSentences.get(i));
					// normalize weights of incoming edges based on the out-neighborhood of their
					// source nodes
					sum_out_sim = 0.0;
					for (int out_neighbor : graph.get(in_neighbor).get("out_neigh")) {
						sum_out_sim += sentenceSimilarity(allSentences.get(in_neighbor),
								allSentences.get(out_neighbor));
					}
					in_weight /= sum_out_sim;
					localPageranks[i - startIndex] += pageranks[in_neighbor] * in_weight;
				}
				localPageranks[i - startIndex] = DAMPING_FACTOR * localPageranks[i - startIndex]
						+ (1 - DAMPING_FACTOR) / numVertices;
			}

			// Gather the local PageRank values from all processes handling varying number
			// of vertices per rank
			globalPageranks = new double[numVertices];
			int[] rcvCount = new int[size];
			int[] displs = new int[size];
			for (int i = 0; i < size; i++) {
				rcvCount[i] = numVertices / size;
				displs[i] = rcvCount[i] * i;
			}
			rcvCount[size - 1] += remainder;
			MPI.COMM_WORLD.allGatherv(localPageranks, verticesPerProcess, MPI.DOUBLE, globalPageranks, rcvCount, displs,
					MPI.DOUBLE);

			// Compute the global PageRank variation for the iteration
			double delta = 0.0;
			for (int i = startIndex; i < endIndex; i++) {
				delta += Math.abs(globalPageranks[i] - pageranks[i]);
			}
			double[] deltas = new double[size];
			MPI.COMM_WORLD.allGather(new double[] { delta }, 1, MPI.DOUBLE, deltas, 1, MPI.DOUBLE);

			// Check convergence
			converged = true;
			for (double d : deltas) {
				if (d > CONVERGENCE_THRESHOLD) {
					converged = false;
					break;
				}
			}
			
			// Update the PageRank values for the next iteration
			pageranks = globalPageranks;
		}

		// Rank 0 computes the summary from the top-3 sentences per PageRank
		if (rank == 0) {
			int topK = 3;
			String summary = extractSummary(globalPageranks, topK, allSentences);
			System.out.println("SUMMARY:\n" + summary);
		}

		// Terminate the MPI execution environment
		MPI.Finalize();
	}

	/**
	 * Reads the input file and returns the list of sentences
	 */
	private static List<String> readInputFile(String fileName) {

		List<String> allSentences = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] sentences = line.split("\\.");
				for (String s : sentences) {
					allSentences.add(s.trim());
				}
			}
		} catch (IOException e) {
			System.err.format("IOException: %s%n", e);
		}
		return allSentences;
	}

	/**
	 * Builds the graph as a Map with vertexId as the key and neighborhoods as the value. 
	 * The neighborhoods are represented by a nested Map that contains two keys, "in_neigh" and "out_neigh",
	 * and their corresponding values are the lists of ids for the in-neighbors and out-neighbors, respectively.
	 */
	private static Map<Integer, Map<String, ArrayList<Integer>>> buildGraph(List<String> allSentences) {
		Map<Integer, Map<String, ArrayList<Integer>>> graph = new HashMap<>();

		for (int i = 0; i < allSentences.size(); i++) {
			for (int j = 0; j < allSentences.size(); j++) {
				if (i != j) {
					if (!graph.containsKey(i)) {
						graph.put(i, new HashMap<String, ArrayList<Integer>>());
						graph.get(i).put("in_neigh", new ArrayList<Integer>());
						graph.get(i).put("out_neigh", new ArrayList<Integer>());
					}
					if (!graph.containsKey(j)) {
						graph.put(j, new HashMap<String, ArrayList<Integer>>());
						graph.get(j).put("in_neigh", new ArrayList<Integer>());
						graph.get(j).put("out_neigh", new ArrayList<Integer>());
					}

					graph.get(i).get("out_neigh").add(j);
					graph.get(j).get("in_neigh").add(i);
				}
			}
		}
		return graph;
	}

        /**
	 * Computes textual similarity between the two input sentences.
	 */
	public static double sentenceSimilarity(String sentence1, String sentence2) {
		Set<String> words1 = new HashSet<>(Arrays.asList(sentence1.toLowerCase().split("[\\s,.;:?!]+")));
		Set<String> words2 = new HashSet<>(Arrays.asList(sentence2.toLowerCase().split("[\\s,.;:?!]+")));
		int commonWords = (int) words1.stream().filter(words2::contains).count();
		return (double) commonWords / (Math.log(words1.size() + 1) + Math.log(words2.size() + 1) + 1);
	}

	/*
	 * Extracts the summary: it is composed of the summary is composed of the top k
	 * sentences per PageRank ordered according to how they appear in the original
	 * text.
	 */
	public static String extractSummary(double[] globalPageranks, int k, List<String> allSentences) {
		Map<Double, Integer> map = new HashMap<>();
		for (int i = 0; i < globalPageranks.length; i++) {
			map.put(globalPageranks[i], i);
		}
		List<Double> values = new ArrayList<>(map.keySet());
		Collections.sort(values, Collections.reverseOrder());
		List<Integer> topKIndexes = new ArrayList<>();
		for (int i = 0; i < k && i < values.size(); i++) {
			topKIndexes.add(map.get(values.get(i)));
		}
		Collections.sort(topKIndexes);
		String summary = topKIndexes.stream().map(allSentences::get).collect(Collectors.joining(".\n")) + ".";
		return summary;
	}
}
