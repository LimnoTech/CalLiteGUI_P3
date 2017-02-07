package gov.ca.water.calgui.bus_service;

import java.awt.Component;
import java.util.List;

import org.swixml.SwingEngine;

import gov.ca.water.calgui.bo.TriggerBO;

/**
 * This is the interface for applying the dynamic controls like enabling,
 * disabling etc.
 *
 * @author Mohan
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
	 *            The item name
	 * @param isSelected
	 *            Whether it is selected or not.
	 * @param isEnabled
	 *            Whether it is enabled or not.
	 * @param swingEngine
	 *            The object of the GUI.
	 */
	public void doDynamicControl(String itemName, boolean isSelected, boolean isEnabled, SwingEngine swingEngine);

	/**
	 * This will return the first {@link TriggerBO} object for the given
	 * {@code id}.
	 *
	 * @param id
	 *            The id of the trigger.
	 * @return Will return the first {@link TriggerBO} object for the given
	 *         {@code id}.
	 */
	public TriggerBO getTriggerBOById(String id);

	/**
	 * This method is used for making components visible or non-visible and it's
	 * children.
	 *
	 * @param component
	 *            The component.
	 * @param isVisible
	 *            value to be set.
	 */
	public void toggleVisComponentAndChildren(Component component, Boolean isVisible);

	/**
	 * This method is used for making components enabling and disabling the
	 * component and it's children.
	 *
	 * @param component
	 *            The component.
	 * @param isEnable
	 *            to enable or not.
	 */
	public void toggleEnComponentAndChildren(Component component, boolean isEnable);

	/**
	 * This method is used to decide what is the lookup value for Gui_Link4 and
	 * the label for the Operations tab. This will return the list of strings in
	 * which the 1st is the lookup value and the 2nd is the label.
	 *
	 * @param swingEngine
	 *            The object of the GUI.
	 * @return Will return the list of strings in which the 1st is the lookup
	 *         value and the 2nd is the label.
	 */
	public List<String> getLabelAndGuiLinks4BOBasedOnTheRadioButtons(SwingEngine swingEngine);

	/**
	 * This is used to prevent the ItemListener.
	 *
	 * @return Will return the roe trigger value.
	 */
	public boolean isPreventRoeTrigger();
}
