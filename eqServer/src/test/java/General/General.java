package General;



import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.Before;
import org.junit.Test;

import frawla.equiz.server.ExamLoader;
import frawla.equiz.util.Message;
import frawla.equiz.util.Util;
import frawla.equiz.util.exam.ExamConfig;
import frawla.equiz.util.exam.QuesinoOrderType;
import frawla.equiz.util.exam.TimingType;
import javafx.embed.swing.JFXPanel;

public class General
{
	@Before public void before(){
		new JFXPanel();
	}


	@Test
	public void messageTest() throws EncryptedDocumentException, InvalidFormatException, IOException 
	{
		Message<String> m1 = new Message<>(Message.PLAIN_TEXT_FROM_CLIENT);
		m1.setData("this name String");
		assertEquals("49 - this name String", m1.toString());

		Message<ExamConfig> m2 = new Message<>(Message.EXAM_OBJECT);
		File f = new File (Util.getResource("example.xlsx"));
		ExamLoader.getInstance().load(f); //new File("example.xlsx")
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
				"No. of Questions       | 7" + "\n" +
				"                       | 5 Multiple Choice" + "\n" +
				"                       | 2 Blank Field" + "\n" +
				"No. of Images          | 2" + "\n" +
				"No. of Recorded Sheets | 0" + "\n";

		assertEquals(Stat, ExamLoader.getInstance().getQuestionStatistics());
	}


}//end class
