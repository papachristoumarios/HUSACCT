package husaccttest.analyse;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
	Java_AccuracyTestDependencyDetection.class,
	CSharp_AccuracyTestDependencyDetection.class,
	ExportImportAnalysedModelTest.class,
	AlgorithmTests.class,
})
public class AnalyseTestSuite {
}
