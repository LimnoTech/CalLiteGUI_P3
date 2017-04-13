package gov.ca.water.calgui.bo;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Vector;

import javax.swing.JList;

import org.apache.log4j.Logger;

import calsim.app.DerivedTimeSeries;
import calsim.app.MultipleTimeSeries;
import gov.ca.water.calgui.results.RBListItem;
import gov.ca.water.calgui.results.ResultUtils;
import hec.heclib.util.HecTime;
import hec.io.TimeSeriesContainer;

/**
 * Class to grab (generate) DSS time series BASED ON DTS list for a set of
 * scenarios passed in a JList. Each scenario is a string that corresponds to a
 * fully qualified (?) DSS file name. The DSS_Grabber instance provides access
 * to the following:
 * <ul>
 * <li>Main time series (result, including necessary math) for each scenario<br>
 * <li>Secondary series (control) where indicated for each scenario<br>
 * <li>Difference for main time series for each scenario<br>
 * <li>Exceedance for main time series for each scenario<br>
 * </ul>
 * Typical usage sequence:
 * <ul>
 * <li>DSS_Grabber</li>
 * <li>setIsCFS</li>
 * <li>setBase</li>
 * <li>setLocation</li>
 * <li>setDateRange</li>
 * <li>getPrimarySeries</li>
 * <li>getSecondarySeries</li>
 * <li>Other calculations</li>
 * </ul>
 */
public class DSSGrabber2BO extends DSSGrabber1BO {

	static Logger log = Logger.getLogger(DSSGrabber2BO.class.getName());

	private final DerivedTimeSeries dts;
	private final MultipleTimeSeries mts;

	private double[][][] annualTAFs;
	private double[][][] annualTAFsDiff;
	private double[][][] annualCFSs;
	private double[][][] annualCFSsDiff;

	public DSSGrabber2BO(JList list, DerivedTimeSeries dts, MultipleTimeSeries mts) {

		super(list);
		this.dts = dts;
		this.mts = mts;

		// GuiUtils.setStatus("Entered DSSGrabber");

	}

	/**
	 * Sets dataset (DSS) names to read from scenario DSS files, title, and axis
	 * labels according to location specified using a coded string. The string
	 * is currently used as a lookup into either Schematic_DSS_Links4.table (if
	 * it starts with "SchVw") or into GUI_Links3.table. These tables may be
	 * combined in Phase 2.
	 *
	 * @param string
	 *            index into GUI_Links3.table or Schematic_DSS_Link4.table
	 */
	public void setLocation(String locationName) {

		// TODO: Combine lookup tables AND review use of complex names
		locationName = locationName.trim();

		if (locationName.startsWith("@@")) {
			// @@ indicates MTS/DTS title
			locationName = locationName.substring(2, locationName.length());
			primaryDSSName = locationName;
			secondaryDSSName = "";
			yLabel = "";
			sLabel = "";
			title = locationName;
		} else if (locationName.startsWith("/")) {
			// Handle names passed from WRIMS GUI
			String parts[] = locationName.split("/");
			title = locationName;
			primaryDSSName = parts[2] + "/" + parts[3];
			secondaryDSSName = "";
			yLabel = "";
			sLabel = "";
		} else if (locationName.startsWith("SchVw")) {
			// Schematic view uses Table5 in mainMenu; this should be combined
			// with GUI_Links3 table
			for (int i = 0; i < ResultUtils.getXMLParsingSvcImplInstance(null).getLookups5Length(); i++) {
				if (locationName.toUpperCase()
						.endsWith(ResultUtils.getXMLParsingSvcImplInstance(null).getLookups5(i, 0))) {
					primaryDSSName = ResultUtils.getXMLParsingSvcImplInstance(null).getLookups5(i, 1);
					secondaryDSSName = ResultUtils.getXMLParsingSvcImplInstance(null).getLookups5(i, 2);
					yLabel = ResultUtils.getXMLParsingSvcImplInstance(null).getLookups5(i, 3);
					title = ResultUtils.getXMLParsingSvcImplInstance(null).getLookups5(i, 4);
					sLabel = ResultUtils.getXMLParsingSvcImplInstance(null).getLookups5(i, 5);
				}
			}
		} else

			for (int i = 0; i < ResultUtils.getXMLParsingSvcImplInstance(null).getLookupsLength(); i++) {
				if (locationName.endsWith(ResultUtils.getXMLParsingSvcImplInstance(null).getLookups(i, 0))) {
					primaryDSSName = ResultUtils.getXMLParsingSvcImplInstance(null).getLookups(i, 1);
					secondaryDSSName = ResultUtils.getXMLParsingSvcImplInstance(null).getLookups(i, 2);
					yLabel = ResultUtils.getXMLParsingSvcImplInstance(null).getLookups(i, 3);
					title = ResultUtils.getXMLParsingSvcImplInstance(null).getLookups(i, 4);
					sLabel = ResultUtils.getXMLParsingSvcImplInstance(null).getLookups(i, 5);
				}
			}
	}

	/**
	 * Reads the DSS results for the primary series for each scenario. Also
	 * stores for reference the units of measure for the primary series in the
	 * private variable originalUnits.
	 *
	 * @return Array of HEC TimeSeriesContainer - one TSC for each scenario
	 */
	public TimeSeriesContainer[] getPrimarySeries(String locationName) {

		TimeSeriesContainer[] results = null;

		if (checkReadiness() != null)
			throw new NullPointerException(checkReadiness());

		else {

			if (locationName.contains("SchVw") && primaryDSSName.contains(",")) {

				// Special handling for DEMO of schematic view - treat multiple
				// series as multiple scenarios
				// TODO: Longer-term approach is probably to add a rank to
				// arrays storing all series

				String[] dssNames = primaryDSSName.split(",");
				scenarios = dssNames.length;
				results = new TimeSeriesContainer[scenarios];
				for (int i = 0; i < scenarios; i++)
					results[i] = getOneSeries(baseName, dssNames[i]);

				originalUnits = results[0].units;

			} else {

				// Store number of scenarios

				scenarios = lstScenarios.getModel().getSize();
				results = new TimeSeriesContainer[scenarios];

				// Base first

				results[0] = getOneSeries_WRIMS(baseName, primaryDSSName, dts);
				originalUnits = results[0].units;

				// Then scenarios

				int j = 0;
				for (int i = 0; i < scenarios; i++) {
					String scenarioName;
					if (baseName.contains("_SV.DSS")) {
						// For SVars, use WRIMS GUI Project object to determine
						// input files
						switch (i) {
						case 0:
							scenarioName = project.getSVFile();
							break;
						case 1:
							scenarioName = project.getSV2File();
							break;
						case 2:
							scenarioName = project.getSV3File();
							break;
						case 3:
							scenarioName = project.getSV4File();
							break;
						default:
							scenarioName = "";
							break;
						}
					} else
						scenarioName = ((RBListItem) lstScenarios.getModel().getElementAt(i)).toString();
					if (!baseName.equals(scenarioName)) {
						j = j + 1;
						results[j] = getOneSeries_WRIMS(scenarioName, primaryDSSName, dts);
					}
				}
			}
		}

		return results;
	}

	public TimeSeriesContainer[] getMultipleTimeSeries(int mtsI) {

		TimeSeriesContainer[] results = null;

		if (checkReadiness() != null)
			throw new NullPointerException(checkReadiness());

		else {

			// Store number of scenarios

			scenarios = lstScenarios.getModel().getSize();
			results = new TimeSeriesContainer[scenarios];

			// Base first

			results[0] = getOneSeries_WRIMS(baseName, mtsI, mts);
			originalUnits = results[0].units;

			// Then scenarios

			int j = 0;
			for (int i = 0; i < scenarios; i++) {
				String scenarioName;
				if (baseName.contains("_SV.DSS")) {
					// For SVars, use WRIMS GUI Project object to determine
					// input files
					switch (i) {
					case 0:
						scenarioName = project.getSVFile();
						break;
					case 1:
						scenarioName = project.getSV2File();
						break;
					case 2:
						scenarioName = project.getSV3File();
						break;
					case 3:
						scenarioName = project.getSV4File();
						break;
					default:
						scenarioName = "";
						break;
					}
				} else
					scenarioName = ((RBListItem) lstScenarios.getModel().getElementAt(i)).toString();
				if (!baseName.equals(scenarioName)) {
					j = j + 1;
					results[j] = getOneSeries_WRIMS(scenarioName, mtsI, mts);
				}
			}
		}

		return results;
	}

	private TimeSeriesContainer getOneSeries_WRIMS(String dssFilename, String dssName, DerivedTimeSeries dts2) {

		Vector<?> dtsNames = dts2.getDtsNames();

		TimeSeriesContainer result = null;
		for (int i = 0; i < dts2.getNumberOfDataReferences(); i++) {
			TimeSeriesContainer interimResult;
			if (!((String) dtsNames.get(i)).isEmpty()) {
				// Operand is reference to another DTS
				DerivedTimeSeries adt = ResultUtils.getXMLParsingSvcImplInstance(null).getProject()
						.getDTS((String) dtsNames.get(i));
				System.out.println((String) dtsNames.get(i) + ":" + adt.getName());
				interimResult = getOneSeries_WRIMS(dssFilename, dssName, adt);
			} else {
				// Operand is a DSS time series
				primaryDSSName = (dts2.getBPartAt(i) + "//" + dts2.getCPartAt(i));
				if (dts2.getVarTypeAt(i).equals("DVAR")) {
					interimResult = getOneSeries(dssFilename,
							(dts2.getBPartAt(i) + "/" + dts2.getCPartAt(i) + "/LOOKUP"));
				} else {
					String svFilename = "";

					if (dssFilename.equals(project.getDVFile()))
						svFilename = project.getSVFile();
					else if (dssFilename.equals(project.getDV2File()))
						svFilename = project.getSV2File();
					else if (dssFilename.equals(project.getDV3File()))
						svFilename = project.getSV3File();
					else if (dssFilename.equals(project.getDV4File()))
						svFilename = project.getSV4File();

					interimResult = getOneSeries(svFilename,
							(dts2.getBPartAt(i) + "/" + dts2.getCPartAt(i) + "/LOOKUP"));
				}
			}
			if (i == 0) {

				// First time through, copy Interim result into result
				result = interimResult;
				if (dts2.getOperationIdAt(i) < 1)

					// Iff operation is "?", treat as a control and convert to
					// on/off
					for (int j = 0; j < interimResult.numberValues; j++)
					result.values[j] = (result.values[j] > 0.1) ? 9876.5 : 0;
			} else
				switch (dts2.getOperationIdAt(i)) {

				case 0:

					// Iff operation is "?", treat as a control

					for (int j = 0; j < interimResult.numberValues; j++)
						result.values[j] = ((result.values[j] > 0.1) && (interimResult.values[j] > 0.1)) ? 9876.5 : 0;
					break;

				case 1:
					for (int j = 0; j < interimResult.numberValues; j++)
						result.values[j] = result.values[j] + interimResult.values[j];
					break;

				case 2:
					for (int j = 0; j < interimResult.numberValues; j++)
						result.values[j] = result.values[j] - interimResult.values[j];
					break;

				case 3:
					for (int j = 0; j < interimResult.numberValues; j++)
						result.values[j] = result.values[j] * interimResult.values[j];
					break;

				case 4:
					for (int j = 0; j < interimResult.numberValues; j++)
						result.values[j] = result.values[j] / interimResult.values[j];
					break;

				default:
					break;

				}

		}
		return result;

	}

	private TimeSeriesContainer getOneSeries_WRIMS(String dssFilename, int i, MultipleTimeSeries mts2) {

		TimeSeriesContainer result = null;
		if (!mts2.getDTSNameAt(i).equals("")) {
			// Operand is reference to a DTS
			DerivedTimeSeries adt = ResultUtils.getXMLParsingSvcImplInstance(null).getProject()
					.getDTS(mts.getDTSNameAt(i));
			result = getOneSeries_WRIMS(dssFilename, "", adt);
			primaryDSSName = mts.getDTSNameAt(i);

		} else {
			// Operand is a DSS time series
			primaryDSSName = (mts2.getBPartAt(i) + "//" + mts2.getCPartAt(i));
			if (mts2.getVarTypeAt(i).equals("DVAR")) {
				result = getOneSeries(dssFilename, (mts2.getBPartAt(i) + "/" + mts2.getCPartAt(i)));
			} else {
				String svFilename = "";

				if (dssFilename.equals(project.getDVFile()))
					svFilename = project.getSVFile();
				else if (dssFilename.equals(project.getDV2File()))
					svFilename = project.getSV2File();
				else if (dssFilename.equals(project.getDV3File()))
					svFilename = project.getSV3File();
				else if (dssFilename.equals(project.getDV4File()))
					svFilename = project.getSV4File();

				result = getOneSeries(svFilename, (mts2.getBPartAt(i) + "/" + mts2.getCPartAt(i)));
			}
		}
		return result;
	}

	/**
	 * Variant of getDifferenceSeries to work with MTS (multiple time series)
	 * 
	 * @param timeSeriesResults
	 *            array of arrays of HEC TimeSeriesContainer objects, each
	 *            representing a set of results for a scenario. Base is in
	 *            position [0].
	 * @return array of arrays of HEC TimeSeriesContainer objects (size one less
	 *         than timeSeriesResult. Position [0] contains difference [1]-[0],
	 *         position [1] contains difference [2]-[0], ...
	 */
	public TimeSeriesContainer[][] getDifferenceSeries(TimeSeriesContainer[][] timeSeriesResults) {

		TimeSeriesContainer[][] results = new TimeSeriesContainer[timeSeriesResults.length][scenarios - 1];

		for (int tsi = 0; tsi < timeSeriesResults.length; tsi++) {

			for (int i = 0; i < scenarios - 1; i++) {

				results[tsi][i] = (TimeSeriesContainer) timeSeriesResults[tsi][i + 1].clone();
				for (int j = 0; j < results[tsi][i].numberValues; j++)
					results[tsi][i].values[j] = results[tsi][i].values[j] - timeSeriesResults[tsi][0].values[j];
			}
		}
		return results;
	}

	/**
	 * Variant of CalcTAFforCFS to work with multiple time series
	 *
	 * @param primaryResults
	 */
	public void calcTAFforCFS(TimeSeriesContainer[][] primaryResults) {

		// Allocate and zero out

		int datasets = primaryResults.length;
		int scenarios = primaryResults[0].length;

		annualTAFs = new double[datasets][scenarios][endWY - startWY + 2];

		for (int mtsi = 0; mtsi < datasets; mtsi++)
			for (int i = 0; i < scenarios; i++)
				for (int j = 0; j < endWY - startWY + 1; j++)
					annualTAFs[mtsi][i][j] = 0.0;

		// Calculate

		if (originalUnits.equals("CFS")) {

			HecTime ht = new HecTime();
			Calendar calendar = Calendar.getInstance();

			// Primary series

			for (int mtsi = 0; mtsi < primaryResults.length; mtsi++) {
				for (int i = 0; i < scenarios; i++) {
					for (int j = 0; j < primaryResults[mtsi][i].numberValues; j++) {

						ht.set(primaryResults[mtsi][i].times[j]);
						calendar.set(ht.year(), ht.month() - 1, 1);
						double monthlyTAF = primaryResults[mtsi][i].values[j]
								* calendar.getActualMaximum(Calendar.DAY_OF_MONTH) * CFS_2_TAF_DAY;
						int wy = ((ht.month() < 10) ? ht.year() : ht.year() + 1) - startWY;
						if (wy >= 0)
							annualTAFs[mtsi][i][wy] += monthlyTAF;
						if (!isCFS)
							primaryResults[mtsi][i].values[j] = monthlyTAF;
					}
					if (!isCFS)
						primaryResults[mtsi][i].units = "TAF per year";
				}
			}
		}

		// Calculate differences if applicable (primary series only)

		if (primaryResults[0].length > 1) {
			annualTAFsDiff = new double[datasets][scenarios - 1][endWY - startWY + 2];
			for (int mtsi = 0; mtsi < primaryResults.length - 1; mtsi++)
				for (int i = 0; i < scenarios; i++)
					for (int j = 0; j < endWY - startWY + 1; j++)
						annualTAFsDiff[mtsi][i][j] = annualTAFs[mtsi + 1][i][j] - annualTAFs[0][i][j];
		}

	}

	public double getAnnualTAF(int mtsi, int i, int wy) {

		return wy < startWY ? -1 : annualTAFs[mtsi][i][wy - startWY];
	}

	public double getAnnualTAFDiff(int mtsi, int i, int wy) {

		return wy < startWY ? -1 : annualTAFsDiff[mtsi][i][wy - startWY];
	}

	/**
	 * Variant of getExceedanceSeries for mts
	 *
	 * @param timeSeriesResults
	 * @return
	 */
	public TimeSeriesContainer[][][] getExceedanceSeries(TimeSeriesContainer[][] timeSeriesResults) {

		TimeSeriesContainer[][][] results;
		if (timeSeriesResults == null)
			results = null;
		else {
			int datasets = timeSeriesResults.length;
			results = new TimeSeriesContainer[14][datasets][scenarios];
			for (int mtsI = 0; mtsI < datasets; mtsI++) {
				for (int month = 0; month < 14; month++) {

					HecTime ht = new HecTime();
					for (int i = 0; i < scenarios; i++) {

						if (month == 13) {
							results[month][mtsI][i] = (TimeSeriesContainer) timeSeriesResults[mtsI][i].clone();
						} else {

							int n;
							int times2[];
							double values2[];

							results[month][mtsI][i] = new TimeSeriesContainer();

							if (month == 12) {

								// Annual totals - grab from annualTAFs
								n = annualTAFs[i].length;
								times2 = new int[n];
								values2 = new double[n];
								for (int j = 0; j < n; j++) {
									ht.setYearMonthDay(j + startWY, 11, 1, 0);
									times2[j] = ht.value();
									values2[j] = annualTAFs[mtsI][i][j];
								}

							} else {

								int[] times = timeSeriesResults[mtsI][i].times;
								double[] values = timeSeriesResults[mtsI][i].values;
								n = 0;
								for (int j = 0; j < times.length; j++) {
									ht.set(times[j]);
									if (ht.month() == month + 1)
										n = n + 1;
								}
								times2 = new int[n];
								values2 = new double[n];
								n = 0;
								for (int j = 0; j < times.length; j++) {
									ht.set(times[j]);
									if (ht.month() == month + 1) {
										times2[n] = times[j];
										values2[n] = values[j];
										n = n + 1;
									}
								}
							}
							results[month][mtsI][i].times = times2;
							results[month][mtsI][i].values = values2;
							results[month][mtsI][i].numberValues = n;
							results[month][mtsI][i].units = timeSeriesResults[mtsI][i].units;
							results[month][mtsI][i].fullName = timeSeriesResults[mtsI][i].fullName;
							results[month][mtsI][i].fileName = timeSeriesResults[mtsI][i].fileName;
						}
						if (results[month][mtsI][i].values != null) {
							double[] sortArray = results[month][mtsI][i].values;
							Arrays.sort(sortArray);
							results[month][mtsI][i].values = sortArray;
						}
					}
				}
			}
		}
		return results;
	}

	/**
	 * Variant of getExceedanceSeriesD that works with MTS files
	 *
	 * Should be recombinable with other exceedance methods.
	 *
	 * @param timeSeriesResults
	 * @return
	 */
	public TimeSeriesContainer[][][] getExceedanceSeriesD(TimeSeriesContainer[][] timeSeriesResults) {

		TimeSeriesContainer[][][] results;
		if (timeSeriesResults == null)
			results = null;
		else {
			int datasets = timeSeriesResults.length;
			results = new TimeSeriesContainer[14][datasets][scenarios - 1];
			for (int mtsI = 0; mtsI < datasets; mtsI++) {

				for (int month = 0; month < 14; month++) {

					HecTime ht = new HecTime();
					for (int i = 0; i < scenarios - 1; i++) {

						if (month == 13) {

							results[month][mtsI][i] = (TimeSeriesContainer) timeSeriesResults[mtsI][i + 1].clone();
							for (int j = 0; j < results[month][mtsI][i].numberValues; j++)
								results[month][mtsI][i].values[j] -= timeSeriesResults[mtsI][0].values[j];

						} else {

							int n;
							int times2[];
							double values2[];

							results[month][mtsI][i] = new TimeSeriesContainer();

							if (month == 12) {

								// Annual totals - grab from annualTAFs
								n = annualTAFs[mtsI][i + 1].length;
								times2 = new int[n];
								values2 = new double[n];
								for (int j = 0; j < n; j++) {
									ht.setYearMonthDay(j + startWY, 11, 1, 0);
									times2[j] = ht.value();
									values2[j] = annualTAFs[mtsI][i + 1][j] - annualTAFs[mtsI][0][j];
								}

							} else {

								int[] times = timeSeriesResults[mtsI][i + 1].times;
								double[] values = timeSeriesResults[mtsI][i + 1].values;
								n = 0;
								for (int j = 0; j < times.length; j++) {
									ht.set(times[j]);
									if (ht.month() == month + 1)
										n = n + 1;
								}
								times2 = new int[n];
								values2 = new double[n];
								int nmax = n; // Added to trap Schematic View
												// case where required flow has
												// extra values
								n = 0;
								for (int j = 0; j < times.length; j++) {
									ht.set(times[j]);
									if ((ht.month() == month + 1) && (n < nmax)
											&& (j < timeSeriesResults[0][mtsI].values.length)) {
										times2[n] = times[j];
										values2[n] = values[j] - timeSeriesResults[0][mtsI].values[j];
										n = n + 1;
									}
								}
							}
							results[month][mtsI][i].times = times2;
							results[month][mtsI][i].values = values2;
							results[month][mtsI][i].numberValues = n;
							results[month][mtsI][i].units = timeSeriesResults[mtsI][i + 1].units;
							results[month][mtsI][i].fullName = timeSeriesResults[mtsI][i + 1].fullName;
							results[month][mtsI][i].fileName = timeSeriesResults[mtsI][i + 1].fileName;
						}
						if (results[month][mtsI][i].values != null) {
							double[] sortArray = results[month][mtsI][i].values;
							Arrays.sort(sortArray);
							results[month][mtsI][i].values = sortArray;
						}
					}
				}
			}
		}
		return results;
	}

}
