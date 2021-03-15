package de.tigges.tchreservation.mvc.table;

import lombok.Data;

@Data
public class TableCell {
	private String content;
	private String cssClass;
	private int rowspan;
	private int colspan;
}
