package frawla.equiz.util.exam;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BlankField extends Question 
{
	private static final long serialVersionUID = 7874885710303162877L;
	List<String> correctAnswerList = new ArrayList<>();

	//This input should be on the follwing format 'Q2/1/Hello'
	public BlankField(String c) throws InputMismatchException
	{
		String Qptrn = "(?s)Q(\\d+)`(\\+?[0-9]*\\.?[0-9]*)`(.*?)($|`)(.*)";
		Pattern pattern = Pattern.compile(Qptrn);
		Matcher matcher = pattern.matcher(c);

		if( matchesOldPatern(c) )
			setDataUsingOldPattern(c);
		
		else if( matcher.matches() )
		{
			setId( Integer.parseInt( matcher.group(1) ));
			setMark(Double.parseDouble(  matcher.group(2)  ));

			String ans = Optional.ofNullable( matcher.group(3) ).orElse("");
			//ans = (ans.equals(""))? "-" : ans;
			setStudentAnswer(ans);

			String tchrNote = Optional.ofNullable( matcher.group(5) ).orElse("");
			setTeacherNote(tchrNote);
						
		}else
			throw new InputMismatchException("This input should be on the follwing format 'Q2/1/Hello' or 'Q2`1`Student Answer`Teacher Notes' but it was '" + c +"'");

		
	}


	public BlankField(){ }

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
		res = 	"Q" + getId() + "`" + 
				 new DecimalFormat("#.##").format( getMark()) + "`" +
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
		((BlankField)q).getCorrectAnswerList()
		.stream()
		.forEach( option -> {
			getCorrectAnswerList().add(option);
		});
		
	}

	// this used if the data is stored in old patrn (manga version 3.5.0.1)
	private boolean matchesOldPatern(String c)
	{		
		return c.matches("(?s)Q\\d+\\/[-+]?[0-9]*\\.?[0-9]*\\/.*");
	}
	
	private void setDataUsingOldPattern(String c)
	{
		String[] Answer = c.split("/", -1);
		setId( Integer.parseInt( Answer[0].substring(1) ));
		setMark(Double.parseDouble(Answer[1]));

		Answer[2] = Optional.ofNullable(Answer[2]).orElse("");
		String[] A = Arrays.copyOfRange(Answer, 2, Answer.length);
		String ans = Arrays.asList(A)
			  .stream()
			  .map( x -> {return (x==null)?"":x ;}  )
			  .collect( Collectors.joining("/"));
		
		setStudentAnswer(ans);
	}

}
