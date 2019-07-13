package frawla.equiz.util;

public class EQuizException extends Exception 
{
	private static final long serialVersionUID = -4766573142400169144L;
	private String id;

	private String msg;

   public EQuizException(String msg) {
	  super(msg);
      this.id = "0";
      this.msg = msg;
   }
   public EQuizException(String msg, int id) {
	  super(msg);
      this.id = id+"";
      this.msg = msg;
   }
   public EQuizException(String msg, String id) {
	  super(msg);
      this.id = id;
      this.msg = msg;
   }

   public String toString() {
      return "EQuizException[" + id + "]: " + msg;
   }
   
	public String getId() {return id;}
	public void setId(String id) {this.id = id;}
   
}//end class