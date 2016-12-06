package gov.ca.water.calgui.bus_delegate.impl;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
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
import gov.ca.water.calgui.bo.DataTableModle;
import gov.ca.water.calgui.bo.GuiLinks4BO;
import gov.ca.water.calgui.bus_delegate.IAllButtonsDele;
import gov.ca.water.calgui.bus_service.IBatchRunSvc;
import gov.ca.water.calgui.bus_service.IDynamicControlSvc;
import gov.ca.water.calgui.bus_service.IResultSvc;
import gov.ca.water.calgui.bus_service.ISeedDataSvc;
import gov.ca.water.calgui.bus_service.ITableSvc;
import gov.ca.water.calgui.bus_service.impl.BatchRunSvcImpl;
import gov.ca.water.calgui.bus_service.impl.DynamicControlSvcImpl;
import gov.ca.water.calgui.bus_service.impl.ResultSvcImpl;
import gov.ca.water.calgui.bus_service.impl.SeedDataSvcImpl;
import gov.ca.water.calgui.bus_service.impl.TableSvcImpl;
import gov.ca.water.calgui.bus_service.impl.XMLParsingSvcImpl;
import gov.ca.water.calgui.constant.Constant;
import gov.ca.water.calgui.presentation.GlobalActionListener;
import gov.ca.water.calgui.presentation.ProgressFrame;
import gov.ca.water.calgui.tech_service.IAuditSvc;
import gov.ca.water.calgui.tech_service.IErrorHandlingSvc;
import gov.ca.water.calgui.tech_service.IFileSystemSvc;
import gov.ca.water.calgui.tech_service.impl.AuditSvcImpl;
import gov.ca.water.calgui.tech_service.impl.CalLiteHelp;
import gov.ca.water.calgui.tech_service.impl.ErrorHandlingSvcImpl;
import gov.ca.water.calgui.tech_service.impl.FileSystemSvcImpl;
import hec.heclib.dss.HecDss;

/**
 * This class is to handle all the button actions in the ui.
 */
public class AllButtonsDeleImp implements IAllButtonsDele {
	private static final Logger LOG = Logger.getLogger(AllButtonsDeleImp.class.getName());
	private SwingEngine swingEngine = XMLParsingSvcImpl.getXMLParsingSvcImplInstance().getSwingEngine();
	private IBatchRunSvc batchRunSvc = new BatchRunSvcImpl();
	private IResultSvc resultSvc = ResultSvcImpl.getResultSvcImplInstance();
	private ISeedDataSvc seedDataSvc = SeedDataSvcImpl.getSeedDataSvcImplInstance();
	private ITableSvc tableSvc = TableSvcImpl.getTableSvcImplInstance(seedDataSvc.getSeedDataBOList());
	private IErrorHandlingSvc errorHandlingSvc = new ErrorHandlingSvcImpl();
	private Properties properties = new Properties();
	private IAuditSvc auditSvc = AuditSvcImpl.getAuditSvcImplInstance();
	private IFileSystemSvc fileSystemSvc = new FileSystemSvcImpl();
	private IDynamicControlSvc dynamicControlSvc = DynamicControlSvcImpl.getDynamicControlSvcImplInstance();

	public AllButtonsDeleImp() {
		try {
			properties.load(GlobalActionListener.class.getClassLoader().getResourceAsStream("callite-gui.properties"));
		} catch (Exception ex) {
			LOG.error("Problem loading properties. " + ex.getMessage());
		}
	}

	@Override
	public void saveAsButton() {
		JFileChooser saver = new JFileChooser(Constant.SCENARIOS_DIR);
		saver.setMultiSelectionEnabled(false);
		int val = saver.showSaveDialog(swingEngine.find(Constant.MAIN_FRAME_NAME));
		if (val == JFileChooser.APPROVE_OPTION) {
			String newScrName = saver.getSelectedFile().getName();
			newScrName = FilenameUtils.removeExtension(newScrName);
			if (!saveCurrentStateToFile(newScrName, true)) {
				errorHandlingSvc.businessErrorHandler("Unable to save the file.", "Unable to save the file.",
				        (JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME));
			}
		}
	}

	@Override
	public boolean saveCurrentStateToFile() {
		String clsFileName = ((JTextField) swingEngine.find("run_txfScen")).getText();
		return saveCurrentStateToFile(FilenameUtils.removeExtension(clsFileName), false);
	}

	@Override
	public boolean saveForViewScen() {
		try {
			resultSvc.saveToCLSFile(Constant.SCENARIOS_DIR + Constant.CURRENT_SCENARIO + Constant.CLS_EXT, swingEngine,
			        seedDataSvc.getSeedDataBOList());
			return true;
		} catch (CalLiteGUIException ex) {
			LOG.debug(ex);
			errorHandlingSvc.businessErrorHandler((JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME), ex);
			return false;
		}
	}

	/**
	 * This method is used to save.
	 *
	 * @param clsFileName
	 *            Just the name of the cls file whithout the extension.
	 * @param isSaveAs
	 *            Will be true when we want to save the file as save as.
	 * @return It will return true if the save is successful.
	 */
	private boolean saveCurrentStateToFile(String clsFileName, boolean isSaveAs) {
		if (!isSaveAs) {
			if (!auditSvc.hasValues()) {
				JOptionPane.showMessageDialog(swingEngine.find(Constant.MAIN_FRAME_NAME), "The File is up-to-date");
				return true;
			}
		}
		ProgressFrame progressFrame = ProgressFrame.getProgressFrameInstance();
		String tempName = Constant.SCENARIOS_DIR + clsFileName + Constant.CLS_EXT;
		boolean proceed = true;
		if ((new File(tempName)).exists())
			proceed = (JOptionPane.showConfirmDialog(swingEngine.find(Constant.MAIN_FRAME_NAME),
			        "The scenario file '" + tempName + "' already exists. Press OK to overwrite.",
			        "CalLite GUI - " + clsFileName + Constant.CLS_EXT, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION);
		if (proceed) {
			((JTextField) swingEngine.find("run_txfScen")).setText(clsFileName + Constant.CLS_EXT);
			((JTextField) swingEngine.find("run_txfoDSS")).setText(clsFileName + Constant.DV_NAME + Constant.DSS_EXT);
			progressFrame.addScenarioNamesAndAction(clsFileName, Constant.SAVE);
			/*
			 * The following code is for checking whether the tables in the "Operations" tab is up to data or not and if not
			 * updating the tables(SWP and CVP) in the menory.
			 */
			String swpFileName = resultSvc.getUserDefinedTable(Constant.SWP_START_FILENAME).getTableName();
			String cvpFileName = resultSvc.getUserDefinedTable(Constant.CVP_START_FILENAME).getTableName();

			if (!fileSystemSvc.getTheLookupFromTheFullFileName(tableSvc.getWsidiForSWPFullFileName())
			        .equalsIgnoreCase(swpFileName)) {
				try {
					resultSvc.addUserDefinedTable(Constant.SWP_START_FILENAME,
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
					resultSvc.addUserDefinedTable(Constant.CVP_START_FILENAME,
					        tableSvc.getWsiDiTable(tableSvc.getWsidiForCVPFullFileName()));
				} catch (CalLiteGUIException ex) {
					LOG.error(ex);
					errorHandlingSvc.businessErrorHandler((JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME), ex);
					return false;
				}
			}
			progressFrame.makeDialogVisible();
			proceed = ResultSvcImpl.getResultSvcImplInstance().save(clsFileName,
			        XMLParsingSvcImpl.getXMLParsingSvcImplInstance().getSwingEngine(),
			        SeedDataSvcImpl.getSeedDataSvcImplInstance().getSeedDataBOList());
			LOG.debug("Save Complete. " + clsFileName);
			auditSvc.clearAudit();
			return proceed;
		}
		return false;
	}

	@Override
	public void runSingleBatch() {
		String clsFileName = ((JTextField) swingEngine.find("run_txfScen")).getText();
		clsFileName = FilenameUtils.removeExtension(clsFileName);
		ProgressFrame progressFrame = ProgressFrame.getProgressFrameInstance();
		boolean isSaved = saveCurrentStateToFile(clsFileName, false);
		if (isSaved) {
			List<String> fileName = Arrays.asList(clsFileName);
			progressFrame.addScenarioNamesAndAction(clsFileName, Constant.BATCH_RUN);
			progressFrame.makeDialogVisible();
			batchRunSvc.doBatch(fileName, swingEngine, false);
		}
	}

	@Override
	public void runMultipleBatch() {
		ProgressFrame progressFrame = ProgressFrame.getProgressFrameInstance();
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
			progressFrame.addScenarioNamesAndAction(fileNames, Constant.BATCH_RUN);
			progressFrame.makeDialogVisible();
			batchRunSvc.doBatch(fileNames, swingEngine, false);
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
			if (!((numrows - 1 == rowsselected[rowsselected.length - 1] - rowsselected[0] && numrows == rowsselected.length)
			        && (numcols - 1 == colsselected[colsselected.length - 1] - colsselected[0]
			                && numcols == colsselected.length))) {
				JOptionPane.showMessageDialog(null, "Invalid Copy Selection", "Invalid Copy Selection", JOptionPane.ERROR_MESSAGE);
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
			// int rowCount = new StringTokenizer(totalData, Constant.NEW_LINE).countTokens();
			int colCount = new StringTokenizer(new StringTokenizer(totalData, Constant.NEW_LINE).nextToken(), Constant.TAB_SPACE)
			        .countTokens();
			if (colCount > table.getColumnCount()) {
				// JOptionPane.showMessageDialog(null, "The column's you selected is more then the column's of the table.", "Error",
				// JOptionPane.ERROR_MESSAGE);
				errorHandlingSvc.validationeErrorHandler("The column's you selected is more then the column's of the table.",
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
		} catch (ArrayIndexOutOfBoundsException ex) {
			errorHandlingSvc.validationeErrorHandler("Please select the field from where you want to paste the data",
			        "Please select the field from where you want to paste the data",
			        (JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME));
		} catch (Exception ex) {
			LOG.debug(ex.getMessage());
			errorHandlingSvc.businessErrorHandler((JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME), new CalLiteGUIException(ex));
		}

	}

	@Override
	public void runSingleBatchForWsiDi() {
		String clsFileName = ((JTextField) swingEngine.find("run_txfScen")).getText();
		clsFileName = FilenameUtils.removeExtension(clsFileName);
		ProgressFrame progressFrame = ProgressFrame.getProgressFrameInstance();
		boolean isSaved = saveCurrentStateToFile(clsFileName, false);
		if (isSaved) {
			List<String> fileName = Arrays.asList(clsFileName);
			progressFrame.addScenarioNamesAndAction(clsFileName, Constant.BATCH_RUN_WSIDI);
			progressFrame.makeDialogVisible();
			batchRunSvc.doBatch(fileName, swingEngine, true);
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
			DataTableModle cvpDtm = tableSvc.getWsiDiTable(cvpFullFileName);
			cvpDtm.setTableName(Constant.USER_DEFINED);
			cvpDtm.setCellEditable(true);
			cvpDtm.setSwingEngine(swingEngine);
			resultSvc.addUserDefinedTable(Constant.CVP_START_FILENAME, cvpDtm);
			tableSvc.setWsidiForCVPFullFileName(Constant.USER_DEFINED);
			// for SWP table
			DataTableModle swpDtm = tableSvc.getWsiDiTable(swpFullFileName);
			swpDtm.setTableName(Constant.USER_DEFINED);
			swpDtm.setCellEditable(true);
			swpDtm.setSwingEngine(swingEngine);
			resultSvc.addUserDefinedTable(Constant.SWP_START_FILENAME, swpDtm);
			tableSvc.setWsidiForSWPFullFileName(Constant.USER_DEFINED);
			JLabel jLabel = (JLabel) swingEngine.find("op_WSIDI_Status");
			jLabel.setText("WSI/DI read from [" + Paths.get(swpFullFileName).getFileName().toString() + " , "
			        + Paths.get(cvpFullFileName).getFileName().toString() + "]" + Constant.UNEDITED_FORLABEL);
			JComponent component = (JComponent) swingEngine.find("op_btn1");
			editButtonOnOperations(component);
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
			GuiLinks4BO guiLinks4BO = seedDataSvc.getObjByRunBasisLodCcprojCcmodelIds(labelNames.get(0));
			String swpFullFileName = Constant.MODEL_W2_WRESL_LOOKUP_DIR + "\\WSIDI\\" + Constant.SWP_START_FILENAME
			        + Constant.UNDER_SCORE + guiLinks4BO.getLookup() + Constant.TABLE_EXT;
			String cvpFullFileName = Constant.MODEL_W2_WRESL_LOOKUP_DIR + "\\WSIDI\\" + Constant.CVP_START_FILENAME
			        + Constant.UNDER_SCORE + guiLinks4BO.getLookup() + Constant.TABLE_EXT;
			// To Load CVP table
			DataTableModle cvpDtm = tableSvc.getWsiDiTable(cvpFullFileName);
			cvpDtm.setTableName(FilenameUtils.removeExtension(Paths.get(cvpFullFileName).getFileName().toString()));
			cvpDtm.setCellEditable(true);
			cvpDtm.setSwingEngine(swingEngine);
			resultSvc.addUserDefinedTable(Constant.CVP_START_FILENAME, cvpDtm);
			tableSvc.setWsidiForCVPFullFileName(cvpFullFileName);
			// To Load SWP table
			DataTableModle swpDtm = tableSvc.getWsiDiTable(swpFullFileName);
			swpDtm.setTableName(FilenameUtils.removeExtension(Paths.get(swpFullFileName).getFileName().toString()));
			swpDtm.setCellEditable(true);
			swpDtm.setSwingEngine(swingEngine);
			resultSvc.addUserDefinedTable(Constant.SWP_START_FILENAME, swpDtm);
			tableSvc.setWsidiForSWPFullFileName(swpFullFileName);
			JLabel jLabel = (JLabel) swingEngine.find("op_WSIDI_Status");
			jLabel.setText(labelNames.get(1) + Constant.UNEDITED_FORLABEL);
			JComponent component = (JComponent) swingEngine.find("op_btn1");
			editButtonOnOperations(component);
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
		DataTableModle dataTableModle = null;
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
					dataTableModle = resultSvc.getUserDefinedTable(tableName);
					dataTableModle.setSwingEngine(swingEngine);
				} else {
					dataTableModle = tableSvc.getWsiDiTable(fileName);
					dataTableModle.setTableName(FilenameUtils.removeExtension(Paths.get(fileName).getFileName().toString()));
					dataTableModle.setCellEditable(true);
					dataTableModle.setSwingEngine(swingEngine);
					resultSvc.addUserDefinedTable(tableName, dataTableModle);
				}
				showTableOnOperations(dataTableModle);
			}
		} catch (CalLiteGUIException ex) {
			errorHandlingSvc.businessErrorHandler((JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME), ex);
		}
	}

	/**
	 * This method is to ask the use whether he wants to contumue or not. This is used in the "Oprtations" tab.
	 *
	 * @return will return true if user wants to continue.
	 */
	private boolean isOkToContinueOnOperations() {
		String label = ((JLabel) swingEngine.find("op_WSIDI_Status")).getText();
		if (label.contains(Constant.UNEDITED_FORLABEL)) {
			return true;
		}
		return (JOptionPane.showConfirmDialog(null,
		        "WSI/DI data tables have been modified.  Are you sure you wish to overwrite these changes?", "CalLite GUI",
		        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
	}

	/**
	 * This method is used to show the table in the "Operations" tab.
	 *
	 * @param dataTableModle
	 *            Table data to show.
	 */
	private void showTableOnOperations(DataTableModle dataTableModle) {
		JComponent component = (JComponent) swingEngine.find("scrOpValues");
		JTable table = (JTable) swingEngine.find("tblOpValues");
		table.setModel(dataTableModle);
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
				// Read all pathnames from the DSS file and set the F-PART textfield as
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
		JOptionPane
		        .showMessageDialog(null,
		                "CalLite v. " + properties.getProperty("version.id") + "\nBuild date: "
		                        + properties.getProperty("build.date") + "\nYour last GUI xml revision date: " + guiXmlDate,
		                "About CalLite", JOptionPane.INFORMATION_MESSAGE);
	}

	@Override
	public void windowClosing() {
		ImageIcon icon = new ImageIcon(getClass().getResource("/images/CalLiteIcon.png"));
		JOptionPane optionPane;
		if (auditSvc.hasValues()) {
			Object[] options = { "save & exit", "exit without save", "cancel" };
			optionPane = new JOptionPane("The state is not saved do you want to save it.", JOptionPane.QUESTION_MESSAGE,
			        JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[0]);
			JDialog dialog = optionPane.createDialog("CalLite");
			dialog.setIconImage(icon.getImage());
			dialog.setResizable(false);
			dialog.setVisible(true);
			if (optionPane.getValue().toString().equals("save & exit")) {
				boolean isSaved = saveCurrentStateToFile();
				if (!isSaved)
					errorHandlingSvc.businessErrorHandler("We encounter a problem when saveing the file.", "",
					        (JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME));
				System.exit(0);
			} else if (optionPane.getValue().toString().equals("exit without save")) {
				System.exit(0);
			}
		} else {
			Object[] options = { "ok", "cancel" };
			optionPane = new JOptionPane("Are you sure that you want to exit.", JOptionPane.QUESTION_MESSAGE,
			        JOptionPane.OK_CANCEL_OPTION, null, options, options[0]);
			JDialog dialog = optionPane.createDialog("CalLite");
			dialog.setIconImage(icon.getImage());
			dialog.setResizable(false);
			dialog.setVisible(true);
			if (optionPane.getValue().toString().equals("ok")) {
				System.exit(0);
			}
		}
	}
}