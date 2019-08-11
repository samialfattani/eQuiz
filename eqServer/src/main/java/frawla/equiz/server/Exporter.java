package frawla.equiz.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import com.itextpdf.text.*;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.pdf.*;

import frawla.equiz.util.Util;
import frawla.equiz.util.exam.ExamConfig;
import frawla.equiz.util.exam.ExamSheet;
import frawla.equiz.util.exam.MultipleChoice;
import frawla.equiz.util.exam.Question;
import frawla.equiz.util.exam.Student;

public class Exporter
{

	//export all Exam Sheet into single PDF File
	public void GeneratePDF(List<ExamSheet> examSheetList, File f, boolean WithCorrection) 
	{
		try{
			Document doc = new Document();
			PdfWriter.getInstance(doc, new FileOutputStream(f));

			doc.open();
			for(ExamSheet sht: examSheetList)
			{
				writeBody(sht, WithCorrection, doc);
				doc.newPage();
			}
			doc.close();
			Util.RunApplication(f);
		}
		catch (FileNotFoundException | DocumentException e){
			Util.showError(e, e.getMessage());
		}

	}

	public void exportToPDF(List<Student> studentList, File selectedDir, boolean WithCorrection) throws DocumentException, FileNotFoundException
	{
		for(Student st: studentList)
		{
			ExamConfig econf = st.getOptionalExamSheet().get().getExamConfig();
			String prefex = econf.courseID + "-" + econf.courseTitle;
			String fileName  = prefex + "-" + st.getId() + "_" + st.getName() + ".pdf"; 
			
			File f = new File(selectedDir.getAbsolutePath()+File.separator+fileName);
			
			exportToPDF(st, f, WithCorrection);
		}
		Util.RunApplication(selectedDir);
	}

	public void exportToPDF(Student student, File f, boolean WithCorrection) throws DocumentException, FileNotFoundException 
	{
		
		Document doc = new Document();				
		PdfWriter.getInstance(doc, new FileOutputStream(f));
		doc.open();

		String header = "Student: " +  
				student.getId().toUpperCase() + " - " + 
				student.getName() + "\n\n";
		
		Paragraph p = new Paragraph();
		p.setFont(new Font(Font.FontFamily.COURIER, 16));
		p.add(header);
		
		doc.add(p);
		ExamSheet examSheet = student.getOptionalExamSheet().get();

		writeBody(examSheet , WithCorrection, doc);
		
		doc.close();
	}

	public void writeBody(ExamSheet examSheet, boolean WithCorrection, Document doc) throws DocumentException
	{
		ExamConfig ec = examSheet.getExamConfig();
		
		String header = "" ;
		header += String.format("-- %s -- \n", ec.courseTitle );
		header += String.format("Course: %s - %s,  Sec(%s), %s/%s\n", 
						ec.courseID, ec.courseName , ec.courseSection ,
						ec.courseYear, ec.courseSemester);
		
		//header += String.format("-- %s -- \n", ec.courseTitle );

		header += "\n\n";
		
		Paragraph p = new Paragraph( header );
		p.setFont( new Font(Font.FontFamily.COURIER, 16));
		doc.add(p);

		examSheet.getQuestionList()
		.forEach( q -> { 
			if(WithCorrection)
				printQuestionWithCorrection(examSheet, doc , q);
			else
				printQuestion(examSheet, doc , q);
		});

		if(WithCorrection)
			totalPrintout(examSheet, doc);
		
		String d = Util.MY_DATE_FORMAT.format(new Date());
		doc.add(new Paragraph( "issue date: " +  d));
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
			
			String t = String.format("Total Marks: %s / %s", 
					Util.MARK_FORMATTER.format(stMark) , 
					Util.MARK_FORMATTER.format(totalMark) );  
			
			Font f = FontFactory.getFont(FontFactory.COURIER, 16, Font.BOLD, BaseColor.RED  );
			doc.add( new Paragraph( t, f ) );
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

			prg.add( String.format( "Q%d. ( %s ) %s\n",
						q.getId(), 
						Util.MARK_FORMATTER.format(q.getMark()) ,
						q.getText() )
					);

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

			prg.add( String.format( "Q%d. ( %s ) %s\n",
					q.getId(), 
					Util.MARK_FORMATTER.format(q.getMark()) ,
					q.getText() )
				);

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
						prg.add( "* " + qmc.getChoices().get(l)+ "   " ); //h2000
						//prg.add( l + ")" + qmc.getChoices().get(l)+ "   " );
				}

				prg.add( "\n" );
			}


			Phrase ph = new Phrase();
			Chunk ch = new Chunk("Answer: ", FontFactory.getFont(FontFactory.HELVETICA, 14, Font.BOLD, BaseColor.BLUE));
			ch.append(q.getStudentAnswerAsText() + "   " );
			
			ph.add(ch);
			ch = getCorrectionImage(q);
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

	private Chunk getCorrectionImage(Question q) throws DocumentException, IOException
	{
		double m = q.getStudentMark();
		Image img;
		URL logoPath;
		
		if (m == q.getMark()) {
			logoPath = Util.getResourceAsURL("images/correct.png");			
			img = Image.getInstance(logoPath);			
		}else if (m == 0) {
			logoPath = Util.getResourceAsURL("images/wrong.png");			
			img = Image.getInstance(logoPath);
		}else {
			logoPath = Util.getResourceAsURL("images/half-correct.png");			
			img = Image.getInstance(logoPath);
		}
		
		img.scaleAbsolute(16f, 16f);
		Chunk chImg = new Chunk(img, 0, 0, true);
		
		return chImg;
	}
	
	private Chunk getCorrectionMark(Question q) throws DocumentException, IOException
	{
		BaseFont bf = BaseFont.createFont(Util.getResourceAsURI("fonts/FreeSans.ttf").toString(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
		Font fnt = new Font(bf, 12, Font.BOLD, BaseColor.RED);
		Chunk ch = new Chunk("", fnt);
		
		String mark = Util.MARK_FORMATTER.format( q.getStudentMark() );
		
		String  correctionMark  = mark + "   " + q.getTeacherNote();
		ch.append( correctionMark  );
		
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
