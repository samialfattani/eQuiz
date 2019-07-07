package frawla.equiz.util.exam;

import java.io.Serializable;
import java.text.DecimalFormat;

import javafx.util.Duration;

public abstract class Question implements Serializable
{
	private static final long serialVersionUID = 8649772481978311983L;
	
	public static final String MULTIPLE_CHOICE = "Multiple Choice";
	public static final String BLANK_FIELD = "Blank Field";
	
	private int id = 0;
	private String Type = "";
	private String text = "";
	private String imgFileName = "";
	private Duration time = Duration.ZERO;
	private Duration consumedTime = Duration.ZERO; 
	private double mark = 0;
	private String teacherNote="";
	
	protected String studentAnswer = "";
	private double StudentMark =0 ;
	


	public Question(){ }

	public Question(String text){
		this.text = text;
	}

	public void setType(String type){Type = type;}
	public String getType(){return Type;}

	public int getId(){return id;}
	public void setId(int id){this.id = id;}
	public String getText(){return text;}
	public void setText(String text){this.text = text;}
	public Duration getTime(){return time;}
	public void setTime(Duration time){this.time = time;}
	public String getStudentAnswer(){return studentAnswer;}
	public void setStudentAnswer(String sa){studentAnswer = sa;}	
	public String getImgFileName(){return imgFileName;}
	public void setImgFileName(String imgFileName){this.imgFileName = imgFileName;}
	public double getMark(){return mark;}
	public void setMark(double mark){this.mark = mark;}

	public String getTeacherNote(){return teacherNote;}
	public void setTeacherNote(String tne){teacherNote = tne;}

	public Duration getConsumedTime(){return consumedTime;}
	public void setConsumedTime(Duration consumedTime){this.consumedTime = consumedTime;}
	public void AppendConsumedTime(Duration t){
		consumedTime = consumedTime.add(t);
	}

	public abstract double correctAndGetTheMark();
	public abstract boolean isCorrectAnswer();

	public double getStudentMark(){return StudentMark;}
	public void setStudentMark(double studentMark){StudentMark = studentMark;}
	
	@Override
	public String toString()
	{	
		//2`what is your name? [55 min, 2.5 marks]
		String res = "";
		res = id + "`" + text + " ["
				+  time  + " min, " +
				 new DecimalFormat("#.##").format( getMark()) + " marks]";
		return res;
	}

	public String getStudentAnswerAsText()
	{
		if(this instanceof MultipleChoice)
			return ((MultipleChoice)this).getChoices().get(getStudentAnswer());
		else
			return getStudentAnswer();
	}

}
