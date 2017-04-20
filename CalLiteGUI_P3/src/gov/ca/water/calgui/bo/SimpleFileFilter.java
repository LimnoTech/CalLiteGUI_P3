package gov.ca.water.calgui.bo;
//! Custom file filter builder for file choosers
import java.io.File;

/**
 * Custom file filter class for use in CalLite GUI
 * 
 * @author tslawecki
 *
 */
public class SimpleFileFilter extends javax.swing.filechooser.FileFilter {
	private final String fileExt;
	private final String desc;

	/**
	 * Creates a FileFilter for use with file choosers. Automatically builds
	 * description string from extension
	 * 
	 * @param aFileExt
	 */
	public SimpleFileFilter(String aFileExt) {
		fileExt = aFileExt.toLowerCase();
		this.desc = aFileExt.toUpperCase() + " File (*." + aFileExt.toLowerCase() + ")";
		;
	}

	/**
	 * Creates a FileFilter for use with file choosers, assigning description
	 * explicitly.
	 * 
	 * @param aFileExt
	 * @param aDesc
	 */
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
