package gov.ca.water.calgui;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;

public class TabbedPaneApp {
	private static void createAndShowGUI() {

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JTabbedPane firstPanel = new JTabbedPane();
		JTabbedPane secondPanel = new JTabbedPane();

		JLabel firstLabel = new JLabel("First tabbed pane");
		JLabel secondLabel = new JLabel("Second tabbed pane");

		JTabbedPane tabbedPane = new JTabbedPane() {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(500, 400);
			};
		};

		firstPanel.add(firstLabel);
		secondPanel.add(secondLabel);

		tabbedPane.add("FirPanel", firstPanel);
		tabbedPane.add("Sec Panel", secondPanel);
		frame.add(tabbedPane);

		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				createAndShowGUI();
			}
		});
	}
}
