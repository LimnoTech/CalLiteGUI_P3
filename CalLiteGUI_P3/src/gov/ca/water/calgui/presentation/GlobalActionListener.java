package gov.ca.water.calgui.presentation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.swixml.SwingEngine;

import gov.ca.water.calgui.bo.DataTableModle;
import gov.ca.water.calgui.bus_delegate.IAllButtonsDele;
import gov.ca.water.calgui.bus_delegate.IScenarioDele;
import gov.ca.water.calgui.bus_delegate.IVerifyControlsDele;
import gov.ca.water.calgui.bus_delegate.impl.AllButtonsDeleImp;
import gov.ca.water.calgui.bus_delegate.impl.ScenarioDeleImp;
import gov.ca.water.calgui.bus_delegate.impl.VerifyControlsDeleImp;
import gov.ca.water.calgui.bus_service.IResultSvc;
import gov.ca.water.calgui.bus_service.ISeedDataSvc;
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
	private SwingEngine swingEngine = XMLParsingSvcImpl.getXMLParsingSvcImplInstance().getSwingEngine();
	private ISeedDataSvc seedDataSvc = SeedDataSvcImpl.getSeedDataSvcImplInstance();
	private IVerifyControlsDele verifyControlsDele = new VerifyControlsDeleImp();
	private IErrorHandlingSvc errorHandlingSvc = new ErrorHandlingSvcImpl();

	@Override
	public void actionPerformed(ActionEvent ae) {
		// LOG.debug(ae.getActionCommand());
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
			loadScenarioButton();
			break;
		case "AC_Help":
			this.allButtonsDele.helpButton();
			break;
		case "AC_RUN":
			this.allButtonsDele.runSingleBatch();
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
				titlestr = titlestr.substring(5);
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
			this.allButtonsDele.runSingleBatchForWsiDi();
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
	 */
	public void loadScenarioButton() {
		JFileChooser fChooser = new JFileChooser(Constant.SCENARIOS_DIR);
		fChooser.setMultiSelectionEnabled(false);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("CLS FILES (.cls)", "cls");
		fChooser.setFileFilter(filter);
		int val = fChooser.showOpenDialog(swingEngine.find(Constant.MAIN_FRAME_NAME));
		if (val == JFileChooser.APPROVE_OPTION && this.allButtonsDele.verifyTheSelectedFiles(fChooser, Constant.CLS_EXT)) {
			String fileName = fChooser.getSelectedFile().getName();
			LOG.debug("loading this cls file " + fileName);
			fileName = FilenameUtils.removeExtension(fileName);
			verifyControlsDele.verifyTheDataBeforeUI(Constant.SCENARIOS_DIR + fileName + Constant.CLS_EXT);
			this.resultSvc.applyClsFile(Constant.SCENARIOS_DIR + fileName + Constant.CLS_EXT, swingEngine,
			        seedDataSvc.getTableIdMap());
			((JTextField) swingEngine.find("run_txfScen")).setText(fileName + Constant.CLS_EXT);
			((JTextField) swingEngine.find("run_txfoDSS")).setText(fileName + Constant.DV_NAME + Constant.DSS_EXT);
			auditSvc.clearAudit();
		}
	}

	/**
	 * This method is used to display the "View Scenario Settings" button on the "Run Settings" tab.
	 */
	public void loadViewScen() {
		boolean pro = this.allButtonsDele.saveForViewScen();
		if (pro) {
			List<DataTableModle> dtmList = scenarioDele.getScenarioTableData(null);
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
}