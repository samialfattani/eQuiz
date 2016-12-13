package General;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import frawla.equiz.util.exam.Exam;
import frawla.equiz.util.exam.MultipleChoice;

public class ExamTest
{

    private File myFile;
	private Workbook wrkBook;

	@Before
	public void beofre() throws EncryptedDocumentException, InvalidFormatException, IOException{
        myFile = new File("data/test.xlsx");
        wrkBook = WorkbookFactory.create( myFile );
        
        
        
	}

	@Test
	public void MultipleChoiceTest()
	{
		MultipleChoice q = new MultipleChoice();
		Map<String, String> h = new HashMap<>();
		h.put("A", "sami");
		h.put("B", "koko");
		h.put("C", "meme");
		h.put("D", "kareem");
		q.setChoices(h);
		
		q.setCorrectAnswer("A");
		
		
		q.setStudentAnswer("meme");
		assertEquals(false, q.isCorrectAnswer());

		q.setStudentAnswer("sami");		
		assertEquals(true, q.isCorrectAnswer());
		
		assertEquals("{A=sami, B=koko, C=meme, D=kareem}", q.getChoices().toString()); 
		assertEquals("[A, B, C, D]", q.getOrderList().toString());

	}
	
	@Test
	public void duplicateAnswersTest(){
		
		Sheet mySheet = wrkBook.getSheet( "Sheet1" );
		List<Cell> cells = new ArrayList<>();
		cells.add( mySheet.getRow(0).getCell(0) );
		cells.add( mySheet.getRow(0).getCell(1) );
		cells.add( mySheet.getRow(0).getCell(2) );
		cells.add( mySheet.getRow(0).getCell(3) );
		cells.add( mySheet.getRow(0).getCell(4) );
		
		assertEquals(false, Exam.isThereAnyDuplicate(cells) );
		
		cells.add( mySheet.getRow(0).getCell(5) );
		assertEquals(true, Exam.isThereAnyDuplicate(cells) );
	}

	@After
	public void after() throws IOException{
        
        wrkBook.close();
	}
}
