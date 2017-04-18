package gov.ca.water.calgui.presentation;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.swixml.SwingEngine;

import calsim.app.Project;
import calsim.gui.DtsTreeModel;
import calsim.gui.DtsTreePanel;
import calsim.gui.GuiUtils;
import gov.ca.water.calgui.bo.DataTableModel;
import gov.ca.water.calgui.bo.ResultUtilsBO;
import gov.ca.water.calgui.bus_delegate.IAllButtonsDele;
import gov.ca.water.calgui.bus_delegate.IApplyDynamicConDele;
import gov.ca.water.calgui.bus_delegate.IScenarioDele;
import gov.ca.water.calgui.bus_delegate.IVerifyControlsDele;
import gov.ca.water.calgui.bus_delegate.impl.AllButtonsDeleImp;
import gov.ca.water.calgui.bus_delegate.impl.ApplyDynamicConDeleImp;
import gov.ca.water.calgui.bus_delegate.impl.ScenarioDeleImp;
import gov.ca.water.calgui.bus_delegate.impl.VerifyControlsDeleImp;
import gov.ca.water.calgui.bus_service.IModelRunSvc;
import gov.ca.water.calgui.bus_service.IScenarioSvc;
import gov.ca.water.calgui.bus_service.ISeedDataSvc;
import gov.ca.water.calgui.bus_service.impl.ModelRunSvcImpl;
import gov.ca.water.calgui.bus_service.impl.ScenarioSvcImpl;
import gov.ca.water.calgui.bus_service.impl.SeedDataSvcImpl;
import gov.ca.water.calgui.bus_service.impl.XMLParsingSvcImpl;
import gov.ca.water.calgui.constant.Constant;
import gov.ca.water.calgui.tech_service.IAuditSvc;
import gov.ca.water.calgui.tech_service.IErrorHandlingSvc;
import gov.ca.water.calgui.tech_service.impl.AuditSvcImpl;
import gov.ca.water.calgui.tech_service.impl.ErrorHandlingSvcImpl;

/**
 * This class is for Listening all the action events(Button click) which are
 * generated by the application.
 *
 * @author Mohan
 */
public class GlobalActionListener implements ActionListener {
	private static final Logger LOG = Logger.getLogger(GlobalActionListener.class.getName());
	private IScenarioDele scenarioDele = new ScenarioDeleImp();
	private IAllButtonsDele allButtonsDele = new AllButtonsDeleImp();
	private IScenarioSvc scenarioSvc = ScenarioSvcImpl.getScenarioSvcImplInstance();
	private IAuditSvc auditSvc = AuditSvcImpl.getAuditSvcImplInstance();
	private IModelRunSvc modelRunSvc = new ModelRunSvcImpl();
	private SwingEngine swingEngine = XMLParsingSvcImpl.getXMLParsingSvcImplInstance().getSwingEngine();
	private ISeedDataSvc seedDataSvc = SeedDataSvcImpl.getSeedDataSvcImplInstance();
	private IVerifyControlsDele verifyControlsDele = new VerifyControlsDeleImp();
	private IErrorHandlingSvc errorHandlingSvc = new ErrorHandlingSvcImpl();
	private IApplyDynamicConDele applyDynamicConDele = new ApplyDynamicConDeleImp();
	private JList<String> lstScenarios = null;
	private JList<String> lstReports = null;
	static DtsTreeModel dtm;

	@SuppressWarnings("unchecked")
	@Override
	public void actionPerformed(ActionEvent ae) {
		lstReports = (JList<String>) swingEngine.find("lstReports");
		lstScenarios = (JList<String>) swingEngine.find("SelectedList");
		JTable table = null;
		switch (ae.getActionCommand()) {

		case "AC_Power":

			PowerFrame pf = new PowerFrame(lstScenarios);
			break;

		case "AC_SaveScen":
			if (FilenameUtils.removeExtension(((JTextField) swingEngine.find("run_txfScen")).getText()).toUpperCase()
					.equals("DEFAULT") && allButtonsDele.defaultCLSIsProtected()) {
				JOptionPane.showMessageDialog(null,
						"The CalLite GUI is not allowed to overwrite DEFAULT.CLS. Please use Save As to save to a different scenario file.");
			} else
				this.allButtonsDele.saveCurrentStateToFile();
			break;
		case "AC_SaveScenAs":
			this.allButtonsDele.saveAsButton();
			break;
		case "AC_ViewScen":
			loadViewScen();
			break;
		case "AC_LoadScen":

			boolean doLoad = true; // Check for changed scenario prior to load -
									// tad 20170202
			if (auditSvc.hasValues()) {
				String clsFileName = FilenameUtils
						.removeExtension(((JTextField) swingEngine.find("run_txfScen")).getText());
				int option = JOptionPane.showConfirmDialog(null, "Scenario selections have changed for " + clsFileName
						+ ". Would you like to save the changes?");
				switch (option) {
				case JOptionPane.CANCEL_OPTION:

					doLoad = false;
					break;

				case JOptionPane.YES_OPTION:

					doLoad = this.allButtonsDele.saveCurrentStateToFile(clsFileName);
					break;

				case JOptionPane.NO_OPTION:

					doLoad = true;
					break;
				}
			}
			if (doLoad) {
				JFileChooser fChooser = new JFileChooser(Constant.SCENARIOS_DIR);
				fChooser.setMultiSelectionEnabled(false);
				FileNameExtensionFilter filter = new FileNameExtensionFilter("CLS FILES (.cls)", "cls");
				fChooser.setFileFilter(filter);
				int val = fChooser.showOpenDialog(swingEngine.find(Constant.MAIN_FRAME_NAME));
				if (val == JFileChooser.APPROVE_OPTION
						&& this.allButtonsDele.verifyTheSelectedFiles(fChooser, Constant.CLS_EXT)) {
					String fileName = fChooser.getSelectedFile().getName();
					loadScenarioButton(fileName);
				}
			}
			break;
		case "AC_Help":
			this.allButtonsDele.helpButton();
			break;
		case "AC_RUN":
			runSingleBatch();
			break;
		case "AC_BATCH":
			this.allButtonsDele.runMultipleBatch();
			break;
		case "AC_About":
			this.allButtonsDele.aboutButton();
			break;
		case "AC_Exit":
			this.allButtonsDele.windowClosing();
			break;
		case "AC_Select_DSS_SV":
			this.allButtonsDele.selectingSVAndInitFile("hyd_DSS_SV", "hyd_DSS_SV_F", "txf_Manual_SV",
					"txf_Manual_SV_F");
			break;
		case "AC_Select_DSS_Init":
			this.allButtonsDele.selectingSVAndInitFile("hyd_DSS_Init", "hyd_DSS_Init_F", "txf_Manual_Init",
					"txf_Manual_Init_F");
			break;
		case "Fac_TableEdit":
			TitledBorder title = null;
			if (ae.getSource() instanceof JButton) {
				JButton btn = (JButton) ae.getSource();
				String titlestr = btn.getText();
				title = BorderFactory.createTitledBorder(titlestr);
			}
			JPanel pan = (JPanel) swingEngine.find("fac_pan6");
			pan.setBorder(title);
			break;
		case "Reg_Copy":
			table = (JTable) swingEngine.find("tblRegValues");
			this.allButtonsDele.copyTableValues(table);
			break;
		case "Reg_Paste":
			table = (JTable) swingEngine.find("tblRegValues");
			JRadioButton userDefined = (JRadioButton) swingEngine.find("btnRegUD");
			if (userDefined.isSelected()) {
				this.allButtonsDele.pasteTableValues(table);
			} else {
				errorHandlingSvc.validationeErrorHandler("You can't paste untel you select user defined.",
						"You can't paste untel you select user defined.",
						(JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME));
			}
			break;
		case "Op_TableEdit":
			this.allButtonsDele.editButtonOnOperations((JComponent) ae.getSource());
			break;
		case "Op_Generate":

			runSingleBatchForWsiDi();
			break;
		case "Op_Read":
			this.allButtonsDele.readButtonInOperations();
			break;
		case "Op_Default":
			this.allButtonsDele.defaultButtonOnOperations();
			break;
		case "Op_Copy":
			table = (JTable) swingEngine.find("tblOpValues");
			this.allButtonsDele.copyTableValues(table);
			break;
		case "Op_Paste":
			table = (JTable) swingEngine.find("tblOpValues");
			this.allButtonsDele.pasteTableValues(table);
			break;

		// From Custom Results dashboard

		case "AC_Controls":
			ControlFrame cf = ResultUtilsBO.getResultUtilsInstance(null).getControlFrame();
			cf.display();
			if (cf.getExtendedState() == JFrame.ICONIFIED)
				cf.setExtendedState(JFrame.NORMAL);
			break;

		case "CR_LoadList":
			ResultUtilsBO.getResultUtilsInstance(null).readCGR();
			break;

		case "CR_SaveList":
			ResultUtilsBO.getResultUtilsInstance(null).writeCGR();
			break;

		case "CR_ClearTree":
			Project p = ResultUtilsBO.getResultUtilsInstance(null).getProject();
			p.clearMTSList();
			p.clearDTSList();
			DtsTreePanel dtp = GuiUtils.getCLGPanel().getDtsTreePanel();
			DtsTreeModel dtm = dtp.getCurrentModel();
			dtm.clearVectors();
			dtm.createTreeFromPrj(null, null, "");
			GuiUtils.getCLGPanel().repaint();
			break;

		// From Quick Results and External PDF

		case "AC_PresetClear":
			clearQRCheckBoxes("presets");
			break;
		case "AC_ShortageClear":
			clearQRCheckBoxes("shortage");
			break;
		case "AC_SJRClear":
			clearQRCheckBoxes("SJR Results");
			break;
		case "AC_WMAClear":
			clearQRCheckBoxes("WMA");
			break;
		case "AC_DShortClear":
			clearQRCheckBoxes("DShort");
			break;
		case "AC_DfcClear":
			clearQRCheckBoxes("delta_flow_criteria");
			break;
		case "AC_GenReport":
			generateReport();
			break;
		case "Rep_All":
			setQRMonthCheckBoxesSelected(true);
			break;
		case "Rep_ClearMonths":
			setQRMonthCheckBoxesSelected(false);
			break;
		case "Rep_AddList":
			addToQRReportList();
			break;
		case "Rep_ClearList":
			lstReports.setListData(new String[0]);
			break;
		case "Rep_LoadList":
			ResultUtilsBO.getResultUtilsInstance(null).readCGR();
			break;
		case "Rep_SaveList":
			ResultUtilsBO.getResultUtilsInstance(null).writeCGR();
			break;
		case "Rep_DispAll":
			if (lstScenarios.getModel().getSize() == 0) {
				JOptionPane.showMessageDialog(null, "No scenarios loaded", "Error", JOptionPane.ERROR_MESSAGE);
			} else {
				for (int i = 0; i < lstReports.getModel().getSize(); i++)
					DisplayFrame.showDisplayFrames((String) (lstReports.getModel().getElementAt(i)), lstScenarios);
			}
			break;
		case "Rep_DispCur":
			if (lstScenarios.getModel().getSize() == 0) {
				JOptionPane.showMessageDialog(null, "No scenarios loaded", "Error", JOptionPane.ERROR_MESSAGE);
			} else if (lstReports.getSelectedValue() == null) {
				JOptionPane.showMessageDialog(null, "No display group selected", "Error", JOptionPane.ERROR_MESSAGE);
			} else {

				DisplayFrame.showDisplayFrames((String) ((JList) swingEngine.find("lstReports")).getSelectedValue(),
						lstScenarios);
			}
			break;
		case "Time_SELECT":

			break;
		case "AC_CompScen":
			IScenarioDele scenarioDele = new ScenarioDeleImp();
			boolean proceed = this.allButtonsDele.saveForViewScen();
			List<String> fileNames = new ArrayList<>();
			for (int i = 0; i < ((DefaultListModel) lstScenarios.getModel()).getSize(); i++) {
				String name = Paths.get(((DefaultListModel) lstScenarios.getModel()).getElementAt(i).toString())
						.getFileName().toString();
				fileNames.add(name.substring(0, name.length() - 7) + Constant.CLS_EXT);
			}
			if (proceed) {
				List<DataTableModel> dtmList = scenarioDele.getScenarioTableData(fileNames);
				ScenarioFrame scenarioFrame = new ScenarioFrame(dtmList);
				scenarioFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				scenarioFrame.setVisible(true);
				try {
					Files.delete(Paths.get(Constant.SCENARIOS_DIR + Constant.CURRENT_SCENARIO + Constant.CLS_EXT));
				} catch (IOException ex) {
					LOG.debug(ex);
				}
			}
			break;
		}
	}

	/**
	 * This method is used for the "load Scenario" button on the "Run Settings"
	 * tab.
	 *
	 * @param fileName
	 *            The Name of the file.
	 */
	public void loadScenarioButton(String fileName) {
		LOG.debug("loading this cls file " + fileName);
		fileName = FilenameUtils.removeExtension(fileName);
		this.verifyControlsDele.verifyTheDataBeforeUI(Constant.SCENARIOS_DIR + fileName + Constant.CLS_EXT);
		this.scenarioSvc.applyClsFile(Constant.SCENARIOS_DIR + fileName + Constant.CLS_EXT, swingEngine,
				seedDataSvc.getTableIdMap());
		((JTextField) swingEngine.find("run_txfScen")).setText(fileName + Constant.CLS_EXT);
		((JTextField) swingEngine.find("run_txfoDSS")).setText(fileName + Constant.DV_NAME + Constant.DSS_EXT);
		applyDynamicConDele.changeSVInitFilesAndTableInOperations(true);
		String[] c1 = new String[0];
		Object[][] data = new Object[0][0];
		((JTable) this.swingEngine.find("tblOpValues")).setModel(new DataTableModel("", c1, data, false));
		applyDynamicConDele.applyDynamicControlForListFromFile();
		allButtonsDele.decisionSVInitFilesAndTableInOperations();
		auditSvc.clearAudit();
	}

	/**
	 * This method is used to display the "View Scenario Settings" button on the
	 * "Run Settings" tab.
	 */
	public void loadViewScen() {
		boolean pro = this.allButtonsDele.saveForViewScen();
		if (pro) {
			List<DataTableModel> dtmList = scenarioDele.getScenarioTableData(null);
			ScenarioFrame scenarioFrame = new ScenarioFrame(dtmList);
			scenarioFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			scenarioFrame.setVisible(true);
			try {
				Files.delete(Paths.get(Constant.SCENARIOS_DIR + Constant.CURRENT_SCENARIO + Constant.CLS_EXT));
			} catch (IOException ex) {
				LOG.debug(ex);
			}
		}
	}

	/**
	 * This method is used to run single batch program.
	 */
	public void runSingleBatch() {
		String clsFileName = ((JTextField) swingEngine.find("run_txfScen")).getText();
		clsFileName = FilenameUtils.removeExtension(clsFileName);
		if (decisionBeforeTheBatchRun()) {
			ProgressFrame progressFrame = ProgressFrame.getProgressFrameInstance();
			List<String> fileName = Arrays.asList(clsFileName);
			progressFrame.addScenarioNamesAndAction(clsFileName, Constant.BATCH_RUN);
			progressFrame.setBtnText(Constant.STATUS_BTN_TEXT_STOP);
			progressFrame.makeDialogVisible();
			modelRunSvc.doBatch(fileName, swingEngine, false);
		}
	}

	/**
	 * This method is used to run batch program for wsidi.
	 */
	public void runSingleBatchForWsiDi() {
		String clsFileName = ((JTextField) swingEngine.find("run_txfScen")).getText();
		clsFileName = FilenameUtils.removeExtension(clsFileName);
		if (decisionBeforeTheBatchRun()) {
			ProgressFrame progressFrame = ProgressFrame.getProgressFrameInstance();
			List<String> fileName = Arrays.asList(clsFileName);
			progressFrame.addScenarioNamesAndAction(clsFileName, Constant.BATCH_RUN_WSIDI);
			progressFrame.setBtnText(Constant.STATUS_BTN_TEXT_STOP);
			progressFrame.makeDialogVisible();
			modelRunSvc.doBatch(fileName, swingEngine, true);
		}
	}

	/**
	 * This will ask the user whether to save and run the batch or run without
	 * saving or Cancel.
	 *
	 * @return Will return true if the user wants to run the batch program.
	 */
	public boolean decisionBeforeTheBatchRun() {
		String clsFileName = FilenameUtils.removeExtension(((JTextField) swingEngine.find("run_txfScen")).getText());
		if (clsFileName.toUpperCase().equals(Constant.DEFAULT)) {
			JOptionPane.showMessageDialog(null,
					"The CalLite GUI is not allowed to modify the default scenario 'DEFAULT.CLS'. Please use Save As to save to a different scenario file.");
			return false;
		}
		if (!Files.isExecutable(Paths.get(Constant.RUN_DETAILS_DIR + clsFileName))) {
			ImageIcon icon = new ImageIcon(getClass().getResource("/images/CalLiteIcon.png"));
			Object[] options = { "Yes", "No" };
			JOptionPane optionPane = new JOptionPane(
					"The cls file does not have a corresponding directory structure.\nThe batch will not run without this.\nDo you want to save to create that directory?",
					JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, options, options[0]);
			JDialog dialog = optionPane.createDialog("CalLite");
			dialog.setIconImage(icon.getImage());
			dialog.setResizable(false);
			dialog.setVisible(true);
			switch (optionPane.getValue().toString()) {
			case "Yes":
				return this.allButtonsDele.saveCurrentStateToFile(clsFileName);
			case "No":
				return false;
			}
		}
		boolean isSaved = false;
		if (auditSvc.hasValues()) {
			int option = JOptionPane.showConfirmDialog(null,
					"Scenario selections have changed. Would you like to save the changes?");
			switch (option) {
			case JOptionPane.YES_OPTION:
				isSaved = this.allButtonsDele.saveCurrentStateToFile(clsFileName);
				break;
			case JOptionPane.NO_OPTION:
				loadScenarioButton(((JTextField) swingEngine.find("run_txfScen")).getText());
				isSaved = true;
				break;
			}
		} else {
			isSaved = true;
		}
		return isSaved;
	}

	/**
	 * Adds items to list of reports on Quick Result dashboard. One item is
	 * added for each checked item on the children panels for "variables"
	 * 
	 */
	private void addToQRReportList() {
		// Store previous list items
		int size = lstReports.getModel().getSize(); // 4
		int n;
		n = 0;
		String[] lstArray = new String[size];
		for (int i = 0; i < size; i++) {
			Object item = lstReports.getModel().getElementAt(i);
			if (item.toString() != " ") {
				lstArray[n] = item.toString();
				n = n + 1;
			}
		}
		String[] lstArray1 = new String[n + 1];
		for (int i = 0; i < n; i++) {
			lstArray1[i] = lstArray[i];
		}
		// Add new items
		String cSTOR = ";Locs-";
		String cSTORIdx = ";Index-";
		Component[] components = ((JTabbedPane) swingEngine.find("variables")).getComponents();
		for (Component c : components) {
			if (c instanceof JPanel) {
				Component[] components2 = ((JPanel) c).getComponents();
				for (Component c2 : components2)
					if (c2 instanceof JCheckBox) {
						JCheckBox cb = (JCheckBox) c2;
						String cName = cb.getName();
						if (cName.startsWith("ckbp")) {
							if (cb.isSelected()) {
								cSTOR = cSTOR + cb.getText().trim() + ",";
								cSTORIdx = cSTORIdx + cName + ",";
							}
						}
						lstArray1[n] = DisplayFrame.quickState() + cSTOR + cSTORIdx;
						// String[] reportNamesEG = {cDate};
						lstReports.setListData(lstArray1);
					}
			}
		}
	}

	/**
	 * Selects/deselects all monthly checkboxes on Quick Result control panel
	 * 
	 * @param b
	 */
	private void setQRMonthCheckBoxesSelected(boolean b) {
		JPanel controls2 = (JPanel) swingEngine.find("controls2");
		Component[] components = controls2.getComponents();
		for (int i = 0; i < components.length; i++) {
			if (components[i] instanceof JCheckBox) {
				JCheckBox c = (JCheckBox) components[i];
				String cName = c.getName();
				if (cName != null) {
					if (cName.startsWith("RepchkMon")) {
						c.setSelected(b);
					}
				}
			}
		}
	}

	/**
	 * Clears Quick Result checkboxes that are on panel named "panelName"
	 * 
	 * @param panelName
	 */
	private void clearQRCheckBoxes(String panelName) {
		JPanel panel = (JPanel) swingEngine.find(panelName);
		Component[] components = panel.getComponents();
		for (int i = 0; i < components.length; i++) {
			if (components[i] instanceof JCheckBox) {
				JCheckBox c = (JCheckBox) components[i];
				c.setSelected(false);
			}
		}
	}

	/**
	 * Generates PDF report from "External PDF" dashboard - action "ACGenReport"
	 */
	private void generateReport() {
		if (((JTextField) swingEngine.find("tfReportFILE1")).getText().isEmpty()
				|| ((JTextField) swingEngine.find("tfReportFILE2")).getText().isEmpty()
				|| ((JTextField) swingEngine.find("tfReportFILE3")).getText().isEmpty()) {
			JOptionPane.showMessageDialog(null, "You must specify the source DSS files and the output PDF file",
					"Error", JOptionPane.ERROR_MESSAGE);
		} else {
			try {
				// Create an inputstream from template file;
				FileInputStream fin = new FileInputStream(
						((JTextField) swingEngine.find("tfTemplateFILE")).getToolTipText());
				BufferedReader br = new BufferedReader(new InputStreamReader(fin));
				// Open the template file
				String theText = br.readLine() + "\n";
				theText = theText + br.readLine() + "\n";
				theText = theText + br.readLine() + "\n";
				br.readLine();
				theText = theText + "FILE_BASE\t" + ((JTextField) swingEngine.find("tfReportFILE1")).getToolTipText()
						+ "\n";
				br.readLine();
				theText = theText + "NAME_BASE\t\"" + ((JTextField) swingEngine.find("tfReportNAME1")).getText()
						+ "\"\n";
				br.readLine();
				theText = theText + "FILE_ALT\t" + ((JTextField) swingEngine.find("tfReportFILE2")).getToolTipText()
						+ "\n";
				br.readLine();
				theText = theText + "NAME_ALT\t\"" + ((JTextField) swingEngine.find("tfReportNAME2")).getText()
						+ "\"\n";
				br.readLine();
				theText = theText + "OUTFILE\t" + ((JTextField) swingEngine.find("tfReportFILE3")).getToolTipText()
						+ "\n";
				br.readLine();
				theText = theText + "NOTE\t\"" + ((JTextArea) swingEngine.find("taReportNOTES")).getText() + "\"\n";
				br.readLine();
				theText = theText + "ASSUMPTIONS\t\"" + ((JTextArea) swingEngine.find("taReportASSUMPTIONS")).getText()
						+ "\"\n";
				br.readLine();
				theText = theText + "MODELER\t\"" + ((JTextField) swingEngine.find("tfReportMODELER")).getText()
						+ "\"\n";

				theText = theText + "TABLE_FONT_SIZE\t" + ((JTextField) swingEngine.find("tfFontSize")).getText()
						+ "\n";

				String aLine = br.readLine();
				while (aLine != null) {
					theText = theText + aLine + "\n";
					aLine = br.readLine();
				}
				br.close();
				theText = theText + "\n";
				ByteArrayInputStream bs = new ByteArrayInputStream(theText.getBytes());
				try {
					Report report = new Report(bs, ((JTextField) swingEngine.find("tfReportFILE3")).getToolTipText());
					report.execute();
				} catch (IOException e1) {
					LOG.debug(e1.getMessage()); // Not sure - should catch
												// thread problems like
												// already-open PDF?
				}
			} catch (IOException e1) {
				LOG.debug(e1.getMessage()); // Failure to open template file
											// (?)
			}
		}
	}
}