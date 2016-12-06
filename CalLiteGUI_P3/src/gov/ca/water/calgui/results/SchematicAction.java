package gov.ca.water.calgui.results;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;

public class SchematicAction implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		if (actionEvent.getActionCommand().equals("AC_Controls")) {
			ControlFrame cf = ResultUtils.getXMLParsingSvcImplInstance(null).getControlFrame();
			cf.display();
			cf.setExtendedState(JFrame.NORMAL);
		}
	}
}