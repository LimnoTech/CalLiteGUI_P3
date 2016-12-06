package gov.ca.water.calgui.results;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;

import calsim.app.Project;
import calsim.gui.DtsTreeModel;
import calsim.gui.DtsTreePanel;
import calsim.gui.GuiUtils;

public class CustomResultsAction implements ActionListener {

	@SuppressWarnings("static-access")
	@Override
	public void actionPerformed(ActionEvent actionEvent) {

		if (actionEvent.getActionCommand().equals("AC_Controls")) {
			ControlFrame cf = ResultUtils.getXMLParsingSvcImplInstance(null).getControlFrame();
			cf.display();
			if (cf.getExtendedState() == JFrame.ICONIFIED)
				cf.setExtendedState(JFrame.NORMAL);
		} else if (actionEvent.getActionCommand().equals("CR_LoadList")) {
			ResultUtils.getXMLParsingSvcImplInstance(null).readCGR();
		} else if (actionEvent.getActionCommand().equals("CR_SaveList")) {
			ResultUtils.getXMLParsingSvcImplInstance(null).writeCGR();
		} else if (actionEvent.getActionCommand().equals("CR_ClearTree")) {
			Project p = ResultUtils.getXMLParsingSvcImplInstance(null).getProject();
			p.clearMTSList();
			p.clearDTSList();
			DtsTreePanel dtp = GuiUtils.getCLGPanel().getDtsTreePanel();
			DtsTreeModel dtm = dtp.getCurrentModel();
			dtm.clearVectors();
			dtm.createTreeFromPrj(null, null, "");
			GuiUtils.getCLGPanel().repaint();
		}
	}
}