package husacct.control.task;

import husacct.ServiceProvider;
import husacct.common.dto.ApplicationDTO;
import husacct.common.dto.ProjectDTO;
import husacct.control.ControlServiceImpl;
import husacct.control.task.MainController;
import husacct.control.task.WorkspaceController;
import husacct.externalinterface.SaccCommandDTO;
import husacct.externalinterface.ViolationReportDTO;

import java.io.File;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

public class ExternalComplianceCheck {
	private ControlServiceImpl controlService;
	private MainController mainController;
	private WorkspaceController workspaceController;
	private ViolationReportDTO violationReport;

	private Logger logger = Logger.getLogger(ExternalComplianceCheck.class);

	/**
	 * Read service definition in class ExternalServiceProvider.
	 * @param saccCommandDTO: Read Javadoc of class SaccCommandDTO.
	 */
	public ViolationReportDTO performSoftwareArchitectureComplianceCheck(SaccCommandDTO saccCommandDTO) {
		violationReport = new ViolationReportDTO();
		try {
			logger.info(String.format(" Start: Software Architecture Compliance Check"));

			if ((saccCommandDTO.getHusacctWorkspaceFile() == null) || saccCommandDTO.getHusacctWorkspaceFile().equals("")) {
				logger.warn(" Parameter not set: husacctWorkspaceFile");
			} else {			
				setControllers();
				loadWorkspace(saccCommandDTO.getHusacctWorkspaceFile());
				setSourceCodePaths(saccCommandDTO);
				analyseApplication();
				checkConformance();
				violationReport = getViolationReportDTO(saccCommandDTO.getImportFilePreviousViolations(), saccCommandDTO.getExportAllViolations(), saccCommandDTO.getExportNewViolations());  
			}
			logger.info(String.format(" Finished: Software Architecture Compliance Check"));
		} catch (Exception e){
			logger.warn(" Exception: " + e.getMessage());
		}
		return violationReport;
	}

	private void setControllers(){ 
		controlService = (ControlServiceImpl) ServiceProvider.getInstance().getControlService();
		mainController = controlService.getMainController();
		workspaceController = mainController.getWorkspaceController();
 	} 
 	 
	private void loadWorkspace(String location) {
		logger.info(String.format("Loading workspace: %s", location));
		File file = new File(location);
		if(file.exists()){
			HashMap<String, Object> dataValues = new HashMap<String, Object>();
			dataValues.put("file", file);
			workspaceController.loadWorkspace("Xml", dataValues);
			if(workspaceController.isAWorkspaceOpened()){
				logger.info(String.format(new Date().toString() + " Workspace %s loaded", location));
			} else {
				logger.warn(String.format("Unable to open workspace: %s", file.getAbsoluteFile()));
			}
		} else {
			logger.warn(String.format("Unable to locate: %s", file.getAbsoluteFile()));
		}
	}

	private void setSourceCodePaths(SaccCommandDTO saccCommandDTO) {
		if ((saccCommandDTO.getSourceCodePaths() != null) && (saccCommandDTO.getSourceCodePaths().size() >= 1)) {
			ApplicationDTO applicationDTO = ServiceProvider.getInstance().getDefineService().getApplicationDetails();
			for (ProjectDTO project : applicationDTO.projects) {
				project.paths = saccCommandDTO.getSourceCodePaths();
			}
			ServiceProvider.getInstance().getDefineService().createApplication(applicationDTO.name, applicationDTO.projects, applicationDTO.version);
		}
	}

	private void analyseApplication() {
		controlService = (ControlServiceImpl) ServiceProvider.getInstance().getControlService();
		mainController = controlService.getMainController();
		mainController.getApplicationController().analyseApplication();
		//analyseApplication() starts a different Thread, and needs some time to finish
		boolean isAnalysing = true;
		while(isAnalysing){
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {}
			isAnalysing = mainController.getStateController().isAnalysing();
		}
	}

	private void checkConformance() {
		ServiceProvider.getInstance().getControlService().setValidating(true);
		logger.info(new Date().toString() + " CheckConformanceTask is Starting: IValidateService.checkConformance()" );
		ServiceProvider.getInstance().getValidateService().checkConformance();
		ServiceProvider.getInstance().getControlService().setValidating(false);
		logger.info(new Date().toString() + " CheckConformanceTask sets state Validating to false" );
		//checkConformance() starts a different Thread, and needs some time
		boolean isValidating = true;
		controlService = (ControlServiceImpl) ServiceProvider.getInstance().getControlService();
		mainController = controlService.getMainController();
		while(isValidating){
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {}
			isValidating = mainController.getStateController().isValidating();
		}
	}

	private ViolationReportDTO getViolationReportDTO(String importFilePreviousViolations, boolean exportAllViolations, boolean exportNewViolations) {
		ViolationReportDTO violationReport = new ViolationReportDTO();
		if (importFilePreviousViolations != null && !importFilePreviousViolations.equals("")) {
			File previousViolationsFile = new File(importFilePreviousViolations);
			if(previousViolationsFile.exists()){
				controlService = (ControlServiceImpl) ServiceProvider.getInstance().getControlService();
				mainController = controlService.getMainController();
				violationReport = mainController.getExportImportController().getViolationReportData(previousViolationsFile, exportAllViolations, exportNewViolations);
			} else {
				logger.warn(String.format("Unable to locate importFilePreviousViolations: %s", previousViolationsFile.getAbsoluteFile()));
				violationReport = mainController.getExportImportController().getViolationReportData(null, exportAllViolations, exportNewViolations);
			}
		} else {
			violationReport = mainController.getExportImportController().getViolationReportData(null, exportAllViolations, exportNewViolations);
		}
		return violationReport;
	}
}
