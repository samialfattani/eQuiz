package General;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import frawla.equiz.util.Util;

public class ExcelTest 
{
	private File myFile = new File(Util.getResource("test.xlsx")); //new File("data/test.xlsx");
	private Workbook wrkBook;
	@Before
	public void before() throws EncryptedDocumentException, InvalidFormatException, IOException
	{
		FileInputStream fin = new FileInputStream(myFile);
		wrkBook = WorkbookFactory.create( fin );
		fin.close();
		
	}
	
	
	@Test
	public void ReadWriteTest()  throws Exception
	{

		Sheet mySheet = wrkBook.getSheet( "Sheet1" );
		double[] values = {45, 10, 20, 55, 100};

		//read from Excel file
        for(int i=0; i<= 4 ; i++){
        	assertEquals(
        			values[i], 
        			mySheet.getRow(i).getCell(1).getNumericCellValue(), 0.0 );
        }

        //Write to Excel file
		Row totalRow = mySheet.createRow( 5 );
		totalRow.createCell( 0 ).setCellValue( "Total" );
		totalRow.createCell( 1 ).setCellFormula( "SUM(B1:B5)+2" );

		//save changes
		FileOutputStream fout = new FileOutputStream( myFile );
		wrkBook.write( fout );
		fout.close();

		Cell c = mySheet.getRow(5).getCell(1); //B6

		//FormulaEvaluator evaluator = wrkBook.getCreationHelper().createFormulaEvaluator();
		HSSFFormulaEvaluator.evaluateAllFormulaCells(wrkBook);
		//evaluator.notifyUpdateCell( c );
		
		assertEquals (c.getCellTypeEnum() , CellType.FORMULA);
		assertEquals (c.getCachedFormulaResultTypeEnum() , CellType.NUMERIC);
		assertEquals (232 , c.getNumericCellValue(), 0);
	}


	@Test
	public void ReWriteTest() throws Exception
	{

		Sheet timerSheet = Optional
				.ofNullable(wrkBook.getSheet("Timer"))
				.orElseGet( () -> wrkBook.createSheet("Timer"));

		assertEquals(timerSheet, wrkBook.getSheet("Timer"));

		FileOutputStream fout = new FileOutputStream( myFile ); 
		wrkBook.write( fout );
		fout.flush();

		fout.close();
	}

	@After
	public void after() throws IOException{
		wrkBook.close();
		
	}

}
