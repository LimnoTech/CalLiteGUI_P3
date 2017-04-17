package gov.ca.water.calgui.tech_service.impl;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import gov.ca.water.calgui.bo.CalLiteGUIException;
import gov.ca.water.calgui.constant.Constant;
import gov.ca.water.calgui.tech_service.IFileSystemSvc;

/**
 * This is the class for File Handling like reading and saving etc.
 *
 * @author Mohan
 */
public class FileSystemSvcImpl implements IFileSystemSvc {
	private static final Logger LOG = Logger.getLogger(FileSystemSvcImpl.class.getName());

	@Override
	public List<String> getFileData(String fileName, boolean isRequired) throws CalLiteGUIException {
		Path p = Paths.get(fileName);
		List<String> list = null;
		if (Files.isExecutable(p)) {
			try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
				list = stream.collect(Collectors.toList());
			} catch (IOException ex) {
				LOG.error(ex.getMessage(), ex);
				throw new CalLiteGUIException("File data is corrupted : " + fileName, ex, isRequired);
			}
		} else {
			LOG.error("File missing : " + fileName);
			FileNotFoundException ex = new FileNotFoundException("File missing : " + fileName);
			throw new CalLiteGUIException("File missing : " + fileName, ex, isRequired);
		}
		return list;
	}

	@Override
	public List<String> getFileData(String fileName, boolean isRequired, Predicate<String> selector)
			throws CalLiteGUIException {
		return this.getFileData(fileName, isRequired).stream().filter(selector).collect(Collectors.toList());
	}

	@Override
	public List<String> getFileDataForTables(String fileName) throws CalLiteGUIException {
		List<String> list = this.getFileData(fileName, false);
		return list.stream().filter((obj) -> {
			return (!obj.startsWith(Constant.EXCLAMATION))
					|| obj.startsWith(Constant.EXCLAMATION + "	" + Constant.HEADERS);
		}).map((obj) -> {
			if (obj.startsWith(Constant.EXCLAMATION))
				return obj.substring(2, obj.length());
			return obj;
		}).collect(Collectors.toList());
	}

	@Override
	public Document getXMLDocument() throws CalLiteGUIException {
		DocumentBuilder dBuilder;
		try {
			dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputStream fi = new FileInputStream(Constant.GUI_XML_FILENAME);
			return dBuilder.parse(fi);
		} catch (Exception ex) {
			LOG.error("The SwiXml is not build." + Constant.NEW_LINE + ex.getMessage(), ex);
			throw new CalLiteGUIException("The SwiXml is not build." + Constant.NEW_LINE + ex.getMessage(), ex, true);
		}
	}

	@Override
	public void saveDataToFile(String fileName, String data) throws CalLiteGUIException {
		if (!fileName.isEmpty()) {
			Path path = Paths.get(fileName);
			try (BufferedWriter writer = Files.newBufferedWriter(path)) {
				writer.write(data);
			} catch (IOException ex) {
				throw new CalLiteGUIException("Can't save the data to file  : " + fileName, ex);
			}
		}
	}

	@Override
	public String getTheLookupFromTheFullFileName(String fullName) {
		String fileName = Paths.get(fullName).getFileName().toString();
		return getLookupFromTheFileName(fileName);
	}

	@Override
	public String getLookupFromTheFileName(String fileName) {
		String[] arr = FilenameUtils.removeExtension(fileName).split(Constant.UNDER_SCORE);
		String lookupValue = arr[arr.length - 1];
		if (lookupValue.equalsIgnoreCase("swp") || lookupValue.equalsIgnoreCase("sys")) {
			return Constant.USER_DEFINED;
		}
		return lookupValue;
	}
}