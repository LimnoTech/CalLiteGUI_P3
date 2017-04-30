package gov.ca.water.calgui.bus_service;

import hec.io.TimeSeriesContainer;

public interface IDSSGrabber1Svc {

	/**
	 * Sets the isCFS flag.
	 *
	 * @param isCFS
	 *            set to True if flows are to be displayed in CFS, False for
	 *            TAFY.
	 */
	void setIsCFS(boolean isCFS);

	/**
	 * Sets the date range. Results are trimmed to the date range when read in
	 * GetOneSeries method.
	 *
	 * @param dateRange
	 *            string describing the date range in format mmmyyyy-mmmyyyy.
	 *            For example, the string "Apr1961-Mar1962" sets the date range
	 *            to run from April 1961 to March 1962.
	 */
	void setDateRange(String dateRange);

	/**
	 * Sets base scenario for calculations and display. The base scenario is
	 * listed first in legends and used as the basis for difference
	 * calculations.
	 *
	 * @param baseName
	 *            name of scenario/DSS file to use as base.
	 */
	void setBase(String baseName);

	/**
	 * Gets name part of base scenario file in baseName.
	 *
	 * @return name of the base scenario file WITHOUT the file extension
	 */
	String getBase();

	/**
	 * Sets dataset (DSS) names to read from scenario DSS files, title, and axis
	 * labels according to location specified using a coded string. The string
	 * is currently used as a lookup into either Schematic_DSS_Links4.table (if
	 * it starts with Constant.SCHEMATIC_PREFIX) or into GUI_Links3.table. These
	 * tables may be combined in Phase 2.
	 *
	 * @param locationName
	 *            index into GUI_Links3.table or Schematic_DSS_Link4.table
	 */
	void setLocation(String locationName);

	/**
	 * Sets dataset (DSS) names to read from scenario DSS files, title, and axis
	 * labels be parsing string defined in web viewer.
	 *
	 * @param locationName
	 *            coded location string passed
	 */
	void setLocationWeb(String locationName);

	/**
	 * Gets primary y-axis label assigned by DSS_Grabber to the results read in
	 * from DSS file.
	 *
	 * @return string containing primary y-axis label
	 */
	String getYLabel();

	/**
	 * Gets secondary y-axis label assigned by DSS_Grabber to the results read
	 * in from DSS file.
	 *
	 * @return string containing secondary y-axis label
	 */
	String getSLabel();

	/**
	 * Gets chart/table title assigned by DSS_Grabber to the results read in
	 * from DSS file. If no title is set, the name of the primary DSS dataset is
	 * returned instad.,
	 *
	 * @return string containing title
	 */
	String getTitle();

	/**
	 * Checks if a DSS file has records with "HYDROPOWER" as the A-PART
	 * 
	 * @param dssFilename
	 * @return
	 */
	boolean hasPower(String dssFilename);

	/**
	 * Reads the DSS results for the primary series for each scenario. Also
	 * stores for reference the units of measure for the primary series in the
	 * private variable originalUnits.
	 *
	 * @return Array of HEC TimeSeriesContainer - one TSC for each scenario
	 */
	TimeSeriesContainer[] getPrimarySeries(String locationName);

	/**
	 * Reads the DSS results for the secondary series for each scenario.
	 *
	 * @return Array of HEC TimeSeriesContainer - one TSC for each scenario
	 */
	TimeSeriesContainer[] getSecondarySeries();

	/**
	 * Calculates the difference between scenarios and the base for a set of DSS
	 * results.
	 *
	 * @param timeSeriesResults
	 *            array of HEC TimeSeriesContainer objects, each representing a
	 *            set of results for a scenario. Base is in position [0].
	 * @return array of HEC TimeSeriesContainer objects (size one less than
	 *         timeSeriesResult. Position [0] contains difference [1]-[0],
	 *         position [1] contains difference [2]-[0], ...
	 */
	TimeSeriesContainer[] getDifferenceSeries(TimeSeriesContainer[] timeSeriesResults);

	/**
	 * Calculates annual volume in TAF for any CFS dataset, and replaces monthly
	 * values if TAF flag is checked.
	 *
	 * @param primaryResults
	 * @param secondaryResults
	 */
	void calcTAFforCFS(TimeSeriesContainer[] primaryResults, TimeSeriesContainer[] secondaryResults);

	double getAnnualTAF(int i, int wy);

	double getAnnualTAFDiff(int i, int wy);

	double getAnnualCFS(int i, int wy);

	double getAnnualCFSDiff(int i, int wy);

	/**
	 * Calculates annual volume in CFS for any TAF dataset, and replaces monthly
	 * values if CFS flag is checked.
	 *
	 * @param primaryResults
	 * @param secondaryResults
	 */
	void calcCFSforTAF(TimeSeriesContainer[] primaryResults, TimeSeriesContainer[] secondaryResults);

	/**
	 * Generates exceedance time series from monthly DSS results.
	 *
	 * @param timeSeriesResults
	 *            array of HEC TimeSeriesContainer objects, each representing a
	 *            set of results for a scenario.
	 * @return array of HEC TimeSeriesContainer objects - 14 for each input.
	 *         Exceedances for all values are in [index=0], for each month's
	 *         values [1..12], and [13] for annual totals [Index=13]
	 */
	TimeSeriesContainer[][] getExceedanceSeries(TimeSeriesContainer[] timeSeriesResults);

	/**
	 * Generated exceedance for difference of time series
	 * 
	 * @param timeSeriesResults
	 * @return
	 */
	TimeSeriesContainer[][] getExceedanceSeriesD(TimeSeriesContainer[] timeSeriesResults);

	String getOriginalUnits();

	void setOriginalUnits(String originalUnits);

	String getPrimaryDSSName();

	void setPrimaryDSSName(String primaryDSSName);

}