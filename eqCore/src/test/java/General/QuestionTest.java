package general;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

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

		assertEquals("Q5`0`ABCD`B", qmc.toString());
		assertEquals("{A=A answer, B=B answer, C=C answer, D=D answer}",qmc.getChoices().toString());
		assertEquals("[A, B, C, D]" , qmc.getOrderList().toString());

	}

	@Test
	public void MultipleChoiceSpecialConstructorTest() throws InputMismatchException
	{
		MultipleChoice qmc = new MultipleChoice("Q2/1/CBEAD/E");

		assertEquals(2, qmc.getId());
		assertEquals(1, qmc.getMark(), 0.1);
		qmc.setCorrectAnswer("A");
		qmc.setStudentAnswer("C");
		assertEquals("C", qmc.getStudentAnswer());

		qmc.setStudentAnswer("A");
		assertTrue(qmc.isCorrectAnswer());

		qmc.setStudentAnswer("B");
		assertFalse(qmc.isCorrectAnswer());

		assertEquals("Q2`1`CBEAD`B", qmc.toString());
		assertEquals("{}",qmc.getChoices().toString());
		assertEquals("[C, B, E, A, D]" , qmc.getOrderList().toString());
		
		
		qmc = new MultipleChoice("Q2/1/CBEAD/");
		assertFalse(qmc.isCorrectAnswer());
		assertEquals("Q2`1`CBEAD`", qmc.toString());
		assertEquals("{}",qmc.getChoices().toString());
		
		assertFalse(qmc.isCorrectAnswer());		
		assertEquals("",qmc.getStudentAnswer());
		assertEquals("", qmc.getTeacherNote());

		qmc = new MultipleChoice("Q2/1/CBEAD/B/mynote");
		assertFalse(qmc.isCorrectAnswer());		
		assertEquals("B",qmc.getStudentAnswer());
		assertEquals("", qmc.getTeacherNote());
		
		// ----- NEW PATTEREN
		
		qmc = new MultipleChoice("Q2`1`CBEAD`E`teacher notes");
		assertFalse(qmc.isCorrectAnswer());
		assertEquals("E",qmc.getStudentAnswer());
		assertEquals("teacher notes", qmc.getTeacherNote());
		
		qmc = new MultipleChoice("Q2`1`CBEAD``teacher notesefe efefef/ efefef/ef/efe");
		assertFalse(qmc.isCorrectAnswer());
		assertEquals("",qmc.getStudentAnswer());
		assertEquals("teacher notesefe efefef/ efefef/ef/efe", qmc.getTeacherNote());

		qmc = new MultipleChoice("Q2`1`CBEAD``teacher notes");
		assertFalse(qmc.isCorrectAnswer());
		assertEquals("",qmc.getStudentAnswer());
		assertEquals("teacher notes", qmc.getTeacherNote());

		qmc = new MultipleChoice("Q2`1`CBEAD``");
		assertFalse(qmc.isCorrectAnswer());		
		assertEquals("",qmc.getStudentAnswer());
		assertEquals("", qmc.getTeacherNote());

		qmc = new MultipleChoice("Q2`1`CBEAD``teacher notes\r\n rgrg\n5165165");
		assertFalse(qmc.isCorrectAnswer());		
		assertEquals("",qmc.getStudentAnswer());
		assertEquals("teacher notes\r\n rgrg\n5165165", qmc.getTeacherNote());

		qmc = new MultipleChoice("Q1`1`DBACE`A");
		assertTrue(qmc.isCorrectAnswer());		
		assertEquals("A",qmc.getStudentAnswer());
		assertEquals("", qmc.getTeacherNote());

		qmc = new MultipleChoice("Q1`1`DBACE`");
		assertFalse(qmc.isCorrectAnswer());		
		assertEquals("",qmc.getStudentAnswer());
		assertEquals("", qmc.getTeacherNote());
	}// end MultipleChoiceSpecialConstructorTest

	@Test
	public void BlankFieldTest()
	{
		BlankField qbf = new BlankField();
		qbf.setId(5);
		qbf.setMark(2.0);
		qbf.setCorrectAnswerList(Arrays.asList("Hi","Hello", "Welcome back Brother"));
				
		assertEquals(5, qbf.getId());
		assertEquals(2, qbf.getMark(), 0.1);
		
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
		List<String> correctAns = Arrays.asList("Hi","Hello", "Welcome back \nBrother\n1/1");
		qbf.setCorrectAnswerList(correctAns);

		assertEquals(5, qbf.getId());
		assertEquals(1.5, qbf.getMark(),0.1);

		assertTrue(qbf.isCorrectAnswer());
		
		qbf = new BlankField("Q5/1.5/ss");
		qbf.setCorrectAnswerList(correctAns);
		assertFalse(qbf.isCorrectAnswer());

		qbf = new BlankField("Q5/1.5/Welcome \n back \nBrother 1/1");
		qbf.setCorrectAnswerList(correctAns);
		assertTrue(qbf.isCorrectAnswer());

		//------- NEW PATTERN ------
		
		qbf = new BlankField("Q5`2`");
		assertEquals("", qbf.getStudentAnswer());
		
		qbf = new BlankField("Q5`1.5``");
		assertEquals("", qbf.getStudentAnswer());
		assertEquals("", qbf.getTeacherNote());
		
		qbf = new BlankField("Q5`1.5`Hi`jji");
		qbf.setCorrectAnswerList(correctAns);

		assertEquals(5, qbf.getId());
		assertEquals(1.5, qbf.getMark(),0.1);
		assertEquals("Hi", qbf.getStudentAnswer());
		assertTrue(qbf.isCorrectAnswer());

		qbf = new BlankField("Q5`1.5`Welcome back\n brother\n1/1`teacher notes");
		qbf.setCorrectAnswerList(correctAns);
		assertTrue(qbf.isCorrectAnswer());

		qbf = new BlankField("Q5`1.5`Welcome back\n brother\n1/1");
		qbf.setCorrectAnswerList(correctAns);
		assertTrue(qbf.isCorrectAnswer());

		qbf = new BlankField("Q5`1.5`Welcome back\n brother\n1/1`");
		qbf.setCorrectAnswerList(correctAns);
		assertTrue(qbf.isCorrectAnswer());
		
	}// BlankFieldSpecialConstructorTest

}


