package frawla.equiz.server;

import java.io.File;
import java.text.ParseException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.List;

import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Row;
import org.odftoolkit.simple.table.Table;

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
/**
 * this calss is exactly same as ExamLoader class. instead of reading 
 * MS-Excel (*.xlsx) files, this class is reading Open-Office (*.ods) files.
 * 
 * @author samipc
 *
 */
public class ExamLoaderODS extends ExamLoader
{

	private File sourceFile;

	private List<Question> questionList = new ArrayList<>();
	private List<String> BWList = new ArrayList<>();
	private List<String> ErrReport = new ArrayList<>();
	private ExamConfig examConfig = new ExamConfig();
	private List<File> imageList = new ArrayList<>();
	private SpreadsheetDocument wrkBook;

	private List<Log> logList;
	private static ObservableList<Student> students;

	public ExamLoaderODS(File srcFile)
	{
		try {
			wrkBook = SpreadsheetDocument.loadDocument(srcFile);
		
			examConfig.SourceFile =srcFile;
			
			students = FXCollections.observableArrayList();
			
			sourceFile = srcFile;
			
			loadFileConfig();
			loadQuestionList();
			loadImages();
			loadStdBWlist();
			loadStudentList();
			loadLog();
			wrkBook.close();
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void loadFileConfig() 
	{
		Table mySheet = wrkBook.getTableByName("Config" );

		String data = "";

		examConfig.sharingFolder = mySheet.getRowByIndex( 0 ).getCellByIndex( 1 ).getDisplayText();
		data = mySheet.getRowByIndex( 1 ).getCellByIndex( 1 ).getDisplayText();
		examConfig.questionOrderType = (data.equals("Sequence")? QuesinoOrderType.SEQUENCE: QuesinoOrderType.RANDOM);
		data = mySheet.getRowByIndex( 2 ).getCellByIndex( 1 ).getDisplayText();
		examConfig.studentListType = (
				data.equals("All Students")?
						StudentListType.ALL_STUDENTS: 
							(data.equals("White List")?
									StudentListType.WHITE_LIST:
										StudentListType.BLACK_LIST));

		data = mySheet.getRowByIndex( 3 ).getCellByIndex( 1 ).getDisplayText();
		examConfig.timingType = (data.equals("Exam Level")?TimingType.EXAM_LEVEL:TimingType.QUESTION_LEVEL);

		double d = mySheet.getRowByIndex( 4 ).getCellByIndex( 1 ).getDoubleValue();
		examConfig.examTime = new Duration(d * 60 * 1000 );


		examConfig.courseID = mySheet.getRowByIndex( 6 ).getCellByIndex( 1 ).getDisplayText();
		examConfig.courseName = mySheet.getRowByIndex( 7 ).getCellByIndex( 1 ).getDisplayText();
		examConfig.courseSection = String.format("%.0f", Double.parseDouble(mySheet.getRowByIndex( 8 ).getCellByIndex( 1 ).getDisplayText()) );
		examConfig.courseYear = String.format("%.0f", Double.parseDouble(mySheet.getRowByIndex( 9 ).getCellByIndex( 1 ).getDisplayText() ) );
		examConfig.courseSemester = mySheet.getRowByIndex( 10 ).getCellByIndex( 1 ).getDisplayText();
		examConfig.courseTitle = mySheet.getRowByIndex( 11 ).getCellByIndex( 1 ).getDisplayText();

	}

	private void loadQuestionList() 
	{
		String data = "";
		Table shtQuestions = wrkBook.getTableByName("Questions");
		getQustionList().clear();

		if( !isValidQuestionSheet(shtQuestions)){
			StringBuilder s = new StringBuilder("INVALID QUESTION SHEET\n");
			ErrReport.forEach( e -> {
				s.append( ErrReport.toString() + "\n" );	
			});

			Util.showError( s.toString() );
			ErrReport.clear();
			return;
		}


		for (int i=2; i < shtQuestions.getRowCount(); i++)
		{			
			shtQuestions.getRowByIndex( i ).getCellByIndex( 0 ).setDoubleValue(i-1.0);

			Question q =null;
			//question Type
			String qtype = shtQuestions.getRowByIndex( i ).getCellByIndex( 2 ).getDisplayText(); 
			if(qtype.equals(Question.MULTIPLE_CHOICE))
				q = new MultipleChoice();
			else if(qtype.equals(Question.BLANK_FIELD))
				q = new BlankField();

			q.setType(qtype);
			q.setId(i-1); //1 ,2, 3, 4...etc
			//Question Text
			q.setText(shtQuestions.getRowByIndex( i ).getCellByIndex( 1 ).getDisplayText()); 
			Cell c ;
			for(int j=0; j<6; j++)
			{
				c = shtQuestions.getRowByIndex( i ).getCellByIndex( j+3 );
				if(!isBlankCell(c)) 
				{
					data = shtQuestions.getRowByIndex( i ).getCellByIndex( j+3 ).getDisplayText();
					addChoice(q, data, j);					
				}
			}

			if( q instanceof MultipleChoice)
				((MultipleChoice) q).resetOrderList();

			c = shtQuestions.getRowByIndex( i ).getCellByIndex( 9 );
			if(!isBlankCell(c)){
				File f = new File(c.getStringValue());
				q.setImgFileName(f.getName());
			}

			q.setMark(shtQuestions.getRowByIndex( i ).getCellByIndex( 10 ).getDoubleValue());

			if(examConfig.timingType == TimingType.QUESTION_LEVEL){
				double min = shtQuestions.getRowByIndex( i ).getCellByIndex( 11 ).getDoubleValue(); 
				q.setTime( Duration.minutes(min) );
			}
			getQustionList().add(q);

		}
		//getQustionList().forEach( qu -> System.out.println(qu) );
	}

	private void loadStdBWlist()
	{
		BWList.clear();
		Table BWSheet = wrkBook.getTableByName("BlackWhite_List");
		if( BWSheet == null)
			ErrReport.add("Sheet of 'BlckWhite_List' is not found");
			
		for (int i=1; i < BWSheet.getRowCount(); i++)
		{
			String s = BWSheet.getRowByIndex(i).getCellByIndex(0).getStringValue();
			BWList.add(s);
		}
	}

	private void loadImages()
	{
		imageList.clear();
		Table shtQuestions = wrkBook.getTableByName("Questions");
		for (int i=2; i < shtQuestions.getRowCount(); i++)
		{			
			Cell c = shtQuestions.getRowByIndex( i ).getCellByIndex( 9 );
			if(!isBlankCell(c)){
				File f = new File(c.getStringValue());

				if(!f.exists())
					f = new File(sourceFile.getParent(), c.getStringValue());

				imageList.add( f );
			}
		}

	}

	private void loadStudentList() 
	{
		Table shtAnswer = wrkBook.getTableByName("Answers");
		Table shtTimer = wrkBook.getTableByName("Timer");
		if(shtAnswer ==null)
			return ;

		for (int i=1; i < shtAnswer.getRowCount(); i++)
		{
			String sid = shtAnswer.getRowByIndex(i).getCellByIndex(0).getStringValue();
			Student std = new Student(sid);			
			std.setName(shtAnswer.getRowByIndex(i).getCellByIndex(1).getStringValue());

			ExamSheet examSheet = new ExamSheet();
			examSheet.setExamConfig( getExamConfig() );

			//read all answers a
			List<Question> Qlst = extractQuestionList( shtAnswer.getRowByIndex(i), shtTimer.getRowByIndex(i) );
			examSheet.setQustionList(  Qlst );
			
			std.setExamSheet(examSheet);
			students.add(std);
		}
	}


	private List<Question> extractQuestionList(Row stRow, Row timerRow) throws InputMismatchException
	{
		
		int[] QIDs = Arrays.asList(stRow.getCellByIndex(2).getStringValue().split(","))
				.stream()
				.map(String::trim)
				.mapToInt(Integer::parseInt)
				.toArray();
		List<Question> Qlst = new ArrayList<>();

		for (int j = 0; j < QIDs.length; j++)
		{
			int base = 3;
			int qid = QIDs[j];
			Question qj = questionList.stream()
							 .filter(q -> q.getId() == qid)
							 .findFirst()
							 .get();

			String timeCellContent = timerRow.getCellByIndex( 2 + (qid-1) ).getStringValue();
			String cellContent = stRow.getCellByIndex(base + (qid-1)*2).getStringValue() ;

			if( qj instanceof MultipleChoice )
			{
				MultipleChoice mcCopy = new MultipleChoice(cellContent);
				mcCopy.setText(qj.getText());
				mcCopy.copyChoices(qj);
				mcCopy.setStudentMark(  stRow.getCellByIndex(base + (qid-1)*2+1).getDoubleValue()   );
				mcCopy.setConsumedTime( toDuration(timeCellContent)  );
				Qlst.add( mcCopy );
			}
			else if(qj instanceof BlankField)
			{
				BlankField qbCopy = new BlankField(cellContent);
				qbCopy.setText(qj.getText());
				qbCopy.copyOptions(qj);
				qbCopy.setStudentMark(  stRow.getCellByIndex(base + (qid-1)*2+1).getDoubleValue() );
				qbCopy.setConsumedTime( toDuration(timeCellContent)  );
				Qlst.add( qbCopy );
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

	private List<Log> loadLog() throws ParseException
	{
		logList = new ArrayList<>();
		
		Table shtLog = wrkBook.getTableByName("Log");
		if(shtLog == null)
			return null;

		for (int i=1; i < shtLog.getRowCount(); i++)
		{
			String time = shtLog.getRowByIndex(i).getCellByIndex(0).getStringValue();
			String text = shtLog.getRowByIndex(i).getCellByIndex(1).getStringValue();
			
			Log log = new Log( new EQDate(Util.MY_DATE_FORMAT, time) , text);
			logList.add( log );
		}
		return logList;
	}

	//--------------------------------------------------
	public List<File> getImageList(){return imageList;}

	private boolean isValidQuestionSheet(Table shtQuestions)
	{
		boolean validSheet = true;

		int QuesCount = shtQuestions.getRowCount();
		//1. Make sure all question text, type, and First Option are not blank
		List<Cell> cells = new ArrayList<>();		
		for (int i=2; i < QuesCount; i++)
		{	
			cells.add(shtQuestions.getRowByIndex(i).getCellByIndex(1));
			cells.add(shtQuestions.getRowByIndex(i).getCellByIndex(2));
			cells.add(shtQuestions.getRowByIndex(i).getCellByIndex(3));
		}

		if( cells.stream().anyMatch( c -> isBlankCell(c) )){
			ErrReport.add("Some required cells found blank. Question No., Text, Type, and Option(A) can never be blank.");
			validSheet = false;
		}

		//2. Make sure question ID is in order (1,2,3,...)
		double id =1;
		for (int i=2; i < QuesCount; i++)
		{	
			double cellID = shtQuestions.getRowByIndex(i).getCellByIndex(0).getDoubleValue();

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
				cells.add(shtQuestions.getRowByIndex(i).getCellByIndex(j));

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
				cells.add(shtQuestions.getRowByIndex(i).getCellByIndex(j));

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
			Cell c = shtQuestions.getRowByIndex(i).getCellByIndex(9);
			if( isBlankCell( c ) )
				continue;

			File f1 = new File(c.getDisplayText()) ;
			File f2 = new File(sourceFile.getParent(),  c.getDisplayText());
			if( !f1.exists()  && !f2.exists())
			{
				ErrReport.add("The File '" + f1.getName() + "' dose not exists.");
				validSheet = false;
			}
		}


		//7. Make sure Every qustion has mark
		cells.clear();		
		for (int i=2; i < QuesCount; i++){
			cells.add(shtQuestions.getRowByIndex(i).getCellByIndex(10));
		}

		if( cells.stream().anyMatch( c -> !Util.isNumeric(c.getDisplayText())  )){
			ErrReport.add("Some Questions dose not have marks or they have invalid marks.");
			validSheet = false;
		}

		//6. Make sure Every qustion has Time
		if(examConfig.timingType == TimingType.QUESTION_LEVEL)
		{
			cells.clear();
			for (int i=2; i < QuesCount; i++){
				cells.add(shtQuestions.getRowByIndex(i).getCellByIndex(11));
			}
			if( cells.stream().anyMatch( c -> !Util.isNumeric(c.getDisplayText())  )){
				ErrReport.add("Some Questions dose not have time. if Timing mode is set to 'Question_Level' then each question must has it's own time.");
				validSheet = false;
			}
		}

		return validSheet;

	}// end sheet Validation

	private static String cellToLower(Cell c){
		return c.getDisplayText().toLowerCase();
		
	}

	private static boolean isBlankCell(Cell c)
	{
		if(c == null )
			return true;

		if(c.getValueType() == null)
			return true;
		
		if(c.getStringValue().trim().replaceAll("\\s+", "").equals(""))
			return true;

		return false;
	}

	public static boolean isThereAnyDuplicate(List<Cell> cells)
	{
		boolean isUnique; 
		isUnique = cells.stream()
				.filter(c -> ! isBlankCell( c ) )
				.map( ExamLoaderODS::cellToLower )
				.allMatch(new HashSet<>()::add );	

		return !isUnique;
	}

	private void addChoice(Question q, String data, int j)
	{
		if(q instanceof MultipleChoice){			
			if(j ==0)
				((MultipleChoice)q).setCorrectAnswer("A");

			String[] Letter = {"A", "B", "C", "D", "E","F","G","H","I"};
			((MultipleChoice)q).getChoices().put( Letter[j] , data);
		}else if(q instanceof BlankField){
			((BlankField)q).getCorrectAnswerList().add(data);
		}

	}

	public ExamConfig getExamConfig(){
		return examConfig;
	}

	public List<Question> getQustionList(){return questionList;}
	public void setQustionList(List<Question> lst){this.questionList = lst;}

//	public List<Question> getCloneOfQustionList(){
//		return new Cloner().deepClone(questionList);
//	}

	public String getQuestionStatistics()
	{
		String[] l = {"No. of Questions", "", "", "No. of Images", "No. of Recorded Sheets"};

		String[] d = {
				questionList.size()+"", 
				questionList.stream().filter(q-> q instanceof MultipleChoice).count()+" "+ Question.MULTIPLE_CHOICE,
				questionList.stream().filter(q-> q instanceof BlankField).count() +" " + Question.BLANK_FIELD,
				questionList.stream().filter(q-> !q.getImgFileName().equals("") ).count() +"" ,
				students.size() + ""
		};

		List<String> labels = Arrays.asList(l);
		List<String> data = Arrays.asList(d);

		String s = "";
		int max = labels
				.stream()
				.mapToInt( String::length )
				.max()
				.getAsInt();

		for (int i=0; i<d.length; i++)
		{
			s += String.format("%-"+max+"s | %s\n", labels.get(i), data.get(i));			
		}
		return s;

	}


	@Override public ObservableList<Student> getStudentList() {return students;}
	@Override public List<String> getBWList(){return BWList;}
	@Override public List<String> getErrReport(){return ErrReport;}
	@Override public List<Log> getLog(){return logList;}


	@Override public List<File> getImageFiles() {return imageList;}



}//ExamLoader
