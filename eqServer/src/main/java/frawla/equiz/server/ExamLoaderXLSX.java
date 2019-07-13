package frawla.equiz.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.rits.cloning.Cloner;

import frawla.equiz.util.EQDate;
import frawla.equiz.util.Log;
import frawla.equiz.util.Util;
import frawla.equiz.util.exam.BlankField;
import frawla.equiz.util.exam.ExamConfig;
import frawla.equiz.util.exam.ExamSheet;
import frawla.equiz.util.exam.MultipleChoice;
import frawla.equiz.util.exam.QuesinoOrderType;
import frawla.equiz.util.exam.Question;
import frawla.equiz.util.exam.Student;
import frawla.equiz.util.exam.StudentListType;
import frawla.equiz.util.exam.TimingType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Duration;

public class ExamLoaderXLSX extends ExamLoader
{

	private File sourceFile;

	private List<Question> questionList = new ArrayList<>();
	private List<String> BWList = new ArrayList<>();
	private List<String> ErrReport = new ArrayList<>();
	private ExamConfig examConfig = new ExamConfig();
	private List<File> imageList = new ArrayList<>();
	private static ObservableList<Student> students;

	private List<Log> logList;

	public ExamLoaderXLSX(File srcFile) 
	{ 
		try(FileInputStream fin = new FileInputStream(srcFile);
			Workbook wrkBook = WorkbookFactory.create( fin );) 
		{
			
			sourceFile = srcFile;
			
			examConfig   = LoadFileConfig( wrkBook);
			questionList = loadQuestionList( wrkBook);
			imageList    = loadImages( wrkBook);
			BWList       = loadStdBWlist( wrkBook);
			students     = loadStudentList( wrkBook); //load answers and times
			logList      = loadLog( wrkBook);
			
		} catch (IOException | EncryptedDocumentException |  ParseException e) {
			e.printStackTrace();
		}
		
	}

	private ExamConfig LoadFileConfig(Workbook wrkBook) 
	{
		Sheet mySheet = wrkBook.getSheet( "Config" );
		ExamConfig examConfig = new ExamConfig();
		examConfig.SourceFile = sourceFile;

		String data = "";

		examConfig.sharingFolder = mySheet.getRow( 0 ).getCell( 1 ).getStringCellValue();
		data = mySheet.getRow( 1 ).getCell( 1 ).getStringCellValue();
		examConfig.questionOrderType = (data.equals("Sequence")? QuesinoOrderType.SEQUENCE: QuesinoOrderType.RANDOM);
		data = mySheet.getRow( 2 ).getCell( 1 ).getStringCellValue();
		examConfig.studentListType = (
				data.equals("All Students")?
						StudentListType.ALL_STUDENTS: 
							(data.equals("White List")?
									StudentListType.WHITE_LIST:
										StudentListType.BLACK_LIST));

		data = mySheet.getRow( 3 ).getCell( 1 ).getStringCellValue();
		examConfig.timingType = (data.equals("Exam Level")?TimingType.EXAM_LEVEL:TimingType.QUESTION_LEVEL);

		double d = mySheet.getRow( 4 ).getCell( 1 ).getNumericCellValue();
		examConfig.examTime = new Duration(d * 60 * 1000 );


		examConfig.courseID = mySheet.getRow( 6 ).getCell( 1 ).getStringCellValue().toString();
		examConfig.courseName = mySheet.getRow( 7 ).getCell( 1 ).getStringCellValue().toString();
		examConfig.courseSection = String.format("%.0f", Double.parseDouble(mySheet.getRow( 8 ).getCell( 1 ).toString()) );
		examConfig.courseYear = String.format("%.0f", Double.parseDouble(mySheet.getRow( 9 ).getCell( 1 ).toString()) );
		examConfig.courseSemester = mySheet.getRow( 10 ).getCell( 1 ).getStringCellValue().toString();
		examConfig.courseTitle = mySheet.getRow( 11 ).getCell( 1 ).getStringCellValue().toString();
		
		return examConfig;
	}

	private List<Question> loadQuestionList(Workbook wrkBook) 
	{
		List<Question> questionList = new ArrayList<>();
		Sheet shtQuestions = wrkBook.getSheet("Questions");
		questionList.clear();

		if( !isValidQuestionSheet(shtQuestions)){
			StringBuilder s = new StringBuilder("INVALID QUESTION SHEET\n");
			ErrReport.forEach( e -> {
				s.append( ErrReport.toString() + "\n" );	
			});

			Util.showError( s.toString() );
			ErrReport.clear();
			return null;
		}


		for (int i=2; i < shtQuestions.getPhysicalNumberOfRows(); i++)
		{
			Row row = shtQuestions.getRow( i );
			row.getCell( 0 ).setCellValue(i-1); //put question number.

			Question q =null;
			String qtype = row.getCell( 2 ).getStringCellValue(); //question Type

			//fill question with options
			if(qtype.equals(Question.MULTIPLE_CHOICE))
				q = getQuestionFromRow( new MultipleChoice(),  row );
			else if(qtype.equals(Question.BLANK_FIELD))
				q = getQuestionFromRow( new BlankField(),  row );

			q.setType(qtype);
			q.setId(i-1); //1 ,2, 3, 4...etc
			q.setText(row.getCell( 1 ).getStringCellValue()); //Question Text
		
			//image cell
			Cell c = row.getCell( 9 );
			if(!isBlankCell(c)){
				File f = new File(c.toString());
				q.setImgFileName(f.getName());
			}

			//mark cell
			q.setMark(row.getCell( 10 ).getNumericCellValue());

			//time cell
			if(examConfig.timingType == TimingType.QUESTION_LEVEL){
				double min = row.getCell( 11 ).getNumericCellValue(); 
				q.setTime( Duration.minutes(min) );
			}
			questionList.add(q);

		}
		//getQustionList().forEach( qu -> System.out.println(qu) );
		return questionList;
	}

	private MultipleChoice getQuestionFromRow(MultipleChoice q, Row row)
	{
		Cell c ;
		q.setCorrectAnswer("A");
		for(int j=0; j<6; j++)
		{
			//option text
			c = row.getCell( j+3 );
			if(!isBlankCell(c)) {
				String[] key = {"A", "B", "C", "D", "E","F","G","H","I"};
				q.getChoices().put( key[j] , c.toString() );				
			}
		}

		q.resetOrderList();
		return q;
	}

	private BlankField getQuestionFromRow(BlankField q, Row row)
	{
		Cell c ;
		for(int j=0; j<6; j++)
		{
			//option text
			c = row.getCell( j+3 );
			if(!isBlankCell(c)) {
				q.getCorrectAnswerList().add( c.getStringCellValue() );
			}
		}
		return q;
	}

	private List<String> loadStdBWlist(Workbook wrkBook)
	{
		List<String> BWList = new ArrayList<>();
		BWList.clear();
		Sheet BWSheet = wrkBook.getSheet("BlackWhite_List");
		if( BWSheet == null) {
			Util.showError("Sheet of 'BlckWhite_List' is not found");
			return null;
		}
		
		for (int i=1; i < BWSheet.getPhysicalNumberOfRows(); i++)
		{
			String s = BWSheet.getRow(i).getCell(0).toString();
			BWList.add(s);
		}
		return BWList;
	}

	private List<File> loadImages(Workbook wrkBook)
	{
		List<File> imageList = new ArrayList<>();
		imageList.clear();
		Sheet shtQuestions = wrkBook.getSheet("Questions");
		for (int i=2; i < shtQuestions.getPhysicalNumberOfRows(); i++)
		{			
			Cell c = shtQuestions.getRow( i ).getCell( 9 );
			if(!isBlankCell(c)){
				File f = new File(c.toString());

				if(!f.exists())
					f = new File(sourceFile.getParent(), c.toString());

				imageList.add( f );
			}
		}
		return imageList;
	}


	private ObservableList<Student> loadStudentList(Workbook wrkBook)
	{
		ObservableList<Student> students = FXCollections.observableArrayList();
		Sheet shtAnswer = wrkBook.getSheet("Answers");
		Sheet shtTimer = wrkBook.getSheet("Timer");
		Row row;
		if(shtAnswer ==null)
			return students;

		for (int i=1; i < shtAnswer.getPhysicalNumberOfRows(); i++)
		{
			row = shtAnswer.getRow(i);
			String sid = row.getCell(0).getStringCellValue();
			Student st = new Student(sid);			
			st.setName(row.getCell(1).getStringCellValue());

			ExamSheet examSheet = new ExamSheet();
			examSheet.setExamConfig( new Cloner().deepClone( examConfig )  );

			//read all answers and timers
			List<Question> Qlst = extractQuestionList( shtAnswer.getRow(i), shtTimer.getRow(i) );
			examSheet.setQustionList(  Qlst );
			st.setExamSheet(examSheet);
			
			//reset Start, Resume, Finish Points
			st.reset( getExamConfig().examTime );
			
			students.add(st);
		}
		
		return students;
	}

	private List<Question> extractQuestionList(Row stRow, Row timerRow) 
	{
		List<Question> Qlst = new ArrayList<>();
		
		if( stRow.getCell(2).toString().isEmpty() )
			return Qlst;
		
		//{5,4,2,1,3}		
		int[] QIDs = Arrays.asList(stRow.getCell(2).getStringCellValue().split(","))
				.stream()
				.map(String::trim)
				.mapToInt(Integer::parseInt)
				.toArray();

		for (int j=0; j < QIDs.length; j++)
		{
			int base = 3;
			int qid = QIDs[j];
			Question qj = questionList.stream()
							 .filter(q -> q.getId() == qid)
							 .findFirst()
							 .get();

			String timeCellContent = timerRow.getCell( 2 + (qid-1) ).getStringCellValue();
			String cellContent = stRow.getCell(base + (qid-1)*2).getStringCellValue() ;

			if( qj instanceof MultipleChoice) 
			{
				
				MultipleChoice mcCopy = new MultipleChoice(cellContent);
				mcCopy.setText(qj.getText()+"");
				mcCopy.copyChoices(qj);
				mcCopy.setStudentMark(  stRow.getCell(base + (qid-1)*2+1).getNumericCellValue() );
				mcCopy.setConsumedTime( toDuration(timeCellContent)  );
				Qlst.add( new Cloner().deepClone(mcCopy)  );
			}
			else if(qj instanceof BlankField)
			{
				BlankField qbCopy = new BlankField(cellContent);
				qbCopy.setText(qj.getText()+"");
				qbCopy.copyOptions(qj);
				qbCopy.setStudentMark(  stRow.getCell(base + (qid-1)*2+1).getNumericCellValue() );
				qbCopy.setConsumedTime( toDuration(timeCellContent)  );
				Qlst.add( new Cloner().deepClone(qbCopy) );
			}

		}
		return Qlst;
		
	}

	private Duration toDuration(String timeCellContent)
	{
		LocalTime lt = LocalTime.parse("00:" + timeCellContent);
		double ms = (lt.getHour()*3600 + lt.getMinute()*60 + lt.getSecond()) *1000 ;
		Duration d = new Duration( ms );
		return d;
	}

	private boolean isValidQuestionSheet(Sheet shtQuestions)
	{
		// 
		boolean validSheet = true;

		int QuesCount = shtQuestions.getPhysicalNumberOfRows();
		//1. Make sure all question text, type, and First Option are not blank
		List<Cell> cells = new ArrayList<>();		
		for (int i=2; i < QuesCount; i++)
		{	
			cells.add(shtQuestions.getRow(i).getCell(1));
			cells.add(shtQuestions.getRow(i).getCell(2));
			cells.add(shtQuestions.getRow(i).getCell(3));
		}

		if( cells.stream().anyMatch( c -> isBlankCell(c) )){
			ErrReport.add("Some required cells found blank. Text, Type, and Option(A) can never be blank.");
			validSheet = false;
		}

		//2. Make sure question ID is in order (1,2,3,...)
		double id =1;
		for (int i=2; i < QuesCount; i++)
		{	
			double cellID = shtQuestions.getRow(i).getCell(0).getNumericCellValue();

			if( cellID != id++ ){
				ErrReport.add("Question ID must be in order (1,2,3,4...");
				validSheet = false;
				break;
			}
		}


		//3. Make sure no duplicate answers.
		for (int i=2; i < QuesCount; i++)
		{
			cells.clear();
			for (int j=3; j< 8; j++)
				cells.add(shtQuestions.getRow(i).getCell(j));

			boolean ok = cells.stream()
					.filter(c -> !isBlankCell( c ) )
					.allMatch(new HashSet<>()::add );
			if(! ok){
				ErrReport.add("Question No. " + (i-1) + " has duplicate answers. Please review choices");
				validSheet = false;
			}
		}

		//4. Make sure no blank answer in middle
		for (int i=2; i < QuesCount; i++)
		{
			cells.clear();
			for (int j=3; j< 8; j++)
				cells.add(shtQuestions.getRow(i).getCell(j));

			long ansCount = cells.stream()
					.filter(c -> !isBlankCell( c ) )
					.count();

			for (int j=0; j< ansCount; j++){
				if( isBlankCell( cells.get(j) ) ){
					ErrReport.add("Question No. " + (i-1) + " has blank answer in the middle. All blank cells should be in the right side.");
					validSheet = false;
				}
			}
		}

		//5. Make sure all images are exists.
		for (int i=2; i < QuesCount; i++)
		{
			Cell c = shtQuestions.getRow(i).getCell(9);
			if( isBlankCell( c ) )
				continue;

			File f1 = new File(c.toString()) ;
			
			File f2 = new File(sourceFile.getParent(),  c.toString());
			if( !f1.exists()  && !f2.exists())
			{
				ErrReport.add("The File '" + f1.getName() + "' dose not exists.");
				validSheet = false;
			}
		}


		//7. Make sure Every qustion has mark
		cells.clear();		
		for (int i=2; i < QuesCount; i++){
			cells.add(shtQuestions.getRow(i).getCell(10));
		}

		if( cells.stream().anyMatch( c -> !Util.isNumeric(c.toString())  )){
			ErrReport.add("Some Questions dose not have marks or they have invalid marks.");
			validSheet = false;
		}

		//6. Make sure Every qustion has Time
		if(examConfig.timingType == TimingType.QUESTION_LEVEL)
		{
			cells.clear();
			for (int i=2; i < QuesCount; i++){
				cells.add(shtQuestions.getRow(i).getCell(11));
			}
			if( cells.stream().anyMatch( c -> !Util.isNumeric(c.toString())  )){
				ErrReport.add("Some Questions dose not have time. if Timing mode is set to 'Question_Level' then each question must has it's own time.");
				validSheet = false;
			}
		}

		return validSheet;

	}// end sheet Validation

	private List<Log> loadLog(Workbook wrkBook) throws ParseException
	{
		logList = new ArrayList<>();
		
		Sheet shtLog = wrkBook.getSheet("Log");
		if(shtLog == null)
			return logList;

		for (int i=1; i < shtLog.getPhysicalNumberOfRows(); i++)
		{
			String time = shtLog.getRow(i).getCell(0).toString();
			String text = shtLog.getRow(i).getCell(1).toString();
			
			Log log = new Log( new EQDate(Util.MY_DATE_FORMAT, time) , text);
			logList.add( log );
		}
		return logList;
	}
	
	//---------------------------------------------

	private static String cellToLower(Cell c){
		return c.toString().toLowerCase();

	}

	private static boolean isBlankCell(Cell c)
	{
		if(c == null )
			return true;

		if(c.toString().trim().replaceAll("\\s+", "").equals(""))
			return true;

		return c.getCellType( ) == CellType.BLANK ;
	}

	public static boolean isThereAnyDuplicate(List<Cell> cells)
	{
		boolean isUnique; 
		isUnique = cells.stream()
				.filter(c -> ! isBlankCell( c ) )
				.map( ExamLoaderXLSX::cellToLower )
				.allMatch(new HashSet<>()::add );	

		return !isUnique;
	}


//	public List<Question> getCloneOfQustionList(){
//		return new Cloner().deepClone(questionList);
//	}

	public List<File> getImageList(){return imageList;}
	@Override public List<String> getBWList(){return BWList;}
	
	@Override public List<Question> getQustionList(){return questionList;}
	@Override public ExamConfig getExamConfig(){return examConfig;}
	@Override public ObservableList<Student> getStudentList() {return students;}
	
	@Override public List<String> getErrReport(){return ErrReport;}
	@Override public List<Log> getLog(){return logList;}
	@Override public List<File> getImageFiles() {return imageList;}

}//ExamLoader
