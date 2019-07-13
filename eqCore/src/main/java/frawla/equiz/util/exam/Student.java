package frawla.equiz.util.exam;

import java.io.Serializable;
import java.util.Optional;

import frawla.equiz.util.Channel;
import frawla.equiz.util.EQDate;
import frawla.equiz.util.EQuizException;
import javafx.beans.property.SimpleStringProperty;
import javafx.util.Duration;
 
public class Student implements Serializable
{
	private static final long serialVersionUID = -2983386758431074593L;
	
	
	public static final String NONE = "None";
	public static final String READY = "Ready";
	public static final String DISCONNECTED = "Disconnected";
	public static final String REJECTED = "Rejected";
	public static final String STARTED = "Started";
	public static final String CUTOFF = "Cut Off";
	public static final String RESUMED = "Resumed";
	public static final String FINISHED = "Finished";
	public static final String GRADED = "Graded";
	
	private Channel serverLinker;
	private String id = "";
	private String name= "";	
	private String status = NONE;
	private SimpleStringProperty statusProperty = new SimpleStringProperty("");
	private boolean connected = false;
	private EQDate startPoint;
	private EQDate cuttoffPoint;
	private EQDate resumePoint ;
	private EQDate finishPoint ;
	private Duration leftTime = Duration.ZERO;
	private Duration spendTime = Duration.ZERO;
	private Optional<ExamSheet> examSheet = Optional.empty();
	private EQDate LastUpdate = new EQDate();
	
	//IMPORTANT for Mockito.spys
	public Student(){ }
	
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
	public EQDate getStartPoint(){return startPoint;}
	public void setStartPoint(EQDate start){startPoint = start;}	
	public EQDate getFinishPoint(){return finishPoint;}
	public void setFinishPoint(EQDate fp){finishPoint = fp;}
	public void setCuttoffPoint(EQDate cuttoff){cuttoffPoint = cuttoff;}
	public EQDate getCuttoffPoint(){return cuttoffPoint;}
	public EQDate getResumePoint(){return resumePoint;}
	public void setResumePoint(EQDate rp){resumePoint = rp;}

	public Optional<ExamSheet> getOptionalExamSheet(){return examSheet;}
	public void setExamSheet(ExamSheet examSheet){
		this.examSheet = Optional.of(examSheet); 
	}
	public EQDate getLastUpdate(){return LastUpdate;}
	public void setLastUpdate(EQDate lastUpdate){LastUpdate = lastUpdate;}
	
	
	public boolean isNONE(){ return getStatus() == NONE; }
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
			throw new EQuizException(getStatus() + " status is not expected to run the Exam");
		
	}

	public SimpleStringProperty getStatusProperty() {return statusProperty;}

	/**
	 * TimeLeft = examTime - spendTime
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
		Duration t1 = Duration.ZERO;
		Duration t2 = Duration.ZERO;
		
		if( isRunningHisExam() )
		{
			long now = new EQDate().getTime() ;
			t1 = spendTime.add( new Duration(now - resumePoint.getTime()) );
		}else
			t1 = spendTime;
		
		t2 = this.calculateSpendTime();
		
		double t = Math.max(t1.toMillis(), t2.toMillis());
		return new Duration(t);
	}
	

	/**
	 * timeSpend = timeSpend + current - resume
	 * this should be executed after each cutoff and each finish.*/
	private void updateSpendTime(){
		spendTime = getSpendTime();
	}

	private Duration calculateSpendTime() 
	{
	    Duration sum = Duration.ZERO;
	    
	    if (!this.getOptionalExamSheet().isPresent())
	    	return sum;
	    
	    for (Question q : this.getOptionalExamSheet().get().getQuestionList() ) {
	        sum = sum.add( q.getConsumedTime() );
	    }

	    return sum;
	}

	private void startNow(Duration examTime)
	{
		setStartPoint(  new EQDate() );
		setResumePoint( new EQDate() );
		setFinishPoint( startPoint.plus(examTime) );
		setStatus( Student.STARTED );		
	}

	public void cutOffNow()
	{
		if ( isREADY() ) {
			setStatus(DISCONNECTED);
		}
		//update spendTime while running and before cutoff
		else if( isRunningHisExam() ) 
		{
			updateSpendTime();
			setStatus( Student.CUTOFF );
			setCuttoffPoint(new EQDate() );			
		}
	}

	private void resumeNow()
	{		
		setResumePoint(new EQDate());
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

	public void reset(Duration examTime) 
	{
		//here is to estimate starting time.
		Duration FullAnsweringTime = getSpendTime();
		
		EQDate now = new EQDate();
		setStartPoint(  now.minus( FullAnsweringTime )  );
		setResumePoint( now.minus( FullAnsweringTime )  );
		setFinishPoint( getStartPoint().plus( examTime ) );
	}

	public void UnFinishNow() 
	{
		if( isRunningHisExam() )
			setStatus(Student.CUTOFF);
		else
			setStatus(Student.DISCONNECTED);
	}

	public void UnGradeNow(){
		setStatus(FINISHED);
	}



//	public void connectedNow(String status){
//			setStatus(status);
//	}

}//end class
