package gov.ca.water.calgui.presentation;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import gov.ca.water.calgui.bus_delegate.IApplyDynamicConDele;
import gov.ca.water.calgui.bus_delegate.impl.ApplyDynamicConDeleImp;
import gov.ca.water.calgui.results.ResultUtils;

/**
 * This class is for Listening all the mouse events which are generated by the
 * application.
 * 
 * @author mohan
 *
 */
public class GlobalMouseListener implements MouseListener {

	private static final Logger LOG = Logger.getLogger(GlobalMouseListener.class.getName());
	private IApplyDynamicConDele applyDynamicConDele = new ApplyDynamicConDeleImp();

	@Override
	public void mouseClicked(MouseEvent me) {
		LOG.debug("mouseClicked");
		// Handles mouse presses for results tabs

		JComponent component = (JComponent) me.getComponent();
		if (SwingUtilities.isRightMouseButton(me)) {

			// Right mouse-clicks are all passed through to dynamic control code

			if (((JCheckBox) component).isSelected()) {
				String cName = component.getName();
				applyDynamicConDele.applyDynamicControl(cName, ((JCheckBox) component).isSelected(),
						((JCheckBox) component).isEnabled(), false);// the false
																	// value
																	// don't
																	// mean any
																	// thing
																	// because
																	// we
																	// implement
																	// right
																	// click on
																	// only
																	// check
																	// box.
				LOG.debug(cName);
			}
		} else {

			// Otherwise, we're looking for a double-click on a "ckbp"
			// checkbox from quick results.

			String cName = component.getName();
			int button = me.getButton();
			Integer iClickCount = me.getClickCount();
			if (button != MouseEvent.NOBUTTON && button != MouseEvent.BUTTON1) {
				// Nothing for right mousepress
			} else {
				// Double Click
				if (iClickCount == 2) {
					if (cName.startsWith("ckbp")) {
						JCheckBox chk = (JCheckBox) component;
						ResultUtils.getXMLParsingSvcImplInstance(null).quickDisplay(chk.getText(), chk.getName());
					}
				}
				// Placeholder for future handling of double-clicks
			}

		}

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
	}
}
