package gov.ca.water.calgui.bus_delegate.impl;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.swixml.SwingEngine;

import gov.ca.water.calgui.bo.CalLiteGUIException;
import gov.ca.water.calgui.bo.DataTableModel;
import gov.ca.water.calgui.bo.GUILinks4BO;
import gov.ca.water.calgui.bus_delegate.IAllButtonsDele;
import gov.ca.water.calgui.bus_delegate.IApplyDynamicConDele;
import gov.ca.water.calgui.bus_service.IDynamicControlSvc;
import gov.ca.water.calgui.bus_service.IModelRunSvc;
import gov.ca.water.calgui.bus_service.IScenarioSvc;
import gov.ca.water.calgui.bus_service.ISeedDataSvc;
import gov.ca.water.calgui.bus_service.ITableSvc;
import gov.ca.water.calgui.bus_service.impl.DynamicControlSvcImpl;
import gov.ca.water.calgui.bus_service.impl.ModelRunSvcImpl;
import gov.ca.water.calgui.bus_service.impl.ScenarioSvcImpl;
import gov.ca.water.calgui.bus_service.impl.SeedDataSvcImpl;
import gov.ca.water.calgui.bus_service.impl.TableSvcImpl;
import gov.ca.water.calgui.bus_service.impl.XMLParsingSvcImpl;
import gov.ca.water.calgui.constant.Constant;
import gov.ca.water.calgui.presentation.GlobalActionListener;
import gov.ca.water.calgui.presentation.ProgressFrame;
import gov.ca.water.calgui.tech_service.IAuditSvc;
import gov.ca.water.calgui.tech_service.IDialogSvc;
import gov.ca.water.calgui.tech_service.IErrorHandlingSvc;
import gov.ca.water.calgui.tech_service.IFileSystemSvc;
import gov.ca.water.calgui.tech_service.impl.AuditSvcImpl;
import gov.ca.water.calgui.tech_service.impl.CalLiteHelp;
import gov.ca.water.calgui.tech_service.impl.DialogSvcImpl;
import gov.ca.water.calgui.tech_service.impl.ErrorHandlingSvcImpl;
import gov.ca.water.calgui.tech_service.impl.FileSystemSvcImpl;
import hec.heclib.dss.HecDss;

/**
 * This class is to handle all the button actions in the ui like Load Scenario,
 * Save Scenario etc.
 *
 * @author Mohan
 */
public class AllButtonsDeleImp implements IAllButtonsDele {
	private static final Logger LOG = Logger.getLogger(AllButtonsDeleImp.class.getName());
	private SwingEngine swingEngine = XMLParsingSvcImpl.getXMLParsingSvcImplInstance().getSwingEngine();
	private IModelRunSvc modelRunSvc = new ModelRunSvcImpl();
	private IScenarioSvc scenarioSvc = ScenarioSvcImpl.getScenarioSvcImplInstance();
	private ISeedDataSvc seedDataSvc = SeedDataSvcImpl.getSeedDataSvcImplInstance();
	private ITableSvc tableSvc = TableSvcImpl.getTableSvcImplInstance(seedDataSvc.getGUILinks2BOList());
	private IErrorHandlingSvc errorHandlingSvc = new ErrorHandlingSvcImpl();
	private Properties properties = new Properties();
	private boolean defaultCLSProtected = true;
	private IAuditSvc auditSvc = AuditSvcImpl.getAuditSvcImplInstance();
	private IFileSystemSvc fileSystemSvc = new FileSystemSvcImpl();
	private IDynamicControlSvc dynamicControlSvc = DynamicControlSvcImpl.getDynamicControlSvcImplInstance();
	private IApplyDynamicConDele applyDynamicConDele = new ApplyDynamicConDeleImp();
	private IDialogSvc dialogSvc = DialogSvcImpl.getDialogSvcInstance();

	public AllButtonsDeleImp() {
		try {
			properties.load(GlobalActionListener.class.getClassLoader().getResourceAsStream("callite-gui.properties"));
			defaultCLSProtected = !properties.getProperty("default.cls.protected").equals("false");
		} catch (Exception ex) {
			LOG.error("Problem loading properties. " + ex.getMessage());
		}
	}

	public boolean defaultCLSIsProtected() {
		return defaultCLSProtected;
	}

	@Override
	public void saveAsButton() {
		setOKToSkipConfirmation(false);
		JFileChooser fileChooser = new JFileChooser(Constant.SCENARIOS_DIR);
		fileChooser.setMultiSelectionEnabled(false);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("CLS FILES (.cls)", "cls");
		fileChooser.setFileFilter(filter);

		String newScrName = "DEFAULT"; // Check to make sure GUI is not
										// overwriting DEFAULT.CLS - tad
										// 20170202
		int val = JFileChooser.APPROVE_OPTION;
		while (newScrName.equals("DEFAULT") && val == JFileChooser.APPROVE_OPTION) {

			val = fileChooser.showSaveDialog(swingEngine.find(Constant.MAIN_FRAME_NAME));

			if (val == JFileChooser.APPROVE_OPTION) {
				newScrName = fileChooser.getSelectedFile().getName();
				if (newScrName.toLowerCase().endsWith(".cls")) {
					newScrName = FilenameUtils.removeExtension(newScrName);
				}
				if (newScrName.toUpperCase().equals("DEFAULT") && defaultCLSProtected) {
					// ImageIcon icon = new
					// ImageIcon(getClass().getResource("/images/CalLiteIcon.png"));
					// Object[] options = { "OK" };
					// JOptionPane optionPane = new JOptionPane(
					// "The CalLite GUI is not allowed to overwrite DEFAULT.CLS.
					// Please choose a different scenario file name or cancel
					// the save operation.",
					// JOptionPane.ERROR_MESSAGE, JOptionPane.OK_OPTION, null,
					// options, options[0]);
					// JDialog dialog =
					// optionPane.createDialog(swingEngine.find(Constant.MAIN_FRAME_NAME),
					// "CalLite");
					// dialog.setIconImage(icon.getImage());
					// dialog.setResizable(false);
					// dialog.setVisible(true);
					dialogSvc.getOK(
							"The CalLite GUI is not allowed to overwrite DEFAULT.CLS. Please choose a different scenario file name or cancel the save operation.",
							JOptionPane.ERROR_MESSAGE);

				} else if (!save(newScrName)) {
					String tempName = Constant.SCENARIOS_DIR + newScrName + Constant.CLS_EXT;
					if (!(new File(tempName)).exists()) {
						errorHandlingSvc.businessErrorHandler("Unable to save the file.", "Unable to save the file.",
								(JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME));
					}
				}
			}
		}
	}

	@Override
	public boolean saveCurrentStateToFile() {
		String clsFileName = ((JTextField) swingEngine.find("run_txfScen")).getText();
		return saveCurrentStateToFile(FilenameUtils.removeExtension(clsFileName));
	}

	@Override
	public boolean saveCurrentStateToFile(String clsFileName) {
		this.setOKToSkipConfirmation(false);
		if (decisionToSaveOrNot(clsFileName)) {
			this.setOKToSkipConfirmation(true);
			return save(clsFileName);
		}
		return true;
	}

	/**
	 * This method will tell whether to save the existing gui state to the file.
	 *
	 * @return Will return true if we need to save to the file.
	 */
	/**
	 * This method will tell whether to save the existing gui state to the file.
	 * 
	 * @param clsFileName
	 *            The cls file name.
	 * @return Will return true if we need to save to the file.
	 */
	private boolean decisionToSaveOrNot(String clsFileName) {
		if (auditSvc.hasValues()) {
			return true;
		}
		if (!Files.isExecutable(Paths.get(Constant.RUN_DETAILS_DIR + clsFileName))) {
			return true;
		}
		// ImageIcon icon = new
		// ImageIcon(getClass().getResource("/images/CalLiteIcon.png"));
		// Object[] options = { "Yes", "No" };
		// JOptionPane optionPane = new JOptionPane("The file is up-to-date. Do
		// you want to save again?",
		// JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null,
		// options, options[0]);
		// JDialog dialog =
		// optionPane.createDialog(swingEngine.find(Constant.MAIN_FRAME_NAME),
		// "CalLite");
		// dialog.setIconImage(icon.getImage());
		// dialog.setResizable(false);
		// dialog.setVisible(true);
		return (dialogSvc.getYesNo("The file is up-to-date. Do you want to save again?", JOptionPane.QUESTION_MESSAGE)
				.equals("Yes"));

		// switch (optionPane.getValue().toString()) {
		// case "Yes":
		// return true;
		// }
		// return false;
	}

	@Override
	public boolean saveForViewScen() {
		try {
			scenarioSvc.saveToCLSFile(Constant.SCENARIOS_DIR + Constant.CURRENT_SCENARIO + Constant.CLS_EXT,
					swingEngine, seedDataSvc.getGUILinks2BOList());
			return true;
		} catch (CalLiteGUIException ex) {
			LOG.debug(ex);
			errorHandlingSvc.businessErrorHandler((JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME), ex);
			return false;
		}
	}

	/**
	 * This method will save the current state of the ui to the cls file name
	 * given.
	 *
	 * @param clsFileName
	 *            The cls file name to save the currnt state of the ui.
	 * @return Will return true if the save is successful.
	 */
	private boolean save(String clsFileName) {

		String tempName = Constant.SCENARIOS_DIR + clsFileName + Constant.CLS_EXT;
		boolean proceed = true;
		if ((new File(tempName)).exists()) {
			// proceed =
			// (JOptionPane.showConfirmDialog(swingEngine.find(Constant.MAIN_FRAME_NAME),
			// "The scenario file '" + tempName + "' already exists. Press OK to
			// overwrite.",
			// "CalLite GUI - " + clsFileName + Constant.CLS_EXT,
			// JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION);

			// ImageIcon icon = new
			// ImageIcon(getClass().getResource("/images/CalLiteIcon.png"));
			// Object[] options = { "OK", "Cancel" };
			// JOptionPane optionPane = new JOptionPane(
			// "The scenario file '" + tempName + "' already exists. Press OK to
			// overwrite.",
			// JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null,
			// options, options[0]);
			// JDialog dialog =
			// optionPane.createDialog(swingEngine.find(Constant.MAIN_FRAME_NAME),
			// "CalLite");
			// dialog.setIconImage(icon.getImage());
			// dialog.setResizable(false);
			// dialog.setVisible(true);
			// switch (optionPane.getValue().toString()) {
			// case "Cancel":
			// proceed = false;
			// break;
			// case "OK":
			// proceed = true;
			// break;
			// default:
			// proceed = false;
			// break;
			// }
			proceed = isOKToSkipConfirmation() || (dialogSvc
					.getOKCancel("The scenario file '" + tempName + "' already exists. Press OK to overwrite.",
							JOptionPane.QUESTION_MESSAGE)
					.equals("OK"));

		}

		if (proceed) {
			((JTextField) swingEngine.find("run_txfScen")).setText(clsFileName + Constant.CLS_EXT);
			((JTextField) swingEngine.find("run_txfoDSS")).setText(clsFileName + Constant.DV_NAME + Constant.DSS_EXT);
			/*
			 * The following code is for checking whether the tables in the
			 * "Operations" tab is up to data or not and if not updating the
			 * tables(SWP and CVP) in the menory.
			 */
			String swpFileName = scenarioSvc.getUserDefinedTable(Constant.SWP_START_FILENAME).getTableName();
			String cvpFileName = scenarioSvc.getUserDefinedTable(Constant.CVP_START_FILENAME).getTableName();

			if (!fileSystemSvc.getTheLookupFromTheFullFileName(tableSvc.getWsidiForSWPFullFileName())
					.equalsIgnoreCase(swpFileName)) {
				try {
					scenarioSvc.addUserDefinedTable(Constant.SWP_START_FILENAME,
							tableSvc.getWsiDiTable(tableSvc.getWsidiForSWPFullFileName()));
				} catch (CalLiteGUIException ex) {
					LOG.error(ex);
					errorHandlingSvc.businessErrorHandler((JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME), ex);
					return false;
				}
			}
			if (!fileSystemSvc.getTheLookupFromTheFullFileName(tableSvc.getWsidiForCVPFullFileName())
					.equalsIgnoreCase(cvpFileName)) {
				try {
					scenarioSvc.addUserDefinedTable(Constant.CVP_START_FILENAME,
							tableSvc.getWsiDiTable(tableSvc.getWsidiForCVPFullFileName()));
				} catch (CalLiteGUIException ex) {
					LOG.error(ex);
					errorHandlingSvc.businessErrorHandler((JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME), ex);
					return false;
				}
			}
			ProgressFrame progressFrame = ProgressFrame.getProgressFrameInstance();
			progressFrame.addScenarioNamesAndAction(clsFileName, Constant.SAVE);
			progressFrame.makeDialogVisible();
			proceed = ScenarioSvcImpl.getScenarioSvcImplInstance().save(clsFileName,
					XMLParsingSvcImpl.getXMLParsingSvcImplInstance().getSwingEngine(),
					SeedDataSvcImpl.getSeedDataSvcImplInstance().getGUILinks2BOList());
			LOG.debug("Save Complete. " + clsFileName);
			auditSvc.clearAudit();
			return proceed;
		}
		return false;
	}

	boolean okToSkipConfirmation = false;

	/**
	 * Controls whether or not the user is asked to confirm overwriting a
	 * scenario. The use should *not* be asked if they've already confirmed
	 * saving an up-to-date scenario; this is done by setting to true
	 * 
	 * @param value
	 *            Set to true if the overwrite confirmation question can be
	 *            skipped
	 * 
	 */
	private void setOKToSkipConfirmation(boolean value) {
		okToSkipConfirmation = value;
	}

	/**
	 * 
	 * @return Current setting for whether or not the confirmation question can
	 *         be skipped
	 */
	private boolean isOKToSkipConfirmation() {
		return okToSkipConfirmation;
	}

	@Override
	public void runMultipleBatch() {
		JFileChooser fileChooser = new JFileChooser(Constant.SCENARIOS_DIR);
		fileChooser.setMultiSelectionEnabled(true);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("CLS FILES (.cls)", "cls");
		fileChooser.setFileFilter(filter);
		int val = fileChooser.showOpenDialog(swingEngine.find(Constant.MAIN_FRAME_NAME));
		if (val == JFileChooser.APPROVE_OPTION && verifyTheSelectedFiles(fileChooser, Constant.CLS_EXT)) {
			List<String> fileNames = new ArrayList<String>();
			for (File file : fileChooser.getSelectedFiles()) {
				fileNames.add(FilenameUtils.removeExtension(file.getName()));
			}
			List<String> filesWhichAreNotSaved = fileNames.stream()
					.filter(fileName -> !Files.isExecutable(Paths.get(Constant.RUN_DETAILS_DIR + fileName)))
					.collect(Collectors.toList());
			if (filesWhichAreNotSaved != null && !filesWhichAreNotSaved.isEmpty()) {
				// ImageIcon icon = new
				// ImageIcon(getClass().getResource("/images/CalLiteIcon.png"));
				// Object[] options = { "Yes", "No" };
				// JOptionPane optionPane = new JOptionPane(
				// "We can't run the batch for following files because they are
				// not saved.\n"
				// + filesWhichAreNotSaved.stream().map(name -> name + ".cls")
				// .collect(Collectors.joining("\n"))
				// + "\n Do you still want to run the rest?",
				// JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION,
				// null, options, options[0]);
				// JDialog dialog =
				// optionPane.createDialog(swingEngine.find(Constant.MAIN_FRAME_NAME),
				// "CalLite");
				// dialog.setIconImage(icon.getImage());
				// dialog.setResizable(false);
				// dialog.setVisible(true);
				String option = dialogSvc.getYesNo(
						"We can't run the batch for following files because they are not saved.\n"
								+ filesWhichAreNotSaved.stream().map(name -> name + ".cls")
										.collect(Collectors.joining("\n"))
								+ "\n Do you still want to run the rest?",
						JOptionPane.QUESTION_MESSAGE);
				switch (option) {
				case "Yes":
					fileNames.removeAll(filesWhichAreNotSaved);
					break;
				case "No":
					return;
				}
			}
			ProgressFrame progressFrame = ProgressFrame.getProgressFrameInstance();
			progressFrame.addScenarioNamesAndAction(fileNames, Constant.BATCH_RUN);
			progressFrame.setBtnText(Constant.STATUS_BTN_TEXT_STOP);
			progressFrame.makeDialogVisible();
			modelRunSvc.doBatch(fileNames, swingEngine, false);
		}
	}

	@Override
	public boolean verifyTheSelectedFiles(JFileChooser fileChooser, String extension) {
		File[] files = fileChooser.getSelectedFiles();
		if (files.length <= 0) {
			File fi = fileChooser.getSelectedFile();
			files = new File[1];
			files[0] = fi;
		}
		boolean isExpectedFiles = true;
		String errorMessage = "";
		for (File file : files) {
			if (!file.getName().toLowerCase().endsWith(extension.toLowerCase())) {
				isExpectedFiles = false;
				errorMessage = "Please select " + extension + " files only. The file you selected is " + file.getName();
				break;
			}
		}
		if (!isExpectedFiles) {
			errorHandlingSvc.validationeErrorHandler(errorMessage, errorMessage,
					((JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME)));
		}
		return isExpectedFiles;
	}

	@Override
	public void copyTableValues(JTable table) {
		try {
			StringBuffer buffer = new StringBuffer();
			int numcols = table.getSelectedColumnCount();
			int numrows = table.getSelectedRowCount();
			int[] rowsselected = table.getSelectedRows();
			int[] colsselected = table.getSelectedColumns();
			if (!((numrows - 1 == rowsselected[rowsselected.length - 1] - rowsselected[0]
					&& numrows == rowsselected.length)
					&& (numcols - 1 == colsselected[colsselected.length - 1] - colsselected[0]
							&& numcols == colsselected.length))) {
				// JOptionPane.showMessageDialog(swingEngine.find(Constant.MAIN_FRAME_NAME),
				// "Invalid Copy Selection", "Invalid Copy Selection",
				// JOptionPane.ERROR_MESSAGE);
				// ImageIcon icon = new
				// ImageIcon(getClass().getResource("/images/CalLiteIcon.png"));
				// Object[] options = { "OK" };
				// JOptionPane optionPane = new JOptionPane("Invalid Copy
				// Selection", JOptionPane.ERROR_MESSAGE,
				// JOptionPane.OK_OPTION, null, options, options[0]);
				// JDialog dialog =
				// optionPane.createDialog(swingEngine.find(Constant.MAIN_FRAME_NAME),
				// "CalLite");
				// dialog.setIconImage(icon.getImage());
				// dialog.setResizable(false);
				// dialog.setVisible(true);
				dialogSvc.getOK("Invalid Copy Selection", JOptionPane.ERROR_MESSAGE);

				return;
			}
			for (int i = 0; i < numrows; i++) {
				for (int j = 0; j < numcols; j++) {
					buffer.append(table.getValueAt(rowsselected[i], colsselected[j]));
					if (j < numcols - 1)
						buffer.append(Constant.TAB_SPACE);
				}
				buffer.append(Constant.NEW_LINE);
			}
			StringSelection stringSelection = new StringSelection(buffer.toString());
			Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			systemClipboard.setContents(stringSelection, stringSelection);
			auditSvc.addAudit("copy", "", table.getName());
		} catch (ArrayIndexOutOfBoundsException ex) {
			errorHandlingSvc.validationeErrorHandler("Please select the field from where you want to copy the data",
					"Please select the field from where you want to copy the data",
					(JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME));
		}
	}

	@Override
	public void pasteTableValues(JTable table) {
		try {
			int startRow = (table.getSelectedRows())[0];
			int startCol = (table.getSelectedColumns())[0];

			// get data from the clipboard.
			String totalData = "";
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			// odd: the Object param of getContents is not currently used
			Transferable contents = clipboard.getContents(null);
			boolean hasTransferableText = (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
			if (hasTransferableText) {
				try {
					totalData = (String) contents.getTransferData(DataFlavor.stringFlavor);
				} catch (UnsupportedFlavorException ex) {
					// highly unlikely since we are using a standard DataFlavor
					LOG.debug(ex.getMessage());
					errorHandlingSvc.businessErrorHandler((JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME),
							new CalLiteGUIException(ex));
				} catch (IOException ex) {
					LOG.debug(ex.getMessage());
					errorHandlingSvc.businessErrorHandler((JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME),
							new CalLiteGUIException(ex));
				}
			}
			totalData = totalData.replaceAll("(?sm)\t\t", "\t \t");
			totalData = totalData.replaceAll("(?sm)\t\n", "\t \n");
			// verify the row's and column's.
			// int rowCount = new StringTokenizer(totalData,
			// Constant.NEW_LINE).countTokens();
			int colCount = new StringTokenizer(new StringTokenizer(totalData, Constant.NEW_LINE).nextToken(),
					Constant.TAB_SPACE).countTokens();
			if (colCount > table.getColumnCount()) {
				// JOptionPane.showMessageDialog(swingEngine.find(Constant.MAIN_FRAME_NAME),
				// "The column's you
				// selected is more then the column's of the table.", "Error",
				// JOptionPane.ERROR_MESSAGE);
				errorHandlingSvc.validationeErrorHandler(
						"The column's you selected is more then the column's of the table.",
						"The column's you selected is more then the column's of the table.",
						(JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME));
				return;
			}
			// poplute data.
			StringTokenizer st1 = new StringTokenizer(totalData, Constant.NEW_LINE);
			for (int i = 0; st1.hasMoreTokens(); i++) {
				String rowstring = st1.nextToken();
				StringTokenizer st2 = new StringTokenizer(rowstring, Constant.TAB_SPACE);
				for (int j = 0; st2.hasMoreTokens(); j++) {
					String value = st2.nextToken();
					if (startRow + i < table.getRowCount() && startCol + j < table.getColumnCount())
						table.setValueAt(value, startRow + i, startCol + j);
					table.repaint();
				}
			}
			auditSvc.addAudit("past", "", table.getName());
		} catch (ArrayIndexOutOfBoundsException ex) {
			errorHandlingSvc.validationeErrorHandler("Please select the field from where you want to paste the data",
					"Please select the field from where you want to paste the data",
					(JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME));
		} catch (Exception ex) {
			LOG.debug(ex.getMessage());
			errorHandlingSvc.businessErrorHandler((JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME),
					new CalLiteGUIException(ex));
		}

	}

	@Override
	public void readButtonInOperations() {
		if (!isOkToContinueOnOperations()) {
			return;
		}
		String swpFullFileName = "";
		String cvpFullFileName = "";
		JFileChooser fChooser = new JFileChooser(Constant.MODEL_W2_WRESL_LOOKUP_DIR);
		fChooser.setMultiSelectionEnabled(false);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Data Table FILES", "table");
		fChooser.setFileFilter(filter);
		fChooser.setDialogTitle("Select WSI/DI SWP data table file");
		int val = fChooser.showOpenDialog(swingEngine.find(Constant.MAIN_FRAME_NAME));
		if (val == JFileChooser.APPROVE_OPTION && verifyTheSelectedFiles(fChooser, Constant.TABLE_EXT)) {
			swpFullFileName = fChooser.getSelectedFile().getAbsolutePath();
		} else {
			return;
		}
		fChooser.setDialogTitle("Select WSI/DI CVP data table file");
		val = fChooser.showOpenDialog(swingEngine.find(Constant.MAIN_FRAME_NAME));
		if (val == JFileChooser.APPROVE_OPTION && verifyTheSelectedFiles(fChooser, Constant.TABLE_EXT)) {
			cvpFullFileName = fChooser.getSelectedFile().getAbsolutePath();
		}
		LOG.debug("SWP file name " + swpFullFileName + " CVP file name " + cvpFullFileName);

		try {
			// for CVP table
			DataTableModel cvpDtm = tableSvc.getWsiDiTable(cvpFullFileName);
			cvpDtm.setTableName(Constant.USER_DEFINED);
			cvpDtm.setCellEditable(true);
			cvpDtm.setSwingEngine(swingEngine);
			scenarioSvc.addUserDefinedTable(Constant.CVP_START_FILENAME, cvpDtm);
			tableSvc.setWsidiForCVPFullFileName(Constant.USER_DEFINED);
			// for SWP table
			DataTableModel swpDtm = tableSvc.getWsiDiTable(swpFullFileName);
			swpDtm.setTableName(Constant.USER_DEFINED);
			swpDtm.setCellEditable(true);
			swpDtm.setSwingEngine(swingEngine);
			scenarioSvc.addUserDefinedTable(Constant.SWP_START_FILENAME, swpDtm);
			tableSvc.setWsidiForSWPFullFileName(Constant.USER_DEFINED);
			JLabel jLabel = (JLabel) swingEngine.find("op_WSIDI_Status");
			jLabel.setText("WSI/DI read from [" + Paths.get(swpFullFileName).getFileName().toString() + " , "
					+ Paths.get(cvpFullFileName).getFileName().toString() + "]" + Constant.UNEDITED_FORLABEL);
			JComponent component = (JComponent) swingEngine.find("op_btn1");
			editButtonOnOperations(component);
			auditSvc.addAudit("wsi_di tables", "", swpFullFileName);
		} catch (CalLiteGUIException ex) {
			errorHandlingSvc.businessErrorHandler((JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME), ex);
		}
	}

	@Override
	public void defaultButtonOnOperations() {
		if (!isOkToContinueOnOperations()) {
			return;
		}
		try {
			List<String> labelNames = dynamicControlSvc.getLabelAndGuiLinks4BOBasedOnTheRadioButtons(swingEngine);
			GUILinks4BO gUILinks4BO = seedDataSvc.getObjByRunBasisLodCcprojCcmodelIds(labelNames.get(0));
			String swpFullFileName = Constant.MODEL_W2_WRESL_LOOKUP_DIR + "\\WSIDI\\" + Constant.SWP_START_FILENAME
					+ Constant.UNDER_SCORE + gUILinks4BO.getLookup() + Constant.TABLE_EXT;
			String cvpFullFileName = Constant.MODEL_W2_WRESL_LOOKUP_DIR + "\\WSIDI\\" + Constant.CVP_START_FILENAME
					+ Constant.UNDER_SCORE + gUILinks4BO.getLookup() + Constant.TABLE_EXT;
			// To Load CVP table
			DataTableModel cvpDtm = tableSvc.getWsiDiTable(cvpFullFileName);
			cvpDtm.setTableName(FilenameUtils.removeExtension(Paths.get(cvpFullFileName).getFileName().toString()));
			cvpDtm.setCellEditable(true);
			cvpDtm.setSwingEngine(swingEngine);
			scenarioSvc.addUserDefinedTable(Constant.CVP_START_FILENAME, cvpDtm);
			tableSvc.setWsidiForCVPFullFileName(cvpFullFileName);
			// To Load SWP table
			DataTableModel swpDtm = tableSvc.getWsiDiTable(swpFullFileName);
			swpDtm.setTableName(FilenameUtils.removeExtension(Paths.get(swpFullFileName).getFileName().toString()));
			swpDtm.setCellEditable(true);
			swpDtm.setSwingEngine(swingEngine);
			scenarioSvc.addUserDefinedTable(Constant.SWP_START_FILENAME, swpDtm);
			tableSvc.setWsidiForSWPFullFileName(swpFullFileName);
			JLabel jLabel = (JLabel) swingEngine.find("op_WSIDI_Status");
			jLabel.setText(labelNames.get(1) + Constant.UNEDITED_FORLABEL);
			JComponent component = (JComponent) swingEngine.find("op_btn1");
			editButtonOnOperations(component);
			auditSvc.addAudit("wsi_di tables", "", swpFullFileName);
		} catch (NullPointerException ex) {
			errorHandlingSvc.businessErrorHandler((JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME),
					new CalLiteGUIException("The data for geting the table name is wrong", ex));
		} catch (CalLiteGUIException ex) {
			errorHandlingSvc.businessErrorHandler((JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME), ex);
		}
	}

	@Override
	public void editButtonOnOperations(JComponent component) {
		String tableName = "";
		String fileName = "";
		DataTableModel dataTableModel = null;
		try {
			if (component instanceof JButton) {
				JButton btn = (JButton) component;
				String titleStr = btn.getText();
				titleStr = titleStr.substring(5);
				TitledBorder title = BorderFactory.createTitledBorder(titleStr);
				JPanel pan = (JPanel) swingEngine.find("op_panTab");
				pan.setBorder(title);
				String comId = component.getName();
				tableName = seedDataSvc.getObjByGuiId(comId).getDataTables();

				if (titleStr.equalsIgnoreCase(Constant.SWP)) {
					fileName = tableSvc.getWsidiForSWPFullFileName();
				} else {
					fileName = tableSvc.getWsidiForCVPFullFileName();
				}
				if (fileName.equalsIgnoreCase(Constant.USER_DEFINED)) {
					dataTableModel = scenarioSvc.getUserDefinedTable(tableName);
					dataTableModel.setSwingEngine(swingEngine);
				} else {
					dataTableModel = tableSvc.getWsiDiTable(fileName);
					dataTableModel
							.setTableName(FilenameUtils.removeExtension(Paths.get(fileName).getFileName().toString()));
					dataTableModel.setCellEditable(true);
					dataTableModel.setSwingEngine(swingEngine);
					scenarioSvc.addUserDefinedTable(tableName, dataTableModel);
				}
				showTableOnOperations(dataTableModel);
			}
			auditSvc.addAudit("wsi_di tables", "", fileName);
		} catch (CalLiteGUIException ex) {
			errorHandlingSvc.businessErrorHandler((JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME), ex);
		}
	}

	@Override
	public void decisionSVInitFilesAndTableInOperations() {
		applyDynamicConDele.changeSVInitFilesAndTableInOperations(true);

		// Commented out because we always want to use the stored values when
		// loading

		// String text = "";
		// if (((JRadioButton) swingEngine.find("hyd_rdb2005")).isSelected()) {
		// text = ((JRadioButton) swingEngine.find("hyd_rdb2005")).getText();
		// }
		// if (((JRadioButton) swingEngine.find("hyd_rdb2030")).isSelected()) {
		// text = ((JRadioButton) swingEngine.find("hyd_rdb2030")).getText();
		// }
		// if (!text.equals("")) {
		// int option =
		// JOptionPane.showConfirmDialog(swingEngine.find(Constant.MAIN_FRAME_NAME),
		// "You have selected " + text + ".\n Do you wish to use the WSI/DI
		// curves for this configuration?");
		// if (option == JOptionPane.YES_OPTION) {
		// return;
		// }
		// }
		/*
		 * The following code we are setting the SWP and CVP file names as user
		 * defined because the table values we are getting it from the cls file
		 * so we consider them as user defined.
		 */
		tableSvc.setWsidiForSWPFullFileName(Constant.USER_DEFINED);
		tableSvc.setWsidiForCVPFullFileName(Constant.USER_DEFINED);
	}

	/**
	 * This method is to ask the use whether he wants to continue or not with
	 * WSI/DI modification. This is used in the "Operations" tab.
	 *
	 * @return will return true if user wants to continue.
	 */
	private boolean isOkToContinueOnOperations() {
		String label = ((JLabel) swingEngine.find("op_WSIDI_Status")).getText();
		if (label.contains(Constant.UNEDITED_FORLABEL)) {
			return true;
		}
		// return
		// (JOptionPane.showConfirmDialog(swingEngine.find(Constant.MAIN_FRAME_NAME),
		// "WSI/DI data tables have been modified. Are you sure you wish to
		// overwrite these changes?",
		// "CalLite GUI", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);

		// ImageIcon icon = new
		// ImageIcon(getClass().getResource("/images/CalLiteIcon.png"));
		// Object[] options = { "Yes", "No" };
		// JOptionPane optionPane = new JOptionPane(
		// "WSI/DI data tables have been modified. Are you sure you wish to
		// overwrite these changes?",
		// JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION, null,
		// options, options[0]);
		// JDialog dialog =
		// optionPane.createDialog(swingEngine.find(Constant.MAIN_FRAME_NAME),
		// "CalLite");
		// dialog.setIconImage(icon.getImage());
		// dialog.setResizable(false);
		// dialog.setVisible(true);
		// switch (optionPane.getValue().toString()) {
		// case "No":
		// return false;
		// case "Yes":
		// return true;
		// default:
		// return false;
		// }
		return (dialogSvc
				.getYesNo("WSI/DI data tables have been modified.  Are you sure you wish to overwrite these changes?",
						JOptionPane.QUESTION_MESSAGE)
				.equals("Yes"));
	}

	/**
	 * This method is used to show the table in the "Operations" tab.
	 *
	 * @param dataTableModel
	 *            Table data to show.
	 */
	private void showTableOnOperations(DataTableModel dataTableModel) {
		JComponent component = (JComponent) swingEngine.find("scrOpValues");
		JTable table = (JTable) swingEngine.find("tblOpValues");
		table.setModel(dataTableModel);
		component.setVisible(true);
		component.setEnabled(true);
		table.setVisible(true);
		table.setCellSelectionEnabled(true);
	}

	@Override
	public void selectingSVAndInitFile(String fileNameForDss, String fPartForDss, String manualFileNameForDss,
			String manualFPartForDss) {
		JFileChooser fChooser = new JFileChooser(Constant.MODEL_W2_DSS_DIR);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("DSS FILES (.dss)", "dss", "dss");
		fChooser.setFileFilter(filter);
		fChooser.setMultiSelectionEnabled(false);
		int val = fChooser.showOpenDialog(swingEngine.find(Constant.MAIN_FRAME_NAME));
		if (val == JFileChooser.APPROVE_OPTION && verifyTheSelectedFiles(fChooser, ".dss")) {
			String fPartResult = "NOT FOUND";
			String fileFullName = fChooser.getSelectedFile().getAbsolutePath();
			try {
				// Read all pathnames from the DSS file and set the F-PART
				// textfield as
				// "NOT FOUND","MULTIPLE F-PARTS", or the first F-PART found.
				HecDss hecDss = HecDss.open(fileFullName);
				Vector<String> pathNames = hecDss.getCatalogedPathnames();
				String lastFPart = "";
				for (int i = 0; i < pathNames.size(); i++) {
					String[] parts = pathNames.elementAt(0).split("/");
					String newFPart = ((parts.length < 7) || (parts[6] == null)) ? "NOT FOUND" : parts[6];
					if (i == 0) {
						lastFPart = newFPart;
						fPartResult = newFPart;
					} else if (!lastFPart.equals(newFPart) && !newFPart.equals("NOT FOUND")) {
						fPartResult = "MULTIPLE F-PARTS";
					}
				}
			} catch (Exception ex) {
				LOG.debug(ex.getMessage());
				errorHandlingSvc.businessErrorHandler((JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME),
						new CalLiteGUIException(ex));
			}
			((JTextField) swingEngine.find(fileNameForDss)).setText(fChooser.getSelectedFile().getName());
			((JTextField) swingEngine.find(fPartForDss)).setText(fPartResult);
			((JTextField) swingEngine.find(manualFileNameForDss)).setText(fChooser.getSelectedFile().getName());
			((JTextField) swingEngine.find(manualFPartForDss)).setText(fPartResult);
		}
	}

	@Override
	public void helpButton() {
		try {
			JTabbedPane jtp = (JTabbedPane) swingEngine.find("tabbedPane1");
			String label = jtp.getTitleAt(jtp.getSelectedIndex());
			CalLiteHelp calLiteHelp = new CalLiteHelp();
			calLiteHelp.showHelp(label);
		} catch (Exception ex) {
			LOG.error("Problem with CalLite Help " + ex.getMessage());
		}
	}

	@Override
	public void aboutButton() {
		Long longTime = new File(Constant.GUI_XML_FILENAME).lastModified();
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("PST"));
		calendar.setTimeInMillis(longTime);
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
		String guiXmlDate = sdf.format(calendar.getTime());
		// JOptionPane.showMessageDialog(swingEngine.find(Constant.MAIN_FRAME_NAME),
		// "CalLite v. " + properties.getProperty("version.id") + "\nBuild date:
		// "
		// + properties.getProperty("build.date") + "\nYour last GUI xml
		// revision date: " + guiXmlDate,
		// "About CalLite", JOptionPane.INFORMATION_MESSAGE);

		// ImageIcon icon = new
		// ImageIcon(getClass().getResource("/images/CalLiteIcon.png"));
		// Object[] options = { "OK" };

		// , JOptionPane.OK_OPTION, null, options, options[0]);
		// JDialog dialog =
		// optionPane.createDialog(swingEngine.find(Constant.MAIN_FRAME_NAME),"About
		// CalLite");
		// dialog.setIconImage(icon.getImage());
		// dialog.setResizable(false);
		// dialog.setVisible(true);
		dialogSvc.getOK(
				"CalLite GUI v. " + properties.getProperty("version.id") + " ("
						+ System.getProperty("sun.arch.data.model") + "-bit)\nBuild date: "
						+ properties.getProperty("build.date") + "\nYour last GUI xml revision date: " + guiXmlDate,
				JOptionPane.INFORMATION_MESSAGE);
	}

	@Override
	public void windowClosing() {
		// ImageIcon icon = new
		// ImageIcon(getClass().getResource("/images/CalLiteIcon.png"));
		// JOptionPane optionPane;
		if (auditSvc.hasValues()) {
			// Object[] options = { "Save", "Don't Save", "Cancel" };
			// optionPane = new JOptionPane("Current scenario not saved. Would
			// you like to save before exiting?",
			// JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION,
			// null, options, options[0]);
			// JDialog dialog =
			// optionPane.createDialog(swingEngine.find(Constant.MAIN_FRAME_NAME),
			// "CalLite");
			// dialog.setIconImage(icon.getImage());
			// dialog.setResizable(false);
			// dialog.setVisible(true);
			String option = dialogSvc.getSaveDontSaveCancel(
					"Current scenario not saved. Would you like to save before exiting?", JOptionPane.QUESTION_MESSAGE);
			if (option.equals("Save")) {
				if (((JTextField) swingEngine.find("run_txfScen")).getText().toUpperCase().equals("DEFAULT.CLS")
						&& defaultCLSProtected) {
					// JOptionPane.showMessageDialog(swingEngine.find(Constant.MAIN_FRAME_NAME),
					// "The CalLite GUI is not allowed to overwrite DEFAULT.CLS.
					// Please use the Save As command to save your changes
					// before exiting.");
					// icon = new
					// ImageIcon(getClass().getResource("/images/CalLiteIcon.png"));
					// Object[] options1 = { "OK" };
					// optionPane = new JOptionPane("The CalLite GUI is not
					// allowed to overwrite DEFAULT.CLS. Please use the Save As
					// command to save your changes before exiting.",
					// JOptionPane.ERROR_MESSAGE, JOptionPane.OK_OPTION, null,
					// options1, options1[0]);
					// dialog =
					// optionPane.createDialog(swingEngine.find(Constant.MAIN_FRAME_NAME),"CalLite");
					// dialog.setIconImage(icon.getImage());
					// dialog.setResizable(false);
					// dialog.setVisible(true);

					dialogSvc.getOK(
							"The CalLite GUI is not allowed to overwrite DEFAULT.CLS. Please use the Save As command to save your changes before exiting.",
							JOptionPane.ERROR_MESSAGE);

				} else {
					boolean isSaved = saveCurrentStateToFile();
					if (!isSaved)
						errorHandlingSvc.businessErrorHandler("We encountered a problem when saving the file.", "",
								(JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME));
					System.exit(0);
				}
			} else if (option.equals("Don't Save")) {
				System.exit(0);
			}
		} else {
			// Object[] options = { "Ok", "Cancel" };
			// optionPane = new JOptionPane("Are you sure you wanto to exit ?",
			// JOptionPane.QUESTION_MESSAGE,
			// JOptionPane.OK_CANCEL_OPTION, null, options, options[0]);
			// JDialog dialog =
			// optionPane.createDialog(swingEngine.find(Constant.MAIN_FRAME_NAME),
			// "CalLite");
			// dialog.setIconImage(icon.getImage());
			// dialog.setResizable(false);
			// dialog.setVisible(true);
			if (dialogSvc.getOKCancel("Are you sure you want to to exit ?", JOptionPane.QUESTION_MESSAGE)
					.equals("OK")) {
				System.exit(0);
			}
		}
	}
}