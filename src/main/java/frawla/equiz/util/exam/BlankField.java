package frawla.equiz.util.exam;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

public class BlankField extends Question implements Serializable
{
	private static final long serialVersionUID = 7874885710303162877L;
	List<String> Options = new ArrayList<>();

	@Override
	public boolean isCorrectAnswer()
	{
		
		final String ans = studentAnswer;
		boolean res = Options		
		.stream()
		.anyMatch( op -> {
			String s1 = op.trim().toLowerCase().replaceAll("\\s+", " ");
			String s2 = ans.trim().toLowerCase().replaceAll("\\s+", " ");
			return s1.equals(s2);
		});
		
		return res;
	}

	public List<String> getOptions(){return Options;}
	public void setOptions(List<String> options){Options = options;}
	
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

	


}
