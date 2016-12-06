package gov.ca.water.calgui.bus_delegate;

/**
 * This interface will apply the dynamic behaver which is controlled by the files
 * 
 * @author mohan
 *
 */
public interface IApplyDynamicConDele {
	/**
	 * This method will apply the dynamic behaver which is controlled by the files listed bellow and it also handle the special
	 * cases for the tabs.
	 *
	 * <pre>
	 *	1. TriggerForDymanicSelection.csv
	 *	2. TriggerForDymanicSelection.csv
	 * </pre>
	 *
	 * @param itemName
	 *            Name of the item which this method is going to handle.
	 * @param isSelected
	 *            whether the item is selected or not.
	 * @param isEnabled
	 *            whether the item is enabled or not.
	 * @param optionFromTheBox
	 *            This is the special field which is used for the popup box result in "run Settings" and "hydroclimate" tabs.
	 */
	public void applyDynamicControl(String itemName, boolean isSelected, boolean isEnabled, boolean optionFromTheBox);

}
