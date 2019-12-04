package guiHospital;

import java.awt.Color;
import java.awt.Component;
import java.util.HashMap;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public class FancyListSellection extends DefaultListCellRenderer {
	private HashMap theChosen = new HashMap();
	private HashMap theChosen2 = new HashMap();

	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		String content=(String)value;
		if (content.contains("CRITICAL")) {
			theChosen.put(value, "chosen");
		}
		if(content.contains("SYMPTOMS")) {
			theChosen2.put(value, "chosen2");
		}

		if (theChosen.containsKey(value)) {
			setForeground(Color.red);
		} 
		if(theChosen2.containsKey(value)) {
			setForeground(Color.yellow);
		}
		return (this);
	}
}
