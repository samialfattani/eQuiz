package general;


import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

import frawla.equiz.server.ExamLoader;
import frawla.equiz.server.ExamLoaderODS;
import frawla.equiz.server.ExamLoaderXLSX;
import frawla.equiz.util.Util;

public class ExamLoaderTest {

	@Test
	public void test() 
	{
		File f;
		f = new File( Util.getResourceAsURI("IT100-2.xlsx"));
		ExamLoader.getInstance(f); 
		
		assertTrue( ExamLoader.getInstance() instanceof ExamLoaderXLSX ); 
		
		
		// --------- SAVE OBJECT FOR MOCK -----
		Util.Save(ExamLoader.getInstance().getStudentList()
					.stream()
					.filter(st -> st.getId().equals("XXX111"))
					.findFirst().get()
					.getOptionalExamSheet()
					.get(), 
					Util.getResourceAsFile("ExamSheet-sami.mock")
					);
		
		Util.Save(ExamLoader
					.getInstance()
					.getExamConfig(),
					Util.getResourceAsFile("ExamConfig.mock"));
		
		
		Util.Save(ExamLoader
				.getInstance()
				.generateNewSheet(),
				Util.getResourceAsFile("ExamSheet-empty.mock"));
		
		///----------------------------
		f = new File( Util.getResourceAsURI("IT100-2-open-office.ods"));
		ExamLoader.getInstance(f); 
		
		assertTrue( ExamLoader.getInstance() instanceof ExamLoaderODS );
		
		
		
	}

}
