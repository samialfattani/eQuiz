package frawla.equiz.util.exam;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultipleChoice extends Question implements Serializable, Randomizable
{
	private static final long serialVersionUID = -9129997940545881330L;
	private Map<String, String> Choices = new HashMap<>();
	private List<String> orderList = new ArrayList<>();

	private String correctAnswer;
	
	//This input should be on the follwing format 'Q2`1`CBEAD`E`teacher notes'
	public MultipleChoice(String c) throws InputMismatchException 
	{
		String Qptrn = "(?s)Q(\\d+)`(\\+?[0-9]*\\.?[0-9]*)`([A-Fa-f]{1,6})`([A-Fa-f\\-]?)(`(.*))?";
		Pattern pattern = Pattern.compile(Qptrn);
		Matcher matcher = pattern.matcher(c);

		if( matchesOldPatern(c) )
			setDataUsingOldPattern(c);
		
		else if( matcher.matches() )
		{
			setId( Integer.parseInt( matcher.group(1) ));  
			setMark(Double.parseDouble( matcher.group(2)  ));
			setCorrectAnswer("A");

			//CBEAD to list of letters
			String ol = matcher.group(3); 
			for (int k = 0; k < ol.length(); k++){
				String letter = ol.substring(k,k+1);
				 getOrderList().add( letter);
			}
			String ans = Optional.ofNullable(matcher.group(4)).orElse("");
			ans = (ans.equals(""))? "-" : ans;
			setStudentAnswer( ans ); 

			String teacherNot = Optional.ofNullable(matcher.group(6)).orElse("");		
			setTeacherNote( teacherNot );
		}else
			throw new InputMismatchException("'" + c + "' is in wrong format.This input should be on the follwing format 'Q2`1`CBEAD`E' or 'Q2`1`CBEAD`E`teacher note'");
		
		
	}



	public MultipleChoice(){}
	
	public String getCorrectAnswer(){return correctAnswer;}
	public void setCorrectAnswer(String ca){correctAnswer = ca;}
	public Map<String, String> getChoices(){return Choices;}
	public void setChoices(Map<String, String> choices){
		Choices = choices;
		resetOrderList();
	}

	public void resetOrderList(){
		orderList.clear();
		Choices.forEach((k,v) -> orderList.add(k) );		
	}
	public List<String> getOrderList(){return orderList;}
	
	public boolean isCorrectAnswer()
	{
		String s1 = studentAnswer.trim().toLowerCase();
		String s2 = correctAnswer.trim().toLowerCase();
		return s1.equals(s2);
	}

	public String getKeyOf(String value){
		return getKeyOf(Choices, value);
	}
	public String getKeyOf(Map<String, String> h, String value)
	{
		for (Entry<String, String> entry : h.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey() + "";
            }
        }
		return "";
	}
	
	@Override
	public String toString()
	{	
		//Q2/3/BAC/B
		StringBuilder res = new StringBuilder("");
		getOrderList();
		res.append("Q" + getId() + "`");
		res.append(new DecimalFormat("#.##").format( getMark()) + "`");
		for(int i=0; i<orderList.size(); i++)
		{
			res.append( orderList.get(i) ); 
		}
		res.append( "`" );
		res.append(studentAnswer);
		
		if(!getTeacherNote().equals(""))
			res.append( "`" + getTeacherNote() );
		
		return res.toString();
	}
	
	@Override
	public void shuffle()
	{
		Collections.shuffle(getOrderList()); 		
	}
	@Override
	public double correctAndGetTheMark()
	{
		if(isCorrectAnswer())
			return getMark();
		return 0;
	}
	public void copyChoices(Question q)
	{
		
		((MultipleChoice)q).getChoices()
		  .forEach( (k,v) -> {
			  	getChoices().put(k, v);
		  });		
	}
	

	private boolean matchesOldPatern(String c)
	{
		return c.matches("(?s)Q(\\d+)\\/(\\+?[0-9]*\\.?[0-9]*)\\/([A-Fa-f]{1,6})\\/([A-Fa-f\\-]?)(\\/(.*))?");
	}

	private void setDataUsingOldPattern(String c)
	{
		String[] Answer = c.split("/", -1);
		Answer[0] = Answer[0].substring(1, Answer[0].length());
		setId( Integer.parseInt( Answer[0] ));  
		setMark(Double.parseDouble( Answer[1]  ));
		setCorrectAnswer("A");

		//CBEAD to list of letters
		String ol = Answer[2];
		for (int k = 0; k < ol.length(); k++){
			String letter = ol.substring(k,k+1);
			 getOrderList().add( letter);
		}
		String ans = Optional.ofNullable( Answer[3] ).orElse("");
		ans = (ans.equals(""))? "-" : ans;
		setStudentAnswer( ans );

		//String teacherNot = Optional.ofNullable( Answer[4] ).orElse("");		
		//setTeacherNote( teacherNot );
	}

	
}//end class
