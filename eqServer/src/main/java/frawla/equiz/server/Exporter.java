package frawla.equiz.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;

import frawla.equiz.util.Util;
import frawla.equiz.util.exam.ExamConfig;
import frawla.equiz.util.exam.ExamSheet;
import frawla.equiz.util.exam.MultipleChoice;
import frawla.equiz.util.exam.Question;
import frawla.equiz.util.exam.Student;

public class Exporter
{
	private List<ExamSheet> examSheetList;
	private List<Student> studentList;

	public Exporter(){}
	public Exporter(Student st){
		examSheetList = new ArrayList<>();
		examSheetList.add(st.getOptionalExamSheet().get());
	}

	public Exporter(ExamSheet exsht){
		examSheetList = new ArrayList<>();
		examSheetList.add(exsht);
	}

	public Exporter(List<ExamSheet> alist){
		examSheetList = alist;
		
	}
	public void setStudentList(List<Student> alist){
		studentList = alist;
	}

	public void exportToPDF(File f, boolean WithCorrection) 
	{
		if(f.isDirectory())
			exportAllToPDF(f, WithCorrection);
		
		try{
			Document doc = new Document();
			PdfWriter.getInstance(doc, new FileOutputStream(f));

			doc.open();
			for(ExamSheet sht: examSheetList)
			{
				exportToPDF(null, sht, doc, WithCorrection);
				doc.newPage();
			}
			doc.close();
			Util.RunApplication(f);
		}
		catch (FileNotFoundException | DocumentException e){
			Util.showError(e, e.getMessage());
		}

	}

	public void exportAllToPDF(File selectedDir, boolean WithCorrection)
	{
		try{
			for(Student st: studentList)
			{
				ExamConfig econf = st.getOptionalExamSheet().get().getExamConfig();
				String prefex = econf.courseID + "-" + econf.courseTitle;
				String fileName  = prefex + "-" + st.getId() + "_" + st.getName() + ".pdf"; 
				Document doc = new Document();
				File f = new File(selectedDir.getAbsolutePath()+File.separator+fileName);
				
				PdfWriter.getInstance(doc, new FileOutputStream(f));

				doc.open();
				exportToPDF(st, st.getOptionalExamSheet().get(), doc, WithCorrection);
				
				doc.close();
			}
			Util.RunApplication(selectedDir);
		}
		catch (FileNotFoundException | DocumentException e){
			Util.showError(e, e.getMessage());
		}
	}

	private void exportToPDF(Student student, ExamSheet examSheet, Document doc, boolean WithCorrection) throws DocumentException 
	{
		String header = "" ;
		ExamConfig ec = examSheet.getExamConfig();
		header += "Course: " + ec.courseID + " - " + ec.courseName + ", " +
				"Sec(" + ec.courseSection + "), " +
				ec.courseYear + "/" + ec.courseSemester + " " + 
				" | " +  ec.courseTitle + " | " +"\n";


		if(student != null)
			header += "Student: " +  student.getId() + " - " + student.getName() + "\n\n";

		doc.add(new Paragraph( header ));


		examSheet.getQuestionList()
		.forEach( q -> { 
			if(WithCorrection)
				printQuestionWithCorrection(examSheet, doc , q);
			else
				printQuestion(examSheet, doc , q);
		});

		if(WithCorrection)
			totalPrintout(examSheet, doc);
	}


	private void totalPrintout(ExamSheet examSheet, Document doc)
	{
		try
		{
			double stMark = examSheet.getQuestionList()
								.stream().mapToDouble( q1 -> q1.getStudentMark())
								.sum();
			double totalMark = examSheet.getQuestionList()
					.stream().mapToDouble( q1 -> q1.getMark())
					.sum();
			
			String t = String.format("Total Marks: %.2f / %.2f", stMark , totalMark);  
			Font f = FontFactory.getFont(FontFactory.COURIER, 16, Font.BOLD, BaseColor.RED  );
			doc.add(new Paragraph(  t, f));
		}
		catch (DocumentException e)
		{
			Util.showError(e, e.getMessage());
		}

	}


	private void  printQuestion(ExamSheet examSheet, Document doc, Question q)
	{
		try
		{
			Paragraph prg =  new Paragraph("", FontFactory.getFont("Consolas"));

			prg.add( "(" +  q.getMark() + ") " + q.getText() + "\n" );

			if(!q.getImgFileName().equals("")){

				getImageOfThisQuestion(examSheet, doc, q);
			}

			if(q instanceof MultipleChoice)
			{
				MultipleChoice qmc = (MultipleChoice)q;
				for(String l : qmc.getOrderList()){
					prg.add( l + ")" + qmc.getChoices().get(l)+ "   " );
				}
				prg.add( "\n" );
			}

			prg.add("Answer: ");
			prg.setSpacingAfter(6);
			prg.setSpacingBefore(0);
			doc.add(prg );

		}
		catch (DocumentException | IOException e)
		{
			Util.showError(e, e.getMessage());
		}

	}

	private void  printQuestionWithCorrection(ExamSheet examSheet, Document doc, Question q)
	{
		try
		{
			Paragraph prg =  new Paragraph("", FontFactory.getFont("Consolas"));

			prg.add( "(" +  q.getMark() + ") " + q.getText() + "\n" );

			if(!q.getImgFileName().equals("")){
				getImageOfThisQuestion(examSheet, doc, q);
			}

			if(q instanceof MultipleChoice)
			{
				MultipleChoice qmc = (MultipleChoice)q;

				for(String l : qmc.getOrderList())
				{
					if(l.equals(qmc.getCorrectAnswer()) )
						prg.add(getCorrectChoicePhrase(qmc , l));
					else
						prg.add( "()" + qmc.getChoices().get(l)+ "   " ); //h2000
						//prg.add( l + ")" + qmc.getChoices().get(l)+ "   " );
				}

				prg.add( "\n" );
			}


			Phrase ph = new Phrase();
			Chunk ch = new Chunk("Answer: ", FontFactory.getFont(FontFactory.HELVETICA, 14, Font.BOLD, BaseColor.BLUE));
			ch.append(q.getStudentAnswerAsText()  + "   " );
			
			ph.add(ch);
			ch = getCorrectionMark(q);
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
	private void getImageOfThisQuestion(ExamSheet examSheet, Document doc, Question q)
	        throws BadElementException, MalformedURLException, IOException, DocumentException
	{
		String parent = examSheet.getExamConfig().SourceFile.getParent();
		Image img = Image.getInstance(  new File( parent, q.getImgFileName()).toString() );
		float ratio  = img.getWidth()/img.getHeight();
		img.scaleAbsolute( ratio*100, 100);
		doc.add(img);
	}

	private Chunk getCorrectionMark(Question q) throws DocumentException, IOException
	{
		Chunk ch;
		BaseFont bf = BaseFont.createFont(Util.getResource("fonts/FreeSans.ttf").toString(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
		Font fnt = new Font(bf, 12, Font.BOLD, BaseColor.RED);
		ch = new Chunk("", fnt);
		String correctionMark  = "";
		if (q.getStudentMark() == 0)
			correctionMark  = "X" + "   " + q.getTeacherNote();
		else if (q.getStudentMark() == q.getMark())
			correctionMark  = '\u221A' + "    "  + q.getStudentMark()  + "   " + q.getTeacherNote();
		else
			correctionMark = '\u221A' + "X   " + q.getStudentMark()  + "   " + q.getTeacherNote();
		
		ch.append(correctionMark );
		return ch;
	}


	private Phrase getCorrectChoicePhrase(MultipleChoice qmc, String l)
	{
		Phrase ph = new Phrase();
		Font fnt = new Font( FontFamily.HELVETICA , 12, Font.BOLD + Font.UNDERLINE);
		//Chunk ch = new Chunk( l + ")" + qmc.getChoices().get(l) , fnt);
		Chunk ch = new Chunk( "()" + qmc.getChoices().get(l) , fnt);
		ph.add(ch);
		ph.add("   ");
		return ph;
	}


}
