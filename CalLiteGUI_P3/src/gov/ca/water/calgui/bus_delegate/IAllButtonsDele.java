package gov.ca.water.calgui.bus_delegate;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JTable;

/**
 * This interface is to handle all the button actions in the ui.
 *
 * @author mohan
 *
 */
public interface IAllButtonsDele {

	/**
	 * This is to handle the "Save" button on the "Run Settings" tab.
	 */
	public boolean saveCurrentStateToFile();

	/**
	 * This method is used to save.
	 *
	 * @param clsFileName
	 *            Just the name of the cls file whithout the extension.
	 * @return It will return true if the save is successful.
	 */
	public boolean saveCurrentStateToFile(String clsFileName);

	/**
	 * This is to handle the "Save As" button on the "Run Settings" tab.
	 */
	public void saveAsButton();

	/**
	 * This will save the current state of the ui for the "View Scenario Settings" button on the "Run Settings" tab.
	 *
	 * @return It will return true if the save is successful.
	 */
	public boolean saveForViewScen();

	/**
	 * This method is used to run multiple batch program at once.
	 */
	public void runMultipleBatch();

	/**
	 * This method is used to handle the help button in the whole ui.
	 */
	public void helpButton();

	/**
	 * This method is used to handle the about button in the whole ui.
	 */
	public void aboutButton();

	/**
	 * This method is used to handle the exit of the ui.
	 */
	public void windowClosing();

	/**
	 * This method is used for the "Select.." buttons on the "Hydroclimate" tab. This method will read the file and set the values
	 * of the text fields.
	 *
	 * @param fileNameForDss
	 * @param fPartForDss
	 * @param manualFileNameForDss
	 * @param manualFPartForDss
	 */
	public void selectingSVAndInitFile(String fileNameForDss, String fPartForDss, String manualFileNameForDss,
	        String manualFPartForDss);

	/**
	 * This method will copy the cells which are selected in the table which is passed as a parameter.
	 *
	 * @param table
	 *            The table from which we should copy.
	 */
	public void copyTableValues(JTable table);

	/**
	 * This method will paste the cells which are selected in the table which is passed as a parameter.
	 *
	 * @param table
	 *            The table from which we should paste.
	 */
	public void pasteTableValues(JTable table);

	/**
	 * This method will handle the read button on the "operations" tab.
	 */
	public void readButtonInOperations();

	/**
	 * This method will handle the default button on the "operations" tab.
	 */
	public void defaultButtonOnOperations();

	/**
	 * This method will handle the edit SWP and CVP buttons on the "operations" tab.
	 *
	 * @param component
	 *            The selected component (SWP or CVP)
	 */
	public void editButtonOnOperations(JComponent component);

	/**
	 * This method is used to verify whether the selected files are of the given extension or not.
	 *
	 * @param fileChooser
	 *            The {@link JFileChooser} for the files which user selectes.
	 * @param extension
	 *            The correct extension of the file.
	 * @return will return true if the files selected by the user and the passed in extension match.
	 */
	public boolean verifyTheSelectedFiles(JFileChooser fileChooser, String extension);

	/**
	 * TODO
	 */
	public void decisionSVInitFilesAndTableInOperations();
}
