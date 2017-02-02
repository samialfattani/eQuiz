package frawla.equiz.server;

import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;

import frawla.equiz.util.Util;
import frawla.equiz.util.exam.Question;
import frawla.equiz.util.exam.Student;
import javafx.collections.ObservableList;

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
			row.createCell(Base + (2*j+0)).setCellValue( "Q" + (j+1) );
			row.createCell(Base + (2*j+1)).setCellValue( "" ) ;
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
			
			String s = st.getOptionalExamSheet()
						.get()
						.getQustionList()
						.stream()
						.map( q -> q.getId() + "")
						.collect(Collectors.joining(", ")) ;

			
			row.createCell(2).setCellValue( s  );
			for (int j=0; j < QuesCount ; j++)
			{
				final int qid = j+1;
				Question q = st.getOptionalExamSheet()
						.get()
						.getQustionList()
						.stream()
						.filter( qs -> qs.getId() == qid)
						.findFirst()
						.get();
				row.createCell(Base + (2*j+0)).setCellValue( q.toString() );
				q.setStudentMark(q.correctAndGetTheMark());
				row.createCell(Base + (2*j+1)).setCellValue( q.getStudentMark());
			}//end for
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
						.getQustionList()
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

			String cStart = CellReference.convertNumToColString(Base + 0 );
			String cEnd = CellReference.convertNumToColString(  Base + (QuesCount-1) );
			// SUM(C2:P2)
			String cf = " SUM("+ 
					cStart + (i+1) + ":" + 
					cEnd   + (i+1) + ")";

			mySheet.getRow(i)
			.createCell( Base + QuesCount )
			.setCellFormula(cf);
		}

		//autoSize all columns
		for (int j=0; j < QuesCount+3 ; j++){
			mySheet.autoSizeColumn(j);
		}

	}//end RecordTimer



}//end class
