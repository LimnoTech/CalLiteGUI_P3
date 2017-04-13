package gov.ca.water.calgui.bus_service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import gov.ca.water.calgui.bo.CalLiteGUIException;
import gov.ca.water.calgui.bo.GUILinks2BO;
import gov.ca.water.calgui.bo.GuiLinks4BO;
import gov.ca.water.calgui.bus_service.ISeedDataSvc;
import gov.ca.water.calgui.constant.Constant;
import gov.ca.water.calgui.tech_service.IErrorHandlingSvc;
import gov.ca.water.calgui.tech_service.IFileSystemSvc;
import gov.ca.water.calgui.tech_service.impl.ErrorHandlingSvcImpl;
import gov.ca.water.calgui.tech_service.impl.FileSystemSvcImpl;

/**
 * This class holds the required data for the application. The gui_Link2.table
 * and gui_Link4.table data is in this class.
 *
 * @author Mohan
 */
public final class SeedDataSvcImpl implements ISeedDataSvc {
	private static final Logger LOG = Logger.getLogger(SeedDataSvcImpl.class.getName());
	private IErrorHandlingSvc errorHandlingSvc;
	private IFileSystemSvc fileSystemSvc;
	private static ISeedDataSvc seedDataSvc;
	private List<GUILinks2BO> gUILinks2BOList;
	private List<GuiLinks4BO> guiLinks4BOList;

	private String gl3_lookups[][];

	private Map<String, GUILinks2BO> guiIdMap;
	private Map<String, GUILinks2BO> tableIdMap;
	private Map<String, GUILinks2BO> regIdMap;
	private Map<String, GuiLinks4BO> guiLinks4Map;

	/**
	 * This method is for implementing the singleton. It will return the
	 * instance of this class if it is empty it will create one.
	 *
	 * @return Will return the instance of this class if it is empty it will
	 *         create one.
	 */
	public static ISeedDataSvc getSeedDataSvcImplInstance() {
		if (seedDataSvc == null) {
			seedDataSvc = new SeedDataSvcImpl();
		}
		return seedDataSvc;
	}

	/*
	 * This will read the gui_link2.table and build the list of {@link
	 * GUILinks2BO} objects, read the gui_link4.table and build the list of
	 * {@link GuiLinks4BO} objects, and read the gui_link3 table to provide GL3
	 * lookupss
	 */
	private SeedDataSvcImpl() {
		LOG.debug("Building SeedDataSvcImpl Object.");
		this.errorHandlingSvc = new ErrorHandlingSvcImpl();
		this.fileSystemSvc = new FileSystemSvcImpl();
		this.gUILinks2BOList = new ArrayList<GUILinks2BO>();
		this.guiLinks4BOList = new ArrayList<GuiLinks4BO>();
		this.guiIdMap = new HashMap<String, GUILinks2BO>();
		this.tableIdMap = new HashMap<String, GUILinks2BO>();
		this.regIdMap = new HashMap<String, GUILinks2BO>();
		this.guiLinks4Map = new HashMap<String, GuiLinks4BO>();
		List<String> guiLinks2StrList;
		List<String> guiLinks4StrList;
		List<String> guiLinks3StrList;
		String errorStr = "";
		String fileName = "";
		try {
			fileName = Constant.GUI_LINKS2_FILENAME;
			guiLinks2StrList = fileSystemSvc.getFileData(fileName, true, SeedDataSvcImpl::isNotComments);
			for (String guiLinks2Str : guiLinks2StrList) {
				errorStr = guiLinks2Str;
				String[] list = guiLinks2Str.split(Constant.DELIMITER);
				GUILinks2BO gUILinks2BO = new GUILinks2BO(list[0], list[1], list[2], list[3], list[4], list[5], list[6],
						list[7], list[8], list[9], list[10], list[11], list[12], list[13], list[14]);
				gUILinks2BOList.add(gUILinks2BO);
				guiIdMap.put(gUILinks2BO.getGuiId(), gUILinks2BO);
				if (!gUILinks2BO.getTableID().equals(Constant.N_A))
					if (tableIdMap.get(gUILinks2BO.getTableID()) == null) {
						tableIdMap.put(gUILinks2BO.getTableID(), gUILinks2BO);
					} else {
						errorHandlingSvc.displayErrorMessageBeforeTheUI(
								new CalLiteGUIException("The Table Id is same for these two controls - "
										+ tableIdMap.get(gUILinks2BO.getTableID()).getGuiId() + " , "
										+ gUILinks2BO.getGuiId(), true));
					}
				if (!gUILinks2BO.getRegID().equals(Constant.N_A))
					if (regIdMap.get(gUILinks2BO.getRegID()) == null) {
						regIdMap.put(gUILinks2BO.getRegID(), gUILinks2BO);
					} else {
						errorHandlingSvc.displayErrorMessageBeforeTheUI(
								new CalLiteGUIException("The RegId is same for these two controls - "
										+ regIdMap.get(gUILinks2BO.getRegID()).getGuiId() + " , "
										+ gUILinks2BO.getGuiId(), true));
					}
			}
			fileName = Constant.GUI_LINKS4_FILENAME;
			guiLinks4StrList = fileSystemSvc.getFileData(fileName, true, SeedDataSvcImpl::isNotComments);
			for (String guiLink4Str : guiLinks4StrList) {
				errorStr = guiLink4Str;
				String[] list = guiLink4Str.split(Constant.DELIMITER);
				GuiLinks4BO guiLinks4BO = new GuiLinks4BO(list[0], list[1], list[2], list[3], list[4], list[5], list[6],
						list[7], list[8], list[9], list[10]);
				guiLinks4BOList.add(guiLinks4BO);
				String id = guiLinks4BO.getRunBasisID() + guiLinks4BO.getLodId() + guiLinks4BO.getCcprojectId()
						+ guiLinks4BO.getCcmodelId();
				guiLinks4Map.put(id, guiLinks4BO);
			}

			fileName = Constant.GUI_LINKS3_FILENAME;
			guiLinks3StrList = fileSystemSvc.getFileData(fileName, true, SeedDataSvcImpl::isNotComments);
			gl3_lookups = new String[guiLinks3StrList.size()][6];
			int i = 0;
			for (String guiLink3Str : guiLinks3StrList) {
				errorStr = guiLink3Str;
				String[] list = guiLink3Str.split(Constant.DELIMITER);
				for (int j = 0; j < 6; j++) {
					if (list[j].equals("null"))
						list[j] = "";
					gl3_lookups[i][j] = list[j];
				}
				i++;

			}

		} catch (ArrayIndexOutOfBoundsException ex) {
			String errorMessage = "In file \"" + fileName + "\" has a corrupted data at line \"" + errorStr + "\""
					+ Constant.NEW_LINE + "The column number which the data is corrupted is " + ex.getMessage();
			LOG.error(errorMessage, ex);
			errorHandlingSvc.displayErrorMessageBeforeTheUI(new CalLiteGUIException(errorMessage, ex, true));
		} catch (CalLiteGUIException ex) {
			LOG.error(ex.getMessage(), ex);
			errorHandlingSvc.displayErrorMessageBeforeTheUI(ex);
		}
	}

	@Override
	public GUILinks2BO getObjByGuiId(String guiId) {
		GUILinks2BO gUILinks2BO = guiIdMap.get(guiId);
		if (gUILinks2BO == null)
			LOG.info("There is no GUI_Link2 Data for this guiId = " + guiId);
		return gUILinks2BO;
	}

	@Override
	public GuiLinks4BO getObjByRunBasisLodCcprojCcmodelIds(String id) {
		GuiLinks4BO guiLinks4BO = this.guiLinks4Map.get(id);
		if (guiLinks4BO == null)
			LOG.info("There is no GuiLinks4BO Object for this value = " + id);
		return guiLinks4BO;
	}

	@Override
	public List<GUILinks2BO> getUserTables() {
		return this.tableIdMap.values().stream().collect(Collectors.toList());
	}

	@Override
	public List<GUILinks2BO> getGUILinks2BOList() {
		return gUILinks2BOList;
	}

	@Override
	public Map<String, GUILinks2BO> getTableIdMap() {
		return tableIdMap;
	}

	@Override
	public boolean hasSeedDataObject(String guiId) {
		return this.getObjByGuiId(guiId) != null;
	}

	@Override
	public List<GUILinks2BO> getRegulationsTabData() {
		return this.regIdMap.values().stream()
				.filter(seedData -> seedData.getDashboard().equalsIgnoreCase(Constant.REGULATIONS_TABNAME))
				.collect(Collectors.toList());
	}

	public String getLookups(int i, int j) {
		return gl3_lookups[i][j];
	}

	public int getLookupsLength() {
		return gl3_lookups.length;
	}

	/**
	 * This will tell whether the line is comment or not.
	 *
	 * @param line
	 *            The line to be checked.
	 * @return Will return true if the line id not comment.
	 */
	private static boolean isNotComments(String line) {
		return !line.startsWith(Constant.EXCLAMATION);
	}
}
