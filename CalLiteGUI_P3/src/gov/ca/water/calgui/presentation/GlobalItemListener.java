package gov.ca.water.calgui.presentation;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.apache.log4j.Logger;
import org.swixml.SwingEngine;

import gov.ca.water.calgui.bus_delegate.IApplyDynamicConDele;
import gov.ca.water.calgui.bus_delegate.impl.ApplyDynamicConDeleImp;
import gov.ca.water.calgui.bus_service.IDynamicControlSvc;
import gov.ca.water.calgui.bus_service.IResultSvc;
import gov.ca.water.calgui.bus_service.impl.DynamicControlSvcImpl;
import gov.ca.water.calgui.bus_service.impl.ResultSvcImpl;
import gov.ca.water.calgui.bus_service.impl.XMLParsingSvcImpl;
import gov.ca.water.calgui.results.ResultUtils;
import gov.ca.water.calgui.tech_service.IAuditSvc;
import gov.ca.water.calgui.tech_service.impl.AuditSvcImpl;

/**
 * This class is for Listening all the item events(radio button, check box)
 * which are generated by the application.
 *
 * @author Mohan
 */
public class GlobalItemListener implements ItemListener {

	private static final Logger LOG = Logger.getLogger(GlobalItemListener.class.getName());
	private IApplyDynamicConDele applyDynamicConDele = new ApplyDynamicConDeleImp();
	private SwingEngine swingEngine = XMLParsingSvcImpl.getXMLParsingSvcImplInstance().getSwingEngine();
	private IDynamicControlSvc dynamicControlSvc = DynamicControlSvcImpl.getDynamicControlSvcImplInstance();
	private IAuditSvc auditSvc = AuditSvcImpl.getAuditSvcImplInstance();
	private IResultSvc resultSvc = ResultSvcImpl.getResultSvcImplInstance();
	private String oldValue = "";
	/*
	 * we use the rollBackFlag to avoid a cascade effect when we show a
	 * ConfirmDialog and user selects cancel
	 * 
	 */
	private boolean rollBackFlag = false;

	@Override
	public void itemStateChanged(ItemEvent ie) {
		if (resultSvc.isCLSFileLoading())
			return;
		if (dynamicControlSvc.isPreventRoeTrigger())
			return;
		String itemName = ((JComponent) ie.getItem()).getName();
		LOG.debug(itemName);

		// ----- Insert ReportListener handling
		if (itemName != null) {
			if (itemName.startsWith("Repckb")) {
				// Checkbox in Reporting page changed
				// if (itemName.startsWith("RepckbExceedancePlot") ||
				// itemName.startsWith("RepckbBAWPlot")) {
				// // Month controls should be turned on if *either* exceedance
				// // or B&W plots are asked for;
				// JPanel controls2 = (JPanel) swingEngine.find("controls2");
				// ResultUtils.getXMLParsingSvcImplInstance(null).toggleEnComponentAndChildren(controls2,
				// (ie.getStateChange() == ItemEvent.SELECTED)
				// || ((JCheckBox)
				// swingEngine.find("RepckbBAWPlot")).isSelected()
				// || ((JCheckBox)
				// swingEngine.find("RepckbExceedancePlot")).isSelected());
				// } else
				if (itemName.startsWith("RepckbSummaryTable")) {
					JPanel controls3 = (JPanel) swingEngine.find("controls3");
					ResultUtils.getXMLParsingSvcImplInstance(null).toggleEnComponentAndChildren(controls3,
							ie.getStateChange() == ItemEvent.SELECTED);
				}
			}
		}

		// End ReportListener handling -----

		boolean isSelected = ie.getStateChange() == ItemEvent.SELECTED;
		boolean isEnabled = ((JComponent) ie.getItem()).isEnabled();
		boolean optionFromTheBox = false;
		if (!rollBackFlag) {
			/*
			 * The following code is used for the special case where we show a
			 * popup box for some controls in the "Run Setting" and
			 * "Hydroclimate" tabs.
			 * 
			 * Extended by tad 20160206 to handle special warning that a change
			 * to the climate projection period will change a Regulations SJR
			 * setting
			 * 
			 */

			int option = JOptionPane.OK_OPTION;
			List<String> controlIdForExtendedDialogBox = Arrays.asList("hyd_rdb2005", "hyd_rdb2030", "hyd_rdbCCEL",
					"hyd_rdbCCLL");

			if (controlIdForExtendedDialogBox.contains(itemName)) {
				if (!isSelected) {
					oldValue = itemName;
				} else {
					JRadioButton regrdb = (JRadioButton) swingEngine.find("rdbRegQS_UD");
					if (!regrdb.isSelected()) {
						String confirmText = "";
						if (itemName.equals("hyd_rdb2005")
								&& !((JRadioButton) swingEngine.find("SJR_interim")).isSelected()) {
							confirmText = "Changing to " + ((JRadioButton) ie.getItem()).getText()
									+ " will also set the San Joaquin River restoration flow selection to 'interim' in the Regulations dashboard.";
						} else if (!itemName.equals("hyd_rdb2005")
								&& !((JRadioButton) swingEngine.find("SJR_full")).isSelected()) {
							confirmText = "Changing to " + ((JRadioButton) ie.getItem()).getText()
									+ " will also set the San Joaquin River restoration flow selection to 'full' in the Regulations dashboard.";
						}
						if (!confirmText.equals("")) {
							option = JOptionPane.showConfirmDialog(null, confirmText, "", JOptionPane.OK_CANCEL_OPTION);
							if (option == JOptionPane.CANCEL_OPTION) {
								rollBackFlag = true;
								((JRadioButton) swingEngine.find(oldValue)).setSelected(true);
								rollBackFlag = false;
								return;
							}
						}
					}
				}
			}

			List<String> controlIdForDialogBox;
			if (!oldValue.equals("hyd_rdbCCEL") && !oldValue.equals("hyd_rdbCCLL"))
				controlIdForDialogBox = Arrays.asList("run_rdbD1485", "run_rdbD1641", "run_rdbBO", "hyd_rdb2005",
						"hyd_rdb2030", "hyd_rdbCCLL", "hyd_rdbCCEL");
			else
				controlIdForDialogBox = Arrays.asList("run_rdbD1485", "run_rdbD1641", "run_rdbBO", "hyd_rdb2005",
						"hyd_rdb2030");

			List<String> controlIdOfCPP = Arrays.asList("hyd_rdbCCEL", "hyd_rdbCCLL", "hyd_ckb1", "hyd_ckb2",
					"hyd_ckb3", "hyd_ckb4", "hyd_ckb5");
			List<String> controlIdForOldValue = Arrays.asList("run_rdbD1485", "run_rdbD1641", "run_rdbBO",
					"hyd_rdb2005", "hyd_rdb2030", "hyd_rdbCCEL", "hyd_rdbCCLL");
			if (controlIdForOldValue.contains(itemName)) {
				if (!isSelected)
					oldValue = itemName;
			}
			if (controlIdForDialogBox.contains(itemName)) {
				if (isSelected) {
					option = JOptionPane.showConfirmDialog(null,
							"You have selected " + ((JRadioButton) ie.getItem()).getText()
									+ ".\n  Do you wish to use the WSI/DI curves for this configuration?");
					if (option == JOptionPane.CANCEL_OPTION) {
						rollBackFlag = true;
						((JRadioButton) swingEngine.find(oldValue)).setSelected(true);
						rollBackFlag = false;
						return;
					} else if (option == JOptionPane.YES_OPTION) {
						optionFromTheBox = true;
					}
				}
			} else if (controlIdOfCPP.contains(itemName)) {
				if (isSelected)
					optionFromTheBox = true;
			}
		}
		if (isEnabled)

		{
			applyDynamicConDele.applyDynamicControl(itemName, isSelected, isEnabled, optionFromTheBox);
		}
		auditSvc.addAudit(itemName, String.valueOf(!isSelected), String.valueOf(isSelected));
	}
}