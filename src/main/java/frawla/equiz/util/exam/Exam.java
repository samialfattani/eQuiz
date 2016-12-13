package frawla.equiz.util.exam;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import frawla.equiz.util.Util;
import javafx.util.Duration;

public class Exam implements Serializable
{

	private static final long serialVersionUID = 1L;
	
	public File SourceFile;
	public String sharingFolder;
	public QuesinoOrderType questionOrderType;
	public StudentListType studentListType;
	public TimingType timingType;
	public Duration examTime;
	public int currentQuesIdx = 0;
	private List<Question> qustionList = new ArrayList<>();
	private List<String> stdIDList = new ArrayList<>();
	private List<String> ErrReport = new ArrayList<>();

//	private ObservableList<Student> Students;
	

	public Exam(File srcFile) throws IOException, EncryptedDocumentException, InvalidFormatException
	{

		FileInputStream fin = new FileInputStream(srcFile);
		Workbook wrkBook = WorkbookFactory.create( fin );
		
		SourceFile = srcFile;
		LoadFileConfig(wrkBook);
		
		//TODO: Later Very important
		//LoadStudentList(wrkBook);
		
		wrkBook.close();
		fin.close();
	}
	



	private void LoadFileConfig(Workbook wrkBook) throws IOException
	{
		Sheet mySheet = wrkBook.getSheet( "Config" );
		
		String data = "";
		
		
		sharingFolder = mySheet.getRow( 0 ).getCell( 1 ).getStringCellValue();
		data = mySheet.getRow( 1 ).getCell( 1 ).getStringCellValue();
		questionOrderType = (data.equals("Sequence")? QuesinoOrderType.SEQUENCE: QuesinoOrderType.RANDOM);
		data = mySheet.getRow( 2 ).getCell( 1 ).getStringCellValue();
		studentListType = (
				data.equals("All Students")?
						StudentListType.ALL_STUDENTS: 
							(data.equals("White List")?
									StudentListType.WHITE_LIST:
										StudentListType.BLACK_LIST));

		data = mySheet.getRow( 3 ).getCell( 1 ).getStringCellValue();
		timingType = (data.equals("Exam Level")?TimingType.EXAM_LEVEL:TimingType.QUESTION_LEVEL);
		
		double d = mySheet.getRow( 4 ).getCell( 1 ).getNumericCellValue();
		examTime = new Duration(d * 60 * 1000 );
		
		Sheet shtQuestions = wrkBook.getSheet("Questions");
		
		if( !isValidQuestionSheet(shtQuestions)){
			StringBuilder s = new StringBuilder("INVALID QUESTIN SHEET\n");
			ErrReport.forEach( e -> {
				s.append( ErrReport.toString() + "\n" );	
			});
			
			Util.showError( s.toString() );
			ErrReport.clear();
			return;
		}
			
		
		for (int i=2; i < shtQuestions.getPhysicalNumberOfRows(); i++)
		{			
			shtQuestions.getRow( i ).getCell( 0 ).setCellValue(i-1); //put question number.
			
			Question q =null;
			String qtype = shtQuestions.getRow( i ).getCell( 2 ).getStringCellValue(); //question Type
			if(qtype.equals(Question.MULTIPLE_CHOICE))
				q = new MultipleChoice();
			else if(qtype.equals(Question.BLANK_FIELD))
				q = new BlankField();
			
			q.setType(qtype);
			q.setId(i-1); //1 ,2, 3, 4...etc
			q.setText(shtQuestions.getRow( i ).getCell( 1 ).getStringCellValue()); //Question Text
			Cell c ;
			for(int j=0; j<6; j++)
			{
				c = shtQuestions.getRow( i ).getCell( j+3 );
				if(!isBlankCell(c)) 
				{
					data = shtQuestions.getRow( i ).getCell( j+3 ).toString();
					addChoice(q, data, j);					
				}
			}

			c = shtQuestions.getRow( i ).getCell( 9 );
			if(!isBlankCell(c)){
				File f = new File(c.toString());
				q.setImgFileName(f.getName());
			}
			
			q.setMark(shtQuestions.getRow( i ).getCell( 10 ).getNumericCellValue());
			
			if(timingType == TimingType.QUESTION_LEVEL){
				double min = shtQuestions.getRow( i ).getCell( 11 ).getNumericCellValue(); 
				q.setTime( Duration.minutes(min) );
			}
			
			
			getQustionList().add(q);
			
		}

		//------------------------------------------------------
		Sheet stdListSheet = wrkBook.getSheet("Student List");
		for (int i=1; i < stdListSheet.getPhysicalNumberOfRows(); i++)
		{
			String s = stdListSheet.getRow(i).getCell(0).toString();
			stdIDList.add(s);
		}
		
		//examConfig.getQustionList().forEach( qu -> System.out.println(qu) );
	}
	
	public List<File> getImageFiles() throws FileNotFoundException, IOException, EncryptedDocumentException, InvalidFormatException
	{
		FileInputStream fin = new FileInputStream(SourceFile);
		Workbook wrkBook = WorkbookFactory.create( fin );
		
		List<File> imageList = new ArrayList<>();
		Sheet shtQuestions = wrkBook.getSheet("Questions");
		for (int i=2; i < shtQuestions.getPhysicalNumberOfRows(); i++)
		{			
			Cell c = shtQuestions.getRow( i ).getCell( 9 );
			if(!isBlankCell(c)){
				File f = new File(c.toString());

				if(!f.exists())
					f = new File(SourceFile.getParent(), c.toString());

				imageList.add( f );
			}
		}
		
		fin.close();
		wrkBook.close();
		return imageList;
	}

	private boolean isValidQuestionSheet(Sheet shtQuestions)
	{
		// TODO: Qestion Sheet validation.
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
			ErrReport.add("Some required cells found blank. Question No., Text, Type, and Option(A) can never be blank.");
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
		
		
		//2. Make sure no duplicate answers.
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
		
		//3. Make sure no blank answer in middle
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
		
		//4. Make sure all images are exists.
		for (int i=2; i < QuesCount; i++)
		{
			Cell c = shtQuestions.getRow(i).getCell(9);
			if( isBlankCell( c ) )
				continue;
			
			File f1 = new File(c.toString()) ;
			File f2 = new File(SourceFile.getParent(),  c.toString());
			if( !f1.exists()  && !f2.exists())
			{
				ErrReport.add("The File '" + f1.getName() + "' dose not exists.");
				validSheet = false;
			}
		}
		
		
		//5. Make sure no dupblicate images - NO NEED sometimes, same image is used for many questions.
//		List<File> l = new ArrayList<>();
//		for (int i=2; i < QuesCount; i++)
//		{
//			Cell c = shtQuestions.getRow(i).getCell(9);
//			if( isBlankCell( c ) )
//				continue;
//			
//			File f = new File(shtQuestions.getRow(i).getCell(9).toString()) ;
//			l.add(  new File( f.getName() ));
//		}
//		if(l.stream()
//			.filter(f -> Collections.frequency(l, f) >1)
//			.collect(Collectors.toSet())
//			.stream()
//			.count() > 0);
//		{
//			ErrReport.add("There Are duplicate File names.");
//			validSheet = false;
//		}

		
		//6. Make sure Every qustion has mark
		cells.clear();		
		for (int i=2; i < QuesCount; i++){
			cells.add(shtQuestions.getRow(i).getCell(10));
		}
		
		if( cells.stream().anyMatch( c -> !Util.isNumeric(c.toString())  )){
			ErrReport.add("Some Questions dose not have marks or they have invalid marks.");
			validSheet = false;
		}

		//7. Make sure Every qustion has Time
		if(timingType == TimingType.QUESTION_LEVEL)
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

	public static String cellToLower(Cell c){
		return c.toString().toLowerCase();
		
	}
	
	public static boolean isBlankCell(Cell c)
	{
		if(c == null )
			return true;
		
		if(c.toString().trim().replaceAll("\\s+", "").equals(""))
			return true;

		return c.getCellType() == Cell.CELL_TYPE_BLANK;
	}

	public static boolean isThereAnyDuplicate(List<Cell> cells)
	{
		boolean isUnique; 
		isUnique = cells.stream()
						.filter(c -> ! Exam.isBlankCell( c ) )
						.map( Exam::cellToLower )
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
			((BlankField)q).getOptions().add(data);
		}
		
	}
//---------------------------------------------------------
	
	public static void shuffleExam(Exam exam)
	{
		//shuffle Options in each Question
		exam.getQustionList()
			.stream()
			.filter(qu -> qu instanceof Randomizable)
			.forEach( q ->  ((Randomizable)q).shuffle()   
		);
		
		//shuffle All questions
		if(exam.questionOrderType == QuesinoOrderType.RANDOM)
			Collections.shuffle(exam.getQustionList());
		
	}
	
	public List<Question> getQustionList(){
		return qustionList;
	}
	public void setQustionList(List<Question> qustionList){
		this.qustionList = qustionList;
	}

	@Override
	public String toString()	
	{	
		String[] l = {"Sharing Folder", "Question Order Type", "Student List Type",
					  "Timing Type", "Exam Time", 
					  "No. of Questions", "", "", "No. of Images"};

		String[] d = {sharingFolder, questionOrderType+"", studentListType+"", 
					  timingType+"", examTime.toMinutes()+" Minutes.", 
					  qustionList.size()+"", 
					  qustionList.stream().filter(q-> q instanceof MultipleChoice).count()+" "+ Question.MULTIPLE_CHOICE,
					  qustionList.stream().filter(q-> q instanceof BlankField).count() +" " + Question.BLANK_FIELD,
					  qustionList.stream().filter(q-> !q.getImgFileName().equals("") ).count() +"" 
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


	public boolean isValidStudent(String id)
	{
		boolean allow = false;
		switch(studentListType){
			case ALL_STUDENTS:
				allow = true;
			break;
			case WHITE_LIST:
				allow = stdIDList.stream()
							   .filter(s -> s.equals(id))
							   .findFirst()
							   .isPresent();
			break;
			case BLACK_LIST:
				allow = ! stdIDList.stream()
								 .filter(s -> s.equals(id))
								 .findFirst()
								 .isPresent();
			break;
		}
		return allow;
	}


	public Question getCurrentQuestion()
	{
		return getQustionList().get(currentQuesIdx);
	}


//	private void LoadStudentList(Workbook wrkBook)
//	{
//		//Students = FXCollections.observableArrayList();
//
//		Sheet shtAnswer = wrkBook.getSheet("Answers");
//		if(shtAnswer ==null)
//			return;
//		
//		for (int i=1; i < shtAnswer.getPhysicalNumberOfRows(); i++)
//		{
//			String sid = shtAnswer.getRow(i).getCell(0).toString();
//			Student s = new Student(sid);			
//			s.setName(shtAnswer.getRow(i).getCell(1).toString());
//
//			int[] Qseq = Arrays.asList(shtAnswer.getRow(i).getCell(2).toString().split(", "))
//								.stream()
//								.mapToInt(Integer::parseInt)
//								.toArray();
//			int base = 2;
//			List<Question> Qlst = new ArrayList<>();
//			
//			
//			for (int j = 0; j < Qseq.length; j++)
//			{
//				Qlst.add(e)
//				int qid = Integer.parseInt(shtAnswer.getRow(i).getCell(1).toString());
//				this.getQustionList()
//					.stream()
//					.filter( st -> st.getId() == Qseq[j])
//					.findFirst()
//					.get().set
//			}
//			Students.add()
//		}
//		
//		
//		
//	}

//	public ObservableList<Student> getStudentList()
//	{
//		return Students;
//	}


}
