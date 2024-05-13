package FPGrowth.main;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;

public class Main {

	public static void main(String[] args) throws IOException,ClassNotFoundException,InterruptedException{
		// TODO Auto-generated method stub
		Configuration conf = new Configuration();
		conf.set("fs.defaultFS", "file:///");
		String inputFile = "outputMR/";
		String outputFile = "/home/user/eclipse-workspace/TrajectoryMining/outputMR3/";
		int minimumSupport = 3;
		int maxPatterns = 500;
		long startTime = System.currentTimeMillis();
		FpGrowth.runFpGrowth(inputFile,outputFile,minimumSupport,maxPatterns);
		long endTime = System.currentTimeMillis();
  		long totalTime = endTime-startTime;
  		System.out.println("time - " + totalTime);
	}

}
