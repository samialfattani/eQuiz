package frawla.equiz.util.exam;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

import com.sun.xml.internal.txw2.annotation.XmlElement;

public class ExamSheet implements Serializable
{
	
	private static final long serialVersionUID = 1L;
	public int currentQuesIdx = 0;
	private List<Question> qustionList;
	private ExamConfig examConfig;
	

	public Question getCurrentQuestion()
	{
		return getQustionList().get(currentQuesIdx);
	}
	
	public void shuffle()
	{
		//shuffle Options in each Question
		qustionList
			.stream()
			.filter(qu -> qu instanceof Randomizable)
			.forEach( q ->  ((Randomizable)q).shuffle()   
		);
		
		//shuffle All questions
		Collections.shuffle(qustionList);
		
	}
	
	public void setQustionList(List<Question> qustionList){
		this.qustionList = qustionList;
	}
	public List<Question> getQustionList(){
		return    qustionList;
	}

	public ExamConfig getExamConfig(){return examConfig;}
	public void setExamConfig(ExamConfig examConfig){this.examConfig = examConfig;}

}
