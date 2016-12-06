package gov.ca.water.calgui.bus_service;

import java.util.List;

import gov.ca.water.calgui.bo.CalLiteGUIException;
import gov.ca.water.calgui.bo.DataTableModle;
import gov.ca.water.calgui.tech_service.ThreeFunction;

/**
 * This is the interface for all the tables.
 *
 * @author mohan
 */
public interface ITableSvc {

	/**
	 * This method will take the Table Name and return the DataTableModle object of that table.
	 *
	 * @param tableName
	 *            Name of the table with the option like D1641 or D1485.
	 * @return It will return the table as {@link DataTableModle}.
	 * @throws CalLiteGUIException
	 *             When loading the tables if it gets an error it will throw it.
	 */
	public DataTableModle getTable(String tableName) throws CalLiteGUIException;

	/**
	 *
	 * @param tableName
	 *            Just the table name without the path and extension.
	 * @param function
	 *            The function which retrieve the data from the table file.
	 * @return The {@link DataTableModle} object with whole table.
	 * @throws CalLiteGUIException
	 *             If anything wrong about the file then it will throw a exception with the information about it.
	 */
	public DataTableModle getTable(String tableName, ThreeFunction<List<String>, Integer, String, String[][]> function)
	        throws CalLiteGUIException;

	/**
	 * This will return the full path of the SWP file name which is to load in the Operations tab.
	 *
	 * @return
	 */
	public String getWsidiForSWPFullFileName();

	/**
	 * This will set the full path of the SWP file name which is to load in the Operations tab.
	 *
	 * @param wsidiFileSuffix
	 */
	public void setWsidiForSWPFullFileName(String wsidiFileSuffix);

	/**
	 * This will return the full path of the CVP file name which is to load in the Operations tab.
	 *
	 * @return
	 */
	public String getWsidiForCVPFullFileName();

	/**
	 * This will set the full path of the CVP file name which is to load in the Operations tab.
	 *
	 * @param wsidiFileSuffix
	 */
	public void setWsidiForCVPFullFileName(String wsidiForCVPFullFileName);

	/**
	 * This method is used to get the WSIDI tables.
	 *
	 * @param fileName
	 *            The whole path of the file with the table name and the extension.
	 * @return Return the Object of {@link DataTableModle} with the table data in it.
	 * @throws CalLiteGUIException
	 *             If anything wrong about the file then it will throw a exception with the information about it.
	 */
	public DataTableModle getWsiDiTable(String fileName) throws CalLiteGUIException;
}
