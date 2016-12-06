package gov.ca.water.calgui.bus_service.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.swixml.SwingEngine;

import gov.ca.water.calgui.bus_service.IBatchRunSvc;
import gov.ca.water.calgui.constant.Constant;
import gov.ca.water.calgui.tech_service.IErrorHandlingSvc;
import gov.ca.water.calgui.tech_service.impl.ErrorHandlingSvcImpl;

/**
 * This class will handle the batch run.
 */
public final class BatchRunSvcImpl implements IBatchRunSvc {

	private static final Logger LOG = Logger.getLogger(BatchRunSvcImpl.class.getName());
	private Properties properties = new Properties();
	private int wsdiIterations;
	private IErrorHandlingSvc errorHandlingSvc = new ErrorHandlingSvcImpl();
	public static int simultaneousRuns;

	public BatchRunSvcImpl() {
		try {
			properties.load(BatchRunSvcImpl.class.getClassLoader().getResourceAsStream("callite-gui.properties"));
			wsdiIterations = Integer.parseInt(properties.getProperty("wsidi.iterations"));
		} catch (Exception ex) {
			LOG.error("Problem loading properties. " + ex.getMessage(), ex);
		}
	}

	@Override
	public void doBatch(List<String> scenarioNamesList, SwingEngine swingEngine, boolean isWsidi) {
		if (scenarioNamesList.isEmpty() || scenarioNamesList == null) {
			errorHandlingSvc.validationeErrorHandler("The scenario name list is empty to run the batch program.",
			        "The scenario name list is empty to run the batch program.",
			        (JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME));
		}
		if (!dateSelectionIsValid(swingEngine)) {
			errorHandlingSvc.validationeErrorHandler("The End Date should be greater then the start date",
			        "The End Date should be greater then the start date", (JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME));
		}
		// Disable run button
		JButton btn = (JButton) swingEngine.find("run_btnRun");
		btn.setEnabled(false);
		// delete previous generated batch file
		deleteBatchFile();
		if (isWsidi) {
			if (scenarioNamesList.size() > 1) {
				JOptionPane.showMessageDialog(swingEngine.find(Constant.MAIN_FRAME_NAME),
				        "WSIDI generation only allowed for a single hydroclimate realization.");
			} else {
				setupMainBatchFileWSIDI(null, scenarioNamesList.get(0), wsdiIterations);
				runBatch();
			}
		} else {
			// how many simultaneous run?
			int numberOfSimultaneousRun = simultaneousRuns;
			// how many sub batch files?
			int numberOfSubBatch = (int) Math.ceil((float) scenarioNamesList.size() / numberOfSimultaneousRun);
			// sub batch file name array
			String[] subBatchFileNameArray = new String[numberOfSubBatch];
			for (int j = 0; j < numberOfSubBatch; j++) {
				subBatchFileNameArray[j] = "group_" + j + ".bat";
				File subBatchFile = new File(System.getProperty("user.dir"), subBatchFileNameArray[j]);
				deleteDirectory(subBatchFile);
				List<String> groupScenFileNameList = new ArrayList<String>();
				for (int i = j * numberOfSimultaneousRun; i < Math.min((j + 1) * numberOfSimultaneousRun,
				        scenarioNamesList.size()); i++) {
					String scenFileName = scenarioNamesList.get(i);
					groupScenFileNameList.add(scenFileName);
				}
				setupBatchFile(subBatchFileNameArray[j], groupScenFileNameList, true);
			}
			// generate main batch file
			setupMainBatchFile(null, scenarioNamesList, subBatchFileNameArray);
			// run all scenarios with 3 secs delay between jvm initialization
			runBatch();
		}
		btn.setEnabled(true);
	}

	/**
	 * This will generate the main .bat file depending on the values passed in.
	 *
	 * @param batFileName
	 *            The batch file name. If it is empty or null it will take the default batch file name.
	 * @param scenarioNamesList
	 *            The list of scenario names in this run.
	 * @param subBatchFileNameArray
	 *            The individual batch file name.
	 */
	public void setupMainBatchFile(String batFileName, List<String> scenarioNamesList, String[] subBatchFileNameArray) {
		if (batFileName == null || batFileName.isEmpty())
			batFileName = "CalLite_w2.bat";
		String del = "";
		for (String scenarioName : scenarioNamesList) {
			String scenarioPath = new File(Constant.RUN_DETAILS_DIR + scenarioName).getAbsolutePath();
			String wsidilogFilePath = new File(scenarioPath, "run\\wsidi_iteration.log").getAbsolutePath();
			String progressFilePath = new File(scenarioPath, "run\\progress.txt").getAbsolutePath();
			String wreslCheckFilePath = new File(scenarioPath, "run\\\"=WreslCheck_main=.log\"").getAbsolutePath();
			String wreslCheckWsidiFilePath = new File(scenarioPath, "run\\\"=WreslCheck_main_wsidi=.log\"").getAbsolutePath();
			del = del + "del /F /Q " + wsidilogFilePath + "\r\n";
			del = del + "del /F /Q " + progressFilePath + "\r\n";
			del = del + "del /F /Q " + wreslCheckFilePath + "\r\n";
			del = del + "del /F /Q " + wreslCheckWsidiFilePath + "\r\n";
		}
		File batchFile = null;
		batchFile = new File(System.getProperty("user.dir"), batFileName);
		PrintWriter batchFilePW;
		try {
			batchFilePW = new PrintWriter(new BufferedWriter(new FileWriter(batchFile)));
			batchFilePW.println(del);
			for (String subBat : subBatchFileNameArray) {
				batchFilePW.println("start /wait /min " + subBat);
				batchFilePW.println();
			}
			batchFilePW.println("exit\n");
			batchFilePW.flush();
			batchFilePW.close();
		} catch (IOException e) {
			LOG.debug(e);
		}
	}

	/**
	 * This will generate the main .bat file for WSI-DI.
	 *
	 * @param batFileName
	 *            The batch file name. If it is empty or null it will take the default batch file name.
	 * @param scenarioFileName
	 *            The list of scenario names in this run.
	 * @param iterations
	 *            The number of iterations.
	 */
	public static void setupMainBatchFileWSIDI(String batFileName, String scenarioFileName, final int iterations) {
		if (batFileName == null || batFileName.isEmpty())
			batFileName = "CalLite_w2.bat";
		String del = "";
		String scenarioName = FilenameUtils.removeExtension(scenarioFileName);
		String scenarioPath = new File(Constant.RUN_DETAILS_DIR + scenarioName).getAbsolutePath();
		String progressFilePath = new File(scenarioPath, "run\\progress.txt").getAbsolutePath();
		String wreslCheckFilePath = new File(scenarioPath, "run\\\"=WreslCheck_main=.log\"").getAbsolutePath();
		String wreslCheckWsidiFilePath = new File(scenarioPath, "run\\\"=WreslCheck_main_wsidi=.log\"").getAbsolutePath();
		String wsidiIterationLogPath = new File(scenarioPath, "run\\wsidi_iteration.log").getAbsolutePath();
		del = del + "del /F /Q " + progressFilePath + "\r\n";
		del = del + "del /F /Q " + wreslCheckFilePath + "\r\n";
		del = del + "del /F /Q " + wreslCheckWsidiFilePath + "\r\n";
		del = del + "del /F /Q " + wsidiIterationLogPath + "\r\n";
		File batchFile = null;
		batchFile = new File(System.getProperty("user.dir"), batFileName);
		PrintWriter batchFilePW;
		String cmd = "Model_w2\\vscript.bat Model_w2\\vscript\\Main.py " + "\""
		        + new File(scenarioPath, scenarioName + "_wsidi.config").getAbsolutePath() + "\" " + iterations;
		try {
			batchFilePW = new PrintWriter(new BufferedWriter(new FileWriter(batchFile)));
			batchFilePW.println("@title=CalLiteRun" + scenarioName);
			batchFilePW.println(del);
			batchFilePW.println();
			batchFilePW.println(cmd);
			batchFilePW.flush();
			batchFilePW.close();
		} catch (IOException e) {
			LOG.debug(e);
		}
	}

	/**
	 * This will generate the main .bat file for single run.
	 *
	 * @param batFileName
	 * @param scenarioFileName
	 * @param isParallel
	 */
	private void setupBatchFile(String batFileName, List<String> scenarioFileName, boolean isParallel) {
		if (batFileName == null || batFileName.isEmpty())
			batFileName = "CalLite_w2.bat";
		File batchFile = null;
		batchFile = new File(System.getProperty("user.dir"), batFileName);
		PrintWriter batchFilePW;
		try {
			batchFilePW = new PrintWriter(new BufferedWriter(new FileWriter(batchFile)));
			for (int i = 0; i < scenarioFileName.size(); i++) {
				String scenarioName = FilenameUtils.removeExtension(scenarioFileName.get(i));
				String scenarioPath = new File(Constant.RUN_DETAILS_DIR + scenarioName).getAbsolutePath();
				String configFilePath = new File(scenarioPath, scenarioName + ".config").getAbsolutePath();
				String batchText = "%~dp0\\Model_w2\\runConfig_calgui " + configFilePath + " " + scenarioName;
				if (isParallel && i < scenarioFileName.size() - 1) {
					batchFilePW.println("start /min " + batchText);
					batchFilePW.println("timeout 3");
				} else {
					batchFilePW.println("@title = \"" + batchText + "\"");
					batchFilePW.println(batchText);
					batchFilePW.println();
				}
			}
			batchFilePW.flush();
			batchFilePW.close();
		} catch (IOException e) {
			LOG.debug(e);
		}
	}

	/**
	 * This method will run the batch program.
	 */
	private static void runBatch() {
		try {
			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec("cmd /c start /min \"CalLiteRun\" " + System.getProperty("user.dir") + "\\CalLite_w2.bat");
			int exitVal = proc.waitFor();
			LOG.debug("Return from batch run " + exitVal);
		} catch (Throwable t) {
			JOptionPane.showMessageDialog(null, t.getMessage(), "Run failure!", JOptionPane.ERROR_MESSAGE);
			LOG.debug(t.getStackTrace());
		}
	}

	/**
	 * This method will delete the whole directory.
	 *
	 * @param directory
	 *            The directory name.
	 * @return This will return whether it is successful or not.
	 */
	public boolean deleteDirectory(File directory) {
		if (directory.isDirectory()) {
			String[] children = directory.list();
			if (children != null) {
				for (int i = 0; i < children.length; i++) {
					boolean success = deleteDirectory(new File(directory, children[i]));
					if (!success) {
						return false;
					}
				}
			}
		}
		return directory.delete();
	}

	/**
	 * This will delete the default batch file.
	 */
	private void deleteBatchFile() {
		File batchFile = new File(System.getProperty("user.dir"), "CalLite_w2.bat");
		batchFile.delete();
	}

	/**
	 * Checks if date selection is valid for scenario currently in memory
	 *
	 * @param swingEngine
	 *            The {@link SwingEngine} Object.
	 */
	private boolean dateSelectionIsValid(SwingEngine swingEngine) {
		String startMon = ((String) ((JSpinner) swingEngine.find("spnRunStartMonth")).getValue()).trim();
		String endMon = ((String) ((JSpinner) swingEngine.find("spnRunEndMonth")).getValue()).trim();
		Integer startYr = (Integer) ((JSpinner) swingEngine.find("spnRunStartYear")).getValue();
		Integer endYr = (Integer) ((JSpinner) swingEngine.find("spnRunEndYear")).getValue();
		Integer iSMon = monthToInt(startMon);
		Integer iEMon = monthToInt(endMon);
		Integer numMon = (endYr - startYr) * 12 + (iEMon - iSMon) + 1;
		return numMon > 1 ? true : false;
	}

	/**
	 * Convert the month name to the int value of it.
	 *
	 * @param month
	 *            - The month name which you want to convert.
	 * @return The int value of the month.
	 */
	public static int monthToInt(String month) {
		HashMap<String, Integer> monthMap = new HashMap<String, Integer>();
		monthMap.put("jan", 1);
		monthMap.put("feb", 2);
		monthMap.put("mar", 3);
		monthMap.put("apr", 4);
		monthMap.put("may", 5);
		monthMap.put("jun", 6);
		monthMap.put("jul", 7);
		monthMap.put("aug", 8);
		monthMap.put("sep", 9);
		monthMap.put("oct", 10);
		monthMap.put("nov", 11);
		monthMap.put("dec", 12);
		month = month.toLowerCase();
		Integer monthCode = null;
		monthCode = monthMap.get(month);
		return monthCode == null ? -1 : monthCode.intValue();
	}
}
