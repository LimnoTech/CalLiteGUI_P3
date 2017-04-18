package gov.ca.water.calgui.presentation;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;

import gov.ca.water.calgui.bo.DSSGrabber1BO;
import gov.ca.water.calgui.bo.RBListItemBO;

public class PowerFrame {
	JFrame frame;

	public PowerFrame(JList lstScenarios) {

		String dssFilename = "";
		DSSGrabber1BO dssGrabber = new DSSGrabber1BO(lstScenarios);
		for (int i = 0; i < lstScenarios.getModel().getSize(); i++) {
			RBListItemBO item = (RBListItemBO) lstScenarios.getModel().getElementAt(i);
			if (item.isSelected()) {
				dssFilename = item.toString();
				dssGrabber.setBase(item.toString());
			}
		}
		if (!dssGrabber.hasPower(dssFilename)) {
			JOptionPane.showMessageDialog(null, "No power records in Base scenario");
		} else {
			frame = new JFrame("Power Viewer:" + dssGrabber.getBase());
			frame.setPreferredSize(new Dimension(800, 600));
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.pack();
			frame.setVisible(true);
		}
	}
}
