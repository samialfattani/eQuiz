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

public class MultipleChoice extends Question implements Serializable, Randomizable
{
	private static final long serialVersionUID = -9129997940545881330L;
	private Map<String, String> Choices = new HashMap<>();
	private List<String> orderList = new ArrayList<>();

	private String correctAnswer;
	
	//This input should be on the follwing format 'Q2/1/CBEAD/E'
	public MultipleChoice(String c) throws InputMismatchException
	{
		if(!c.matches("Q\\d+\\/[-+]?[0-9]*\\.?[0-9]*\\/[A-Fa-f]{1,6}\\/[A-Fa-f]?"))
			throw new InputMismatchException("'" + c + "' is in wrong format.This input should be on the follwing format 'Q2/1/CBEAD/E'");
		
		String[] Answer = c.split("/", -1);
		setId( Integer.parseInt( Answer[0].substring(1) ));
		setMark(Double.parseDouble(Answer[1]));
		setCorrectAnswer("A");

//		Arrays.asList(Answer[2].toCharArray())
//			  .stream()
//			  .forEach( letter ->{
//				  
//				  getOrderList().add( new String(letter));				 
//			  });

		for (int k = 0; k < Answer[2].length(); k++){
			String letter = Answer[2].substring(k,k+1);
			 getOrderList().add( letter);
		}
		Answer[3] = Optional.ofNullable(Answer[3]).orElse("");
		setStudentAnswer( Answer[3] );

		
	}
	public MultipleChoice()
	{
	}
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
		String res = "";
		getOrderList();
		res = "Q" + getId() + "/";
		res +=  new DecimalFormat("#.##").format( getMark()) + "/";
		for(int i=0; i<orderList.size(); i++)
		{
			res += orderList.get(i); 
		}
		res += "/" + studentAnswer;
		return res;
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
	

}
