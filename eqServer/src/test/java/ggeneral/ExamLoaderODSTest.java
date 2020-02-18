package ggeneral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Table;

import frawla.equiz.server.ExamLoader;
import frawla.equiz.server.ExamLoaderODS;
import frawla.equiz.util.Util;
import frawla.equiz.util.exam.ExamConfig;
import frawla.equiz.util.exam.QuesinoOrderType;
import frawla.equiz.util.exam.StudentListType;
import frawla.equiz.util.exam.TimingType;
import javafx.embed.swing.JFXPanel;
import javafx.util.Duration;

public class ExamLoaderODSTest
{
	static ExamConfig exConfig;

	@BeforeAll
	public static void before() throws Exception
	{
		new JFXPanel();
		File f = new File( Util.getResourceAsURI("IT100-2-open-office.ods"));
		ExamLoader.getInstance(f); 
		exConfig = ExamLoader.getInstance().getExamConfig();
	}

	@Test
	public void LoadExamFileTest() 
	{
		assertEquals(QuesinoOrderType.RANDOM , exConfig.questionOrderType);
		assertEquals(StudentListType.ALL_STUDENTS, exConfig.studentListType);
		assertEquals(TimingType.EXAM_LEVEL, exConfig.timingType);
		assertEquals(new Duration( 45*60*1000 ), exConfig.examTime);
		
		assertEquals("IT100", exConfig.courseID);
		assertEquals("Computer Skills", exConfig.courseName);
		assertEquals("1", exConfig.courseSection);
		assertEquals("2016", exConfig.courseYear);
		assertEquals("Fall", exConfig.courseSemester);
		assertEquals("2nd MidTerm", exConfig.courseTitle);
	}
	
	@Test
	public void LoadTest() 
	{
		assertEquals(5, ExamLoader.getInstance().getQustionList().size());
	}

	@Test
	public void AllStudentAnswersAreNotNullTest() 
	{
		//make sure that all student answers don't have null
		ExamLoader.getInstance()
				  .getStudentList()
				  .stream()
				  .forEach( st -> {

					  st.getOptionalExamSheet()
					  	.ifPresent( sht -> {
					  		
					  		sht.getQuestionList()
					  		   .stream()
					  		   .forEach( q -> {
					  			   //System.out.println(q);
					  			   assertNotNull( q.getStudentAnswer() );
					  		   });
					  	});
				  });
	}//end test

	
	@Test
	public void duplicateAnswersTest() throws Exception 
	{
		
		
		File f = new File(Util.getResourceAsURI("IT100-2-open-office.ods"));
		SpreadsheetDocument wrkBook = SpreadsheetDocument.loadDocument(f);
		
		Table mySheet = wrkBook.getTableByName( "test" );
		List<Cell> cells = new ArrayList<>();
		cells.add( mySheet.getRowByIndex(0).getCellByIndex(0) );
		cells.add( mySheet.getRowByIndex(0).getCellByIndex(1) );
		cells.add( mySheet.getRowByIndex(0).getCellByIndex(2) );
		cells.add( mySheet.getRowByIndex(0).getCellByIndex(3) );
		cells.add( mySheet.getRowByIndex(0).getCellByIndex(4) );
		
		assertEquals(false, ExamLoaderODS.isThereAnyDuplicate(cells) );
		
		cells.add( mySheet.getRowByIndex(0).getCellByIndex(5) );
		assertEquals(true, ExamLoaderODS.isThereAnyDuplicate(cells) );
        wrkBook.close();
	}
	
}//end class
