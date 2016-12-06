package gov.ca.water.calgui;

import javax.xml.parsers.DocumentBuilder;

public class Main {

	public static void main(String[] args) {
		DocumentBuilder dBuilder;
		try {
			// dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			// InputStream fi = new FileInputStream(Paths.get(System.getProperty("user.dir") + "//Config//TEST.xml").toString());
			// Document d = dBuilder.parse(fi);
			// SwingEngine engine = new SwingEngine();
			// engine.render(d);
			// engine.find("desktop").setVisible(true);

			// FileTime fileTime = Files.getLastModifiedTime(Paths.get(Constant.GUI_LINKS2_FILENAME));
			// System.out.println(System.currentTimeMillis() - fileTime.toMillis());

			// JFrame frame = new JFrame();
			// frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			// JFileChooser jf = new JFileChooser();
			// FileNameExtensionFilter filter = new FileNameExtensionFilter("CLS FILES", "cls", "cls");
			// jf.setFileFilter(filter);
			// jf.setMultiSelectionEnabled(false);
			// int val = jf.showOpenDialog(frame);
			// System.out.println(val);
			TestJFrame ss = new TestJFrame();
			ss.setVisible(true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
}
