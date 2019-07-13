package frawla.equiz.server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import com.rits.cloning.Cloner;

import frawla.equiz.util.Log;
import frawla.equiz.util.exam.BlankField;
import frawla.equiz.util.exam.ExamConfig;
import frawla.equiz.util.exam.ExamSheet;
import frawla.equiz.util.exam.MultipleChoice;
import frawla.equiz.util.exam.QuesinoOrderType;
import frawla.equiz.util.exam.Question;
import frawla.equiz.util.exam.Randomizable;
import frawla.equiz.util.exam.Student;
import javafx.collections.ObservableList;

public abstract class ExamLoader
{
	private static ExamLoader instance;

	private static List<Question> questionList = new ArrayList<>();
	private static List<String> BWList = new ArrayList<>();
	private static ExamConfig examConfig = new ExamConfig();
	private static ObservableList<Student> students;

	protected ExamLoader(){ }

	public static ExamLoader getInstance(){return instance;}
	public static ExamLoader getInstance(File srcFile)
	{
		if(instance == null)
			create(srcFile);
		else if( instance.getExamConfig().SourceFile != srcFile )
			create(srcFile);
		
		return instance;
	}

	public static void create(File srcFile) 
	{
		String ext = FilenameUtils.getExtension(srcFile.getAbsolutePath());
		//TODO: guessing mime types
		//String t = getMimeType(srcFile);
		if( ext.trim().toLowerCase().equals("xlsx") )
		{
			instance = new ExamLoaderXLSX(srcFile);
		}
		else if( ext.trim().toLowerCase().equals("ods") )
		{
			instance = new ExamLoaderODS(srcFile);
		}
		students     = instance.getStudentList();
		examConfig   = instance.getExamConfig();
		BWList       = instance.getBWList();
		questionList = instance.getQustionList();
		
		students.forEach(st -> st.setStatus(Student.GRADED) );
		
	}

	//TODO: later
//	private static String getMimeType(File srcFile) throws IOException {
//		//File file = new File(srcFile);
//		InputStream is = new BufferedInputStream(new FileInputStream(srcFile));
//		String mimeType = URLConnection.guessContentTypeFromStream(is);		
//		return mimeType;
//	}

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

	public String getQuestionStatistics()
	{
		String Questions = questionList.size()+"";
		String MC = questionList.stream().filter(q-> q instanceof MultipleChoice).count() + "";
		String BF = questionList.stream().filter(q-> q instanceof BlankField).count() +"";
		String Images = questionList.stream().filter(q-> !q.getImgFileName().equals("") ).count() +"";
		String RecordedSheets = students.size() + "";

		String s = "";
		s += String.format("%2s %s\n", Questions, "Questions" );
		s += String.format("%2s %2s %s\n", " ", MC, Question.MULTIPLE_CHOICE );
		s += String.format("%2s %2s %s\n", " ", BF, Question.BLANK_FIELD );
		s += String.format("%2s %s\n", Images, "Images" );
		s += String.format("%2s %s\n", RecordedSheets, "Recorded Sheets" );
		
		return s;

	}

	public int getQuestionCount() {
		return instance.getQustionList().size();
	}
	
	public abstract List<Question> getQustionList();
	public abstract ExamConfig getExamConfig();
	public abstract List<String> getBWList();
    public abstract List<File> getImageFiles();
	public abstract List<String> getErrReport();
	
	public abstract ObservableList<Student> getStudentList();
	public abstract List<Log> getLog();

	public ExamSheet generateNewSheet() 
	{
		ExamSheet newSheet = new ExamSheet();
		newSheet.setExamConfig( new Cloner().deepClone(examConfig) );
		newSheet.setQustionList( new Cloner().deepClone(questionList) );

		if(examConfig.questionOrderType == QuesinoOrderType.RANDOM)
			newSheet.shuffle();

		//shuffle Options in each Question
		newSheet.getQuestionList()
			.stream()
			.filter(qu -> qu instanceof Randomizable)
			.forEach( q ->  ((Randomizable)q).shuffle() );
		
		return newSheet;
	}

}//ExamLoader
