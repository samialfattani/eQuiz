package general;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import frawla.equiz.util.EQuizException;
import frawla.equiz.util.exam.Student;
import javafx.util.Duration;

public class StudentTest
{
	@Test
	public void rejectedTest() throws InterruptedException, EQuizException
	{
		//TODO: we need to move the validation method to be inside Student class		
	}

	@Test
	public void cutOffOnReadyTest() throws InterruptedException, EQuizException
	{
		Student st = new Student("NBE222");
		st.setStatus(Student.READY);//mocking
		
		st.cutOffNow();
		assertEquals(Student.DISCONNECTED, st.getStatus());
		
		Duration examTime = new Duration(120000); //2 min.
		st.runExam(examTime);
		
		assertEquals(Student.STARTED, st.getStatus());
	}

	@Test
	public void cutOffAndResumeTest() throws InterruptedException, EQuizException
	{
		Student st = new Student("NBE222");
		st.setStatus(Student.READY); //mocking
		
		Duration examTime = new Duration(120000);
		st.runExam(examTime); //2min.
		
		assertEquals(	st.getFinishPoint().getTime(), 
						st.getStartPoint().getTime() + examTime.toMillis(),
						0.001);
		
		
		assertEquals(Student.STARTED, st.getStatus());
		assertEquals(0, st.getSpendTime().toSeconds(), 0.1);

		Thread.sleep(2000);
		assertEquals(2, st.getSpendTime().toSeconds(), 0.1);

		//First CutOff
			st.cutOffNow();
			assertEquals(Student.CUTOFF, st.getStatus());
			assertEquals(2, st.getSpendTime().toSeconds(), 0.1);
			
			Thread.sleep(1500);
			
			st.runExam(examTime);
			assertEquals(Student.RESUMED, st.getStatus());
			assertEquals(st.getSpendTime().toSeconds(), 2, 0.01);
			assertEquals(st.getLeftTime().toSeconds(), 118, 0.01);

			Thread.sleep(2000);
			
		//Second CutOff
			st.cutOffNow();
			assertEquals(Student.CUTOFF, st.getStatus());
			assertEquals(4, st.getSpendTime().toSeconds(), 0.1);
			
			Thread.sleep(1500);
			
			st.runExam(examTime);
			assertEquals(Student.RESUMED, st.getStatus());
			assertEquals(st.getSpendTime().toSeconds(), 4, 0.01);
			assertEquals(st.getLeftTime().toSeconds(), 116, 0.01);
	}//end Test

	@Test
	public void cutOffAfterFinishTest() throws InterruptedException, EQuizException
	{
		Student st = new Student("NBE222");
		st.setStatus(Student.READY);//mocking
		
		Duration examTime = new Duration(120000);
		st.runExam(examTime); //2min.
		
		assertEquals(st.getLeftTime().toSeconds(), 120, 0.1);
		
		assertEquals(Student.STARTED, st.getStatus());
		assertEquals(0, st.getSpendTime().toSeconds(), 0.1);
		Thread.sleep(1000);
		st.finishNow();
		
		assertEquals(Student.FINISHED, st.getStatus());
		assertEquals(1, st.getSpendTime().toSeconds(), 0.1);
		
		st.cutOffNow();
		assertEquals(Student.FINISHED, st.getStatus());
		
	}

}//end class
