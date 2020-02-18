package ggeneral;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import frawla.equiz.server.ExamLoader;
import frawla.equiz.server.ExamLoaderXLSX;
import frawla.equiz.util.Util;
import frawla.equiz.util.exam.ExamConfig;
import frawla.equiz.util.exam.QuesinoOrderType;
import frawla.equiz.util.exam.Student;
import frawla.equiz.util.exam.StudentListType;
import frawla.equiz.util.exam.TimingType;
import javafx.embed.swing.JFXPanel;
import javafx.util.Duration;

public class ExamLoaderXLSXTest
{
	static ExamConfig exConfig;

	@BeforeAll
	public static void before() throws Exception
	{
		new JFXPanel();
		//       new File("./data/IT100-2.xlsx")
		//File f = new File( "D:\\Dropbox\\Sami-Programming\\JAVA\\eQuiz\\data\\MidTerm-1\\SE260-MidTerm-1.xlsx");
		File f = new File( Util.getResourceAsURI("IT100-2.xlsx"));
		
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
	}
	
	@Test
	public void LoadTest() throws EncryptedDocumentException, InvalidFormatException, IOException
	{
		assertEquals(5, ExamLoader.getInstance().getQustionList().size());
		assertEquals(2, ExamLoader.getInstance().getImageFiles().size());
		assertEquals(5, ExamLoader.getInstance().getStudentList().size());
		assertEquals(3, ExamLoader.getInstance().getBWList().size());
		assertEquals(0, ExamLoader.getInstance().getLog().size());
		
		System.out.println(
				ExamLoader.getInstance().getStudentList()
				.get(0).getLeftTime()
				);
	}

	@Test
	public void AllStudentAnswersAreNotNullTest() throws EncryptedDocumentException, InvalidFormatException, IOException
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
	public void duplicateAnswersTest() throws EncryptedDocumentException, InvalidFormatException, IOException{
		
		Workbook wrkBook;
		File f = new File(Util.getResourceAsURI("test.xlsx"));
        wrkBook = WorkbookFactory.create( f); //new File("data/test.xlsx") 
		Sheet mySheet = wrkBook.getSheet( "Sheet1" );
		List<Cell> cells = new ArrayList<>();
		cells.add( mySheet.getRow(0).getCell(0) );
		cells.add( mySheet.getRow(0).getCell(1) );
		cells.add( mySheet.getRow(0).getCell(2) );
		cells.add( mySheet.getRow(0).getCell(3) );
		cells.add( mySheet.getRow(0).getCell(4) );
		
		assertEquals(false, ExamLoaderXLSX.isThereAnyDuplicate(cells) );
		
		cells.add( mySheet.getRow(0).getCell(5) );
		assertEquals(true, ExamLoaderXLSX.isThereAnyDuplicate(cells) );
        wrkBook.close();
	}
	

	@Test
	public void LoadTimeTest() 
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
			  			   assertTrue(
			  					 q.getConsumedTime().greaterThanOrEqualTo( Duration.ZERO ) 
			  					   );
			  		   });
			  	});
		  });
		
	}//LoadTimeTest


	@Test
	public void LoadTimeCalculationTest() 
	{
		ExamLoader.getInstance()
		  .getStudentList()
		  .forEach(s -> s.toString());

		//make sure that all student answers don't have null
		Student st = ExamLoader.getInstance()
		  .getStudentList()
		  .stream()
		  .filter(s -> s.getId().equals("XXX111"))
		  .findFirst()
		  .get();
		
		assertEquals(
				5 * 60 + 30, 
				st.getSpendTime().toSeconds(), 1 ); 

		assertEquals(
				5 * 60 + 30, 
				st.getSpendTime().toSeconds(), 1 ); 

	}//LoadTimeTest

}//end class
