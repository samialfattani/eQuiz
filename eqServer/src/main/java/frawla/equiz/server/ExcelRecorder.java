
package frawla.equiz.server;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;

import frawla.equiz.util.Log;
import frawla.equiz.util.Util;
import frawla.equiz.util.exam.Question;
import frawla.equiz.util.exam.Student;
import javafx.collections.ObservableList;
import javafx.util.Duration;

public class ExcelRecorder
{


	public static void RecordAnswers(Workbook wrkBook, ObservableList<Student> Students, int QuesCount)
	{
		Optional.ofNullable(wrkBook.getSheet("Answers"))
				.ifPresent( sh -> wrkBook.removeSheetAt(wrkBook.getSheetIndex("Answers") ));

		Sheet mySheet = wrkBook.createSheet("Answers"); 
		Row row = mySheet.createRow(0);

		row.createCell(0).setCellValue("ID");
		row.createCell(1).setCellValue("Name");
		row.createCell(2).setCellValue("Order");
		int Base = 3; //starting after second cell

		//Header: 1, 2, 3, 
		for (int j=0; j < QuesCount ; j++){
			String mark = wrkBook.getSheet("Questions").getRow(j+2).getCell(10).toString();
			row.createCell(Base + (2*j+0)).setCellValue( "Q" + (j+1) );
			row.createCell(Base + (2*j+1)).setCellValue( mark ) ;
		}

		//Body
		for (int i=0; i < Students.size() ; i++)
		{
			Student st = Students.get(i);			
			row = mySheet.createRow(i+1);
			row.createCell(0).setCellValue( st.getId()  );
			row.createCell(1).setCellValue( st.getName() );
			
			if(!st.getOptionalExamSheet().isPresent())
				continue;
			
			//5,2,4,1,3
			String quesList = st.getOptionalExamSheet()
						.get()
						.getQuestionList()
						.stream()
						.map( q -> q.getId() + "")
						.collect(Collectors.joining(", ")) ;
			
			row.createCell(2).setCellValue( quesList  );
			
			//record answer "Q2`2`ABCDE`E`teacher note"
			//record mark 
			for (int j=0; j < QuesCount ; j++)
			{
				final int qid = j+1;
				Question q = st.getOptionalExamSheet()
						.get()
						.getQuestionList()
						.stream()
						.filter( qs -> qs.getId() == qid)
						.findFirst()
						.get();
				row.createCell(Base + (2*j+0)).setCellValue( q.toString() );
				row.getCell(2*j+0).getCellStyle().setWrapText(true);
				
				row.createCell(Base + (2*j+1)).setCellValue( q.getStudentMark());
			}//end for
			
			//st.setStatus(Student.GRADED);
		}//end for

		mySheet.getRow(0)
		.createCell( Base + (QuesCount*2+0) )
		.setCellValue( "Total" ) ;
		for (int i=1; i <= Students.size() ; i++)
		{

			String cStart = CellReference.convertNumToColString(Base);
			String cEnd = CellReference.convertNumToColString( Base + (QuesCount*2-1) );
			// SUMIF(C2:P2 ; ">0")
			String cf = " SUMIF("+ 
					cStart + (i+1) + ":" + 
					cEnd + (i+1) + " , "+
					" \">0\") ";
			mySheet.getRow(i)
			.createCell( Base + (QuesCount*2+0) )
			.setCellFormula(cf);
		}

		//autoSize all columns
		for (int j=0; j < QuesCount*2+3 ; j++){
			mySheet.autoSizeColumn(j);
		}

	}

	public static void RecordTimer(Workbook wrkBook, ObservableList<Student> Students, int QuesCount)
	{

		Optional.ofNullable(wrkBook.getSheet("Timer"))
				.ifPresent( sh -> wrkBook.removeSheetAt(wrkBook.getSheetIndex("Timer") ));
		Sheet mySheet = wrkBook.createSheet("Timer"); 

		Row row = mySheet.createRow(0);

		row.createCell(0).setCellValue("ID");
		row.createCell(1).setCellValue("Name");
		int Base = 2; //starting after second cell

		//Header: 1, 2, 3, 
		for (int j=0; j < QuesCount ; j++){
			row.createCell(Base + j).setCellValue( "Q" + (j+1) ) ;
		}

		
		//Body
		for (int i=0; i < Students.size() ; i++)
		{

			Student st = Students.get(i);			

			if(!st.getOptionalExamSheet().isPresent())
				continue;

			row = mySheet.createRow(i+1);
			row.createCell(0).setCellValue( st.getId()  );
			row.createCell(1).setCellValue( st.getName() );
			for (int j=0; j < QuesCount ; j++)
			{
				final int qid = j+1;
				Question q = st.getOptionalExamSheet()
						.get()
						.getQuestionList()
						.stream()
						.filter( qs -> qs.getId() == qid)
						.findFirst()
						.get();
				
				row.createCell(Base + j).setCellValue( Util.formatTime( q.getConsumedTime() )  );
			}//end for
			
		}//end for

		mySheet.getRow(0)
		.createCell( Base + QuesCount )
		.setCellValue( "Total" ) ;
		for (int i=1; i <= Students.size() ; i++)
		{
			Student st = Students.get(i-1);			

			if(!st.getOptionalExamSheet().isPresent())
				continue;

			double totalTime = st.getOptionalExamSheet()
								  .get()
								  .getQuestionList()
								  .stream()
								  .mapToDouble( q -> q.getConsumedTime().toSeconds() )
								  .sum();

			String tt = Util.formatTime(new Duration(totalTime*1000));
			mySheet.getRow(i)
			   .createCell( Base + QuesCount )
		       .setCellValue(  tt );
		}

		//autoSize all columns
		for (int j=0; j < QuesCount+3 ; j++){
			mySheet.autoSizeColumn(j);
			int w = (mySheet.getColumnWidth(j) > 50)? 50 : mySheet.getColumnWidth(j); 
			mySheet.setColumnWidth(j,  w);
		}
		

	}//end RecordTimer


	public static void RecordLogs(Workbook wrkBook, List<Log> logList) 
	{
		Optional.ofNullable(wrkBook.getSheet("Log"))
			.ifPresent( sh -> wrkBook.removeSheetAt(wrkBook.getSheetIndex("Log") ));
		
		Sheet mySheet = wrkBook.createSheet("Log");
		
		//Header: 1, 2, 3,
		Row row = mySheet.createRow(0);

		row.createCell(0).setCellValue("Time");
		row.createCell(1).setCellValue("Description");
		int Base = 0;
		
		//Body
		for (int i=0; i < logList.size() ; i++){
			row = mySheet.createRow(i+1);
			row.createCell(Base + 0).setCellValue( logList.get(i).getTime().format() ) ;
			row.createCell(Base + 1).setCellValue( logList.get(i).getText() ) ;
		}
		
	}
}//end class
