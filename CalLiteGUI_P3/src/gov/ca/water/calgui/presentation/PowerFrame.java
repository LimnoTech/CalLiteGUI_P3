package gov.ca.water.calgui.presentation;

import java.awt.Dimension;
import java.awt.HeadlessException;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.swixml.SwingEngine;

import gov.ca.water.calgui.bo.RBListItemBO;
import gov.ca.water.calgui.bus_service.IDSSGrabber1Svc;
import gov.ca.water.calgui.bus_service.impl.DSSGrabber1SvcImpl;
import gov.ca.water.calgui.bus_service.impl.XMLParsingSvcImpl;
import gov.ca.water.calgui.constant.Constant;
import gov.ca.water.calgui.tech_service.IDialogSvc;
import gov.ca.water.calgui.tech_service.IErrorHandlingSvc;
import gov.ca.water.calgui.tech_service.impl.DialogSvcImpl;
import gov.ca.water.calgui.tech_service.impl.ErrorHandlingSvcImpl;

public class PowerFrame {
	JFrame frame;
	private SwingEngine swingEngine = XMLParsingSvcImpl.getXMLParsingSvcImplInstance().getSwingEngine();
	private static final Logger LOG = Logger.getLogger(PowerFrame.class.getName());
	private IErrorHandlingSvc errorHandlingSvc = new ErrorHandlingSvcImpl();
	private IDialogSvc dialogSvc = DialogSvcImpl.getDialogSvcInstance();

	public PowerFrame(JList lstScenarios) {

		try {
			String dssFilename = "";
			IDSSGrabber1Svc dssGrabber = new DSSGrabber1SvcImpl(lstScenarios);
			for (int i = 0; i < lstScenarios.getModel().getSize(); i++) {
				RBListItemBO item = (RBListItemBO) lstScenarios.getModel().getElementAt(i);
				if (item.isSelected()) {
					dssFilename = item.toString();
					dssGrabber.setBase(item.toString());
				}
			}
			if (!dssGrabber.hasPower(dssFilename)) {
				// JOptionPane.showMessageDialog(swingEngine.find(Constant.MAIN_FRAME_NAME),
				// "No power records in Base scenario");
				// ImageIcon icon = new
				// ImageIcon(getClass().getResource("/images/CalLiteIcon.png"));
				// Object[] options = { "OK" };
				// JOptionPane optionPane = new JOptionPane("No power records in
				// Base scenario",
				// JOptionPane.ERROR_MESSAGE, JOptionPane.OK_OPTION, null,
				// options, options[0]);
				// JDialog dialog =
				// optionPane.createDialog(swingEngine.find(Constant.MAIN_FRAME_NAME),"CalLite");
				// dialog.setIconImage(icon.getImage());
				// dialog.setResizable(false);
				// dialog.setVisible(true);
				dialogSvc.getOK("Error - No power records found in Base scenario", JOptionPane.ERROR_MESSAGE);
			} else {
				frame = new JFrame("Power Viewer:" + dssGrabber.getBase());
				frame.setPreferredSize(new Dimension(800, 600));
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				frame.pack();
				frame.setVisible(true);
			}
		} catch (HeadlessException e) {
			LOG.error(e.getMessage());
			String messageText = "Unable to display Power frame.";
			errorHandlingSvc.businessErrorHandler(messageText, (JFrame) swingEngine.find(Constant.MAIN_FRAME_NAME), e);
		}
	}
}
