package frawla.equiz.util.exam;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Optional;

public class BlankField extends Question implements Serializable
{
	private static final long serialVersionUID = 7874885710303162877L;
	List<String> correctAnswerList = new ArrayList<>();

	//This input should be on the follwing format 'Q2/1/Hello'
	public BlankField(String c) throws InputMismatchException
	{
		if(!c.matches("(?s)Q\\d+\\/[-+]?[0-9]*\\.?[0-9]*\\/.*"))
			throw new InputMismatchException("This input should be on the follwing format 'Q2/1/Hello' but it was '" + c +"'");

		String[] Answer = c.split("/", -1);
		setId( Integer.parseInt( Answer[0].substring(1) ));
		setMark(Double.parseDouble(Answer[1]));

		Answer[2] = Optional.ofNullable(Answer[2]).orElse("");
		setStudentAnswer(Answer[2]);
		
	}

	public BlankField()
	{
		
	}

	@Override
	public boolean isCorrectAnswer()
	{
		
		final String ans = studentAnswer;
		boolean res = correctAnswerList		
		.stream()
		.anyMatch( op -> {
			String s1 = op.trim().toLowerCase().replaceAll("\\s+", " ");
			String s2 = ans.trim().toLowerCase().replaceAll("\\s+", " ");
			return s1.equals(s2);
		});
		
		return res;
	}

	public List<String> getCorrectAnswerList(){return correctAnswerList;}
	public void setCorrectAnswerList(List<String> options){correctAnswerList = options;}
	
	@Override
	public String toString()
	{	
		String res = "";
		res = 	"Q" + getId() + "/" + 
				 new DecimalFormat("#.##").format( getMark()) + "/" +
				studentAnswer;
		return res;
	}

	@Override
	public double correctAndGetTheMark()
	{
		if(isCorrectAnswer())
			return getMark();
		return 0;
	}

	public void copyOptions(Question q)
	{
		// TODO Auto-generated method stub
		((BlankField)q).getCorrectAnswerList()
		.stream()
		.forEach( option -> {
			getCorrectAnswerList().add(option);
		});
		
	}

	


}
