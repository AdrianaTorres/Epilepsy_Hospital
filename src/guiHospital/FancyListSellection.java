package guiHospital;

import java.awt.Color;
import java.awt.Component;
import java.util.HashMap;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public class FancyListSellection extends DefaultListCellRenderer {
	private HashMap theChosen = new HashMap();

	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		System.out.println(value);
		String content=(String)value;
		if (content.contains("1")) {
			theChosen.put(value, "chosen");
			/*Allows setting up a priority system with codes, we can later mask*/
		}

		if (theChosen.containsKey(value)) {
			setForeground(Color.red);
		} else {
			setForeground(Color.black);
		}

		return (this);
	}
}
