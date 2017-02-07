package gov.ca.water.calgui.bus_service;

/**
 * This is the interface for Monitor the process which is done behind the seen.
 * 
 * @author Mohan
 */
public interface IMonitorSvc {

	/**
	 * This method will return the status of the save process.
	 *
	 * @param scenarioName
	 *            Just the scenario name.
	 * @return Will return the string to be displayed.
	 */
	public String save(String scenarioName);

	/**
	 * This method will return the status of the batch run process.
	 *
	 * @param scenarioName
	 *            Just the scenario name.
	 * @return Will return the string to be displayed.
	 */
	public String batchRun(String scenarioName);

	/**
	 * This method will return the status of the batch run process for WSIDI.
	 *
	 * @param scenarioName
	 *            Just the scenario name.
	 * @return Will return the string to be displayed.
	 */
	public String batchRunWsidi(String scenarioName);
}
