package General;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.Before;
import org.junit.Test;

import frawla.equiz.util.Util;
import frawla.equiz.util.exam.ExamConfig;
import frawla.equiz.util.exam.ExamLoader;
import frawla.equiz.util.exam.ExamSheet;
import frawla.equiz.util.exam.Student;
import javafx.embed.swing.JFXPanel;

public class JAXBTest
{
	ExamConfig exConfig;
	ExamSheet examSheet = new ExamSheet();
	
	@Before
	public void testBefore() throws EncryptedDocumentException, InvalidFormatException, IOException
	{
		new JFXPanel();
		ExamLoader.getInstance().load(new File("data/IT100-2.xlsx"));
		exConfig = ExamLoader.getInstance().getExamConfig();
		examSheet.setExamConfig(exConfig);
		examSheet.setQustionList(ExamLoader.getInstance().getQustionList());
		
	}


	@Test
	public void test() throws IOException
	{
		Date d = new Date();
		Student st = new Student("NAE003");
		st.setCuttoffPoint(d);
		st.setName("Sami Alfattani");
		st.setFinishPoint(d);
		st.setStatus(Student.FINISHED);
		st.setExamSheet(examSheet);
		
		File f =   new File("Data//hh.xml");  //Util.getTempFile(); 
		Util.jaxbStudentToXML(st, f);
		
		
		//Util.RunApplication(f);
		Student stFromFile = Util.jaxbXMLToStudent(f);
		
		assertEquals("NAE003", stFromFile.getId());
		assertEquals("Sami Alfattani", stFromFile.getName());
		assertEquals(d, stFromFile.getFinishPoint());
		assertEquals(d, stFromFile.getCuttoffPoint());
		assertEquals(Student.FINISHED, stFromFile.getStatus());
		
		assertEquals(Student.FINISHED, stFromFile.getOptionalExamSheet().get());
		
	}

}
