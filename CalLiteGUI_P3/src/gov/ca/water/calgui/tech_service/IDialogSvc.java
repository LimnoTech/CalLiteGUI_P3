package gov.ca.water.calgui.tech_service;

/**
 * Interface for common JOptionPane for simple dialogs with CalLite logo,
 * centered on main frame
 * 
 * @author tslawecki
 *
 */
public interface IDialogSvc {

	String getOK(String message, int messageType);

	String getYesNo(String message, int messageType);

	String getOKCancel(String message, int messageType);

	String getSaveDontSaveCancel(String message, int messageType);

	String getYesNoCancel(String message, int messageType);

}