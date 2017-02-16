package frawla.equiz.util.exam;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

public class Exporter
{
	private Student student;
	private List<ExamSheet> examSheetList;

	public Exporter(Student st)
	{
		examSheetList = new ArrayList<>();
		student = st;
		examSheetList.add(student.getOptionalExamSheet().get());
	}

	public Exporter(ExamSheet exsht)
	{
		examSheetList = new ArrayList<>();
		examSheetList.add(exsht);
	}

	public Exporter(List<ExamSheet> examShtLst)
	{
		examSheetList = examShtLst;
	}

	public void exportToPDF(File f, boolean WithCorrection) 
	{
		try{
			Document doc = new Document();
			PdfWriter.getInstance(doc, new FileOutputStream(f));

			doc.open();
			examSheetList.stream()
			.forEach( sht -> {
				try{
					exportToPDF(sht, doc, WithCorrection);
					doc.newPage();
				}catch (DocumentException e){ Util.showError(e, e.getMessage()); }
			});

			doc.close();
			Util.RunApplication(f);
		}
		catch (FileNotFoundException | DocumentException e){
			Util.showError(e, e.getMessage());
		}

	}

	private void exportToPDF(ExamSheet examSheet, Document doc, boolean WithCorrection) throws DocumentException 
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
			String t = "Total Marks: " + examSheet
					.getQuestionList()
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


	private void  printQuestion(ExamSheet examSheet, Document doc, Question q)
	{
		try
		{
			Paragraph prg =  new Paragraph("", FontFactory.getFont("Consolas"));

			prg.add( "(" +  q.getMark() + ") " + q.getText() + "\n" );

			if(!q.getImgFileName().equals("")){

				String parent = examSheet.getExamConfig().SourceFile.getParent();
				Image img = Image.getInstance(  new File( parent, q.getImgFileName()).toString() );
				float ratio  = img.getWidth()/img.getHeight();
				img.scaleAbsolute( ratio*100, 100);
				doc.add(img);
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

				String parent = examSheet.getExamConfig().SourceFile.getParent();
				Image img = Image.getInstance(  new File( parent, q.getImgFileName()).toString() );
				float ratio  = img.getWidth()/img.getHeight();
				img.scaleAbsolute( ratio*100, 100);
				doc.add(img);
			}

			if(q instanceof MultipleChoice)
			{
				MultipleChoice qmc = (MultipleChoice)q;

				for(String l : qmc.getOrderList())
				{
					if(l.equals(qmc.getCorrectAnswer()) )
						prg.add(getCorrectChoicePhrase(qmc , l));
					else
						prg.add( l + ")" + qmc.getChoices().get(l)+ "   " );

				}

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


	private Phrase getCorrectChoicePhrase(MultipleChoice qmc, String l)
	{
		Phrase ph = new Phrase();
		Font fnt = new Font( FontFamily.HELVETICA , 12, Font.BOLD + Font.UNDERLINE);
		Chunk ch = new Chunk( l + ")" + qmc.getChoices().get(l) , fnt);
		ph.add(ch);
		ph.add("   ");
		return ph;
	}


}
