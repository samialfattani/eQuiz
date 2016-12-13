package General;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.Before;
import org.junit.Test;

import frawla.equiz.util.exam.Exam;
import frawla.equiz.util.exam.QuesinoOrderType;
import frawla.equiz.util.exam.StudentListType;
import frawla.equiz.util.exam.TimingType;
import javafx.embed.swing.JFXPanel;
import javafx.util.Duration;

public class FullCycleTest
{
	Exam exConfig;
	
	@Before
	public void before() throws EncryptedDocumentException, InvalidFormatException, IOException
	{
		new JFXPanel();
		exConfig = new Exam(new File("data/IT100-1.xlsx"));
	}
	
	@Test
	public void LoadExamFileTest() 
	{
		assertEquals(QuesinoOrderType.RANDOM , exConfig.questionOrderType);
		assertEquals(StudentListType.ALL_STUDENTS, exConfig.studentListType);
		assertEquals(TimingType.EXAM_LEVEL, exConfig.timingType);
		assertEquals(new Duration( 45*60*1000 ), exConfig.examTime);
	}
	

}
