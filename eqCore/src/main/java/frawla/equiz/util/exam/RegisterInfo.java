package frawla.equiz.util.exam;

import java.io.Serializable;

public class RegisterInfo implements Serializable
{
	private static final long serialVersionUID = -533079684356075038L;
	public String ID = "";
	public String Name = "";

	public RegisterInfo(String studentID, String studentName)
	{
		ID = studentID;
		Name = studentName;
	}

	@Override
	public boolean equals(Object obj)
	{
		RegisterInfo o = (RegisterInfo)obj;
		return ID.equals(o.ID) && Name.equals(o.Name);
	}
	
	@Override
	public String toString(){
		return ID + ": " + Name;
	} 
}
