package gov.ca.water.calgui.tech_service;

/**
 * This is the interface for processing all the Audit records like adding,
 * delete etc.
 * 
 * @author Mohan
 */
public interface IAuditSvc {
	/**
	 * This will add the value to the Audit record.
	 *
	 * @param controlId
	 *            The id of the control.
	 * @param oldValue
	 *            The old value of the control.
	 * @param newValue
	 *            The new value of the control.
	 */
	public void addAudit(String controlId, String oldValue, String newValue);

	/**
	 * This will remove all the Audit records.
	 */
	public void clearAudit();

	/**
	 * This will tell whether the Audit is empty or not.
	 *
	 * @return Will return false if the audit is empty and vise versa.
	 */
	public boolean hasValues();
}
