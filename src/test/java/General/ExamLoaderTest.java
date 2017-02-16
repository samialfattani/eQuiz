package General;

import static org.junit.Assert.*;

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
import org.junit.Before;
import org.junit.Test;

import frawla.equiz.util.exam.ExamConfig;
import frawla.equiz.util.exam.ExamLoader;
import frawla.equiz.util.exam.QuesinoOrderType;
import frawla.equiz.util.exam.StudentListType;
import frawla.equiz.util.exam.TimingType;
import javafx.embed.swing.JFXPanel;
import javafx.util.Duration;

public class ExamLoaderTest
{
	ExamConfig exConfig;

	@Before
	public void before() throws EncryptedDocumentException, InvalidFormatException, IOException
	{
		new JFXPanel();
		ExamLoader.getInstance().load(new File("data/IT100-2.xlsx"));
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
		assertEquals(6, ExamLoader.getInstance().getQustionList().size());
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
        wrkBook = WorkbookFactory.create( new File("data/test.xlsx") );
		Sheet mySheet = wrkBook.getSheet( "Sheet1" );
		List<Cell> cells = new ArrayList<>();
		cells.add( mySheet.getRow(0).getCell(0) );
		cells.add( mySheet.getRow(0).getCell(1) );
		cells.add( mySheet.getRow(0).getCell(2) );
		cells.add( mySheet.getRow(0).getCell(3) );
		cells.add( mySheet.getRow(0).getCell(4) );
		
		assertEquals(false, ExamLoader.isThereAnyDuplicate(cells) );
		
		cells.add( mySheet.getRow(0).getCell(5) );
		assertEquals(true, ExamLoader.isThereAnyDuplicate(cells) );
        wrkBook.close();
	}
	
}//end class
