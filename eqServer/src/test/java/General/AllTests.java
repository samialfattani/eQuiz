package general;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
	ExamLoaderODSTest.class, 
	ExamLoaderTest.class, 
	ExamLoaderXLSXTest.class, 
	ExcelTest.class,
	General.class 
	})
public class AllTests {

}
