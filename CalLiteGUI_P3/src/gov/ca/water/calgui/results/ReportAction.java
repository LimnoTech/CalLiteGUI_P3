package gov.ca.water.calgui.results;

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
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.swixml.SwingEngine;

import calsim.gui.DtsTreeModel;
import gov.ca.water.calgui.bo.DataTableModel;
import gov.ca.water.calgui.bus_delegate.IAllButtonsDele;
import gov.ca.water.calgui.bus_delegate.IScenarioDele;
import gov.ca.water.calgui.bus_delegate.impl.AllButtonsDeleImp;
import gov.ca.water.calgui.bus_delegate.impl.ScenarioDeleImp;
import gov.ca.water.calgui.constant.Constant;
import gov.ca.water.calgui.presentation.ScenarioFrame;

public class ReportAction implements ActionListener {
	private final SwingEngine swix;
	private final JList<String> lstScenarios;
	private static Logger log = Logger.getLogger(ReportAction.class.getName());
	static DtsTreeModel dtm;
	private IAllButtonsDele allButtonsDele = new AllButtonsDeleImp();

	public ReportAction(SwingEngine swix) {
		this.swix = swix;
		lstScenarios = (JList<String>) swix.find("SelectedList");
	}

	private void clearCheckboxes(String panelName) {
		JPanel panel = (JPanel) swix.find(panelName);
		Component[] components = panel.getComponents();
		for (int i = 0; i < components.length; i++) {
			if (components[i] instanceof JCheckBox) {
				JCheckBox c = (JCheckBox) components[i];
				c.setSelected(false);
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		System.out.println(e.getActionCommand());
		if (e.getActionCommand().startsWith("AC_PresetClear")) {
			clearCheckboxes("presets");
		} else if (e.getActionCommand().startsWith("AC_ShortageClear")) {
			clearCheckboxes("shortage");
		} else if (e.getActionCommand().startsWith("AC_SJRClear")) {
			clearCheckboxes("SJR Results");
		} else if (e.getActionCommand().startsWith("AC_WMAClear")) {
			clearCheckboxes("WMA");
		} else if (e.getActionCommand().startsWith("AC_DShortClear")) {
			clearCheckboxes("DShort");
			// / delta flow criteria
		} else if (e.getActionCommand().startsWith("AC_DfcClear")) {
			JPanel delta_flow_criteria = (JPanel) swix.find("delta_flow_criteria");
			Component[] components = delta_flow_criteria.getComponents();
			for (int i = 0; i < components.length; i++) {
				if (components[i] instanceof JCheckBox) {
					JCheckBox c = (JCheckBox) components[i];
					c.setSelected(false);
				}
			}
		} else if (e.getActionCommand().startsWith("AC_GenReport")) {
			if (((JTextField) swix.find("tfReportFILE1")).getText().isEmpty()
					|| ((JTextField) swix.find("tfReportFILE2")).getText().isEmpty()
					|| ((JTextField) swix.find("tfReportFILE3")).getText().isEmpty()) {
				JOptionPane.showMessageDialog(null, "You must specify the source DSS files and the output PDF file",
						"Error", JOptionPane.ERROR_MESSAGE);
			} else {
				try {
					// Create an inputstream from template file;
					FileInputStream fin = new FileInputStream(
							((JTextField) swix.find("tfTemplateFILE")).getToolTipText());
					BufferedReader br = new BufferedReader(new InputStreamReader(fin));
					// Open the template file
					String theText = br.readLine() + "\n";
					theText = theText + br.readLine() + "\n";
					theText = theText + br.readLine() + "\n";
					br.readLine();
					theText = theText + "FILE_BASE\t" + ((JTextField) swix.find("tfReportFILE1")).getToolTipText()
							+ "\n";
					br.readLine();
					theText = theText + "NAME_BASE\t\"" + ((JTextField) swix.find("tfReportNAME1")).getText() + "\"\n";
					br.readLine();
					theText = theText + "FILE_ALT\t" + ((JTextField) swix.find("tfReportFILE2")).getToolTipText()
							+ "\n";
					br.readLine();
					theText = theText + "NAME_ALT\t\"" + ((JTextField) swix.find("tfReportNAME2")).getText() + "\"\n";
					br.readLine();
					theText = theText + "OUTFILE\t" + ((JTextField) swix.find("tfReportFILE3")).getToolTipText() + "\n";
					br.readLine();
					theText = theText + "NOTE\t\"" + ((JTextArea) swix.find("taReportNOTES")).getText() + "\"\n";
					br.readLine();
					theText = theText + "ASSUMPTIONS\t\"" + ((JTextArea) swix.find("taReportASSUMPTIONS")).getText()
							+ "\"\n";
					br.readLine();
					theText = theText + "MODELER\t\"" + ((JTextField) swix.find("tfReportMODELER")).getText() + "\"\n";

					theText = theText + "TABLE_FONT_SIZE\t" + ((JTextField) swix.find("tfFontSize")).getText() + "\n";

					String aLine = br.readLine();
					while (aLine != null) {
						theText = theText + aLine + "\n";
						aLine = br.readLine();
					}
					br.close();
					theText = theText + "\n";
					ByteArrayInputStream bs = new ByteArrayInputStream(theText.getBytes());
					try {
						Report report = new Report(bs, ((JTextField) swix.find("tfReportFILE3")).getToolTipText());
						report.execute();
					} catch (IOException e1) {
						log.debug(e1.getMessage()); // Not sure - should catch
													// thread problems like
													// already-open PDF?
					}
				} catch (IOException e1) {
					log.debug(e1.getMessage()); // Failure to open template file
												// (?)
				}
			}

		} else if (e.getActionCommand().startsWith("Rep_All")) {
			JPanel controls2 = (JPanel) swix.find("controls2");
			Component[] components = controls2.getComponents();
			for (int i = 0; i < components.length; i++) {
				if (components[i] instanceof JCheckBox) {
					JCheckBox c = (JCheckBox) components[i];
					String cName = c.getName();
					if (cName != null) {
						if (cName.startsWith("RepchkMon")) {
							c.setSelected(true);
						}
					}
				}
			}
		} else if (e.getActionCommand().startsWith("Rep_ClearMonths")) {
			JPanel controls2 = (JPanel) swix.find("controls2");
			Component[] components = controls2.getComponents();
			for (int i = 0; i < components.length; i++) {
				if (components[i] instanceof JCheckBox) {
					JCheckBox c = (JCheckBox) components[i];
					String cName = components[i].getName();
					if (cName != null) {
						if (cName.startsWith("RepchkMon")) {
							c.setSelected(false);
						}
					}
				}
			}
		} else if (e.getActionCommand().startsWith("Rep_AddList")) {
			// Store previous list items
			JList<String> lstReports = (JList<String>) swix.find("lstReports");
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
			String cSTOR = ";Locs-";
			String cSTORIdx = ";Index-";
			// TODO: replace with iterator over panel components of tabbedpane
			// "variables"
			String[] panelIDs = { "presets", "SJR Results", "shortage", "WMA", "DShort" };
			for (String panelID : panelIDs) {
				JPanel panel = (JPanel) swix.find(panelID);
				Component[] components = panel.getComponents();
				for (int i = 0; i < components.length; i++) {
					if (components[i] instanceof JCheckBox) {
						JCheckBox c = (JCheckBox) components[i];
						String cName = c.getName();
						if (cName.startsWith("ckbp")) {
							if (c.isSelected()) {
								cSTOR = cSTOR + c.getText().trim() + ",";
								cSTORIdx = cSTORIdx + cName + ",";
							}
						}
					}
					lstArray1[n] = DisplayFrame.quickState() + cSTOR + cSTORIdx;
					// String[] reportNamesEG = {cDate};
					lstReports.setListData(lstArray1);
				}
			}
		} else if (e.getActionCommand().startsWith("Rep_ClearList")) {

			JList<String> lstReports = (JList<String>) swix.find("lstReports");
			String[] lstArray1 = new String[0];
			lstReports.setListData(lstArray1);

		} else if (e.getActionCommand().startsWith("Rep_LoadList")) {

			ResultUtils.getXMLParsingSvcImplInstance(null).readCGR();

		} else if (e.getActionCommand().startsWith("Rep_SaveList")) {

			ResultUtils.getXMLParsingSvcImplInstance(null).writeCGR();
		} else if (e.getActionCommand().startsWith("Rep_DispAll")) {

			if (lstScenarios.getModel().getSize() == 0) {
				JOptionPane.showMessageDialog(null, "No scenarios loaded", "Error", JOptionPane.ERROR_MESSAGE);
			} else {

				JList list = (JList) swix.find("lstReports");
				for (int i = 0; i < list.getModel().getSize(); i++)
					DisplayFrame.showDisplayFrames((String) (list.getModel().getElementAt(i)), lstScenarios);
			}
		}

		else if (e.getActionCommand().startsWith("Rep_DispCur")) {

			if (lstScenarios.getModel().getSize() == 0) {
				JOptionPane.showMessageDialog(null, "No scenarios loaded", "Error", JOptionPane.ERROR_MESSAGE);
			} else if (((JList) swix.find("lstReports")).getSelectedValue() == null) {
				JOptionPane.showMessageDialog(null, "No display group selected", "Error", JOptionPane.ERROR_MESSAGE);
			} else {

				DisplayFrame.showDisplayFrames((String) ((JList) swix.find("lstReports")).getSelectedValue(),
						lstScenarios);
			}
		} else if (e.getActionCommand().startsWith("Time_SELECT")) {

		} else if (e.getActionCommand().startsWith("AC_CompScen")) {
			IScenarioDele scenarioDele = new ScenarioDeleImp();
			boolean pro = this.allButtonsDele.saveForViewScen();
			JList lstScenarios = (JList) swix.find("SelectedList");
			List<String> fileNames = new ArrayList<>();
			for (int i = 0; i < ((DefaultListModel) lstScenarios.getModel()).getSize(); i++) {
				String name = Paths.get(((DefaultListModel) lstScenarios.getModel()).getElementAt(i).toString())
						.getFileName().toString();
				fileNames.add(name.substring(0, name.length() - 7) + Constant.CLS_EXT);
			}
			if (pro) {
				List<DataTableModel> dtmList = scenarioDele.getScenarioTableData(fileNames);
				ScenarioFrame scenarioFrame = new ScenarioFrame(dtmList);
				scenarioFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				scenarioFrame.setVisible(true);
				try {
					Files.delete(Paths.get(Constant.SCENARIOS_DIR + Constant.CURRENT_SCENARIO + Constant.CLS_EXT));
				} catch (IOException ex) {
					log.debug(ex);
				}
			}
		}
	}
}