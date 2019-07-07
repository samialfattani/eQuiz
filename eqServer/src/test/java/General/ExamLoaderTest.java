package general;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

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
		
		f = new File( Util.getResourceAsURI("IT100-2-open-office.ods"));
		ExamLoader.getInstance(f); 
		
		assertTrue( ExamLoader.getInstance() instanceof ExamLoaderODS ); 

	}

}
