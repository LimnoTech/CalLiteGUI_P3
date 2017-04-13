package gov.ca.water.calgui.bo;

import java.io.File;

public class SimpleFileFilter extends javax.swing.filechooser.FileFilter {
	private final String fileExt;
	private final String desc;

	public SimpleFileFilter(String aFileExt) {
		fileExt = aFileExt.toLowerCase();
		this.desc = aFileExt.toUpperCase() + " File (*." + aFileExt.toLowerCase() + ")";
		;
	}

	public SimpleFileFilter(String aFileExt, String aDesc) {
		fileExt = aFileExt.toLowerCase();
		this.desc = aDesc;
	}

	@Override
	public boolean accept(File file) {
		// Convert to lower case before checking extension
		return (file.getName().toLowerCase().endsWith("." + fileExt) || file.isDirectory());
	}

	@Override
	public String getDescription() {
		return desc;
	}
}
