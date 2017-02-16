package frawla.equiz.util.exam;

import java.io.Serializable;
import java.util.Date;
import java.util.Optional;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import frawla.equiz.util.Channel;
import javafx.util.Duration;

@XmlRootElement
public class Student implements Serializable
{
	private static final long serialVersionUID = -2983386758431074593L;
	
	public static final String REJECTED = "Rejected";
	public static final String READY = "Ready";
	public static final String STARTED = "Started";
	public static final String CUTOFF = "Cut Off";
	public static final String RESUMED = "Resumed";
	public static final String FINISHED = "Finished";
	
	@XmlTransient
	private Channel serverLinker;
	private String id = "";
	private String name= "";	
	private String status = "";
	private boolean connected = false;
	private Date startPoint;
	private Date cuttoffPoint;
	private Date resumePoint ;
	private Date finishPoint ;
	private Duration timeLeft = Duration.ZERO;
	private Duration timeSpend = Duration.ZERO;
	private Optional<ExamSheet> examSheet = Optional.empty();
	private Date LastUpdate = new Date();
	

	//IMPORTANT for JAXB
	public Student(){
	}

	public Student(Channel ch){
		serverLinker = ch;
		connected = true;
	}
	
	public Student(String id)
	{
		this.id = id;
	}

	public String getId(){return id;}
	public void setId(String id){this.id = id;}
	public String getName(){return name;}
	public void setName(String name){this.name = name;}
	public String getStatus(){return status;}
	public void setStatus(String status){this.status = status;}

	@XmlTransient
	public Channel getServerLinker(){return serverLinker;}
	public void setServerLinker(Channel ch){serverLinker = ch;}
	
	public Date getStartPoint(){return startPoint;}
	public void setStartPoint(Date start){startPoint = start;}	
	public Date getFinishPoint(){return finishPoint;}
	public void setFinishPoint(Date fp){finishPoint = fp;}
	public void setCuttoffPoint(Date cuttoff){cuttoffPoint = cuttoff;}
	public Date getCuttoffPoint(){return cuttoffPoint;}
	public Date getResumePoint(){return resumePoint;}
	public void setResumePoint(Date rp){resumePoint = rp;}

	@XmlElement
	public Optional<ExamSheet> getOptionalExamSheet(){return examSheet;}
	public void setExamSheet(ExamSheet examSheet){
		this.examSheet = Optional.of(examSheet);
	}
	public Date getLastUpdate(){return LastUpdate;}
	public void setLastUpdate(Date lastUpdate){LastUpdate = lastUpdate;}
	
	
	public boolean isREADY(){ return getStatus() == READY; }
	public boolean isSTARTED(){ return getStatus() == STARTED; }
	public boolean isCUTOFF(){ return getStatus() == CUTOFF; }
	public boolean isRESUMED(){ return getStatus() == RESUMED; }
	public boolean isREJECTED(){ return getStatus() == REJECTED; }
	public boolean isFINISHED(){ return getStatus() == FINISHED; }
	
	public Duration getTimeLeft(){
		if (getStatus() == STARTED || getStatus() == RESUMED){
			
			Duration examTime = new Duration (finishPoint.getTime() - startPoint.getTime());
			timeLeft = examTime.subtract( getTimeSpend() );
			return timeLeft;
		}
		return null;
	}

	public Duration getTimeSpend(){
		Duration t = null;
		if (getStatus() == STARTED || getStatus() == RESUMED)
		{
			long current = new Date().getTime() ;
			t = timeSpend.add( new Duration(current - resumePoint.getTime()) );
		}else
			t = timeSpend;
		return t;
	}
	
	/**
	 * timeSpend = timeSpend + current - resume
	 * this will should be executed after each cuttof and each resume.*/
	public void updateTimeSpend()
	{
		if (getStatus() == STARTED || getStatus() == RESUMED){
			
			timeSpend = getTimeSpend();
		}
	}

	public boolean isConnected()
	{
		if(serverLinker == null)
			return false;
		
		if(!serverLinker.getSocket().isClosed() && serverLinker.getSocket().isBound())
			connected = true;
		else
			connected = false;
		
		return connected;
	}

//	public void connectedNow(String status){
//			setStatus(status);
//	}
	
	public void runExam(Duration examTime){
		if(getStatus() == CUTOFF)
			resumeNow();
		else
			startNow(examTime); //READY OR NONE
	}
	
	
	private void startNow(Duration examTime)
	{
		setStartPoint(  new Date() );
		setResumePoint(startPoint);
		setFinishPoint( new Date(startPoint.getTime() + (long)examTime.toMillis()) );
		setStatus( Student.STARTED );		
	}

	private void resumeNow()
	{		
		setResumePoint(new Date());
		updateTimeSpend();
		setStatus( Student.RESUMED );
	}

	public void cutOffNow()
	{
		if(status.equals(FINISHED))			
			return;		

		setCuttoffPoint(new Date() );
		updateTimeSpend();
		setStatus( Student.CUTOFF );
	}

	@Override
	public String toString()
	{
		return getId() ;
	}

}
