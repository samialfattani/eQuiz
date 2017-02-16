package frawla.equiz.util.exam;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class ExamSheet implements Serializable
{
	
	private static final long serialVersionUID = 1L;
	public int currentQuesIdx = 0;
	private List<Question> qustionList;
	private ExamConfig examConfig;
	

	public Question getCurrentQuestion()
	{
		return getQuestionList().get(currentQuesIdx);
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
	
	public void setQustionList(List<Question> lst){this.qustionList = lst;}
	public List<Question> getQuestionList(){return qustionList;}
	public ExamConfig getExamConfig(){return examConfig;}
	public void setExamConfig(ExamConfig examConfig){this.examConfig = examConfig;}

}
