package domain.indirect.allowedfrom;

import domain.indirect.BaseIndirect;

public class CallInstanceMethodIndirect_SuperClass extends BaseIndirect{
	
	public CallInstanceMethodIndirect_SuperClass(){};
	
	public void MethodOfSuperClass() {
		subDao.MethodOnSuperClass(); 
	}
}
