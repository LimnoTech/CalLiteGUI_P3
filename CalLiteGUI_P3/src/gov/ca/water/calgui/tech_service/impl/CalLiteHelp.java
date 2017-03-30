package gov.ca.water.calgui.tech_service.impl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import javax.help.HelpSet;
import javax.help.JHelp;
import javax.swing.JFrame;

import org.apache.log4j.Logger;

/**
 * Generates help window allowing search of Cal-Lite user's manual.
 */
public class CalLiteHelp {
	private static final Logger LOG = Logger.getLogger(CalLiteHelp.class.getName());
	private static Properties properties = new Properties();

	/**
	 * Renders help dialog
	 *
	 * @param label
	 *            Takes calling dashboard's title which is mapped to help html docs via docs/map.xml
	 */
	public void showHelp(String label) {
		try {
			properties.load(CalLiteHelp.class.getClassLoader().getResourceAsStream("callite-gui.properties"));
		} catch (IOException e1) {
			LOG.debug(e1);
		}
		String path = new File(properties.getProperty("help.dir")).getAbsolutePath();
		String versionId = properties.getProperty("version.id");
		try {
			URL url = new URL("file:///" + path);
			JHelp helpViewer = new JHelp(new HelpSet(null, url));
			JFrame help = new JFrame("CalLite " + versionId + " GUI Help");
			help.getContentPane().add(helpViewer);
			helpViewer.setCurrentID(label);
			help.pack();
			help.setVisible(true);
		} catch (Exception e) {
			LOG.debug("Helpset not found at: " + path + ". " + e);
		}
	}
}
