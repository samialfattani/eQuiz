package frawla.equiz.server;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.odftoolkit.simple.SpreadsheetDocument;

import com.rits.cloning.Cloner;

import frawla.equiz.util.exam.BlankField;
import frawla.equiz.util.exam.ExamConfig;
import frawla.equiz.util.exam.MultipleChoice;
import frawla.equiz.util.exam.Question;
import frawla.equiz.util.exam.Student;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ExamLoader
{

	private static ExamLoader instance;
	//private File sourceFile;

	private List<Question> questionList = new ArrayList<>();
	private List<String> BWList = new ArrayList<>();
	private List<String> ErrReport = new ArrayList<>();
	private ExamConfig examConfig = new ExamConfig();
	private List<File> imageList = new ArrayList<>();
	//private Workbook wrkBook;
	private ObservableList<Student> students;

	private ExamLoader(){ }

	public static ExamLoader getInstance()
	{
		if (instance == null)
			instance = new ExamLoader();

		return instance;
	}


	public void load(File srcFile) throws Exception 
	{
		String ext = FilenameUtils.getExtension(srcFile.getAbsolutePath()); 
		if( ext.trim().toLowerCase().equals("xlsx") )
		{
			FileInputStream fin = new FileInputStream(srcFile);
			Workbook wrkBook = WorkbookFactory.create( fin );
			students = FXCollections.observableArrayList();
	
			//sourceFile = srcFile;
			 
			ExamLoaderXLSX examLoader = new ExamLoaderXLSX(wrkBook, srcFile);
			
			examConfig = examLoader.getExamConfig();
			questionList = examLoader.getQustionList();
			imageList = examLoader.getImageList();
			BWList = examLoader.getBWList();
			students = examLoader.getStudentList();
			ErrReport = examLoader.getErrReport();
			
			examConfig.SourceFile =srcFile;
			wrkBook.close();
			fin.close();
		}else if( ext.trim().toLowerCase().equals("ods") ){
			
			SpreadsheetDocument wrkBook = SpreadsheetDocument.loadDocument(srcFile);
			ExamLoaderODS examLoader = new ExamLoaderODS(wrkBook, srcFile);
			examConfig = examLoader.getExamConfig();
			questionList = examLoader.getQustionList();
			imageList = examLoader.getImageList();
			BWList = examLoader.getBWList();
			students = examLoader.getStudentList();
			ErrReport = examLoader.getErrReport();
			
			examConfig.SourceFile =srcFile;
			wrkBook.close();
			
		}

	}



	public List<File> getImageFiles() 
	{
		return imageList;
	}

	public  boolean isValidStudent(String id)
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

	public List<Question> getQustionList(){return questionList;}
	public void setQustionList(List<Question> lst){this.questionList = lst;}

	public List<Question> getCloneOfQustionList(){
		return new Cloner().deepClone(questionList);
	}

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

	public int getQuestionCount()
	{
		return questionList.size();
	}

	public ObservableList<Student> getStudentList() {
		return students;
	}

	public List<String> getErrReport()
	{
		return ErrReport;
	}

}//ExamLoader
