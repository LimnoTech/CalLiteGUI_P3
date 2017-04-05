package gov.ca.water.calgui.bus_service;

import java.util.List;
import java.util.Map;

import gov.ca.water.calgui.bo.GuiLinks4BO;
import gov.ca.water.calgui.bo.SeedDataBO;

/**
 * This is the interface for loading the GUI_Link2.table and GUI_Link4.table.
 * 
 * @author Mohan
 */
public interface ISeedDataSvc {

	/**
	 * This will take the {@code guiId} and return the Object of that
	 * {@code guiId}. If the guiId is not there then it will return null.
	 *
	 * @param guiId
	 *            The guiId.
	 * @return Will return the seedData object of the gui id passed in.
	 */
	public SeedDataBO getObjByGuiId(String guiId);

	/**
	 * This will concatenate "RunBasis_ID LOD_ID CCProject_ID CCModel_ID" id's
	 * from the Gui_Link4.table and match with the {@code id} and return the
	 * object which is matched. If the {@code id} is not there then it will
	 * return null.
	 *
	 * @param id
	 *            The id value for "RunBasis_ID LOD_ID CCProject_ID CCModel_ID".
	 * @return Will return the gui link object.
	 */
	public GuiLinks4BO getObjByRunBasisLodCcprojCcmodelIds(String id);

	/**
	 * This will return the list of {@link SeedDataBO} objects which has data
	 * Table value in it.
	 *
	 * @return Will return the list of {@link SeedDataBO} objects which has data
	 *         Table value in it.
	 */
	public List<SeedDataBO> getUserTables();

	/**
	 * This will return the Gui_Link2.table data as list of {@link SeedDataBO}
	 * Objects.
	 *
	 * @return Will return the Gui_Link2.table data as list of
	 *         {@link SeedDataBO} Objects.
	 */
	public List<SeedDataBO> getSeedDataBOList();

	/**
	 * This will return the map with key as the Table id and value as
	 * {@link SeedDataBO}.
	 *
	 * @return Will return the map with key as the Table id and value as
	 *         {@link SeedDataBO}.
	 */
	public Map<String, SeedDataBO> getTableIdMap();

	/**
	 * This will tell whether the guiId have {@link SeedDataBO} or not.
	 * 
	 * @param guiId
	 *            The gui Id
	 * @return Will tell whether the guiId have {@link SeedDataBO} or not.
	 */
	public boolean hasSeedDataObject(String guiId);

	/**
	 * This will return the list of {@link SeedDataBO} which belong to the
	 * Regulation Tab.
	 *
	 * @return Will return the list of {@link SeedDataBO} which belong to the
	 *         Regulation Tab.
	 */
	public List<SeedDataBO> getRegulationsTabData();

	/**
	 * 
	 * Provides access to GUI_Links3.csv values controlling Quick Results
	 * charting
	 * 
	 * @param i
	 *            row
	 * @param j
	 *            column
	 * @return value at (i,j)
	 */
	public String getLookups(int i, int j);

	/**
	 * 
	 * @return number of rows in GUI_Links3.csv
	 */
	public int getLookupsLength();
}
