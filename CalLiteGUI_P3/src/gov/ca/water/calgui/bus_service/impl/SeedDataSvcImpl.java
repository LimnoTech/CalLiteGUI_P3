package gov.ca.water.calgui.bus_service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import gov.ca.water.calgui.bo.CalLiteGUIException;
import gov.ca.water.calgui.bo.GuiLinks4BO;
import gov.ca.water.calgui.bo.SeedDataBO;
import gov.ca.water.calgui.bus_service.ISeedDataSvc;
import gov.ca.water.calgui.constant.Constant;
import gov.ca.water.calgui.tech_service.IErrorHandlingSvc;
import gov.ca.water.calgui.tech_service.IFileSystemSvc;
import gov.ca.water.calgui.tech_service.impl.ErrorHandlingSvcImpl;
import gov.ca.water.calgui.tech_service.impl.FileSystemSvcImpl;

/**
 * This class holds the required data for the application. The gui_Link2.table and gui_Link4.table data is in this class.
 *
 * @author mohan
 */
public final class SeedDataSvcImpl implements ISeedDataSvc {
	private static final Logger LOG = Logger.getLogger(SeedDataSvcImpl.class.getName());
	private IErrorHandlingSvc errorHandlingSvc;
	private IFileSystemSvc fileSystemSvc;
	private static ISeedDataSvc seedDataSvc;
	private List<SeedDataBO> seedDataBOList;
	private List<GuiLinks4BO> guiLinks4BOList;
	private Map<String, SeedDataBO> guiIdMap;
	private Map<String, SeedDataBO> tableIdMap;
	private Map<String, SeedDataBO> regIdMap;
	private Map<String, GuiLinks4BO> guiLinks4Map;

	/**
	 * This method is for implementing the singleton.
	 *
	 * @return
	 */
	public static ISeedDataSvc getSeedDataSvcImplInstance() {
		if (seedDataSvc == null) {
			seedDataSvc = new SeedDataSvcImpl();
		}
		return seedDataSvc;
	}

	/**
	 * This will read the gui_link2.table and build the list of {@link SeedDataBO} objects and read the gui_link4.table and build
	 * the list of {@link GuiLinks4BO} objects.
	 */
	private SeedDataSvcImpl() {
		LOG.debug("Building SeedDataSvcImpl Object.");
		this.errorHandlingSvc = new ErrorHandlingSvcImpl();
		this.fileSystemSvc = new FileSystemSvcImpl();
		this.seedDataBOList = new ArrayList<SeedDataBO>();
		this.guiLinks4BOList = new ArrayList<GuiLinks4BO>();
		this.guiIdMap = new HashMap<String, SeedDataBO>();
		this.tableIdMap = new HashMap<String, SeedDataBO>();
		this.regIdMap = new HashMap<String, SeedDataBO>();
		this.guiLinks4Map = new HashMap<String, GuiLinks4BO>();
		List<String> seedDataStrList;
		List<String> guiLink4StrList;
		String errorStr = "";
		String fileName = "";
		try {
			fileName = Constant.GUI_LINKS2_FILENAME;
			seedDataStrList = fileSystemSvc.getFileData(fileName, true, SeedDataSvcImpl::isNotComments);
			for (String seedDataStr : seedDataStrList) {
				errorStr = seedDataStr;
				String[] list = seedDataStr.split(Constant.DELIMITER);
				SeedDataBO seedDataBO = new SeedDataBO(list[0], list[1], list[2], list[3], list[4], list[5], list[6], list[7],
				        list[8], list[9], list[10], list[11], list[12], list[13], list[14]);
				seedDataBOList.add(seedDataBO);
				guiIdMap.put(seedDataBO.getGuiId(), seedDataBO);
				if (!seedDataBO.getTableID().equals(Constant.N_A))
					if (tableIdMap.get(seedDataBO.getTableID()) == null) {
						tableIdMap.put(seedDataBO.getTableID(), seedDataBO);
					} else {
						errorHandlingSvc.displayErrorMessageBeforeTheUI(new CalLiteGUIException(
						        "The Table Id is same for there two controls - "
						                + tableIdMap.get(seedDataBO.getTableID()).getGuiId() + " , " + seedDataBO.getGuiId(),
						        true));
					}
				if (!seedDataBO.getRegID().equals(Constant.N_A))
					if (regIdMap.get(seedDataBO.getRegID()) == null) {
						regIdMap.put(seedDataBO.getRegID(), seedDataBO);
					} else {
						errorHandlingSvc
						        .displayErrorMessageBeforeTheUI(new CalLiteGUIException(
						                "The RegId is same for there two controls - "
						                        + regIdMap.get(seedDataBO.getRegID()).getGuiId() + " , " + seedDataBO.getGuiId(),
						                true));
					}
			}
			fileName = Constant.GUI_LINKS4_FILENAME;
			guiLink4StrList = fileSystemSvc.getFileData(fileName, true, SeedDataSvcImpl::isNotComments);
			for (String guiLink4Str : guiLink4StrList) {
				errorStr = guiLink4Str;
				String[] list = guiLink4Str.split(Constant.DELIMITER);
				GuiLinks4BO guiLinks4BO = new GuiLinks4BO(list[0], list[1], list[2], list[3], list[4], list[5], list[6], list[7],
				        list[8], list[9], list[10]);
				guiLinks4BOList.add(guiLinks4BO);
				String id = guiLinks4BO.getRunBasisID() + guiLinks4BO.getLodId() + guiLinks4BO.getCcprojectId()
				        + guiLinks4BO.getCcmodelId();
				guiLinks4Map.put(id, guiLinks4BO);
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
	public SeedDataBO getObjByGuiId(String guiId) {
		SeedDataBO seedDataBO = guiIdMap.get(guiId);
		if (seedDataBO == null)
			LOG.info("There is no GUI_Link2 Data for this guiId = " + guiId);
		return seedDataBO;
	}

	@Override
	public GuiLinks4BO getObjByRunBasisLodCcprojCcmodelIds(String id) {
		GuiLinks4BO guiLinks4BO = this.guiLinks4Map.get(id);
		if (guiLinks4BO == null)
			LOG.info("There is no GuiLinks4BO Object for this value = " + id);
		return guiLinks4BO;
	}

	@Override
	public List<SeedDataBO> getUserTables() {
		return this.tableIdMap.values().stream().collect(Collectors.toList());
	}

	@Override
	public List<SeedDataBO> getSeedDataBOList() {
		return seedDataBOList;
	}

	@Override
	public Map<String, SeedDataBO> getTableIdMap() {
		return tableIdMap;
	}

	@Override
	public boolean hasSeedDataObject(String guiId) {
		return this.getObjByGuiId(guiId) != null;
	}

	@Override
	public List<SeedDataBO> getRegulationsTabData() {
		return this.regIdMap.values().stream()
		        .filter(seedData -> seedData.getDashboard().equalsIgnoreCase(Constant.REGULATIONS_TABNAME))
		        .collect(Collectors.toList());
	}

	/**
	 * This will tell whether the line is comment or not.
	 *
	 * @param line
	 * @return
	 */
	private static boolean isNotComments(String line) {
		return !line.startsWith(Constant.EXCLAMATION);
	}
}
