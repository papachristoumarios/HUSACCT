package husacct.validate.task;

import husacct.ServiceProvider;
import husacct.analyse.IAnalyseService;
import husacct.common.dto.RuleDTO;
import husacct.common.dto.ViolationDTO;
import husacct.common.locale.ILocaleService;
import husacct.define.IDefineService;
import husacct.validate.domain.assembler.ViolationDtoAssembler;
import husacct.validate.domain.configuration.ConfigurationServiceImpl;
import husacct.validate.domain.validation.Regex;
import husacct.validate.domain.validation.Severity;
import husacct.validate.domain.validation.Violation;
import husacct.validate.domain.validation.internaltransferobjects.FilterSettingsDTO;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class FilterController {

	private final ConfigurationServiceImpl configuration;
	private ArrayList<String> ruletypes = new ArrayList<String>();
	private ArrayList<String> violationtypes = new ArrayList<String>();
	private ArrayList<String> paths = new ArrayList<String>();
	private ILocaleService localeService = ServiceProvider.getInstance().getLocaleService();

	public FilterController(ConfigurationServiceImpl configuration) {
		this.configuration = configuration;
	}

	public void setFilterValues(FilterSettingsDTO dto, List<Violation> violations) {
		this.ruletypes = dto.getRuletypes();
		this.violationtypes = dto.getViolationtypes();
		this.paths = dto.getPaths();
	}

	public ArrayList<Violation> filterViolations(List<Violation> violations) {
		ArrayList<Violation> filteredViolations = new ArrayList<Violation>();
		for (Violation violation : violations) {
			boolean passesRuleTypeFilter = false;
			boolean passesViolationTypeFilter = false;
			boolean passesPathFilter = false;
			if(ruletypes.isEmpty()) {
				passesRuleTypeFilter = true;
			} else if(ruletypes.contains(localeService.getTranslatedString(violation.getRuletypeKey()))) {
				passesRuleTypeFilter = true;
			}
			if (passesRuleTypeFilter) {
				if(violationtypes.isEmpty()) {
					passesViolationTypeFilter = true;
				} else if(violationtypes.contains(localeService.getTranslatedString(violation.getViolationTypeKey()))) {
					passesViolationTypeFilter = true;
				}
				if (passesViolationTypeFilter) {
					if(paths.isEmpty()) {
						passesPathFilter = true;
					} else {
						if(paths.contains(violation.getClassPathFrom())) {
							passesPathFilter = true;
						} else {
							for (String path : paths) {
								if (Regex.matchRegex(Regex.makeRegexString(path), violation.getClassPathFrom())) {
									passesPathFilter = true;
								}
							}
							
						}
							
					}
					if (passesPathFilter) {
						filteredViolations.add(violation);
					}
				}
			}
		}
		return filteredViolations;
	}

	public ArrayList<String> loadRuletypes(List<Violation> violations) {
		ArrayList<String> AppliedRuletypes = new ArrayList<String>();

		for (Violation violation : violations) {
			if (!AppliedRuletypes.contains(localeService.getTranslatedString(violation.getRuletypeKey()))) {
				AppliedRuletypes.add(localeService.getTranslatedString(violation.getRuletypeKey()));
			}
		}

		return AppliedRuletypes;
	}

	public ArrayList<String> loadViolationtypes(List<Violation> violations) {
		ArrayList<String> appliedViolationtypes = new ArrayList<String>();

		for (Violation violation : violations) {
			String violationTypeKey = violation.getViolationTypeKey();
			if ((violationTypeKey != null) && (violationTypeKey != "")){
				if (!appliedViolationtypes.contains(localeService.getTranslatedString(violationTypeKey))) {
					appliedViolationtypes.add(localeService.getTranslatedString(violationTypeKey));
				}
			}
		}
		return appliedViolationtypes;
	}

	public ViolationDTO[] getViolationsByLogicalPath(String logicalpathFrom, String logicalpathTo) {
		IDefineService defineService = ServiceProvider.getInstance().getDefineService();
		List<String> physicalPathsFrom = new ArrayList<String>();
		physicalPathsFrom.addAll(defineService.getModule_AllPhysicalClassPathsOfModule(logicalpathFrom));
		List<String> physicalPathsTo = new ArrayList<String>();
		physicalPathsTo.addAll(defineService.getModule_AllPhysicalClassPathsOfModule(logicalpathTo));
		ViolationDTO[] returnValue = getViolationsByPhysicalPathLists(physicalPathsFrom, physicalPathsTo);
		return returnValue;
	}

	public ViolationDTO[] getViolationsByPhysicalPath(String physicalPathFrom, String physicalPathTo) {
		IAnalyseService analyseService = ServiceProvider.getInstance().getAnalyseService();
		List<String> physicalPathsFrom = analyseService.getAllPhysicalClassPathsOfSoftwareUnit(physicalPathFrom);
		List<String> physicalPathsTo = analyseService.getAllPhysicalClassPathsOfSoftwareUnit(physicalPathTo);
		ViolationDTO[] returnValue = getViolationsByPhysicalPathLists(physicalPathsFrom, physicalPathsTo);
		return returnValue;
	}

	private ViolationDTO[] getViolationsByPhysicalPathLists(List<String> physicalPathsFrom, List<String> physicalPathsTo){
		List<Violation> violations = new  ArrayList<Violation>();
		for (String pathFrom : physicalPathsFrom){
			for (String pathTo : physicalPathsTo){
				violations.addAll(configuration.getViolationsFromTo(pathFrom, pathTo));
			}
		}
		ViolationDtoAssembler assembler = new ViolationDtoAssembler();
		List<ViolationDTO> violationDTOs = assembler.createViolationDTO(violations);
		ViolationDTO[] returnValue = violationDTOs.toArray(new ViolationDTO[violationDTOs.size()]);
		return returnValue;
	}
	
	public ViolationDTO[] getViolationsByRule(RuleDTO appliedRule) {
		List<Violation> violations = configuration.getViolationsByRule(appliedRule.moduleFrom.logicalPath, appliedRule.moduleTo.logicalPath, appliedRule.ruleTypeKey);
		ViolationDtoAssembler assembler = new ViolationDtoAssembler();
		List<ViolationDTO> violationDTOs = assembler.createViolationDTO(violations);
		ViolationDTO[] returnValue = violationDTOs.toArray(new ViolationDTO[violationDTOs.size()]);
		return returnValue;
	}

	public LinkedHashMap<Severity, Integer> getViolationsPerSeverity(List<Violation> shownViolations, List<Severity> severities) {
		LinkedHashMap<Severity, Integer> violationsPerSeverity = new LinkedHashMap<Severity, Integer>();

		for (Severity severity : severities) {
			violationsPerSeverity.put(severity, 0);
		}

		for (Violation violation : shownViolations) {
			if (violation.getSeverity() != null) {
				int count = 0;
				try {
					count = violationsPerSeverity.get(violation.getSeverity());
				} catch (Exception e) {
				} finally {
					violationsPerSeverity.remove(violation.getSeverity());
					count = count + 1;
					violationsPerSeverity.put(violation.getSeverity(), count);
				}

			}
		}

		for (Severity severity : severities) {
			int amount = violationsPerSeverity.get(severity);
			violationsPerSeverity.remove(severity);
			violationsPerSeverity.put(severity, amount);
		}

		return violationsPerSeverity;
	}

	public ArrayList<String> getEnabledFilterRuleTypes() {
		return this.ruletypes;
	}

	public ArrayList<String> getEnabledFilterViolations() {
		return this.violationtypes;
	}
	
	public ArrayList<String> getEnabledFilterPaths() {
		return this.paths;
	}

}