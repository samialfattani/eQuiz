package frawla.equiz.util.exam;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;

import frawla.equiz.util.Util;

public class Exporter
{
	Student student;
	public Exporter(Student st)
	{
		student = st;
	}


	public void exportToPDF(File f) throws FileNotFoundException, DocumentException 
	{

		Document doc = new Document();
		PdfWriter.getInstance(doc, new FileOutputStream(f));

		doc.open();
		ExamConfig ec = student.getOptionalExamSheet().get().getExamConfig();
		String header = "" ;
		header += "Course: " + ec.courseID + " - " + ec.courseName + ", " +
				"Section: " + ec.courseSection + ", " +
				ec.courseYear + "/" + ec.courseSemester + "\n";


		header += "Student: " +  student.getId() + " - " + student.getName() + "\n\n";
		doc.add(new Paragraph( header ));


		student.getOptionalExamSheet()
		.ifPresent( sht -> {

			sht.getQustionList()
			.forEach( q -> { QuesPrintout(doc , q); } );

			totalPrintout(doc);
		});

		//		FontFactory.registerDirectory("fonts" );
		//        Set<String> fonts = new TreeSet<String>(FontFactory.getRegisteredFonts());
		//        for (String fontname : fonts) {
		//            System.out.println(fontname);
		//        }

		doc.close();
		Util.RunApplication(f);
	}


	private void totalPrintout(Document doc)
	{
		try
		{
			String t = "Total Marks: " + 
					student.getOptionalExamSheet()
			.get()
			.getQustionList()
			.stream()
			.mapToDouble( q1 -> q1.getStudentMark())
			.sum();

			Font f = FontFactory.getFont(FontFactory.COURIER, 18, Font.BOLD, BaseColor.BLUE  );
			doc.add(new Paragraph(  t, f));
		}
		catch (DocumentException e)
		{
			Util.showError(e, e.getMessage());
		}

	}


	private void  QuesPrintout(Document doc, Question q)
	{
		try
		{
			Paragraph prg =  new Paragraph("", FontFactory.getFont("Consolas"));

			prg.add( "(" +  q.getMark() + ") " + q.getText() + "\n" );

			if(!q.getImgFileName().equals("")){

				String parent = student.getOptionalExamSheet().get().getExamConfig().SourceFile.getParent();
				Image img = Image.getInstance(  new File( parent, q.getImgFileName()).toString() );
				float ratio  = img.getWidth()/img.getHeight();
				img.scaleAbsolute( ratio*100, 100);
				doc.add(img);
			}

			if(q instanceof MultipleChoice)
			{
				MultipleChoice qmc = (MultipleChoice)q;

				for(String l : qmc.getOrderList())
					prg.add( l + ")" + qmc.getChoices().get(l)+ "   " );

				prg.add( "\n" );
			}


			Phrase ph = new Phrase();
			Chunk ch = new Chunk("Answer: ", FontFactory.getFont(FontFactory.HELVETICA, 14, Font.BOLD, BaseColor.BLUE));
			ch.append(q.studentAnswer  + "   " );
			ph.add(ch);
			BaseFont bf = BaseFont.createFont(Util.getResource("fonts/FreeSans.ttf").toString(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
			Font fnt = new Font(bf, 12, Font.BOLD, BaseColor.RED);
			ch = new Chunk("", fnt);
			ch.append((q.getStudentMark() == 0)? "X" : '\u221A'+"");
			ph.add(ch);

			prg.add(ph);
			prg.setSpacingAfter(6);
			prg.setSpacingBefore(0);
			doc.add(prg );

		}
		catch (DocumentException | IOException e)
		{
			Util.showError(e, e.getMessage());
		}

	}

}
