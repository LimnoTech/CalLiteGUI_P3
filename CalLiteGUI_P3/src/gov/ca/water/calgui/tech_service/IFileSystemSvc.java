package gov.ca.water.calgui.tech_service;

import java.util.List;
import java.util.function.Predicate;

import org.w3c.dom.Document;

import gov.ca.water.calgui.bo.CalLiteGUIException;

/**
 * This is the interface for File Handling like reading and saving.
 *
 * @author mohan
 */
public interface IFileSystemSvc {

	/**
	 * This will take the file name and return the lines in the file as list of strings.
	 *
	 * @param fileName
	 *            Full path and the file Name.
	 * @param isRequired
	 *            If file is required for the application to start and if the file is missing then it will throw the exception.
	 * @return return the lines in the file as list.
	 * @throws CalLiteGUIException
	 */
	public List<String> getFileData(String fileName, boolean isRequired) throws CalLiteGUIException;

	/**
	 *
	 * @param fileName
	 *            Full path and the file Name.
	 * @param isRequired
	 *            If file is required for the application to start and if the file is missing then it will exit the program. If not
	 *            throw the exception.
	 * @param selector
	 *            The function which decide which lines to be in the result.
	 * @return The list by removing all the line which don't satisfy the function passed in.
	 * @throws CalLiteGUIException
	 */
	public List<String> getFileData(String fileName, boolean isRequired, Predicate<String> selector) throws CalLiteGUIException;

	/**
	 * This will remove all the comment lines from the table file except the header line.
	 *
	 * @param fileName
	 *            Full path and the file Name.
	 * @return return the lines in the file as list after removing the comments.
	 * @throws CalLiteGUIException
	 */
	public List<String> getFileDataForTables(String fileName) throws CalLiteGUIException;

	/**
	 * This will save the given data into the given file.
	 *
	 * @param fileName
	 *            Full path and the file Name.
	 * @param data
	 *            The data which is writen to the file.
	 * @throws CalLiteGUIException
	 */
	public void saveDataToFile(String fileName, String data) throws CalLiteGUIException;

	/**
	 * This will generate the Document from the XML file.
	 *
	 * @return
	 * @throws CalLiteGUIException
	 */
	public Document getXMLDocument() throws CalLiteGUIException;

	/**
	 * This method will return the lookup string from the full file path. This is only used for SWP and CVP. This will return user
	 * defined if the file name does't have any lookup value
	 *
	 * @param fullName
	 * @return
	 */
	public String getTheLookupFromTheFullFileName(String fullName);

	/**
	 * This method will return the lookup string from the file name. This is only used for SWP and CVP. This will return user
	 * defined if the file name does't have any lookup value
	 *
	 * @param fileName
	 * @return
	 */
	public String getLookupFromTheFileName(String fileName);
}