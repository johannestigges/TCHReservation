package de.tigges.tchreservation.mvc.table;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Getter
@Log4j2
public class TableData {
	private int tableRows;
	private int tableColumns;
	private List<List<TableCell>> table;

	public TableData() {
		clearTable();
	}

	public void clearTable() {
		tableRows = 0;
		tableColumns = 0;
		table = new ArrayList<>();
	}

	public void setCell(int row, int column) {
		setCell(row, column, 1, 1);
	}

	public void setCell(int row, int column, int rowspan, int colspan) {
		log.debug("setCell({},{}) span({},{})", row, column, rowspan, colspan);
		while (row >= tableRows) {
			newRow();
		}
		while (column >= tableColumns) {
			newColumn();
		}
		setRowspan(row, column, rowspan, colspan);
		setColspan(row, column, rowspan, colspan);
	}

	public void setData(int row, int column, String content, String cssClass) {
		TableCell cell = getCell(row, column);
		cell.setContent(content);
		cell.setCssClass(cssClass);
	}

	public TableCell getCell(int row, int column) {
		return table.get(row).get(column);
	}

	private void setColspan(int row, int column, int rowspan, int colspan) {
		getCell(row, column).setColspan(colspan);
		for (int i = 1; i < colspan; i++) {
			if (column + i >= tableColumns) {
				newColumn();
			}
			getCell(row, column + i).setColspan(0);
		}
	}

	private void setRowspan(int row, int column, int rowspan, int colspan) {
		getCell(row, column).setRowspan(rowspan);
		for (int i = 1; i < rowspan; i++) {
			if (row + i >= tableRows) {
				newRow();
			}
			getCell(row + i, column).setRowspan(0);
		}
	}

	private void newColumn() {
		log.debug("new column {}", tableColumns);
		for (int r = 0; r < tableRows; r++) {
			table.get(r).add(new TableCell());
		}
		tableColumns++;
	}

	private void newRow() {
		log.debug("new row {}", tableRows);
		List<TableCell> newRow = new ArrayList<>();
		for (int c = 0; c < tableColumns; c++) {
			newRow.add(new TableCell());
		}
		table.add(newRow);
		tableRows++;
	}
}
