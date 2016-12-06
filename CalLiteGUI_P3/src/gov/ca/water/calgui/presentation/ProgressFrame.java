package gov.ca.water.calgui.presentation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.swixml.SwingEngine;

import gov.ca.water.calgui.bo.CalLiteGUIException;
import gov.ca.water.calgui.bo.DataTableModle;
import gov.ca.water.calgui.bus_delegate.IAllButtonsDele;
import gov.ca.water.calgui.bus_delegate.impl.AllButtonsDeleImp;
import gov.ca.water.calgui.bus_service.IDynamicControlSvc;
import gov.ca.water.calgui.bus_service.IMonitorSvc;
import gov.ca.water.calgui.bus_service.IResultSvc;
import gov.ca.water.calgui.bus_service.ITableSvc;
import gov.ca.water.calgui.bus_service.impl.DynamicControlSvcImpl;
import gov.ca.water.calgui.bus_service.impl.MonitorSvcImpl;
import gov.ca.water.calgui.bus_service.impl.ResultSvcImpl;
import gov.ca.water.calgui.bus_service.impl.TableSvcImpl;
import gov.ca.water.calgui.bus_service.impl.XMLParsingSvcImpl;
import gov.ca.water.calgui.constant.Constant;
import gov.ca.water.calgui.results.ResultUtils;

/**
 * This frame is used for displaying the monitor status of batch run and save.
 *
 * @author mohan
 */
public final class ProgressFrame extends JFrame implements ActionListener {

	private static final Logger LOG = Logger.getLogger(ProgressFrame.class.getName());
	private static final long serialVersionUID = -606008444073979623L;
	private static ProgressFrame progressFrame;
	private JList list;
	private JScrollPane listScroller;
	private Map<String, String> scenarioNamesAndAction;
	private IMonitorSvc monitorSvc = new MonitorSvcImpl();
	private ITableSvc tableSvc = TableSvcImpl.getTableSvcImplInstance(null);
	private IResultSvc resultSvc = ResultSvcImpl.getResultSvcImplInstance();
	private IAllButtonsDele allButtonsDele = new AllButtonsDeleImp();
	private SwingEngine swingEngine = XMLParsingSvcImpl.getXMLParsingSvcImplInstance().getSwingEngine();
	private IDynamicControlSvc dynamicControlSvc = DynamicControlSvcImpl.getDynamicControlSvcImplInstance();
	private SwingWorker<Void, String> workerScenarioMonitor = new SwingWorker<Void, String>() {

		private String[] oldValue;

		@Override
		protected Void doInBackground() throws InterruptedException {
			while (true) {
				if (isCancelled()) {
					return null;
				}
				Thread.sleep(2000);
				boolean sleepAfterDisplay = false;
				String[] listData = null;
				List<String> data = new ArrayList<String>();
				String text = "";
				if (scenarioNamesAndAction.isEmpty()) {
					listData = new String[1];
					listData[0] = "No active scenarios";
				} else {

					for (String scenarioName : scenarioNamesAndAction.keySet()) {
						switch (scenarioNamesAndAction.get(scenarioName)) {
						case Constant.SAVE:
							text = monitorSvc.save(scenarioName);
							data.add(text);
							if (text.endsWith("Save is completed.")) {
								sleepAfterDisplay = true;
								scenarioNamesAndAction.remove(scenarioName);
							}
							break;
						case Constant.BATCH_RUN:
							text = monitorSvc.batchRun(scenarioName);
							data.add(text);
							if (text.toLowerCase().endsWith("Run completed".toLowerCase())) {
								ResultUtils.getXMLParsingSvcImplInstance(null).getFdDSSFiles()
	                                    .addFileToList(new File(Constant.SCENARIOS_DIR + scenarioName + "_DV.DSS"));
								sleepAfterDisplay = true;
								scenarioNamesAndAction.remove(scenarioName);
							}
							break;
						case Constant.BATCH_RUN_WSIDI:
							text = monitorSvc.batchRunWsidi(scenarioName);
							data.add(text);
							if (text.toLowerCase().endsWith("DONE - run completed".toLowerCase())) {
								sleepAfterDisplay = true;
								loadGeneratedWSIDI(scenarioName);
								ResultUtils.getXMLParsingSvcImplInstance(null).getFdDSSFiles()
	                                    .addFileToList(new File(Constant.SCENARIOS_DIR + scenarioName + "_DV.DSS"));
								scenarioNamesAndAction.remove(scenarioName);
							}
							break;
						}
					}
					listData = new String[data.size()];
					for (int i = 0; i < data.size(); i++) {
						listData[i] = data.get(i);
					}
				}
				if (!Arrays.equals(oldValue, listData)) {
					setList(listData);
					oldValue = listData;
				}
				if (sleepAfterDisplay) {
					Thread.sleep(2000);
					sleepAfterDisplay = false;
				}
			}
		}

		@Override
		protected void done() {
			return;

		}
	};

	/**
	 * This method is for implementing the singleton.
	 *
	 * @return
	 */
	public static ProgressFrame getProgressFrameInstance() {
		if (progressFrame == null) {
			progressFrame = new ProgressFrame("");
		}
		return progressFrame;
	}

	/**
	 * This will prepare the Dialog box to show.
	 *
	 * @param title
	 */
	private ProgressFrame(String title) {
		this.scenarioNamesAndAction = new HashMap<String, String>();
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setPreferredSize(new Dimension(400, 210));
		setMinimumSize(new Dimension(400, 210));
		setLayout(new BorderLayout(5, 5));
		setTitle(title);
		String[] data = { "No scenarios active" };
		list = new JList(data);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(-1);
		list.setDragEnabled(true);
		list.setVisible(true);
		listScroller = new JScrollPane(list);
		listScroller.setPreferredSize(new Dimension(350, 150));
		listScroller.setMinimumSize(new Dimension(350, 150));
		listScroller.setVisible(true);
		add(BorderLayout.PAGE_START, listScroller);
		JButton btnClose = new JButton("Stop all runs");
		btnClose.setPreferredSize(new Dimension(50, 20));
		btnClose.setMinimumSize(new Dimension(50, 20));
		btnClose.addActionListener(this);
		btnClose.setActionCommand("Stop");
		btnClose.setVisible(true);
		add(BorderLayout.PAGE_END, btnClose);
		pack();
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((dim.width - 400) / 2, (dim.height - 200) / 2);
		java.net.URL imgURL = getClass().getResource("/images/CalLiteIcon.png");
		setIconImage(Toolkit.getDefaultToolkit().getImage(imgURL));
		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosed(java.awt.event.WindowEvent windowEvent) {
				workerScenarioMonitor.cancel(true);
				progressFrame = null;
			}
		});
		workerScenarioMonitor.execute();
	}

	/**
	 * This method will set the monitor window to visible.
	 */
	public void makeDialogVisible() {

		// this.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
		setVisible(true);
		// repaint();
		paintComponents(this.getGraphics());
		// print(this.getGraphics());
		// paintAll(getGraphics());
	}

	/**
	 * This will set the listData to the monitor window.
	 *
	 * @param listData
	 */
	public void setList(String[] listData) {
		if (!listScroller.isVisible()) {
			listScroller.setVisible(true);
		}
		list.setListData(listData);
		// repaint();
		paintComponents(this.getGraphics());
		// print(this.getGraphics());
		// paintAll(getGraphics());
	}

	/**
	 * This method will add the key and type to the monitor list.
	 *
	 * @param key
	 *            The name of which we should monitor.
	 * @param type
	 *            which type.
	 */
	public void addScenarioNamesAndAction(String key, String type) {
		scenarioNamesAndAction.put(key, type);
	}

	/**
	 * This method will add the list of key and type to the monitor list.
	 *
	 * @param keys
	 *            The names of which we should monitor.
	 * @param type
	 *            which type.
	 */
	public void addScenarioNamesAndAction(List<String> keys, String type) {
		keys.forEach(key -> scenarioNamesAndAction.put(key, type));
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if ("Go".equals(ae.getActionCommand())) {
			this.setVisible(true);
		} else if ("Stop".equals(ae.getActionCommand())) {
			Runtime rt = Runtime.getRuntime();
			Process proc;
			try {
				proc = rt.exec("taskkill /f /t /fi \"WINDOWTITLE eq CalLiteRun*\" ");
				scenarioNamesAndAction.clear();
			} catch (IOException ex) {
				LOG.error(ex);
			}
		}
	}

	/**
	 * After the WSIDI batch runs this method is called to load the tables in the "Operations" tab.
	 *
	 * @param scenarioName
	 *            The scenario file name which the batch program is run. the name should not have any extension.
	 */
	public void loadGeneratedWSIDI(String scenarioName) {
		Properties properties = new Properties();
		try {
			properties.load(GlobalActionListener.class.getClassLoader().getResourceAsStream("callite-gui.properties"));
		} catch (Exception e) {
			LOG.debug("Problem loading properties. " + e.getMessage());
		}
		String wsiDiSwpPath = Paths.get(Constant.RUN_DETAILS_DIR + scenarioName + Constant.RUN_DIR + Constant.LOOKUP_DIR
		        + Constant.SWP_START_FILENAME + Constant.TABLE_EXT).toString();
		String wsiDiCvpSwpPath = Paths.get(Constant.RUN_DETAILS_DIR + scenarioName + Constant.RUN_DIR + Constant.LOOKUP_DIR
		        + Constant.CVP_START_FILENAME + Constant.TABLE_EXT).toString();
		try {
			DataTableModle swpDtm = tableSvc.getWsiDiTable(wsiDiSwpPath);
			swpDtm.setCellEditable(true);
			swpDtm.setTableName(Constant.SWP_START_FILENAME);
			swpDtm.setSwingEngine(swingEngine);
			DataTableModle cvpDtm = tableSvc.getWsiDiTable(wsiDiCvpSwpPath);
			cvpDtm.setCellEditable(true);
			cvpDtm.setTableName(Constant.CVP_START_FILENAME);
			cvpDtm.setSwingEngine(swingEngine);
			resultSvc.addUserDefinedTable(Constant.SWP_START_FILENAME, swpDtm);
			resultSvc.addUserDefinedTable(Constant.CVP_START_FILENAME, cvpDtm);
			/*
			 * The following code we are sedtting the SWP and CVP file names as user defined because the table values we are getting
			 * after the batch run we consider them as user defined.
			 */
			tableSvc.setWsidiForSWPFullFileName(Constant.USER_DEFINED);
			tableSvc.setWsidiForCVPFullFileName(Constant.USER_DEFINED);
			JComponent component = (JComponent) swingEngine.find("op_btn1");
			allButtonsDele.editButtonOnOperations(component);
			List<String> value = dynamicControlSvc.getLabelAndGuiLinks4BOBasedOnTheRadioButtons(swingEngine);
			JLabel jLabel = (JLabel) swingEngine.find("op_WSIDI_Status");
			jLabel.setText(value.get(1) + " (Generated via " + Integer.parseInt(properties.getProperty("wsidi.iterations"))
			        + " iterations)");
		} catch (CalLiteGUIException ex) {
			LOG.error(ex);
		}
	}
}