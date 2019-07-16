package general;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import frawla.equiz.util.Util;

class JSONTest {

	@Test
	void test() throws IOException 
	{
		File dir = new File ( System.clearProperty("user.home")+"\\.equiz" );
		if(!dir.exists())
			dir.mkdir();
		
		File initFile = new File(dir + "\\init.json");
		if(initFile.exists())
			initFile.delete();
		
		Util.copyFile(Util.getResourceAsFile("init.json"), initFile);
		
		String json = Util.readFileAsString(initFile);
        JSONObject obj = new JSONObject(json);
        String str;
        str = obj.getString("hi");
        assertEquals("--HelloWorld--", str);
        
        str = obj.getString("openExam");
        assertEquals("", str);

        str = obj.getString("pdfFolder");
        assertEquals("", str);
        
        obj.putOpt("openExam", initFile.getParent());
        str = obj.getString("openExam");
        assertEquals(dir.toString(), str);
        
        Util.Save(obj.toString(2), initFile, false);
    }

}
