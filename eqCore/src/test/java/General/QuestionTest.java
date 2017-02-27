package General;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Map;

import org.junit.Test;

import frawla.equiz.util.exam.BlankField;
import frawla.equiz.util.exam.MultipleChoice;

public class QuestionTest
{

	@Test
	public void MultipleChoiceTest()
	{
		MultipleChoice qmc = new MultipleChoice();
		qmc.setId(5);

		Map<String,String> choices = new HashMap<>();
		choices.put("A", "A answer");
		choices.put("B", "B answer");
		choices.put("C", "C answer");
		choices.put("D", "D answer");
		qmc.setChoices(choices);

		qmc.setCorrectAnswer("A");


		qmc.setStudentAnswer("C");
		assertEquals("C", qmc.getStudentAnswer());

		qmc.setStudentAnswer("A");
		assertTrue(qmc.isCorrectAnswer());

		qmc.setStudentAnswer("B");
		assertFalse(qmc.isCorrectAnswer());

		assertEquals("Q5/0/ABCD/B", qmc.toString());
		assertEquals("{A=A answer, B=B answer, C=C answer, D=D answer}",qmc.getChoices().toString());
		assertEquals("[A, B, C, D]" , qmc.getOrderList().toString());

	}

	@Test
	public void MultipleChoiceSpecialConstructorTest() throws InputMismatchException
	{
		MultipleChoice qmc = new MultipleChoice("Q2/1/CBEAD/E");

		qmc.setCorrectAnswer("A");
		qmc.setStudentAnswer("C");
		assertEquals("C", qmc.getStudentAnswer());

		qmc.setStudentAnswer("A");
		assertTrue(qmc.isCorrectAnswer());

		qmc.setStudentAnswer("B");
		assertFalse(qmc.isCorrectAnswer());

		assertEquals("Q2/1/CBEAD/B", qmc.toString());
		assertEquals("{}",qmc.getChoices().toString());
		assertEquals("[C, B, E, A, D]" , qmc.getOrderList().toString());
	}

	@Test
	public void BlankFieldTest()
	{
		BlankField qbf = new BlankField();
		qbf.setId(5);

		assertEquals(5, qbf.getId());

		qbf.setMark(2.0);


		qbf.setCorrectAnswerList(Arrays.asList("Hi","Hello", "Welcome back Brother"));

		qbf.setStudentAnswer("  hi  ");		
		assertTrue(qbf.isCorrectAnswer());

		qbf.setStudentAnswer("  hellO ");		
		assertTrue(qbf.isCorrectAnswer());

		qbf.setStudentAnswer("welcome back brother");		
		assertTrue(qbf.isCorrectAnswer());


		qbf.setStudentAnswer("welcomebackbrother");		
		assertFalse(qbf.isCorrectAnswer());

		qbf.setStudentAnswer("welcome back");		
		assertFalse(qbf.isCorrectAnswer());
	}

	@Test
	public void BlankFieldSpecialConstructorTest() throws InputMismatchException
	{

		BlankField qbf = new BlankField("Q5/1.5/Hello");
		qbf.setCorrectAnswerList(Arrays.asList("Hi","Hello", "Welcome back Brother"));

		assertEquals(5, qbf.getId());
		assertEquals(1.5, qbf.getMark(),0.1);

		assertTrue(qbf.isCorrectAnswer());
		
		qbf = new BlankField("Q5/1.5/ss");
		qbf.setCorrectAnswerList(Arrays.asList("Hi","Hello", "Welcome back Brother"));
		assertFalse(qbf.isCorrectAnswer());

		qbf = new BlankField("Q5/1.5/ss\nrgergrgqwrg3eg'll'ervkr\"efff");
		qbf.setCorrectAnswerList(Arrays.asList("Hi","Hello", "Welcome back Brother"));
		assertFalse(qbf.isCorrectAnswer());
	
	}

}
