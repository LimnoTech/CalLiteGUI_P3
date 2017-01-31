package gov.ca.water.calgui.presentation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.swixml.SwingEngine;

import gov.ca.water.calgui.bo.DataTableModel;
import gov.ca.water.calgui.bus_delegate.IAllButtonsDele;
import gov.ca.water.calgui.bus_delegate.IApplyDynamicConDele;
import gov.ca.water.calgui.bus_delegate.IScenarioDele;
import gov.ca.water.calgui.bus_delegate.IVerifyControlsDele;
import gov.ca.water.calgui.bus_delegate.impl.AllButtonsDeleImp;
import gov.ca.water.calgui.bus_delegate.impl.ApplyDynamicConDeleImp;
import gov.ca.water.calgui.bus_delegate.impl.ScenarioDeleImp;
import gov.ca.water.calgui.bus_delegate.impl.VerifyControlsDeleImp;
import gov.ca.water.calgui.bus_service.IBatchRunSvc;
import gov.ca.water.calgui.bus_service.IResultSvc;
import gov.ca.water.calgui.bus_service.ISeedDataSvc;
import gov.ca.water.calgui.bus_service.impl.BatchRunSvcImpl;
import gov.ca.water.calgui.bus_service.impl.ResultSvcImpl;
import gov.ca.water.calgui.bus_service.impl.SeedDataSvcImpl;
import gov.ca.water.calgui.bus_service.impl.XMLParsingSvcImpl;
import gov.ca.water.calgui.constant.Constant;
import gov.ca.water.calgui.tech_service.IAuditSvc;
import gov.ca.water.calgui.tech_service.IErrorHandlingSvc;
import gov.ca.water.calgui.tech_service.impl.AuditSvcImpl;
import gov.ca.water.calgui.tech_service.impl.ErrorHandlingSvcImpl;

/**
 * This class is for Listening all the action events(Button click) which are generated by the application.
 *
 * @author mohan
 *
 */
public class GlobalActionListener implements ActionListener {
	private static final Logger LOG = Logger.getLogger(GlobalActionListener.class.getName());
	private IScenarioDele scenarioDele = new ScenarioDeleImp();
	private IAllButtonsDele allButtonsDele = new AllButtonsDeleImp();
	private IResultSvc resultSvc = ResultSvcImpl.getResultSvcImplInstance();
	private IAuditSvc auditSvc = AuditSvcImpl.getAuditSvcImplInstance();
	private IBatchRunSvc batchRunSvc = new BatchRunSvcImpl();
	private SwingEngine swingEngine = XMLParsingSvcImpl.getXMLParsingSvcImplInstance().getSwingEngine();
	private ISeedDataSvc seedDataSvc = SeedDataSvcImpl.getSeedDataSvcImplInstance();
	private IVerifyControlsDele verifyControlsDele = new VerifyControlsDeleImp();
	private IErrorHandlingSvc errorHandlingSvc = new ErrorHandlingSvcImpl();
	private IApplyDynamicConDele applyDynamicConDele = new ApplyDynamicConDeleImp();

	@Override
	public void actionPerformed(ActionEvent ae) {
		JTable table = null;
		switch (ae.getActionCommand()) {
		case "AC_SaveScen":
			this.allButtonsDele.saveCurrentStateToFile();
			break;
		case "AC_SaveScenAs":
			this.allButtonsDele.saveAsButton();
			break;
		case "AC_ViewScen":
			loadViewScen();
			break;
		case "AC_LoadScen":
			JFileChooser fChooser = new JFileChooser(Constant.SCENARIOS_DIR);
			fChooser.setMultiSelectionEnabled(false);
			FileNameExtensionFilter filter = new FileNameExtensionFilter("CLS FILES (.cls)", "cls");
			fChooser.setFileFilter(filter);
			int val = fChooser.showOpenDialog(swingEngine.find(Constant.MAIN_FRAME_NAME));
			if (val == JFileChooser.APPROVE_OPTION && this.allButtonsDele.verifyTheSelectedFiles(fChooser, Constant.CLS_EXT)) {
				String fileName = fChooser.getSelectedFile().getName();
				loadScenarioButton(fileName);
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
			this.allButtonsDele.selectingSVAndInitFile("hyd_DSS_SV", "hyd_DSS_SV_F", "txf_Manual_SV", "txf_Manual_SV_F");
			break;
		case "AC_Select_DSS_Init":
			this.allButtonsDele.selectingSVAndInitFile("hyd_DSS_Init", "hyd_DSS_Init_F", "txf_Manual_Init", "txf_Manual_Init_F");
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
				        "You can't paste untel you select user defined.", (JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME));
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
		}
	}

	/**
	 * This method is used for the "load Scenario" button on the "Run Settings" tab.
	 *
	 * @param fileName
	 *            The Name of the file.
	 */
	public void loadScenarioButton(String fileName) {
		LOG.debug("loading this cls file " + fileName);
		fileName = FilenameUtils.removeExtension(fileName);
		this.verifyControlsDele.verifyTheDataBeforeUI(Constant.SCENARIOS_DIR + fileName + Constant.CLS_EXT);
		this.resultSvc.applyClsFile(Constant.SCENARIOS_DIR + fileName + Constant.CLS_EXT, swingEngine, seedDataSvc.getTableIdMap());
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
	 * This method is used to display the "View Scenario Settings" button on the "Run Settings" tab.
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
		ProgressFrame progressFrame = ProgressFrame.getProgressFrameInstance();
		if (decisionBeforeTheBatchRun()) {
			List<String> fileName = Arrays.asList(clsFileName);
			progressFrame.addScenarioNamesAndAction(clsFileName, Constant.BATCH_RUN);
			progressFrame.makeDialogVisible();
			batchRunSvc.doBatch(fileName, swingEngine, false);
		}
	}

	/**
	 * This method is used to run batch program for wsidi.
	 */
	public void runSingleBatchForWsiDi() {
		String clsFileName = ((JTextField) swingEngine.find("run_txfScen")).getText();
		clsFileName = FilenameUtils.removeExtension(clsFileName);
		ProgressFrame progressFrame = ProgressFrame.getProgressFrameInstance();
		if (decisionBeforeTheBatchRun()) {
			List<String> fileName = Arrays.asList(clsFileName);
			progressFrame.addScenarioNamesAndAction(clsFileName, Constant.BATCH_RUN_WSIDI);
			progressFrame.makeDialogVisible();
			batchRunSvc.doBatch(fileName, swingEngine, true);
		}
	}

	/**
	 * This will ask the user whether to save and run the batch or run without saving or Cancel.
	 *
	 * @return
	 */
	public boolean decisionBeforeTheBatchRun() {
		String clsFileName = FilenameUtils.removeExtension(((JTextField) swingEngine.find("run_txfScen")).getText());
		if (!Files.isExecutable(Paths.get(Constant.RUN_DETAILS_DIR + clsFileName))) {
			ImageIcon icon = new ImageIcon(getClass().getResource("/images/CalLiteIcon.png"));
			Object[] options = { "Yes", "No" };
			JOptionPane optionPane = new JOptionPane(
			        "The cls file is new, do not have corresponding directory structure.\nThe batch will not run with out it.\nDo you want to save to create that directory?",
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
}
