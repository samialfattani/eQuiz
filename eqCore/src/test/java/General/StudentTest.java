package General;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import frawla.equiz.util.exam.Student;
import javafx.util.Duration;

public class StudentTest
{
	

	@Test
	public void test()
	{
		Student st = new Student("NBE222");
		st.setStatus(Student.READY);
		
		Duration examTime = new Duration(120000);
		st.runExam(examTime); //2min.
		
		assertEquals(	st.getFinishPoint().getTime(), 
						st.getStartPoint().getTime() + examTime.toMillis(),
						0.001);
		
		st.setStatus(Student.STARTED);
		try{
			Thread.sleep(2000);
		}catch(Exception e){}

		st.cutOffNow();
		assertEquals(st.getTimeSpend().toSeconds(), 2, 0.001);
		try{
			Thread.sleep(1000);
		}catch(Exception e){}
		
		st.runExam(examTime);
		
		assertEquals(st.getTimeSpend().toSeconds(), 2, 0.01);
		assertEquals(st.getTimeLeft().toSeconds(), 118, 0.01);

	}

}
