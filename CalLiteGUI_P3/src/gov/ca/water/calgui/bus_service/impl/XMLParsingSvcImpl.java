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
import javax.swing.JTable;
import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.swixml.SwingEngine;

import gov.ca.water.calgui.bo.CalLiteGUIException;
import gov.ca.water.calgui.bus_service.IXMLParsingSvc;
import gov.ca.water.calgui.constant.Constant;
import gov.ca.water.calgui.presentation.NumericTextField;
import gov.ca.water.calgui.tech_service.IErrorHandlingSvc;
import gov.ca.water.calgui.tech_service.IFileSystemSvc;
import gov.ca.water.calgui.tech_service.impl.ErrorHandlingSvcImpl;
import gov.ca.water.calgui.tech_service.impl.FileSystemSvcImpl;

/**
 * This class will parse the gui.xml into the Swing Engine.
 *
 * @author mohan
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

	/**
	 * This method is for implementing the singleton.
	 *
	 * @return
	 */
	public static IXMLParsingSvc getXMLParsingSvcImplInstance() {
		if (xmlParsingSvc == null) {
			xmlParsingSvc = new XMLParsingSvcImpl();
		}
		return xmlParsingSvc;
	}

	/**
	 * In this we 1st build the SwingEngine from the Gui.xml file. We build the newUserDefinedTables list by getting all the JTable
	 * component id and exclude the default ones.
	 */
	private XMLParsingSvcImpl() {
		LOG.debug("Building XMLParsingSvcImpl Object.");
		this.errorHandlingSvc = new ErrorHandlingSvcImpl();
		this.fileSystemSvc = new FileSystemSvcImpl();
		this.compNameIdMap = new HashMap<String, String>();
		this.jTextFieldIds = new ArrayList<>();
		this.swingEngine = new SwingEngine();
		swingEngine.getTaglib().registerTag("numtextfield", NumericTextField.class);
		try {
			swingEngine.render(fileSystemSvc.getXMLDocument());
		} catch (CalLiteGUIException ex) {
			errorHandlingSvc.displayErrorMessageBeforeTheUI(ex);
		} catch (Exception ex) {
			errorHandlingSvc.displayErrorMessageBeforeTheUI(
			        new CalLiteGUIException("This is from Swing Engine : " + Constant.NEW_LINE + ex.getMessage(), ex, true));
		}
		Set<String> compIds = this.getIdFromXML();
		compIds.stream().forEach((compId) -> {
			Component component = this.swingEngine.find(compId);
			if (component instanceof JCheckBox) {
				this.compNameIdMap.put(((JCheckBox) component).getText(), compId);
			}
		});
		List<String> temp = compIds.stream().filter((compId) -> swingEngine.find(compId) instanceof JTextField)
		        .collect(Collectors.toList());
		for (String string : temp) {
			if (checkIsItFromResultPart(string)) {
				jTextFieldIds.add(string);
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
	 * @return
	 */
	private boolean checkIsItFromResultPart(String compId) {
		List<String> resultTabNames = Arrays.asList("Custom", "externalPDF", "Reporting", "schematics");
		List<String> names = new java.util.ArrayList<String>();
		getAllThePanelNamesOfParent(swingEngine.find(compId).getParent(), names);
		boolean con = false;
		for (String string : resultTabNames) {
			con = con || names.contains(string);
		}
		return !con;
	}

	/**
	 * This will get all the {@code JPanel} parent of the {@code Component}
	 *
	 * @param component
	 * @param names
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
		Set<String> swt = map.keySet();
		swt.stream().forEach(key -> LOG.debug(key + " " + map.get(key).getClass().getName()));
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
