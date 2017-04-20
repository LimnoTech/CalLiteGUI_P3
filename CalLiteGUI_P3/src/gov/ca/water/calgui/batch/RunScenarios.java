package gov.ca.water.calgui.batch;
//! Creates and executes batch files using multiple threads

import java.io.IOException;
import java.util.ArrayList;

/**
 * This class will run the multiple batch files created when running on multiple
 * threads.
 * 
 */
public class RunScenarios {

	public RunScenarios() {
	}

	public void runParallel(ArrayList<String> scenarioList) throws IOException, InterruptedException {
		for (String sc : scenarioList) {
			String fn = "run_" + sc + ".bat";
			Runtime rt = Runtime.getRuntime();
			// Process proc = rt.exec("cmd /c start " +
			// System.getProperty("user.dir") + "\\CalLite_w2.bat");
			Process proc = rt.exec("cmd /c start /min " + System.getProperty("user.dir") + "\\" + fn);
			proc.waitFor();
		}
	}

	public static void main(String[] args) {
		ArrayList<String> scenarioList = new ArrayList<String>();
		scenarioList.add("test1");
		scenarioList.add("test2");
		RunScenarios rs = new RunScenarios();
		try {
			rs.runParallel(scenarioList);
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
	}
}