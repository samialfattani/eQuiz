package frawla.equiz.util.exam;

import java.io.Serializable;
import java.util.Date;
import java.util.Optional;


import frawla.equiz.util.Channel;
import frawla.equiz.util.EQuizException;
import javafx.beans.property.SimpleStringProperty;
import javafx.util.Duration;
 
public class Student implements Serializable
{
	private static final long serialVersionUID = -2983386758431074593L;
	
	public static final String REJECTED = "Rejected";
	public static final String READY = "Ready";
	public static final String DISCONNECTED = "Disconnected";
	public static final String STARTED = "Started";
	public static final String CUTOFF = "Cut Off";
	public static final String RESUMED = "Resumed";
	public static final String FINISHED = "Finished";
	public static final String GRADED = "Graded";
	
	private Channel serverLinker;
	private String id = "";
	private String name= "";	
	private String status = "";
	private SimpleStringProperty statusProperty = new SimpleStringProperty("");
	private boolean connected = false;
	private Date startPoint;
	private Date cuttoffPoint;
	private Date resumePoint ;
	private Date finishPoint ;
	private Duration leftTime = Duration.ZERO;
	private Duration spendTime = Duration.ZERO;
	private Optional<ExamSheet> examSheet = Optional.empty();
	private Date LastUpdate = new Date();
	
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

	public Optional<ExamSheet> getOptionalExamSheet(){return examSheet;}
	public void setExamSheet(ExamSheet examSheet){
		this.examSheet = Optional.of(examSheet); 
	}
	public Date getLastUpdate(){return LastUpdate;}
	public void setLastUpdate(Date lastUpdate){LastUpdate = lastUpdate;}
	
	
	public boolean isREADY(){ return getStatus() == READY; }
	public boolean isDISCONNECTED(){ return getStatus() == DISCONNECTED; }
	public boolean isSTARTED(){ return getStatus() == STARTED; }
	public boolean isCUTOFF(){ return getStatus() == CUTOFF; }
	public boolean isRESUMED(){ return getStatus() == RESUMED; }
	public boolean isREJECTED(){ return getStatus() == REJECTED; }
	public boolean isFINISHED(){ return getStatus() == FINISHED; }
	public boolean isGRADED(){ return getStatus() == GRADED; }
	
	public String getStatus(){return status;}
	public void setStatus(String status){
		this.status = status;
		statusProperty.set(status);
	}

	public boolean isRunningHisExam() {
		return isSTARTED() || isRESUMED();
	}
	

	public void runExam(Duration examTime) throws EQuizException{
		if( isCUTOFF() )
			resumeNow();
		else if( isREADY() || isDISCONNECTED() )
			startNow(examTime); //READY
		else
			throw new EQuizException(getStatus()+ " status is not expected to run the Exam");
		
	}

	public SimpleStringProperty getStatusProperty() {return statusProperty;}

	/**
	 * TimeLeft = (Finish - Start) - spendTime
	 * @return
	 */
	public Duration getLeftTime()
	{
		if (isRunningHisExam()){
			
			Duration examTime = new Duration (finishPoint.getTime() - startPoint.getTime());
			leftTime = examTime.subtract( getSpendTime() );
			return leftTime;
		}
		return null;
	}

	/**
	 * if the exam is running (started or resumed), then it will return the 
	 * consumed time until now. otherwise, it returns last updated spendTime.
	 * @return the real consumed time of the exam.
	 */
	public Duration getSpendTime(){ 
		Duration t = null;
		if( isRunningHisExam() )
		{
			long current = new Date().getTime() ;
			t = spendTime.add( new Duration(current - resumePoint.getTime()) );
		}else
			t = spendTime;
		return t;
	}
	

	/**
	 * timeSpend = timeSpend + current - resume
	 * this should be executed after each cutoff and each resume.*/
	private void updateSpendTime()
	{
		spendTime = getSpendTime();
	}

	private void startNow(Duration examTime)
	{
		setStartPoint(  new Date() );
		setResumePoint(startPoint);
		setFinishPoint( new Date(startPoint.getTime() + (long)examTime.toMillis()) );
		setStatus( Student.STARTED );		
	}

	public void cutOffNow()
	{
		if ( isREADY() )
			setStatus(DISCONNECTED);
		//update spendTime while running and before cutoff
		else if( isRunningHisExam() ) 
		{
			updateSpendTime();
			setStatus( Student.CUTOFF );
			setCuttoffPoint(new Date() );			
		}
	}

	private void resumeNow()
	{		
		setResumePoint(new Date());
		setStatus( Student.RESUMED );
	}

	public void finishNow() 
	{
		//update spendTime while running and before finish
		updateSpendTime();
		setStatus( Student.FINISHED );
	}

	@Override
	public String toString(){
		return getId() ;
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

}//end class
