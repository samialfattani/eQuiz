package frawla.equiz.server;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import frawla.equiz.util.Util;
import frawla.equiz.util.exam.Student;


public class UtilServer extends Util
{
//	@Override
//	public static URI getResource(String s)
//	{
//		URI uri =null;
//		try{
//			uri = UtilServer.class.getClassLoader().getResource(s).toURI();
//		}catch (URISyntaxException e){
//			UtilServer.showError(e , e.getMessage());
//		}
//		return uri ;
//	}
//
//	@Override
//	public static InputStream getResourceAsStream(String s)
//	{		
//		return UtilServer.class.getClassLoader().getResourceAsStream(s);
//	}

	public static void jaxbStudentToXML(Student st, File f) {

        try {
            JAXBContext context = JAXBContext.newInstance(Student.class);
            Marshaller m = context.createMarshaller();
            //for pretty-print XML in JAXB
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            // Write to System.out for debugging
            // m.marshal(emp, System.out);

            // Write to File
            m.marshal(st, f);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

	public static Student jaxbXMLToStudent(File f) {
        try {
            JAXBContext context = JAXBContext.newInstance(Student.class);
            Unmarshaller un = context.createUnmarshaller();
            Student st = (Student) un.unmarshal(f);
            return st;
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return null;
    }

}
