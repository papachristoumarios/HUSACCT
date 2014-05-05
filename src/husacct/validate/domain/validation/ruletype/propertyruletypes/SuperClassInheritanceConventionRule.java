package husacct.validate.domain.validation.ruletype.propertyruletypes;

import husacct.common.dto.AnalysedModuleDTO;
import husacct.common.dto.DependencyDTO;
import husacct.common.dto.RuleDTO;
import husacct.validate.domain.check.util.CheckConformanceUtilClass;
import husacct.validate.domain.configuration.ConfigurationServiceImpl;
import husacct.validate.domain.validation.Severity;
import husacct.validate.domain.validation.Violation;
import husacct.validate.domain.validation.ViolationType;
import husacct.validate.domain.validation.internaltransferobjects.Mapping;
import husacct.validate.domain.validation.ruletype.RuleType;
import husacct.validate.domain.validation.ruletype.RuleTypes;

import java.util.EnumSet;
import java.util.List;

public class SuperClassInheritanceConventionRule extends RuleType {
	private final static EnumSet<RuleTypes> superClassInheritanceExceptionRules = EnumSet.of(RuleTypes.IS_ALLOWED_TO_USE);

	public SuperClassInheritanceConventionRule(String key, String category, List<ViolationType> violationTypes, Severity severity) {
		super(key, category, violationTypes, superClassInheritanceExceptionRules, severity);
	}

	@Override
	public List<Violation> check(ConfigurationServiceImpl configuration, RuleDTO rootRule, RuleDTO currentRule) {
		violations.clear();
		mappings = CheckConformanceUtilClass.getMappingFromAndMappingTo(currentRule);
		physicalClasspathsFrom = mappings.getMappingFrom();
		List<Mapping> physicalClasspathsTo = mappings.getMappingTo();

		for (Mapping classPathFrom : physicalClasspathsFrom) {
			for (Mapping classPathTo : physicalClasspathsTo) {
				AnalysedModuleDTO from = analyseService.getModuleForUniqueName(classPathFrom.getPhysicalPath());
				AnalysedModuleDTO to = analyseService.getModuleForUniqueName(classPathTo.getPhysicalPath());
				if((!from.type.equals("package")) && (!to.type.equals("package"))){
					boolean classInherits = false;
					DependencyDTO[] dependencies = analyseService.getDependenciesFromTo(classPathFrom.getPhysicalPath(), classPathTo.getPhysicalPath());
					if(dependencies != null && dependencies.length > 0){
						for(DependencyDTO dependency : dependencies){
							if((dependency != null) && (dependency.type.equals("Inheritance"))){
								classInherits = true;
							}
						}	
					}
					if(classInherits == false){
						Violation violation = createViolation(rootRule, classPathFrom, classPathTo, configuration);
	                    violations.add(violation);
					}
				}
			}
		}
		return violations;
	}
}
