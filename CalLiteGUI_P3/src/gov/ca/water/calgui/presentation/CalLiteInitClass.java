package gov.ca.water.calgui.presentation;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.jfree.util.Log;
import org.swixml.SwingEngine;

import calsim.app.AppUtils;
import calsim.app.DerivedTimeSeries;
import calsim.app.MultipleTimeSeries;
import calsim.gui.GuiUtils;
import gov.ca.water.calgui.bo.CalLiteGUIException;
import gov.ca.water.calgui.bo.DataTableModle;
import gov.ca.water.calgui.bus_delegate.IAllButtonsDele;
import gov.ca.water.calgui.bus_delegate.IVerifyControlsDele;
import gov.ca.water.calgui.bus_delegate.impl.AllButtonsDeleImp;
import gov.ca.water.calgui.bus_delegate.impl.ApplyDynamicConDeleImp;
import gov.ca.water.calgui.bus_delegate.impl.VerifyControlsDeleImp;
import gov.ca.water.calgui.bus_service.IResultSvc;
import gov.ca.water.calgui.bus_service.ISeedDataSvc;
import gov.ca.water.calgui.bus_service.ITableSvc;
import gov.ca.water.calgui.bus_service.IXMLParsingSvc;
import gov.ca.water.calgui.bus_service.impl.BatchRunSvcImpl;
import gov.ca.water.calgui.bus_service.impl.DynamicControlSvcImpl;
import gov.ca.water.calgui.bus_service.impl.ResultSvcImpl;
import gov.ca.water.calgui.bus_service.impl.SeedDataSvcImpl;
import gov.ca.water.calgui.bus_service.impl.TableSvcImpl;
import gov.ca.water.calgui.bus_service.impl.XMLParsingSvcImpl;
import gov.ca.water.calgui.constant.Constant;
import gov.ca.water.calgui.results.CustomResultsAction;
import gov.ca.water.calgui.results.DisplayFrame;
import gov.ca.water.calgui.results.FileDialog;
import gov.ca.water.calgui.results.ReportAction;
import gov.ca.water.calgui.results.ReportListener;
import gov.ca.water.calgui.results.ResultUtils;
import gov.ca.water.calgui.results.SchematicAction;
import gov.ca.water.calgui.results.SchematicListener;
import gov.ca.water.calgui.results.SchematicMain;
import gov.ca.water.calgui.results.WRIMSGUILinks;
import gov.ca.water.calgui.tech_service.IAuditSvc;
import gov.ca.water.calgui.tech_service.IErrorHandlingSvc;
import gov.ca.water.calgui.tech_service.impl.AuditSvcImpl;
import gov.ca.water.calgui.tech_service.impl.ErrorHandlingSvcImpl;
import vista.set.DataReference;
import vista.set.Group;

/**
 * This class is for initializing the Application and adding the Action, Item,
 * Mouse Listener's to the main frame.
 *
 * @author mohan
 *
 */
public class CalLiteInitClass {
	private static final Logger LOG = Logger.getLogger(CalLiteInitClass.class.getName());
	private SwingEngine swingEngine;
	private IAuditSvc auditSvc;
	private IErrorHandlingSvc errorHandlingSvc = new ErrorHandlingSvcImpl();

	/**
	 * This method is called to initialize the ui.
	 */
	public void init() {
		// Building all the Services.
		ISeedDataSvc seedDataSvc = SeedDataSvcImpl.getSeedDataSvcImplInstance();
		IXMLParsingSvc xmlParsingSvc = XMLParsingSvcImpl.getXMLParsingSvcImplInstance();
		IVerifyControlsDele verifyControlsDele = new VerifyControlsDeleImp();
		verifyControlsDele.verifyTheDataBeforeUI(Constant.SCENARIOS_DIR + Constant.DEFAULT + Constant.CLS_EXT);
		DynamicControlSvcImpl.getDynamicControlSvcImplInstance();
		ITableSvc tableSvc = TableSvcImpl.getTableSvcImplInstance(seedDataSvc.getUserTables());
		this.swingEngine = xmlParsingSvc.getSwingEngine();
		IResultSvc resultSvc = ResultSvcImpl.getResultSvcImplInstance();
		IAllButtonsDele allButtonsDele = new AllButtonsDeleImp();
		// Set up the GUI
		// Set up month spinners
		JSpinner spnSM1 = (JSpinner) swingEngine.find("spnRunStartMonth");
		setMonthModelAndIndex(spnSM1, 9);
		JSpinner spnEM1 = (JSpinner) swingEngine.find("spnRunEndMonth");
		setMonthModelAndIndex(spnEM1, 8);
		// Set up year spinners
		JSpinner spnSY1 = (JSpinner) swingEngine.find("spnRunStartYear");
		setNumberModelAndIndex(spnSY1, 1921, Constant.MIN_YEAR, Constant.MAX_YEAR, 1, "####");
		JSpinner spnEY1 = (JSpinner) swingEngine.find("spnRunEndYear");
		setNumberModelAndIndex(spnEY1, 2003, Constant.MIN_YEAR, Constant.MAX_YEAR, 1, "####");
		// Setting up all the Listener's.
		swingEngine.setActionListener(swingEngine.find(Constant.MAIN_FRAME_NAME), new GlobalActionListener());
		setCheckBoxorMouseListener(swingEngine.find(Constant.MAIN_FRAME_NAME), new GlobalMouseListener());
		setCheckBoxorRadioButtonItemListener(swingEngine.find(Constant.MAIN_FRAME_NAME), new GlobalItemListener());
		ImageIcon icon = new ImageIcon(getClass().getResource("/images/CalLiteIcon.png"));
		((JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME)).setIconImage(icon.getImage());
		((JTabbedPane) swingEngine.find("reg_tabbedPane")).addChangeListener(new GlobalChangeListener());
		((JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME))
				.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);// EXIT_ON_CLOSE
		((JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME)).addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				allButtonsDele.windowClosing();
			}
		});
		// Loading the default cls file.
		resultSvc.applyClsFile(Constant.SCENARIOS_DIR + Constant.DEFAULT + Constant.CLS_EXT, swingEngine,
				seedDataSvc.getTableIdMap());
		// check
		checkForNewUserDefinedTables(xmlParsingSvc.getNewUserDefinedTables(), resultSvc, tableSvc, swingEngine);
		auditSvc = AuditSvcImpl.getAuditSvcImplInstance();
		auditSvc.clearAudit(); // we clear because when we 1st load the cls file
								// we should not have any records.
		addJTextFieldListener(xmlParsingSvc.getjTextFieldIds());
		addJSpinnerListener();
		// Count threads and update selector appropriately
		int maxThreads = Math.max(1, Runtime.getRuntime().availableProcessors());
		BatchRunSvcImpl.simultaneousRuns = maxThreads;
		((JSlider) swingEngine.find("run_sldThreads")).addChangeListener(new GlobalChangeListener());
		((JSlider) swingEngine.find("run_sldThreads")).setEnabled(maxThreads > 1);
		((JSlider) swingEngine.find("run_sldThreads")).setMaximum(maxThreads);
		((JLabel) swingEngine.find("run_lblThreads")).setText(" " + maxThreads + ((maxThreads > 1) ? " runs" : " run"));
		((JLabel) swingEngine.find("run_lblThreadsInfo"))
				.setText("Simultaneous runs " + ((maxThreads > 1) ? "(1-" + maxThreads + ")" : "(1)"));
		// For Result part.
		ResultUtils resultUtils = ResultUtils.getXMLParsingSvcImplInstance(swingEngine);
		swingEngine.setActionListener(swingEngine.find("Reporting"), new ReportAction(swingEngine));
		swingEngine.setActionListener(swingEngine.find("externalPDF"), new ReportAction(swingEngine));
		setCheckBoxorRadioButtonItemListener(swingEngine.find("Reporting"), new ReportListener(swingEngine));
		setCheckBoxorMouseListener(swingEngine.find("Reporting"), resultUtils);
		// Setup for Reporting page
		// Set up additional UI elements
		JList lstScenarios = (JList) swingEngine.find("SelectedList");
		JRadioButton rdb1 = (JRadioButton) swingEngine.find("rdbp001");
		JRadioButton rdb2 = (JRadioButton) swingEngine.find("rdbp002");
		FileDialog fdDSSFiles = new FileDialog(lstScenarios, (JLabel) swingEngine.find("lblBase"), rdb1, rdb2, true);
		resultUtils.setFdDSSFiles(fdDSSFiles);
		lstScenarios.setModel(fdDSSFiles.lmScenNames);
		lstScenarios.setBorder(new LineBorder(Color.gray, 1));
		JButton btnScenario = (JButton) swingEngine.find("btnAddScenario");
		btnScenario.addActionListener(fdDSSFiles);
		JButton btnScenarioDel = (JButton) swingEngine.find("btnDelScenario");
		btnScenarioDel.addActionListener(fdDSSFiles);
		JButton btnClearAll = (JButton) swingEngine.find("btnClearScenario");
		btnClearAll.addActionListener(fdDSSFiles);
		// Set up month spinners
		JSpinner spnSM = (JSpinner) swingEngine.find("spnStartMonth");
		resultUtils.SetMonthModelAndIndex(spnSM, 9, resultUtils, true);
		JSpinner spnEM = (JSpinner) swingEngine.find("spnEndMonth");
		resultUtils.SetMonthModelAndIndex(spnEM, 8, resultUtils, true);
		// Set up year spinners
		JSpinner spnSY = (JSpinner) swingEngine.find("spnStartYear");
		resultUtils.SetNumberModelAndIndex(spnSY, 1921, 1921, 2003, 1, "####", resultUtils, true);
		JSpinner spnEY = (JSpinner) swingEngine.find("spnEndYear");
		resultUtils.SetNumberModelAndIndex(spnEY, 2003, 1921, 2003, 1, "####", resultUtils, true);
		// Set up report list
		JList lstReports = (JList) swingEngine.find("lstReports");
		lstReports.setBorder(new LineBorder(Color.gray, 1));
		lstReports.setVisible(true);
		// for Custom Results.
		swingEngine.setActionListener(swingEngine.find("Custom"), new CustomResultsAction());
		WRIMSGUILinks.buildWRIMSGUI((JPanel) swingEngine.find("WRIMS"));
		WRIMSGUILinks.setStatus("Initialized.");
		// Replace WRIMS GUI display action with CalLite GUI action
		JButton retrieveBtn = GuiUtils.getCLGPanel().getRetrievePanel().getRetrieveBtn();
		for (ActionListener al : retrieveBtn.getActionListeners()) {
			retrieveBtn.removeActionListener(al);
		}
		retrieveBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				retrieve();
			}
		});
		Component openButtonComponent = findFirstButtonWithLabel(GuiUtils.getCLGPanel(), "Open");
		if (openButtonComponent != null) {
			JButton openButton = (JButton) openButtonComponent;
			for (ActionListener al : openButton.getActionListeners()) {
				openButton.removeActionListener(al);
			}
			openButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					retrieve2();
				}
			});
		}
		// PDF Report
		((JButton) swingEngine.find("btnGetTemplateFile"))
				.addActionListener(new FileDialog(null, (JTextField) swingEngine.find("tfTemplateFILE"), "inp"));
		((JButton) swingEngine.find("btnGetReportFile1"))
				.addActionListener(new FileDialog(null, (JTextField) swingEngine.find("tfReportFILE1")));
		((JButton) swingEngine.find("btnGetReportFile2"))
				.addActionListener(new FileDialog(null, (JTextField) swingEngine.find("tfReportFILE2")));
		((JButton) swingEngine.find("btnGetReportFile3"))
				.addActionListener(new FileDialog(null, (JTextField) swingEngine.find("tfReportFILE3"), "PDF"));
		// Schematic views
		new SchematicMain((JPanel) swingEngine.find("schematic_holder"),
				"file:///" + System.getProperty("user.dir") + "/Config/callite_merged.svg", swingEngine, 1.19, 0.0, 0.0,
				1.19, -8.0, 5.0);
		new SchematicMain((JPanel) swingEngine.find("schematic_holder2"),
				"file:///" + System.getProperty("user.dir") + "/Config/callite-massbalance_working.svg", swingEngine,
				1.2, 0, 0.0, 1.2, 21.0, 15.0);
		swingEngine.setActionListener(swingEngine.find("schematics"), new SchematicAction());
		setCheckBoxorRadioButtonItemListener(swingEngine.find("schematics"), new SchematicListener(swingEngine));
		// Recolor results tabs
		JTabbedPane jTabbedPane = (JTabbedPane) swingEngine.find("tabbedPane1");
		jTabbedPane.setForegroundAt(6, Color.blue);
		jTabbedPane.setForegroundAt(7, Color.blue);
		jTabbedPane.setForegroundAt(8, Color.blue);
		jTabbedPane.setForegroundAt(9, Color.blue);
		jTabbedPane.setBackgroundAt(6, Color.WHITE);
		jTabbedPane.setBackgroundAt(7, Color.WHITE);
		jTabbedPane.setBackgroundAt(8, Color.WHITE);
		jTabbedPane.setBackgroundAt(9, Color.WHITE);
		jTabbedPane.addChangeListener(resultUtils);
		JMenuBar menuBar = (JMenuBar) this.swingEngine.find("menu");
		((JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME)).setJMenuBar(menuBar);
		menuBar.setVisible(true);
		((JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME)).addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent event) {
				int height = event.getComponent().getHeight();
				int width = event.getComponent().getWidth();
				((JPanel) swingEngine.find("schematic_holder"))
						.setPreferredSize(new Dimension(width - 50, height - 200));
				SwingUtilities.updateComponentTreeUI(event.getComponent());
			}
		});

		new ApplyDynamicConDeleImp().applyDynamicControlForListFromFile();
		// Display the GUI
		swingEngine.find(Constant.MAIN_FRAME_NAME).setVisible(true);

		/**
		 * This will ask the user to select the wsidi tables whether to use the
		 * default or from the cls file.
		 * allButtonsDele.decisionSVInitFilesAndTableInOperations();
		 */
	}

	/**
	 * This method will check for the new tables which are defined in the
	 * gui.xml file and load them.
	 *
	 * @param newUserDefinedIds
	 * @param resultSvc
	 * @param tableSvc
	 * @param swingEngine
	 */
	public void checkForNewUserDefinedTables(List<String> newUserDefinedIds, IResultSvc resultSvc, ITableSvc tableSvc,
			SwingEngine swingEngine) {
		DataTableModle dtm = null;
		for (String newUserDefinedId : newUserDefinedIds) {
			if (resultSvc.hasUserDefinedTable(newUserDefinedId)) {
				dtm = resultSvc.getUserDefinedTable(newUserDefinedId);
				((JTable) swingEngine.find(newUserDefinedId)).setModel(dtm);
			} else {
				try {
					dtm = tableSvc.getTable(newUserDefinedId, TableSvcImpl::handleTableFileWithColumnNumber);
					dtm.setCellEditable(true);
					resultSvc.addUserDefinedTable(newUserDefinedId, dtm);
					((JTable) swingEngine.find(newUserDefinedId)).setModel(dtm);
				} catch (CalLiteGUIException ex) {
					Log.error(ex);
					errorHandlingSvc.displayErrorMessageBeforeTheUI(new CalLiteGUIException(
							"There is a table id " + newUserDefinedId
									+ " in the gui.xml file but there is no table file with that name. Please provide the file.",
							ex));
				}
			}
		}
	}

	/**
	 * This method is for loading the strings in the {@link JSpinneer}.
	 *
	 * @param jspn
	 * @param idx
	 */
	public void setMonthModelAndIndex(JSpinner jspn, int idx) {
		String[] monthNames = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
		try {
			SpinnerListModel monthModel = new SpinnerListModel(monthNames);
			jspn.setModel(monthModel);
			jspn.setValue(monthNames[idx]);
		} catch (Exception e) {
		}
	}

	/**
	 * This method will set the max and min values for the {@link JSpinner}
	 * object.
	 *
	 * @param jspn
	 * @param value
	 * @param min
	 * @param max
	 * @param step
	 * @param format
	 */
	public void setNumberModelAndIndex(JSpinner jspn, int value, int min, int max, int step, String format) {
		SpinnerModel spnmod = new SpinnerNumberModel(value, min, max, step);
		jspn.setModel(spnmod);
		jspn.setEditor(new JSpinner.NumberEditor(jspn, format));
	}

	/**
	 * This method will set the item listener for the component and for all it's
	 * children which are Check Box and radio button.
	 *
	 * @param component
	 * @param itemListener
	 *            Object of the Item Listener.
	 */
	public void setCheckBoxorRadioButtonItemListener(Component component, Object itemListener) {
		if (component instanceof JCheckBox || component instanceof JRadioButton) {
			((AbstractButton) component).addItemListener((ItemListener) itemListener);
		}
		for (Component child : ((Container) component).getComponents()) {
			setCheckBoxorRadioButtonItemListener(child, itemListener);
		}
	}

	/**
	 * This method will set the mouse listener for the component and for all
	 * it's children which are Check Box.
	 *
	 * @param component
	 * @param mouseListener
	 *            Object of the Mouse Listener.
	 */
	public void setCheckBoxorMouseListener(Component component, Object mouseListener) {
		if (component instanceof JCheckBox) {
			((AbstractButton) component).addMouseListener((MouseListener) mouseListener);
		}
		for (Component child : ((Container) component).getComponents()) {
			setCheckBoxorMouseListener(child, mouseListener);
		}
	}

	/**
	 * This method is to add the listrnrt for the {@link JTextField} for
	 * tracking the changes.
	 */
	private void addJTextFieldListener(List<String> listOfNames) {
		FocusListener focusListenerForTextField = new FocusListener() {
			String oldValue = "";

			@Override
			public void focusLost(FocusEvent e) {
				JTextField field = ((JTextField) e.getComponent());
				String newValue = field.getText();
				if (!oldValue.equals(newValue)) {
					auditSvc.addAudit(field.getName(), oldValue, newValue);
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
				oldValue = ((JTextField) e.getComponent()).getText();
			}
		};
		FocusListener focusListenerForTextArea = new FocusListener() {
			String oldValue = "";

			@Override
			public void focusLost(FocusEvent e) {
				JTextArea field = ((JTextArea) e.getComponent());
				String newValue = field.getText();
				if (!oldValue.equals(newValue)) {
					auditSvc.addAudit(field.getName(), oldValue, newValue);
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
				oldValue = ((JTextArea) e.getComponent()).getText();
			}
		};
		for (String name : listOfNames) {
			((JTextField) swingEngine.find(name)).addFocusListener(focusListenerForTextField);
		}
		// JTextField demtxt1 = (JTextField) swingEngine.find("demtxt1");
		// demtxt1.addFocusListener(focusListenerForTextField);
		// JTextField demtxt2 = (JTextField) swingEngine.find("demtxt2");
		// demtxt2.addFocusListener(focusListenerForTextField);
		// JTextField demtxt3 = (JTextField) swingEngine.find("demtxt3");
		// demtxt3.addFocusListener(focusListenerForTextField);
		// JTextField demtxt3a = (JTextField) swingEngine.find("demtxt3a");
		// demtxt3a.addFocusListener(focusListenerForTextField);
		// JTextField demtxt4 = (JTextField) swingEngine.find("demtxt4");
		// demtxt4.addFocusListener(focusListenerForTextField);
		// JTextField demtxt5 = (JTextField) swingEngine.find("demtxt5");
		// demtxt5.addFocusListener(focusListenerForTextField);
		// JTextField demtxt6 = (JTextField) swingEngine.find("demtxt6");
		// demtxt6.addFocusListener(focusListenerForTextField);
		// JTextField hydDssSv = (JTextField) swingEngine.find("hyd_DSS_SV");
		// hydDssSv.addFocusListener(focusListenerForTextField);
		// JTextField hydDssSvF = (JTextField) swingEngine.find("hyd_DSS_SV_F");
		// hydDssSvF.addFocusListener(focusListenerForTextField);
		// JTextField hydDssInit = (JTextField)
		// swingEngine.find("hyd_DSS_Init");
		// hydDssInit.addFocusListener(focusListenerForTextField);
		// JTextField hydDssInitF = (JTextField)
		// swingEngine.find("hyd_DSS_Init_F");
		// hydDssInitF.addFocusListener(focusListenerForTextField);
		JTextArea run_txaScenDesc = (JTextArea) swingEngine.find("run_txaScenDesc");
		run_txaScenDesc.addFocusListener(focusListenerForTextArea);
	}

	/**
	 * This method is to add the listrnrt for the {@link JSpinner} for tracking
	 * the changes.
	 */
	private void addJSpinnerListener() {
		ChangeListener listener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				auditSvc.addAudit(((JSpinner) e.getSource()).getName(), " ",
						String.valueOf(((JSpinner) e.getSource()).getValue()));
			}
		};
		JSpinner spnRunEndMonth = (JSpinner) swingEngine.find("spnRunEndMonth");
		spnRunEndMonth.addChangeListener(listener);
		JSpinner spnRunEndYear = (JSpinner) swingEngine.find("spnRunEndYear");
		spnRunEndYear.addChangeListener(listener);
	}

	/**
	 * Data retrieval for single DSS from Custom Results dashboard; modeled on
	 * calsim.gui.GeneralRetrievePanel.retrieve()
	 *
	 */
	void retrieve() {
		JList lstScenarios = (JList) swingEngine.find("SelectedList");
		if (!AppUtils.baseOn) {
			JOptionPane.showMessageDialog(null, "The Base DSS files need to be selected", "DSS Not Selected",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		try {
			String noRowsString = "";
			JTable _table = GuiUtils.getCLGPanel().getRetrievePanel().getTable();
			if (_table.getRowCount() == 0)
				noRowsString = " after using \"Filter\" to load variables";
			Group _group = GuiUtils.getCLGPanel().getRetrievePanel().getGroup();
			if (_group == null || _table.getSelectedRowCount() == 0) {
				JOptionPane.showMessageDialog(null, "Select one or more variables" + noRowsString,
						"Variable(s) Not Selected", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			int[] rows = _table.getSelectedRows(); // checked if count > 0 above
			DataReference[] array = new DataReference[rows.length];
			for (int i = 0; i < rows.length; i++)
				array[i] = _group.getDataReference(rows[i]);
			// GuiUtils.displayData(array);
			for (int i = 0; i < rows.length; i++) {
				System.out.println(array[i]);

				String[] parts = array[i].getName().split("::");
				String[] parts2 = parts[2].split("/");
				parts[2] = "/" + parts2[1] + "/" + parts2[2] + "/" + parts2[3] + "/" + parts[3] + "/" + parts2[5] + "/"
						+ parts2[6] + "/";

				if (parts[1].toUpperCase().contains(("_SV.DSS"))) {
					DisplayFrame.showDisplayFrames(DisplayFrame.quickState() + ";Locs-" + parts[2] + ";Index-"
							+ parts[2] + ";File-" + parts[1], lstScenarios);
				} else {
					DisplayFrame.showDisplayFrames(
							DisplayFrame.quickState() + ";Locs-" + parts[2] + ";Index-" + parts[2], lstScenarios);
				}
			}
		} catch (Exception e) {

			// VistaUtils.displayException(GuiUtils.getCLGPanel(), e);
			LOG.debug("Error in retrieve() -", e);
		}
		WRIMSGUILinks.setStatus("Well??");
	}

	/**
	 * Data retrieval for DTS/MTS from Custom Results dashboard; modeled on
	 * calsim.gui.GeneralRetrievePanel.retrieve()
	 *
	 */

	void retrieve2() {
		JList lstScenarios = (JList) swingEngine.find("SelectedList");
		WRIMSGUILinks.setStatus("Retrieve2");

		if (!AppUtils.baseOn) {
			JOptionPane.showMessageDialog(null, "The Base DSS files need to be selected", "DSS Not Selected",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		DerivedTimeSeries dts = GuiUtils.getCLGPanel().getDtsTreePanel().getTable().getDTS();
		MultipleTimeSeries mts = GuiUtils.getCLGPanel().getDtsTreePanel().getTable().getMTS();

		if (((mts == null) && (dts == null)) || ((dts != null) && (dts.getBParts().size() < 1))
				|| ((mts != null) && (mts.getNumberOfDataReferences() < 1))) {
			JOptionPane.showMessageDialog(null, "Specify DTS or MTS data references", "Nothing to Display",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		try {
			DisplayFrame.showDisplayFrames_WRIMS(DisplayFrame.quickState() + ";Locs-;Index-;File-", lstScenarios, dts,
					mts);

		} catch (Exception e) {
			// VistaUtils.displayException(GuiUtils.getCLGPanel(), e);
			LOG.debug("Error in retrieve2() -", e);

		}
		WRIMSGUILinks.setStatus("Done??");
	}

	public static Component findFirstButtonWithLabel(Component comp, String l) {

		if ((comp instanceof JButton)) {
			if (((JButton) comp).getText().equals(l))
				return comp;
		}

		if (comp instanceof Container) {
			Container container = (Container) comp;
			for (int i = 0; i < container.getComponentCount(); i++) {
				Component comp2 = findFirstButtonWithLabel(container.getComponent(i), l);
				if (comp2 != null)
					return comp2;
			}
		}
		return null;
	}
}