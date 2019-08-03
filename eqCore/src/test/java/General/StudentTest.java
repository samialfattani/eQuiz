package general;



import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import frawla.equiz.util.EQDate;
import frawla.equiz.util.EQuizException;
import frawla.equiz.util.Util;
import frawla.equiz.util.exam.ExamConfig;
import frawla.equiz.util.exam.ExamSheet;
import frawla.equiz.util.exam.Student;
import javafx.util.Duration;


public class StudentTest
{
	private static ExamSheet examSheetMocked;	
	private static ExamSheet emptyExamSheet;
	private static ExamConfig examConfigMocked;
	
	@BeforeAll
	public  static void readObjectFromFileAsMock() throws InterruptedException, EQuizException
	{
		File mockFile;
		mockFile = Util.getResourceAsFile("ExamSheet-sami.mock");
		examSheetMocked = (ExamSheet) Util.readFileAsObject( mockFile  );
		
		mockFile = Util.getResourceAsFile("ExamSheet-empty.mock");
		emptyExamSheet = (ExamSheet) Util.readFileAsObject( mockFile  );

		mockFile = Util.getResourceAsFile("ExamConfig.mock");
		examConfigMocked = (ExamConfig) Util.readFileAsObject( mockFile  );
		
	}

	
	@Test
	public void dataTest() throws InterruptedException, EQuizException
	{
		assertEquals(0.5, examSheetMocked.getTotalMarks(), 0.0 );	
	}

	@Test
	public void rejectedTest() throws InterruptedException, EQuizException
	{
		//TODO: we need to move the validation method to be inside Student class		
	}
	
	@Test
	public void StartAndFinishTest() throws InterruptedException, EQuizException
	{
		Student st = new Student("NBE222");
		st.setExamSheet(examSheetMocked);
		
		st.setStatus(Student.READY);//mocking
		
		Duration examTime = new Duration( 2 * 60 * 1000); //2 min.
		st.runExam(examTime);
		assertEquals(Student.STARTED, st.getStatus());

		Thread.sleep(2000);
		st.finishNow();
		assertEquals(Student.FINISHED, st.getStatus());
	}

	@Test
	public void cutOffOnReadyTest() throws InterruptedException, EQuizException
	{
		Student st = new Student("NBE222");
		st.setExamSheet(examSheetMocked);
		
		st.setStatus(Student.READY);//mocking
		
		st.cutOffNow();
		assertEquals(Student.DISCONNECTED, st.getStatus());
		
		Duration examTime = new Duration( 2 * 60 * 1000); //2 min.
		st.runExam(examTime);
		
		assertEquals(Student.STARTED, st.getStatus());
	}

	@Test
	public void cutOffAndResumeTest() throws InterruptedException, EQuizException
	{
		Student st = new Student("NBE222");
		st.setExamSheet(emptyExamSheet);
		
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
	public void  cutOffAfterFinishTest() throws InterruptedException, EQuizException
	{
		Student st = new Student("NBE222");
		st.setExamSheet(emptyExamSheet);
		Duration examTime = new Duration(10 * 60 * 1000); //10 min.
		emptyExamSheet.getExamConfig().examTime = examTime;
		
		
		st.setStatus(Student.READY);//mocking
		st.runExam(examTime); 
		
		assertEquals(10, st.getLeftTime().toMinutes(), 0.01);
		assertEquals(10, st.getLeftTime().toMinutes(), 0.01);
		
		assertEquals(Student.STARTED, st.getStatus());
		assertEquals(0, st.getSpendTime().toSeconds(), 1);
		Thread.sleep(1000);
		st.finishNow();
		
		assertEquals(Student.FINISHED, st.getStatus());
		
		assertEquals(1, st.getSpendTime().toSeconds(), 0.1);
		
		st.cutOffNow();
		assertEquals(Student.FINISHED, st.getStatus());
		
	}
	
	@Test
	public void resumeAfterFinishTest() throws InterruptedException, EQuizException
	{
		Student st = new Student();
		//spy is better than mock
		//st = spy(Student.class);
		//doReturn( new Duration( 100 * 1000 ) ).when(st).calculateSpendTime();
		
		st.setId("NBE222");
		st.setExamSheet(examSheetMocked);
		Duration examTime = new Duration( 10 * 60 * 1000 );
		examSheetMocked.getExamConfig().examTime = examTime;
		
		st.setStatus(Student.GRADED);//mocking
		st.setStatus( Student.CUTOFF ); //mocking
		
		//here is to estimate starting time.
		Duration FullAnsweringTime = st.getSpendTime();
		assertEquals(330 , FullAnsweringTime.toSeconds() , 1); //5:30 min
		
		EQDate now = new EQDate();
		st.setStartPoint(  now.minus( FullAnsweringTime )  );
		st.setResumePoint( now.minus( FullAnsweringTime )  );
		st.setFinishPoint( st.getStartPoint().plus( examTime ) );
		
		//now he is running
		st.setStatus(Student.STARTED);
		assertEquals(5.5 , st.getSpendTime().toMinutes() , 0.01); //5:30 min
		
		st.cutOffNow();

		st.runExam(examTime);
		assertEquals(4.5, st.getLeftTime().toMinutes(), 0.01);
		assertEquals(Student.RESUMED, st.getStatus());
	
	}
	

}//end class




//doCallRealMethod().when(st).setStartPoint( any(EQDate.class) );
//doCallRealMethod().when(st).getStartPoint(  );
//doCallRealMethod().when(st).setCuttoffPoint( any(EQDate.class) );
//doCallRealMethod().when(st).setFinishPoint( any(EQDate.class) );
//doCallRealMethod().when(st).getLeftTime(  );

