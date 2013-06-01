package husacct.define.domain.appliedrule.relationrules;

import husacct.define.domain.appliedrule.AppliedRuleStrategy;
import husacct.define.domain.module.modules.Layer;
import husacct.define.task.conventions_checker.LayerCheckerHelper;
import husacct.define.task.conventions_checker.ModuleCheckerHelper;

import java.util.ArrayList;

public class IsNotAllowedToMakeSkipCallRule extends AppliedRuleStrategy{
	private ModuleCheckerHelper moduleCheckerHelper;
	private LayerCheckerHelper layerCheckerHelper;

	public boolean checkConvention() {
		moduleCheckerHelper = new ModuleCheckerHelper();
		layerCheckerHelper = new LayerCheckerHelper(this.getModuleTo());
		boolean conventionSuccess = moduleCheckerHelper.checkRuleTypeAlreadySet(
				this.getRuleType(), this.getModuleFrom());
		if (conventionSuccess) {
			conventionSuccess = layerCheckerHelper.checkTypeIsLayer(this.getModuleFrom());
		}
		if (conventionSuccess) {
			ArrayList<Layer> skipCallLayers = layerCheckerHelper
					.getSkipCallLayers(this.getModuleFrom().getId());
			for (Layer skipCallLayer : skipCallLayers) {
				this.setModuleTo(skipCallLayer);
				if (!checkIsNotAllowedToUse()) {
					conventionSuccess = false;
					break;
				}
			}
		}
		return true;
	}

	private boolean checkIsNotAllowedToUse() {
		boolean isNotAllowedToUseSucces = moduleCheckerHelper
				.checkRuleTypeAlreadyFromThisToSelected("IsOnlyAllowedToUse",
						this.getModuleFrom(), this.getModuleTo());
		if (isNotAllowedToUseSucces) {
			isNotAllowedToUseSucces = moduleCheckerHelper
					.checkRuleTypeAlreadyFromThisToSelected(
							"IsOnlyModuleAllowedToUse", this.getModuleFrom(), this.getModuleTo());
		}
		if (isNotAllowedToUseSucces) {
			isNotAllowedToUseSucces = moduleCheckerHelper
					.checkRuleTypeAlreadyFromThisToSelected("IsAllowedToUse",
							this.getModuleFrom(), this.getModuleTo());
		}
		if (isNotAllowedToUseSucces) {
			isNotAllowedToUseSucces = moduleCheckerHelper
					.checkRuleTypeAlreadyFromThisToSelected("MustUse",
							this.getModuleFrom(), this.getModuleTo());
		}
		if (isNotAllowedToUseSucces
				&& layerCheckerHelper.checkTypeIsLayer(this.getModuleFrom())
				&& layerCheckerHelper.checkTypeIsLayer(this.getModuleTo())) {
			ArrayList<Layer> backCallLayers = layerCheckerHelper
					.getBackCallLayers(this.getModuleFrom().getId());
			ArrayList<Layer> skipCallLayers = layerCheckerHelper
					.getSkipCallLayers(this.getModuleFrom().getId());
			for (Layer skipCallLayer : skipCallLayers) {
				if (skipCallLayer.equals(this.getModuleTo())) {
					isNotAllowedToUseSucces = moduleCheckerHelper
							.checkRuleTypeAlreadySet(
									"IsNotAllowedToMakeSkipCall", this.getModuleFrom());
				}
			}
			for (Layer backCallLayer : backCallLayers) {
				if (backCallLayer.equals(this.getModuleTo())) {
					isNotAllowedToUseSucces = moduleCheckerHelper
							.checkRuleTypeAlreadySet(
									"IsNotAllowedToMakeBackCall", this.getModuleFrom());
				}
			}
		}
		return isNotAllowedToUseSucces;
	}

}
