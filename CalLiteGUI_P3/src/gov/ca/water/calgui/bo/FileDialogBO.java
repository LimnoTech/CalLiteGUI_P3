package gov.ca.water.calgui.bo;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.apache.log4j.Logger;

import gov.ca.water.calgui.presentation.WRIMSGUILinks;

/**
 * Supports selection of different types of CalLite files from customized
 * JFileChoosers.
 *
 * @author tslawecki
 *
 *
 */
public class FileDialogBO implements ActionListener {

	public DefaultListModel lmScenNames;
	public JFileChooser fc = new JFileChooser2();
	public int dialogRC;

	JList theList;
	JLabel theLabel;
	JTextField theTextField;
	boolean theMultipleFlag = false;
	String theFileExt = null;
	JRadioButton rdbopt1 = null;;
	JRadioButton rdbopt2 = null;;

	private static Logger log = Logger.getLogger(FileDialogBO.class.getName());

	/**
	 * Basic constructor for use with DSS files. Result is appended to aList.
	 *
	 * @param aList
	 *            - JList containing selected DSS files.
	 * @param aLabel
	 *            - not currently used
	 *
	 */
	public FileDialogBO(JList aList, JLabel aLabel) {
		theLabel = aLabel;
		theFileExt = "DSS";
		theTextField = null;
		setup(aList);
	}

	/**
	 * Constructor for use with DSS files, result is appended to list and placed
	 * in textfield.
	 *
	 * @param aList
	 * @param aTextField
	 */
	public FileDialogBO(JList aList, JTextField aTextField) {
		theLabel = null;
		theFileExt = "DSS";
		theTextField = aTextField;
		setup(aList);
	}

	/**
	 * Constructor for use with arbitrary files, result is appended to list and
	 * placed in textfield.
	 *
	 * @param aList
	 * @param aTextField
	 * @param aFileExt
	 */
	public FileDialogBO(JList aList, JTextField aTextField, String aFileExt) {
		theLabel = null;
		theTextField = aTextField;
		theFileExt = aFileExt;
		setup(aList);
	}

	/**
	 * Constructor for use with arbitrary files, result is appended to list and
	 * placed in textfield.
	 *
	 * @param aList
	 * @param aTextField
	 * @param aFileExt
	 * @param isMultiple
	 */
	public FileDialogBO(JList aList, JTextField aTextField, String aFileExt, boolean isMultiple) {
		theLabel = null;
		theTextField = aTextField;
		theFileExt = aFileExt;
		theMultipleFlag = isMultiple;
		setup(aList);
	}

	/**
	 * Constructor used for DSS files, result is appended to list, radiobuttons
	 * are enabled when list length greater than 1
	 *
	 * @param aList
	 * @param aLabel
	 * @param rdb1
	 * @param rdb2
	 */
	public FileDialogBO(JList aList, JLabel aLabel, JRadioButton rdb1, JRadioButton rdb2, boolean isMultiple) {
		theLabel = aLabel;
		theFileExt = "DSS";
		theTextField = null;
		setup(aList);
		rdbopt1 = rdb1;
		rdbopt2 = rdb2;
		theMultipleFlag = isMultiple;
	}

	/**
	 * Constructor used for DSS files, result is appended to list, radiobuttons
	 * are enabled when list length greater than 1, button is enabled when
	 * length > 0;
	 *
	 * @param aList
	 * @param aLabel
	 * @param rdb1
	 * @param rdb2
	 * @param btn
	 */
	public FileDialogBO(JList aList, JLabel aLabel, JRadioButton rdb1, JRadioButton rdb2, JButton btn,
			boolean isMultiple) {
		theLabel = aLabel;
		theFileExt = "DSS";
		theTextField = null;
		setup(aList);
		rdbopt1 = rdb1;
		rdbopt2 = rdb2;
		btn1 = btn;
		theMultipleFlag = isMultiple;
	}

	JButton btn1 = null;;

	/**
	 * Common code for all constructors
	 *
	 * @param aList
	 */
	private void setup(JList aList) {

		// Set up file chooser

		if (theFileExt.equals("DSS")) {
			fc.setFileFilter(new SimpleFileFilter("DSS"));
			fc.setCurrentDirectory(new File(".//Scenarios"));
		} else if (theFileExt.equals("DSS2")) {
			theFileExt = "DSS";
			fc.setFileFilter(new SimpleFileFilter("DSS"));
			fc.setCurrentDirectory(new File(".//Model_w2/DSS_Files"));
		} else {
			fc.setFileFilter(new SimpleFileFilter(theFileExt));
			if (theFileExt.equals("PDF") || theFileExt.equals("CLS"))
				fc.setCurrentDirectory(new File(".//Scenarios"));
			else
				fc.setCurrentDirectory(new File(".//Config"));
		}

		// If a list is specified, create customized JList for handling of
		// files.

		if (aList != null) {

			lmScenNames = new DefaultListModel();
			lmScenNames.addListDataListener(new MyListDataListener());

			theList = aList;
			theList.setCellRenderer(new RBListRenderer());
			theList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			// Add a mouse listener to handle changing selection

			theList.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent event) {

					JList list = (JList) event.getSource();

					// Get index of item clicked

					if (list.getModel().getSize() > 0) {
						int index = list.locationToIndex(event.getPoint());

						// Toggle selected state

						for (int i = 0; i < list.getModel().getSize(); i++) {
							RBListItemBO item = (RBListItemBO) list.getModel().getElementAt(i);
							if (i == index) {
								item.setSelected(true);
								list.repaint(list.getCellBounds(i, i));
							} else {
								if (item.isSelected())
									list.repaint(list.getCellBounds(i, i));
								item.setSelected(false);
							}
						}

						// Repaint cell

						list.repaint(list.getCellBounds(index, index));

						WRIMSGUILinks.updateProjectFiles(list);

					}
				}
			});
		}
	}

	/**
	 * Custom ListDataListener to enable/disable controls based on number of
	 * files in list.
	 *
	 * @author tslawecki
	 *
	 */
	private class MyListDataListener implements ListDataListener {
		@Override
		public void contentsChanged(ListDataEvent e) {
		}

		@Override
		public void intervalAdded(ListDataEvent e) {
			if (rdbopt1 != null && rdbopt2 != null) {
				rdbopt1.setEnabled(lmScenNames.getSize() > 1);
				rdbopt2.setEnabled(lmScenNames.getSize() > 1);
			}
			WRIMSGUILinks.updateProjectFiles(theList);
			if (btn1 != null)
				btn1.setEnabled(true);
		}

		@Override
		public void intervalRemoved(ListDataEvent e) {
			if (rdbopt1 != null && rdbopt2 != null) {
				rdbopt1.setEnabled(lmScenNames.getSize() > 1);
				rdbopt2.setEnabled(lmScenNames.getSize() > 1);
			}
			WRIMSGUILinks.updateProjectFiles(theList);
			if (btn1 != null)
				btn1.setEnabled(lmScenNames.getSize() > 0);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object obj = null;
		if (e != null)
			obj = e.getSource();
		if ((obj != null) && (((Component) obj).getName() != null)
				&& ((Component) obj).getName().equals("btnDelScenario")) {
			// If invoked by QR DelScenario button, delete a scenario from Quick
			// Results scenario list
			if ((theList != null) && lmScenNames.getSize() > 0) {
				int todel = -1;
				for (int i = 0; i < lmScenNames.getSize(); i++)
					if (((RBListItemBO) lmScenNames.getElementAt(i)).isSelected())
						todel = i;
				if (todel > 0)
					((RBListItemBO) lmScenNames.getElementAt(todel - 1)).setSelected(true);
				else if (todel < lmScenNames.getSize() - 1)
					((RBListItemBO) lmScenNames.getElementAt(todel + 1)).setSelected(true);
				lmScenNames.remove(todel);
			}
		} else if (e.getActionCommand().equals("btnClearScenario")) {
			((DefaultListModel) theList.getModel()).clear();
			theList.repaint();

		} else {
			// Otherwise, show dialog
			int rc;
			if (theMultipleFlag) {
				UIManager.put("FileChooser.openDialogTitleText", "Select Scenarios");
				fc.setMultiSelectionEnabled(true);
				dialogRC = fc.showDialog(null, "Select");
				if (theList != null & dialogRC != 1) {
					for (File file : fc.getSelectedFiles()) {
						addFileToList(file);
					}
				}
			} else {
				if (theFileExt == null)
					rc = fc.showOpenDialog(null);
				else
					rc = fc.showDialog(null, theFileExt.equals("DSS") ? "Open" : "Save");
				dialogRC = rc;
				File file;
				if (rc == 0) {
					file = fc.getSelectedFile();
					if (theFileExt.equals("PDF") && !file.getName().toLowerCase().endsWith(".pdf")) {
						file = new File(file.getPath() + ".PDF");
					}
					if (theFileExt.equals("CLS") && !file.getName().toLowerCase().endsWith(".cls")) {
						file = new File(file.getPath() + ".CLS");
					}
					if (theList != null) {
						addFileToList(file);
					} else if (theLabel != null) {
						// theLabel.setText(file.getName());
						// theLabel.setToolTipText(file.getPath());
					} else if (theTextField != null) {
						theTextField.setText(file.getName());
						theTextField.setToolTipText(file.getPath());
					}
				}
			}
		}
		return;
	}

	/**
	 * Adds file to list of scenarios if not already in list. Currently used to
	 * manage list of scenarios on Quick Result dashboard
	 *
	 * @param file
	 */
	public void addFileToList(File file) {
		boolean match = false;
		for (int i = 0; i < lmScenNames.getSize(); i++) {
			RBListItemBO rbli = (RBListItemBO) lmScenNames.getElementAt(i);
			match = match | (rbli.toString().equals(file.getPath()));
		}

		if (match)

			// Per client request, don't alert.

			// JOptionPane.showMessageDialog(null,
			// "Scenario \"" + file.getPath() + "\"\n" + "and will not be added.
			// It is already in the Scenarios list.",
			// "Alert", JOptionPane.ERROR_MESSAGE);
			;
		else {
			lmScenNames.addElement(new RBListItemBO(file.getPath(), file.getName()));
			if (lmScenNames.getSize() == 1)
				((RBListItemBO) lmScenNames.getElementAt(0)).setSelected(true);
			theList.ensureIndexIsVisible(lmScenNames.getSize() - 1);
			theList.revalidate();
			theList.validate();
			theList.getParent().invalidate();
		}
	}

	/**
	 * Custom class shows scenario description in tooltip
	 *
	 * @author tslawecki
	 *
	 */
	private class FileNameRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = -3040003845509293885L;

		private final JFileChooser2 theOwner;
		private final Map<String, String> theToolTips = new HashMap<String, String>();

		private FileNameRenderer(JFileChooser2 jFileChooser) {
			theOwner = jFileChooser;
		}

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (!theOwner.toolTipFlag) {
				theToolTips.clear();
				File folder = new File(System.getProperty("user.dir") + "\\Scenarios"); // change
																						// to
																						// read
																						// current
																						// directory
				File[] listOfFiles = folder.listFiles();

				for (int i = 0; i < listOfFiles.length; i++) {
					if (listOfFiles[i].isFile()) {
						if (listOfFiles[i].getName().toLowerCase().endsWith(".txt")) {
							try {
								FileInputStream fin = new FileInputStream(listOfFiles[i]);
								BufferedReader br = new BufferedReader(new InputStreamReader(fin));
								String theKey = br.readLine().toLowerCase();
								String theValue = br.readLine() + "\n" + br.readLine() + "\n" + br.readLine();
								theToolTips.put(theKey.toLowerCase(), theValue);
								br.close();
							} catch (IOException e1) {
								log.debug(e1.getMessage());

							}

						}
					}
					theOwner.toolTipFlag = true; // need to flag when directory
													// changes
				}
			}
			File file = new File(String.valueOf(value));

			if (theToolTips.containsKey(file.getName().toLowerCase())) {
				lbl.setToolTipText(theToolTips.get(file.getName().toLowerCase()));
			} else {
				lbl.setToolTipText("No scenario information for this file");
			}
			lbl.setText(file.getName());
			return lbl;
		}
	}

	/**
	 * Custom FileChooser that puts scenario description for *.cls (assumedin
	 * *.txt) in tooltip
	 *
	 * @author tslawecki
	 *
	 */
	private class JFileChooser2 extends javax.swing.JFileChooser {
		private static final long serialVersionUID = -150877374751505363L;

		public boolean toolTipFlag = false;

		private Component findJList(Component comp) {

			if (comp instanceof JList)
				return comp;
			if (comp instanceof Container) {
				Component[] components = ((Container) comp).getComponents();
				for (int i = 0; i < components.length; i++) {
					Component child = findJList(components[i]);
					if (child != null)
						return child;
				}
			}
			return null;
		}

		@Override
		public int showOpenDialog(Component c) {
			JList myList = (JList) findJList(this);
			myList.setCellRenderer(new FileNameRenderer(this));
			toolTipFlag = false;
			return super.showOpenDialog(c);
		}
	}

	/**
	 * Custom class to show radiobutton items in place of textfields in a list
	 *
	 * @author tslawecki
	 *
	 */
	private class RBListRenderer extends JRadioButton implements ListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean hasFocus) {
			setEnabled(list.isEnabled());
			setSelected(((RBListItemBO) value).isSelected());
			setFont(list.getFont());
			setBackground(list.getBackground());
			setForeground(list.getForeground());
			setText(((RBListItemBO) value).toString2());
			this.setToolTipText(value.toString() + " 	\n" + ((RBListItemBO) value).getSVFilename());
			return this;
		}
	}
}
