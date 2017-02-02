package frawla.equiz.util.exam;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.rits.cloning.Cloner;

import frawla.equiz.util.Util;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Duration;

public class ExamLoader
{

	private static ExamLoader instance;
	private File sourceFile;

	private List<Question> qustionList = new ArrayList<>();
	private List<String> BWList = new ArrayList<>();
	private List<String> ErrReport = new ArrayList<>();
	private ExamConfig examConfig = new ExamConfig();
	private List<File> imageList;
	private Workbook wrkBook;
	private static ObservableList<Student> students;

	private ExamLoader(){ }

	public static ExamLoader getInstance()
	{
		if (instance == null)
			instance = new ExamLoader();

		return instance;
	}


	public void load(File srcFile) throws EncryptedDocumentException, InvalidFormatException, IOException
	{

		FileInputStream fin = new FileInputStream(srcFile);
		wrkBook = WorkbookFactory.create( fin );

		sourceFile = srcFile;
		examConfig.SourceFile =srcFile; 
		LoadFileConfig();
		loadQuestionList();
		loadImages();
		loadStdBWlist();
		loadStudentList();

		wrkBook.close();
		fin.close();

	}

	private void LoadFileConfig() throws IOException
	{
		Sheet mySheet = wrkBook.getSheet( "Config" );

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
		examConfig.courseSection = String.format("%.0f",mySheet.getRow( 8 ).getCell( 1 ).getNumericCellValue() );
		examConfig.courseYear = String.format("%.0f",mySheet.getRow( 9 ).getCell( 1 ).getNumericCellValue() );
		examConfig.courseSemester = mySheet.getRow( 10 ).getCell( 1 ).getStringCellValue().toString();

	}
	
	private void loadQuestionList() 
	{
		String data = "";
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

			if(examConfig.timingType == TimingType.QUESTION_LEVEL){
				double min = shtQuestions.getRow( i ).getCell( 11 ).getNumericCellValue(); 
				q.setTime( Duration.minutes(min) );
			}
			getQustionList().add(q);

		}
		//getQustionList().forEach( qu -> System.out.println(qu) );
	}
	

	public void loadStdBWlist()throws FileNotFoundException, IOException, EncryptedDocumentException, InvalidFormatException
	{
		Sheet BWSheet = wrkBook.getSheet("BlackWhite List");
		for (int i=1; i < BWSheet.getPhysicalNumberOfRows(); i++)
		{
			String s = BWSheet.getRow(i).getCell(0).toString();
			BWList.add(s);
		}
	}

	public void loadImages()throws FileNotFoundException, IOException, EncryptedDocumentException, InvalidFormatException
	{
		imageList = new ArrayList<>();
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

	}
	

	public void loadStudentList() throws EncryptedDocumentException, InvalidFormatException, IOException
	{
		students = FXCollections.observableArrayList();
		FileInputStream fin = new FileInputStream(sourceFile);
		Workbook wrkBook = WorkbookFactory.create( fin );

		Sheet shtAnswer = wrkBook.getSheet("Answers");
		if(shtAnswer ==null)
			return ;
		
		for (int i=1; i < shtAnswer.getPhysicalNumberOfRows(); i++)
		{
			String sid = shtAnswer.getRow(i).getCell(0).toString();
			Student std = new Student(sid);			
			std.setName(shtAnswer.getRow(i).getCell(1).toString());

			ExamSheet examSheet = new ExamSheet();
			examSheet.setExamConfig(ExamLoader.getInstance().getExamConfig());
			
			
			int[] QIDs = Arrays.asList(shtAnswer.getRow(i).getCell(2).toString().split(","))
								.stream()
								.map(String::trim)
								.mapToInt(Integer::parseInt)
								.toArray();
			int base = 3;
			List<Question> Qlst = new ArrayList<>();
			
			
			for (int j = 0; j < QIDs.length; j++)
			{
				int qid = QIDs[j];
				Question q = ExamLoader.getInstance()
									   .getQustionList()
									   .stream()
									   .filter(q2 -> q2.getId() == qid)
									   .findFirst()
									   .get();
				
				
				String c = shtAnswer.getRow(i).getCell(base + (qid-1)*2).toString() ;
				
				String[] Answer = c.split("/");
				
				if( q instanceof MultipleChoice )
				{
					MultipleChoice qmc = (MultipleChoice) q;
					q.setStudentAnswer( qmc.getChoices().get(Answer[ 3])  );
					
					qmc.getOrderList().clear();
					for (int k = 0; k < Answer[2].length(); k++)
					{
						String letter = Answer[2].substring(k,k+1);
						qmc.getOrderList().add(letter);
					}
					
				}
				else if(q instanceof BlankField)
				{
					q.setStudentAnswer(Answer[2]);
				}
				q.setStudentMark(  Double.parseDouble(shtAnswer.getRow(i).getCell(base + (qid-1)*2+1).toString() )  );
				Qlst.add( q );
				
			}

			examSheet.setQustionList(Qlst);
			std.setExamSheet(examSheet);
			students.add(std);
		}
	}
	
	public List<File> getImageFiles() 
	{
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
			File f2 = new File(sourceFile.getParent(),  c.toString());
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
				.filter(c -> ! isBlankCell( c ) )
				.map( ExamLoader::cellToLower )
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

	public boolean isValidStudent(String id)
	{
		boolean allow = false;
		switch(examConfig.studentListType){
			case ALL_STUDENTS:
				allow = true;
				break;
			case WHITE_LIST:
				allow = BWList.stream()
				.filter(s -> s.equals(id))
				.findFirst()
				.isPresent();
				break;
			case BLACK_LIST:
				allow = ! BWList.stream()
				.filter(s -> s.equals(id))
				.findFirst()
				.isPresent();
				break;
		}
		return allow;
	}


	public ExamConfig getExamConfig(){
		return examConfig;
	}

	public List<Question> getQustionList(){
		return    qustionList;
	}
	public void setQustionList(List<Question> qustionList){
		this.qustionList = qustionList;
	}
	public List<Question> getCloneOfQustionList()
	{
		return new Cloner().deepClone(qustionList);
	}

	public String getQuestionStatistics()
	{
		String[] l = {"No. of Questions", "", "", "No. of Images", "No. of Recorded Sheets"};

		String[] d = {
				qustionList.size()+"", 
				qustionList.stream().filter(q-> q instanceof MultipleChoice).count()+" "+ Question.MULTIPLE_CHOICE,
				qustionList.stream().filter(q-> q instanceof BlankField).count() +" " + Question.BLANK_FIELD,
				qustionList.stream().filter(q-> !q.getImgFileName().equals("") ).count() +"" ,
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

	public int getQuestionCount()
	{
		return qustionList.size();
	}



	public ObservableList<Student> getStudentList() {
		return students;
	}
	
}//ExamLoader
