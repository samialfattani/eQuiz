package ggeneral;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import frawla.equiz.server.ExamLoader;
import frawla.equiz.util.Message;
import frawla.equiz.util.Util;
import frawla.equiz.util.exam.ExamConfig;
import frawla.equiz.util.exam.QuesinoOrderType;
import frawla.equiz.util.exam.TimingType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.util.Duration;

public class General
{
	@BeforeAll 
	public static void before(){
		new JFXPanel();
	}


	@Test
	public void messageTest() throws Exception 
	{
		Message<String> m1 = new Message<>(Message.PLAIN_TEXT_FROM_CLIENT);
		m1.setData("this name String");
		assertEquals("49 - this name String", m1.toString());

		Message<ExamConfig> m2 = new Message<>(Message.EXAM_OBJECT);
		File f = new File (Util.getResourceAsURI("example.xlsx"));
		ExamLoader.getInstance(f); //new File("example.xlsx")
		ExamConfig e = ExamLoader.getInstance().getExamConfig();
		e.questionOrderType = QuesinoOrderType.RANDOM;
		e.timingType = TimingType.EXAM_LEVEL;		
		m2.setData(e);
		
		String m2Str = 
		"55 - Sharing Folder      | \\\\localhost\\resources" + "\n" +
		"Question Order Type | RANDOM" + "\n" +
		"Student List Type   | ALL_STUDENTS" + "\n" +
		"Timing Type         | EXAM_LEVEL" + "\n" +
		"Exam Time           | 2.0 Minutes." + "\n" +
		"Course              | IT100 - Computer Skills" + "\n" +
		"Section             | 1" + "\n" +
		"Semester            | 2016/Fall" + "\n"+ 
		"Title               | 2nd MidTerm"+ "\n";
		
		assertEquals(m2Str, m2.toString() );
		
		String Stat = 
				" 7 Questions" + "\n" +
				"    5 Multiple Choice"  + "\n" +
				"    2 Blank Field"  + "\n" +
				" 2 Images"  + "\n" +
				" 0 Recorded Sheets\n" ;

		assertEquals(Stat, ExamLoader.getInstance().getQuestionStatistics());
	}


	@Test
	public void LocalTimeTest()
	{
		LocalTime lt = LocalTime.parse ( "01:01:00" ); //3660
		Duration d;
		double ms = (lt.getHour()*3600 + lt.getMinute()*60 + lt.getSecond())*1000;
		d = new Duration(ms);
		
		assertEquals(d.toSeconds(), 3660 , 0.0);

		lt = LocalTime.parse ( "01:30:00", DateTimeFormatter.ofPattern("HH:mm:ss")   ); //366
		ms = (lt.getHour()*3600 + lt.getMinute()*60 + lt.getSecond())*1000;
		d = new Duration(ms);
		
		assertEquals(d.toSeconds(), 3600+30*60 , 0.0);
		
	}
	
	
	@Test
	public void ObservableListTest() 
	{
		ObservableList<StringBuilder> lst1 = FXCollections.observableArrayList();
		lst1.add( new StringBuilder("ahmed") );
		lst1.add( new StringBuilder("ali") );
		lst1.add( new StringBuilder("sami"));
		
		ObservableList<StringBuilder> lst2 = lst1.stream()
										 .filter(s -> s.toString().startsWith("a"))
										 .collect(Collectors.toCollection(FXCollections::observableArrayList));
		
		lst2.forEach(s -> s.append("2"));
		
		assertEquals("[ahmed2, ali2]", lst2.toString());
		assertEquals("[ahmed2, ali2, sami]", lst1.toString());
	}
	
	
}//end class
