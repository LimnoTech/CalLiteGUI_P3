package gov.ca.water.calgui.bus_delegate.impl;

import java.awt.Component;
import java.awt.Container;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.log4j.Logger;
import org.swixml.SwingEngine;

import gov.ca.water.calgui.bo.CalLiteGUIException;
import gov.ca.water.calgui.bo.DataTableModle;
import gov.ca.water.calgui.bo.GuiLinks4BO;
import gov.ca.water.calgui.bo.SeedDataBO;
import gov.ca.water.calgui.bus_delegate.IApplyDynamicConDele;
import gov.ca.water.calgui.bus_service.IDynamicControlSvc;
import gov.ca.water.calgui.bus_service.IResultSvc;
import gov.ca.water.calgui.bus_service.ISeedDataSvc;
import gov.ca.water.calgui.bus_service.ITableSvc;
import gov.ca.water.calgui.bus_service.IXMLParsingSvc;
import gov.ca.water.calgui.bus_service.impl.DynamicControlSvcImpl;
import gov.ca.water.calgui.bus_service.impl.ResultSvcImpl;
import gov.ca.water.calgui.bus_service.impl.SeedDataSvcImpl;
import gov.ca.water.calgui.bus_service.impl.TableSvcImpl;
import gov.ca.water.calgui.bus_service.impl.XMLParsingSvcImpl;
import gov.ca.water.calgui.constant.Constant;
import gov.ca.water.calgui.tech_service.IErrorHandlingSvc;
import gov.ca.water.calgui.tech_service.IFileSystemSvc;
import gov.ca.water.calgui.tech_service.impl.ErrorHandlingSvcImpl;
import gov.ca.water.calgui.tech_service.impl.FileSystemSvcImpl;

/**
 * This class will apply the dynamic behaver which is controlled by the files listed bellow.
 *
 * <pre>
 *	1. TriggerForDymanicSelection.csv
 *	2. TriggerForDymanicSelection.csv
 * </pre>
 *
 * @author mohan
 *
 */
public class ApplyDynamicConDeleImp implements IApplyDynamicConDele {
	private static final Logger LOG = Logger.getLogger(ApplyDynamicConDeleImp.class.getName());
	private ISeedDataSvc seedDataSvc = SeedDataSvcImpl.getSeedDataSvcImplInstance();
	private IDynamicControlSvc dynamicControlSvc = DynamicControlSvcImpl.getDynamicControlSvcImplInstance();
	private ITableSvc tableSvc = TableSvcImpl.getTableSvcImplInstance(null);
	private IErrorHandlingSvc errorHandlingSvc = new ErrorHandlingSvcImpl();
	private IXMLParsingSvc xmlParsingSvc = XMLParsingSvcImpl.getXMLParsingSvcImplInstance();
	private SwingEngine swingEngine = xmlParsingSvc.getSwingEngine();
	private IResultSvc resultSvc = ResultSvcImpl.getResultSvcImplInstance();
	private IFileSystemSvc fileSystemSvc = new FileSystemSvcImpl();

	@Override
	public void applyDynamicControlForListFromFile() {
		try {
			List<String> controlIds = fileSystemSvc.getFileData(Constant.DYNAMIC_CONTROL_FOR_STARTUP_FILENAME, false,
			        line -> !line.startsWith(Constant.EXCLAMATION));

			List<JCheckBox> checkBoxList = controlIds.stream().filter(id -> swingEngine.find(id) instanceof JCheckBox)
			        .map(id -> (JCheckBox) swingEngine.find(id)).filter(JCheckBox::isSelected).collect(Collectors.toList());

			List<JRadioButton> radioButtonList = controlIds.stream().filter(id -> swingEngine.find(id) instanceof JRadioButton)
			        .map(id -> (JRadioButton) swingEngine.find(id)).filter(JRadioButton::isSelected).collect(Collectors.toList());
			for (JRadioButton jRadioButton : radioButtonList) {
				dynamicControlSvc.doDynamicControl(jRadioButton.getName(), jRadioButton.isSelected(), jRadioButton.isEnabled(),
				        swingEngine);
			}
			for (JCheckBox jCheckBox : checkBoxList) {
				dynamicControlSvc.doDynamicControl(jCheckBox.getName(), jCheckBox.isSelected(), jCheckBox.isEnabled(), swingEngine);
			}
		} catch (CalLiteGUIException ex) {
			LOG.error(ex);
			errorHandlingSvc.businessErrorHandler((JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME), ex);
		}
	}

	@Override
	public void applyDynamicControl(String itemName, boolean isSelected, boolean isEnabled, boolean optionFromTheBox) {
		try {
			regulations(itemName, isSelected);
		} catch (CloneNotSupportedException ex) {
			LOG.error(ex);
			errorHandlingSvc.businessErrorHandler((JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME),
			        new CalLiteGUIException("Unable to clone the table class.", ex));
		} catch (CalLiteGUIException ex) {
			LOG.error(ex);
			errorHandlingSvc.businessErrorHandler((JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME), ex);
		}
		if (itemName.equals("fac_ckb2") && !isSelected) {
			((JRadioButton) swingEngine.find("fac_rdb0")).setSelected(true);
		}
		dynamicControlSvc.doDynamicControl(itemName, isSelected, isEnabled, swingEngine);
		// This is for changing the "SV" and "Init" Files in the "Hydroclimate" tab.
		List<String> controlIdForChangeOfSVInitFiles = Arrays.asList("run_rdbD1485", "run_rdbD1641", "run_rdbBO", "hyd_rdb2005",
		        "hyd_rdb2030", "hyd_rdbCCEL", "hyd_rdbCCLL", "hyd_ckb1", "hyd_ckb2", "hyd_ckb3", "hyd_ckb4", "hyd_ckb5");
		if (isSelected && controlIdForChangeOfSVInitFiles.contains(itemName)) {
			changeSVInitFilesAndTableInOperations(optionFromTheBox);
		}
		// This is for changing the "San Joaquin River Restoration flows" in the "Regulations" tab.
		List<String> controlIdForChangeOfPanelInRegulations = Arrays.asList("hyd_rdb2005", "hyd_rdb2030", "hyd_rdbCCEL",
		        "hyd_rdbCCLL");
		if (isSelected && controlIdForChangeOfPanelInRegulations.contains(itemName)) {
			changePanelInRegulationsBasedOnId(itemName);
		}
		if (isSelected && itemName.equals("btnDSS_Auto")) {
			changeSVInitFilesAndTableInOperations(false);
		}
		if (isSelected && itemName.equals("btnDSS_Manual")) {
			((JTextField) swingEngine.find("hyd_DSS_SV")).setText(((JTextField) swingEngine.find("txf_Manual_SV")).getText());
			((JTextField) swingEngine.find("hyd_DSS_SV_F")).setText(((JTextField) swingEngine.find("txf_Manual_SV_F")).getText());
			((JTextField) swingEngine.find("hyd_DSS_Init")).setText(((JTextField) swingEngine.find("txf_Manual_Init")).getText());
			((JTextField) swingEngine.find("hyd_DSS_Init_F"))
			        .setText(((JTextField) swingEngine.find("txf_Manual_Init_F")).getText());
		}
	}

	/**
	 * This method is used for controling the panel in the "Regulations" tab based on the "Hydroclimate" tab.
	 *
	 * @param itemName
	 *            The item name of the control.
	 */
	private void changePanelInRegulationsBasedOnId(String itemName) {
		JRadioButton regrdb = (JRadioButton) swingEngine.find("rdbRegQS_UD");
		if (!regrdb.isSelected()) {
			if (itemName.equals("hyd_rdb2005")) {
				regrdb = (JRadioButton) swingEngine.find("SJR_interim");
				regrdb.setSelected(true);
			} else {
				regrdb = (JRadioButton) swingEngine.find("SJR_full");
				regrdb.setSelected(true);
			}
			dynamicControlSvc.toggleEnComponentAndChildren(swingEngine.find("regpan2c"), false);
		}
	}

	@Override
	public void changeSVInitFilesAndTableInOperations(boolean optionFromTheBox) {
		try {
			List<String> labelValues = dynamicControlSvc.getLabelAndGuiLinks4BOBasedOnTheRadioButtons(swingEngine);
			GuiLinks4BO guiLinks4BO = seedDataSvc.getObjByRunBasisLodCcprojCcmodelIds(labelValues.get(0));
			if (!((JRadioButton) swingEngine.find("btnDSS_Manual")).isSelected()) {
				((JTextField) swingEngine.find("hyd_DSS_SV")).setText(guiLinks4BO.getSvFile());
				((JTextField) swingEngine.find("hyd_DSS_SV_F")).setText(guiLinks4BO.getfPartSV1());
				((JTextField) swingEngine.find("hyd_DSS_Init")).setText(guiLinks4BO.getInitFile());
				((JTextField) swingEngine.find("hyd_DSS_Init_F")).setText(guiLinks4BO.getfPartSV2());
			}
			if (optionFromTheBox) {
				String lookup = guiLinks4BO.getLookup();
				tableSvc.setWsidiForSWPFullFileName(Constant.MODEL_W2_WRESL_LOOKUP_DIR + "\\WSIDI\\" + Constant.SWP_START_FILENAME
				        + Constant.UNDER_SCORE + lookup + Constant.TABLE_EXT);
				tableSvc.setWsidiForCVPFullFileName(Constant.MODEL_W2_WRESL_LOOKUP_DIR + "\\WSIDI\\" + Constant.CVP_START_FILENAME
				        + Constant.UNDER_SCORE + lookup + Constant.TABLE_EXT);
				// Change WSI/DI Status Label
				JLabel jLabel = (JLabel) swingEngine.find("op_WSIDI_Status");
				jLabel.setText(labelValues.get(1) + Constant.UNEDITED_FORLABEL);
			}
		} catch (NullPointerException ex) {
			errorHandlingSvc.businessErrorHandler((JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME),
			        new CalLiteGUIException("The data for geting the table name is wrong", ex));
		}
	}

	/**
	 * This method will handle all the functionality of "Regulations" tab.
	 *
	 * @param itemName
	 *            The item name which is selected.
	 * @param isSelected
	 *            weather the item is selected or not.
	 * @throws CalLiteGUIException
	 * @throws CloneNotSupportedException
	 */
	private void regulations(String itemName, boolean isSelected) throws CalLiteGUIException, CloneNotSupportedException {
		List<SeedDataBO> seedDataList = seedDataSvc.getRegulationsTabData();
		int[] regFlags = resultSvc.getRegulationoptions();
		String tableName = "";
		String optionName = "";
		Component scrRegValues = (this.swingEngine.find("scrRegValues"));
		boolean toDisplayMessage = true;
		try {
			// if (isSelected) {
			if (isSelected && itemName.equals(Constant.QUICK_SELECT_RB_D1485)) {
				for (SeedDataBO seedDataBO : seedDataList) {
					// if (seedDataBO.getD1485().equals(Constant.N_A)) {
					regFlags[Integer.parseInt(seedDataBO.getRegID())] = 3;
					// } else if (seedDataBO.getD1641().equals(Constant.N_A)) {
					// regFlags[Integer.parseInt(seedDataBO.getRegID())] = 1;
					// } else if (seedDataBO.getUserDefined().equals(Constant.N_A)) {
					// regFlags[Integer.parseInt(seedDataBO.getRegID())] = 4;
					// }
					if (!seedDataBO.getDataTables().equals(Constant.N_A)) {
						resultSvc.removeUserDefinedTable(seedDataBO.getDataTables());
					}
				}
			} else if (isSelected && itemName.equals(Constant.QUICK_SELECT_RB_D1641)
			        || itemName.equals(Constant.QUICK_SELECT_RB_D1641_BO)) {
				for (SeedDataBO seedDataBO : seedDataList) {
					// if (seedDataBO.getD1641().equals(Constant.N_A)) {
					regFlags[Integer.parseInt(seedDataBO.getRegID())] = 1;
					// } else if (seedDataBO.getD1485().equals(Constant.N_A)) {
					// regFlags[Integer.parseInt(seedDataBO.getRegID())] = 3;
					// } else if (seedDataBO.getUserDefined().equals(Constant.N_A)) {
					// regFlags[Integer.parseInt(seedDataBO.getRegID())] = 4;
					// }
					if (!seedDataBO.getDataTables().equals(Constant.N_A)) {
						resultSvc.removeUserDefinedTable(seedDataBO.getDataTables());
					}
				}
			} else if (itemName.startsWith("ckbReg")) {
				SeedDataBO seedDataBO = seedDataSvc.getObjByGuiId(itemName);
				makeRBVisible(seedDataBO);
				String panelId = dynamicControlSvc.getTriggerBOById(itemName).getAffectdeGuiId();
				String guiTableName = getTableNameFromTheConponent(swingEngine.find(panelId));
				((TitledBorder) ((JPanel) this.swingEngine.find(panelId)).getBorder())
				        .setTitle(((JCheckBox) this.swingEngine.find(itemName)).getText());
				if (!isSelected) {
					((TitledBorder) ((JPanel) this.swingEngine.find(panelId)).getBorder())
					        .setTitle(((JCheckBox) this.swingEngine.find(itemName)).getText() + " (not selected)");
				}
				((JPanel) this.swingEngine.find(panelId)).repaint();
				if (isSelected) {
					int regId = Integer.parseInt(seedDataBO.getRegID());
					if (regFlags[regId] == 1) {
						if (seedDataBO.getD1641().equals(Constant.N_A)) {
							((JRadioButton) swingEngine.find(Constant.PANEL_RB_D1641)).setSelected(true);
							optionName = Constant.D1641;
						} else if (seedDataBO.getD1485().equals(Constant.N_A)) {
							((JRadioButton) swingEngine.find(Constant.PANEL_RB_D1485)).setSelected(true);
							optionName = Constant.D1485;
						} else if (seedDataBO.getUserDefined().equals(Constant.N_A)) {
							regFlags[regId] = 2;
							optionName = Constant.USER_DEFINED;
						}
					} else if (regFlags[regId] == 3) {
						if (seedDataBO.getD1485().equals(Constant.N_A)) {
							((JRadioButton) swingEngine.find(Constant.PANEL_RB_D1485)).setSelected(true);
							optionName = Constant.D1485;
						} else if (seedDataBO.getD1641().equals(Constant.N_A)) {
							((JRadioButton) swingEngine.find(Constant.PANEL_RB_D1641)).setSelected(true);
							optionName = Constant.D1641;
						} else if (seedDataBO.getUserDefined().equals(Constant.N_A)) {
							regFlags[regId] = 2;
							optionName = Constant.USER_DEFINED;
						}
					} else if (regFlags[regId] == 2) {
						((JRadioButton) swingEngine.find(Constant.PANEL_RB_USER_DEFIND)).setSelected(true);
						optionName = Constant.USER_DEFINED;
					}
					if (!seedDataBO.getDataTables().equals(Constant.N_A)) {
						tableName = seedDataBO.getDataTables();
						scrRegValues.setVisible(true);
						toDisplayMessage = loadTableToUI((JTable) this.swingEngine.find(guiTableName), tableName, regFlags[regId],
						        seedDataBO, optionName);
					} else {
						String valueToDisplay = "Access regulation table by selecting or right-clicking on item at left";
						if (itemName.equals("ckbReg_VAMP")) {
							valueToDisplay = "If D1485 is selected, take VAMP D1641 hydrology with a D1485 run.";
						}
						changeTheLabel(valueToDisplay);
					}
				}
			} else if (isSelected && itemName.startsWith("btnReg")) {
				JRadioButton radioButton = ((JRadioButton) this.swingEngine.find(itemName));
				TitledBorder titledBorder = (TitledBorder) ((JPanel) radioButton.getParent()).getBorder();
				SeedDataBO seedData = seedDataSvc.getObjByGuiId(xmlParsingSvc.getcompIdfromName(titledBorder.getTitle()));
				String guiTableName = getTableNameFromTheConponent(radioButton.getParent());
				tableName = seedData.getDataTables();
				if (itemName.endsWith(Constant.D1641)) {
					optionName = Constant.D1641;
				} else if (itemName.endsWith(Constant.D1485)) {
					optionName = Constant.D1485;
				} else if (itemName.endsWith(Constant.USER_DEFINED)) {
					optionName = Constant.USER_DEFINED;
				}
				int regId = Integer.parseInt(seedData.getRegID());
				if (!tableName.equals(Constant.N_A)) {
					scrRegValues.setVisible(true);
					toDisplayMessage = loadTableToUI((JTable) this.swingEngine.find(guiTableName), tableName, regFlags[regId],
					        seedData, optionName);
				} else {
					String valueToDisplay = "Access regulation table by selecting or right-clicking on item at left";
					if (seedData.getGuiId().equals("ckbReg_VAMP")) {
						valueToDisplay = "If D1485 is selected, take VAMP D1641 hydrology with a D1485 run.";
					}
					changeTheLabel(valueToDisplay);
				}
				/*
				 * setting the regFlag cann't be done in the above if else statement. Please see the getTable method in this class.
				 */
				if (itemName.endsWith(Constant.D1641)) {
					regFlags[regId] = 1;
				} else if (itemName.endsWith(Constant.D1485)) {
					regFlags[regId] = 3;
				} else if (itemName.endsWith(Constant.USER_DEFINED)) {
					regFlags[regId] = 2;
				}
			}
			// }
		} catch (NullPointerException ex) {
			errorHandlingSvc.businessErrorHandler((JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME), new CalLiteGUIException(
			        "The control id " + itemName + " don't have the proper data in the TriggerForDynamicDisplay File", ex));
		}
		if (toDisplayMessage)
			scrRegValues.setVisible(false);
	}

	private boolean loadTableToUI(JTable table, String tableName, int regValue, SeedDataBO seedDataBO, String optionName)
	        throws CalLiteGUIException, CloneNotSupportedException {
		DataTableModle dtm = getTable(tableName, regValue, seedDataBO, optionName);
		if (dtm == null) {
			changeTheLabel("The table is not available. The table name is " + tableName);
			return true;
		}
		table.setModel(dtm);
		table.setCellSelectionEnabled(true);
		DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) table.getDefaultRenderer(Object.class);
		renderer.setHorizontalAlignment(JLabel.RIGHT);
		this.swingEngine.find(Constant.MAIN_FRAME_NAME).repaint();
		return false;
	}

	private void changeTheLabel(String label) {
		JLabel lab = (JLabel) swingEngine.find("labReg");
		lab.setText(label);
	}

	/**
	 * This method is only for the "Regulations" tab tables.
	 *
	 * This will return the {@link DataTableModle} object based on the values passed in.
	 *
	 * @param tableName
	 *            just the table name
	 * @param regValue
	 *            The regId from gui_link2.csv file which tells us what is the option for user defined table.
	 * @param seedDataBO
	 *            seedData of the selected object.
	 * @param optionName
	 *            The shared option that is from the "Regulations" tab.
	 * @return
	 * @throws CalLiteGUIException
	 * @throws CloneNotSupportedException
	 */
	private DataTableModle getTable(String tableName, int regValue, SeedDataBO seedDataBO, String optionName)
	        throws CalLiteGUIException, CloneNotSupportedException {
		DataTableModle dataTableModle = null;
		switch (optionName) {
		case Constant.D1641:
			dataTableModle = desideTableNameAndGetTable(tableName, seedDataBO, Constant.D1641);
			if (resultSvc.hasUserDefinedTable(tableName))
				resultSvc.removeUserDefinedTable(tableName);
			break;
		case Constant.D1485:
			dataTableModle = desideTableNameAndGetTable(tableName, seedDataBO, Constant.D1485);
			if (resultSvc.hasUserDefinedTable(tableName))
				resultSvc.removeUserDefinedTable(tableName);
			break;
		case Constant.USER_DEFINED:
			if (resultSvc.hasUserDefinedTable(tableName)) {
				dataTableModle = resultSvc.getUserDefinedTable(tableName);
			} else {
				if (regValue == 1) {
					dataTableModle = desideTableNameAndGetTable(tableName, seedDataBO, Constant.D1641);
				} else if (regValue == 3) {
					dataTableModle = desideTableNameAndGetTable(tableName, seedDataBO, Constant.D1485);
				} else if (regValue == 2) {
					dataTableModle = desideTableNameAndGetTable(tableName, seedDataBO, Constant.USER_DEFINED);
				}
				if (dataTableModle != null) {
					dataTableModle = (DataTableModle) dataTableModle.clone();
					dataTableModle.setCellEditable(true);
					resultSvc.addUserDefinedTable(tableName, dataTableModle);
				}
			}
			break;
		}
		return dataTableModle;
	}

	/**
	 * This method is only for the "Regulations" tab tables.
	 *
	 * This method will deside the table name based on the seedDataBo and type passed in and will return the object of
	 * {@link DataTableModle}.
	 *
	 * @param tableName
	 *            just the table name
	 * @param seedDataBO
	 *            seedData of the selected object.
	 * @param type
	 *            The type from the "Regulations" tab.
	 * @return
	 * @throws CalLiteGUIException
	 */
	private DataTableModle desideTableNameAndGetTable(String tableName, SeedDataBO seedDataBO, String type)
	        throws CalLiteGUIException {
		DataTableModle dtm = null;
		switch (type) {
		case Constant.D1485:
			if (seedDataBO.getD1485().equalsIgnoreCase(Constant.N_A)) {
				dtm = tableSvc.getTable(tableName + Constant.DASH + Constant.D1485);
			} else if (seedDataBO.getD1641().equalsIgnoreCase(Constant.N_A)) {
				dtm = tableSvc.getTable(tableName + Constant.DASH + Constant.D1641);
			} else {
				dtm = tableSvc.getTable(tableName);
			}
			break;
		case Constant.D1641:
			if (seedDataBO.getD1641().equalsIgnoreCase(Constant.N_A)) {
				dtm = tableSvc.getTable(tableName + Constant.DASH + Constant.D1641);
			} else if (seedDataBO.getD1485().equalsIgnoreCase(Constant.N_A)) {
				dtm = tableSvc.getTable(tableName + Constant.DASH + Constant.D1485);
			} else {
				dtm = tableSvc.getTable(tableName);
			}
			break;
		case Constant.USER_DEFINED:
			dtm = tableSvc.getTable(tableName);
			break;
		}
		return dtm;
	}

	/**
	 * This method will return the table name from the component which is passed in.
	 *
	 * @param component
	 * @return
	 */
	private String getTableNameFromTheConponent(Component component) {
		if (component instanceof JTable) {
			return component.getName();
		}
		for (Component child : ((Container) component).getComponents()) {
			String value = getTableNameFromTheConponent(child);
			if (!value.equals(""))
				return value;
		}
		return "";
	}

	/**
	 * This method will make the shared radio buttons in the "Regulations" tab visible based on the seedData passed in.
	 *
	 * @param seedDataBO
	 */
	private void makeRBVisible(SeedDataBO seedDataBO) {
		swingEngine.find(Constant.PANEL_RB_D1485).setVisible(false);
		swingEngine.find(Constant.PANEL_RB_D1641).setVisible(false);
		swingEngine.find(Constant.PANEL_RB_USER_DEFIND).setVisible(false);
		if (seedDataBO.getD1485().equals(Constant.N_A)) {
			swingEngine.find(Constant.PANEL_RB_D1485).setVisible(true);
		}
		if (seedDataBO.getD1641().equals(Constant.N_A)) {
			swingEngine.find(Constant.PANEL_RB_D1641).setVisible(true);
		}
		if (seedDataBO.getUserDefined().equals(Constant.N_A)) {
			swingEngine.find(Constant.PANEL_RB_USER_DEFIND).setVisible(true);
		}
	}
}