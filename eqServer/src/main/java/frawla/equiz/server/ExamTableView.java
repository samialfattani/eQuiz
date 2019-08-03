package frawla.equiz.server;

import java.util.Date;

import frawla.equiz.util.Util;
import frawla.equiz.util.exam.Student;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import javafx.util.Duration;

public class ExamTableView extends TableView<Student>
{

	private TableColumn<Student, String> colId = new TableColumn<>("ID");
	private TableColumn<Student, String> colName= new TableColumn<>("Name");
	private TableColumn<Student, Date> colStartPoint = new TableColumn<>("Start");	
	private TableColumn<Student, String> colStatus= new TableColumn<>("Status");
	private TableColumn<Student, Boolean> colConnected= new TableColumn<>("C");
	private TableColumn<Student, Duration> colTimeLeft = new TableColumn<>("Time Left");
	private TableColumn<Student, Date> colLastUpdate = new TableColumn<>("Last Backup");
	private TableColumn<Student, Double> colMarks = new TableColumn<>("Marks");

	private StyleChangingRowFactory<Student> rowFactory;
	//private ObservableList<Student> Students = FXCollections.observableArrayList();

	public ExamTableView()
	{
		this(null);
	}

	@SuppressWarnings("unchecked")
	public ExamTableView(ObservableList<Student> lst)
	{
		//Students = lst;
		setItems(lst);
		getStylesheets().add("mystyle.css");		
		rowFactory = new StyleChangingRowFactory<>("highlightedRow");
		setRowFactory(rowFactory);

		setFocusTraversable(false);
		setTableMenuButtonVisible(true);

		colConnected.setPrefWidth(40);	colConnected.setMinWidth(USE_PREF_SIZE);
		colId.setPrefWidth(75); 		colId.setMinWidth(USE_PREF_SIZE);		
		colName.setPrefWidth(150);		colName.setMinWidth(USE_PREF_SIZE);
		colStatus.setPrefWidth(70);			colStatus.setMinWidth(USE_PREF_SIZE);
		colStartPoint.setPrefWidth(100);	colStartPoint.setMinWidth(USE_PREF_SIZE);		
		colTimeLeft.setPrefWidth(100);		colTimeLeft.setMinWidth(USE_PREF_SIZE);
		colLastUpdate.setPrefWidth(100);	colLastUpdate.setMinWidth(USE_PREF_SIZE);
		colMarks.setPrefWidth(75);	colMarks.setMinWidth(USE_PREF_SIZE);
		
		colConnected.setSortable(false);
		colId.setSortable(true);		
		colName.setSortable(true);		
		colStatus.setSortable(true);
		colStartPoint.setSortable(true);
		colTimeLeft.setSortable(true);
		colLastUpdate.setSortable(true);
		colMarks.setSortable(true);

		colConnected.setCellValueFactory(new PropertyValueFactory<>("connected"));
		colId.setCellValueFactory(new PropertyValueFactory<>("id"));
		colName.setCellValueFactory(new PropertyValueFactory<>("name"));
		colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
		colStartPoint.setCellValueFactory(new PropertyValueFactory<>("startPoint"));
		colTimeLeft.setCellValueFactory(new PropertyValueFactory<>("leftTime"));
		colLastUpdate.setCellValueFactory(new PropertyValueFactory<>("lastUpdate"));
		colMarks.setCellValueFactory(new PropertyValueFactory<>("totalMarks"));
		
		//spectial values representation
		colConnected.setCellFactory( new IsConnectedCellFactory());
		colStartPoint.setCellFactory(new DateCellFactory());
		colTimeLeft.setCellFactory(new DurationCellFactory());
		colLastUpdate.setCellFactory(new DateCellFactory());

		getColumns().addAll(colConnected, colId, colName, colStatus, colMarks, colStartPoint, colTimeLeft, colLastUpdate);

	}//end constructor

	public void setStudentList(ObservableList<Student> ol)
	{
		setItems(ol);
	}


}//end class

class IsConnectedCellFactory implements Callback<TableColumn<Student,Boolean>, TableCell<Student,Boolean>>
{
	@Override
	public TableCell<Student, Boolean> call(TableColumn<Student, Boolean> param)
	{
		TableCell<Student, Boolean> c = new TableCell<Student, Boolean>()
		{
			@Override
			protected void updateItem(Boolean item, boolean empty)
			{
				BorderPane pnl = new BorderPane();
				ImageView imgView;

				if(item != null){
					if(item.booleanValue() == true)
						imgView = new ImageView(Util.getConnectedImage() );
					else
						imgView = new ImageView(Util.getDisconnectedImage() );

					imgView.setPreserveRatio(true);
					imgView.setFitWidth(20);
					
					pnl.getChildren().add(imgView);
					setGraphic(pnl);
				}
			}
		};
		return c;
	}
}	


class DateCellFactory implements Callback<TableColumn<Student,Date>, TableCell<Student,Date>>
{
	@Override
	public TableCell<Student, Date> call(TableColumn<Student, Date> param)
	{
		TableCell<Student, Date> c = new TableCell<Student, Date>()
		{
			@Override
			protected void updateItem(Date item, boolean empty)
			{
				if(item != null){
					long min = (new Date().getTime() - item.getTime())/1000/60;
					setText(min + " min. ago");
					setTooltip(new Tooltip(Util.MY_DATE_FORMAT.format(item)) );
				}
			}
		};		
		return c;
	}
}	

class DurationCellFactory implements Callback<TableColumn<Student,Duration>, TableCell<Student,Duration>>
{
	@Override
	public TableCell<Student,Duration> call(TableColumn<Student, Duration> param)
	{
		TableCell<Student, Duration> c = new TableCell<Student, Duration>()
		{
			@Override
			protected void updateItem(Duration item, boolean empty)
			{
				if(item != null)
				{					
					setText(Util.formatTime(item));
				}
			}
		};		
		return c;
	}
}	
