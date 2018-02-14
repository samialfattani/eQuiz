package frawla.equiz.util.exam;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javafx.util.Duration;

public class ExamConfig implements Serializable
{

	private static final long serialVersionUID = 1L;
	
	public File SourceFile;
	public String sharingFolder;
	public QuesinoOrderType questionOrderType;
	public StudentListType studentListType;
	public TimingType timingType;
	public Duration examTime;
	public String courseID;
	public String courseName;
	public String courseSection;
	public String courseYear;
	public String courseSemester;
	public String courseTitle;

	@Override
	public String toString()	
	{	
		String[] l = {"Sharing Folder", "Question Order Type", "Student List Type", "Timing Type", "Exam Time",
					  "Course", "Section", "Semester", "Title"};

		String[] d = {sharingFolder, questionOrderType+"", studentListType+"", 
					  timingType+"", examTime.toMinutes()+" Minutes.",
					  courseID +" - "+ courseName, courseSection, courseYear + "/" + courseSemester,
					  courseTitle};

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


}
