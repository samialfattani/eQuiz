package frawla.equiz.util.exam;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlType;

public class MultipleChoice extends Question implements Serializable, Randomizable
{
	private static final long serialVersionUID = -9129997940545881330L;
	private Map<String, String> Choices = new HashMap<>();
	private List<String> orderList = new ArrayList<>();

	private String correctAnswer;
	
	public String getCorrectAnswer(){return correctAnswer;}
	public void setCorrectAnswer(String ca){correctAnswer = ca;}
	public Map<String, String> getChoices(){return Choices;}
	public void setChoices(Map<String, String> choices){
		Choices = choices;
		orderList.clear();
		Choices.forEach((k,v) -> orderList.add(k) );
	}
	public List<String> getOrderList()
	{
//		if(orderList.isEmpty())
//			Choices.forEach((k,v) -> orderList.add(k) );
			
		return orderList;
	}
	
	@Override
	public void setStudentAnswer(String sa)
	{
		String ans = findKey(Choices, sa);
		studentAnswer = ans;
	}
	
	public boolean isCorrectAnswer()
	{
		String s1 = studentAnswer.trim().toLowerCase();
		String s2 = correctAnswer.trim().toLowerCase();
		return s1.equals(s2);
	}

	private String findKey(Map<String, String> h, String value)
	{
		for (Entry<String, String> entry : h.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey() + "";
            }
        }
		return null;
	}
	
	@Override
	public String toString()
	{	
		//Q2/3/BAC/B
		String res = "";
		getOrderList();
		res = "Q" + getId() + "/";
		res +=  new DecimalFormat("#.##").format( getMark()) + "/";
		for(int i=0; i<Choices.size(); i++)
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
	

}
