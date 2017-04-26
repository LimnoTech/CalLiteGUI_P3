package gov.ca.water.calgui.bus_service.impl;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.swixml.SwingEngine;

import gov.ca.water.calgui.bo.CalLiteGUIException;
import gov.ca.water.calgui.bo.JLinkedSlider;
import gov.ca.water.calgui.bo.NumericTextField;
import gov.ca.water.calgui.bus_service.IXMLParsingSvc;
import gov.ca.water.calgui.constant.Constant;
import gov.ca.water.calgui.tech_service.IErrorHandlingSvc;
import gov.ca.water.calgui.tech_service.IFileSystemSvc;
import gov.ca.water.calgui.tech_service.impl.ErrorHandlingSvcImpl;
import gov.ca.water.calgui.tech_service.impl.FileSystemSvcImpl;

/**
 * This class will parse the gui.xml into the Swing Engine.
 *
 * @author Mohan
 *
 */
public final class XMLParsingSvcImpl implements IXMLParsingSvc {

	private static final Logger LOG = Logger.getLogger(XMLParsingSvcImpl.class.getName());
	private SwingEngine swingEngine;
	private static IXMLParsingSvc xmlParsingSvc;
	private IFileSystemSvc fileSystemSvc;
	private IErrorHandlingSvc errorHandlingSvc;
	private Map<String, String> compNameIdMap;
	private List<String> newUserDefinedTables;
	private List<String> jTextFieldIds;
	private List<String> jTextFieldIdsForLinkedSliders;
	private List<String> jCheckBoxIDs; // Checkboxes on regulations screen

	/**
	 * This method is for implementing the singleton. It will return the
	 * instance of this class if it is empty it will create one.
	 *
	 * @return Will return the instance of this class if it is empty it will
	 *         create one.
	 */
	public static IXMLParsingSvc getXMLParsingSvcImplInstance() {
		if (xmlParsingSvc == null) {
			xmlParsingSvc = new XMLParsingSvcImpl();
		}
		return xmlParsingSvc;
	}

	/*
	 * In this we 1st build the SwingEngine from the Gui.xml file. We build the
	 * newUserDefinedTables list by getting all the JTable component id and
	 * exclude the default ones.
	 */
	private XMLParsingSvcImpl() {
		LOG.debug("Building XMLParsingSvcImpl Object.");
		this.errorHandlingSvc = new ErrorHandlingSvcImpl();
		this.fileSystemSvc = new FileSystemSvcImpl();
		this.compNameIdMap = new HashMap<String, String>();
		this.jTextFieldIds = new ArrayList<>();
		this.jCheckBoxIDs = new ArrayList<>();
		this.jTextFieldIdsForLinkedSliders = new ArrayList<>();

		this.swingEngine = new SwingEngine();
		swingEngine.getTaglib().registerTag("numtextfield", NumericTextField.class);
		swingEngine.getTaglib().registerTag("linkedslider", JLinkedSlider.class);

		try {
			swingEngine.render(fileSystemSvc.getXMLDocument());
		} catch (CalLiteGUIException ex) {
			errorHandlingSvc.displayErrorMessageBeforeTheUI(ex);
		} catch (Exception ex) {
			errorHandlingSvc.displayErrorMessageBeforeTheUI(new CalLiteGUIException(
					"This is from Swing Engine : " + Constant.NEW_LINE + ex.getMessage(), ex, true));
		}

		Set<String> compIds = this.getIdFromXML();

		// Force names to match IDs for all checkboxes and radio buttons
		compIds.stream().forEach((compId) -> {
			Component component = this.swingEngine.find(compId);
			if (component instanceof JCheckBox) {
				if (!((JCheckBox) component).getName().equals(compId))
					((JCheckBox) component).setName(compId);
			} else if (component instanceof JRadioButton) {
				if (!((JRadioButton) component).getName().equals(compId))
					((JRadioButton) component).setName(compId);
			}
		});

		compIds.stream().forEach((compId) -> {
			Component component = this.swingEngine.find(compId);
			if (component instanceof JCheckBox) {
				this.compNameIdMap.put(((JCheckBox) component).getText(), compId);
			}
		});
		List<String> temp = compIds.stream().filter((compId) -> swingEngine.find(compId) instanceof JTextField)
				.collect(Collectors.toList());
		for (String string : temp) {
			if (!checkIsItFromResultPart(string)) {
				jTextFieldIds.add(string);
			}
		}

		// Build a list of TextBoxes that are referenced by JLinkedSliders

		temp = compIds.stream().filter((compId) -> swingEngine.find(compId) instanceof JLinkedSlider)
				.collect(Collectors.toList());
		for (String string : temp) {
			if (!checkIsItFromResultPart(string)) {
				JLinkedSlider ls = (JLinkedSlider) swingEngine.find(string);
				if (!ls.getLTextBoxID().equals(""))
					jTextFieldIdsForLinkedSliders.add(ls.getLTextBoxID());
				if (!ls.getRTextBoxID().equals(""))
					jTextFieldIdsForLinkedSliders.add(ls.getRTextBoxID());
			}
		}

		temp = compIds.stream().filter((compId) -> swingEngine.find(compId) instanceof JCheckBox)
				.collect(Collectors.toList());
		for (String string : temp) {
			if (checkIsItFromRegulationPart(string)) {
				jCheckBoxIDs.add(string);
			}
		}
		this.newUserDefinedTables = compIds.stream().filter((compId) -> swingEngine.find(compId) instanceof JTable)
				.collect(Collectors.toList());
		this.newUserDefinedTables.remove("tblRegValues");
		this.newUserDefinedTables.remove("tblOpValues");
		this.newUserDefinedTables.remove("tblIF3");
		this.newUserDefinedTables.remove("tblIF2");
		this.newUserDefinedTables.remove("tblIF1");
	}

	/**
	 * This method will tell whether the id is from the result part or not.
	 *
	 * @param compId
	 *            The ID of the component.
	 * @return Will check whether the id is from the result part of the ui.
	 */
	public boolean checkIsItFromResultPart(String compId) {
		List<String> resultTabNames = Arrays.asList("WRIMS", "External PDF", "Reporting", "schematics",
				"Data_tabbedPane2", "controls");
		List<String> names = new java.util.ArrayList<String>();
		getAllThePanelNamesOfParent(swingEngine.find(compId).getParent(), names);

		boolean con = false;
		for (String string : resultTabNames) {
			con = con || names.contains(string);
		}
		return con;
	}

	/**
	 * This method will tell whether the id is from the regulations part or not.
	 *
	 * @param compId
	 *            The ID of the component.
	 * @return Will check whether the id is from the result part of the ui.
	 */
	private boolean checkIsItFromRegulationPart(String compId) {
		List<String> names = new java.util.ArrayList<String>();
		getAllThePanelNamesOfParent(swingEngine.find(compId).getParent(), names);
		return names.contains("regulations");
	}

	/**
	 * This will get all the {@code JPanel} parent of the {@code Component}
	 *
	 * @param component
	 *            The component to which we need the parent names.
	 * @param names
	 *            This method will populate the parent names into this field.
	 */
	private void getAllThePanelNamesOfParent(Component component, List<String> names) {
		if (component instanceof JPanel) {
			if (((JPanel) component).getName() != null) {
				names.add(((JPanel) component).getName());
			}
		}
		if (component.getParent() != null) {
			getAllThePanelNamesOfParent(component.getParent(), names);
		}
	}

	@Override
	public List<String> getjTextFieldIds() {
		return jTextFieldIds;
	}

	@Override
	public List<String> getjTextFieldIdsForLinkedSliders() {
		return jTextFieldIdsForLinkedSliders;
	}

	@Override
	public List<String> getjCheckBoxIDs() {
		return jCheckBoxIDs;
	}

	@Override
	public String getcompIdfromName(String name) {
		return this.compNameIdMap.get(name);
	}

	@Override
	public SwingEngine getSwingEngine() {
		return this.swingEngine;
	}

	@Override
	public Set<String> getIdFromXML() {
		Map<String, Object> map = this.swingEngine.getIdMap();
		// Set<String> swt = map.keySet();
		// swt.stream().forEach(key -> LOG.debug(key + " " +
		// map.get(key).getClass().getName()));
		return map.keySet().stream().filter((id) -> {
			Object component = map.get(id);
			if (component instanceof Component) {
				return ((Component) component).isVisible();
			}
			return false;
		}).collect(Collectors.toSet());
	}

	@Override
	public List<String> getNewUserDefinedTables() {
		return newUserDefinedTables;
	}
}
