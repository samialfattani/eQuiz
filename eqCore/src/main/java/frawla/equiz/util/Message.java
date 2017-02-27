package frawla.equiz.util;

import java.io.Serializable;


public class Message<E> implements Serializable
{
	private static final long serialVersionUID = 1405995340835757508L;
	private String code = "";
	private E data;
	

	public Message(String code){
		this.code = code;
	}
	public Message(String code, E data){
		this.code = code;
		this.data = data;
	}
	
	public String getCode(){return code;}
	public void setCode(String code){this.code = code;}
	
	public E getData(){return data;}
	public void setData(E data){this.data = data;}
	 
	@Override
	public String toString()
	{
		if(data == null)
			return "NULL";
		return code + " - " + data.toString();
	}


	//server messages
	public static final String SERVER_LOG = "50";
	public static final String WELCOME_FROM_SERVER = "51";
	public static final String SERVER_IP = "52";
	public static final String SERVER_HOST_NAME = "53";
	public static final String NEW_CLIENT_HAS_BEEN_CONNECTED = "54";
	public static final String EXAM_OBJECT = "55";
	public static final String TIME_LEFT = "56";
	public static final String REGESTER_INFO = "57";
	public static final String IMAGES_LIST = "58";
	public static final String SERVER_CLOSED = "60";
	public static final String YOU_HAVE_ALREADY_FINISHED = "71";
	public static final String YOU_ARE_ALREADY_CONNECTED = "72";
	public static final String YOU_ARE_REJECTED = "73";
	public static final String FINAL_COPY_IS_RECIEVED = "80";
	public static final String GIVE_ME_A_BACKUP_NOW = "81";
	public static final String KHALAS_TIMES_UP = "90";
	public static final String STOP_RECIEVING_MESSAGES = "91";

	//client messages
	public static final String STUDENT_START_TIME = "03"; 
	public static final String STUDENT_CUTOFF = "20"; 	 	//in unexpected cases such as errors.
	public static final String STUDENT_DISCONNECT = "21";	//he close it normally.
	public static final String IAM_READY_TO_GET_EXAM_OBJECT = "22";
	public static final String EXAM_OBJECT_RECIVED_SUCCESSFYLLY = "23";
	public static final String IMAGE_LIST_RECIVED_SUCCESSFYLLY = "25";
	public static final String BACKUP_COPY_OF_EXAM_WITH_ANSWERS = "30";
	public static final String FINAL_COPY_OF_EXAM_WITH_ANSWERS = "31";
	public static final String I_HAVE_FINISHED = "48";
	public static final String PLAIN_TEXT_FROM_CLIENT = "49";


}
