package de.tigges.tchreservation.mvc;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ReservationController {

	List<List<Row>> table;

	@GetMapping("/mvc/reservation/{systemConfigId}")
	public String showReservations(@PathVariable long systemConfigId, Model model) {
		createEmptyTable(10, 5);
		model.addAttribute("table", table);
		return "reservation";
	}

	private void createEmptyTable(int rows, int columns) {
		table = new ArrayList<>(rows);
		for (int row = 0; row < rows; row++) {
			List<Row> tableRow = new ArrayList<>();
			for (int col = 0; col < columns; col++) {
				tableRow.add(new Row("&nbsp;", "css", 1, 1));
			}
			table.add(tableRow);
		}
	}

	public static class Row {
		private int rowspan;
		private int colspan;
		private String content;
		private String cssClass;

		public Row() {
			this(1, 1);
		}

		public Row(int rowspan, int colspan) {
			this(null, null, rowspan, colspan);
		}

		public Row(String content, String cssClass) {
			this(content, cssClass, 1, 1);
		}

		public Row(String content, String cssClass, int rowspan, int colspan) {
			setRowspan(rowspan);
			setColspan(colspan);
			setContent(content);
			setCssClass(cssClass);
		}


		public int getRowspan() {
			return rowspan;
		}

		public void setRowspan(int rowspan) {
			this.rowspan = rowspan;
		}

		public int getColspan() {
			return colspan;
		}

		public void setColspan(int colspan) {
			this.colspan = colspan;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public String getCssClass() {
			return cssClass;
		}

		public void setCssClass(String cssClass) {
			this.cssClass = cssClass;
		}
	}
}
