package gov.ca.water.calgui.bus_service;

import java.awt.Component;
import java.util.List;

import org.swixml.SwingEngine;

import gov.ca.water.calgui.bo.TriggerBO;

/**
 * This is the interface for applying the dynamic controls.
 *
 * @author mohan
 */
public interface IDynamicControlSvc {

	/**
	 * This will apply all the dynamic controls which are from these two files.
	 *
	 * <pre>
	 * 1. TriggerForDymanicSelection.table
	 * 2. TriggerForDynamicDisplay.table
	 * </pre>
	 *
	 * @param itemName
	 * @param isSelected
	 * @param isEnabled
	 * @param swingEngine
	 */
	public void doDynamicControl(String itemName, boolean isSelected, boolean isEnabled, SwingEngine swingEngine);

	/**
	 * This will return the first {@link TriggerBO} object for the given {@code id}.
	 *
	 * @param id
	 * @return
	 */
	public TriggerBO getTriggerBOById(String id);

	/**
	 * This method is used for making components visible and non-visible the component and it's children.
	 *
	 * @param component
	 * @param isVisible
	 */
	public void toggleVisComponentAndChildren(Component component, Boolean isVisible);

	/**
	 * This method is used for making components enabling and disabling the component and it's children.
	 *
	 * @param component
	 * @param b
	 */
	public void toggleEnComponentAndChildren(Component component, boolean isEnable);

	/**
	 * This method is used to decide what is the lookup value for Gui_Link4 and the label for the Operations tab. This will return
	 * the list of strings in which the 1st is the lookup value and the 2nd is the label.
	 *
	 * @param swingEngine
	 * @return
	 */
	public List<String> getLabelAndGuiLinks4BOBasedOnTheRadioButtons(SwingEngine swingEngine);

	/**
	 * This is used to prevent the ItemListener.
	 *
	 * @return
	 */
	public boolean isPreventRoeTrigger();
}
