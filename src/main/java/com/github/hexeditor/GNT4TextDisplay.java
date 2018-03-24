package com.github.hexeditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import javafx.util.Pair;

/**
 * A GNT module for text display. Given one or more pointers, displays the associated text for each pointer
 * to the user.
 */
public class GNT4TextDisplay extends GNTModule
{
	/**
	 * Constructor for GNT4TextDisplay
	 * @param editor the hex editor window
	 */
	public GNT4TextDisplay(BinEdit editor)
	{
		super(editor);
	}

	/**
	 * Opens a dialog that allows you to display text for Naruto GNT4 seq files.
	 */
	public void displayTextGNT4()
	{
		// Get pointer info from the user
		int pointerTableStart = getPointerTableStart();
		if (pointerTableStart == -1)
		{
			return;
		}
		int pointerTableEnd = getPointerTableEnd();
		if (pointerTableEnd == -1)
		{
			return;
		}
		if (((pointerTableEnd - pointerTableStart) + 1) % 4 != 0)
		{
			JOptionPane.showMessageDialog(editor, "Please make sure your pointers are in increments of four (inclusive).");
			return;
		}
		
		// Get origin pointers and their associated text pointers
		List<Integer> originPointers = new ArrayList<Integer>();
		List<Integer> textPointers = new ArrayList<Integer>();
		byte[] fileBytes = BinUtil.getFileBytes(editor);
		for (int originPointer = pointerTableStart; originPointer <= pointerTableEnd; originPointer += 4)
		{
			originPointers.add(originPointer);
			textPointers.add(BinUtil.getUint32Big(fileBytes, originPointer));
		}
		
		// This maps the origin pointer to a pair containing the text pointer it links to along with its associated string
		Map<Integer, Pair<Integer, String>> pointersToText = new TreeMap<Integer, Pair<Integer, String>>();
		for (int i = 0; i < textPointers.size(); i++)
		{
			int originPointer = originPointers.get(i);
			int textPointer = textPointers.get(i);
			String sjisText = BinUtil.readShiftJisText(fileBytes, textPointer);
			Pair<Integer, String> textPair = new Pair<Integer, String>(textPointer, sjisText);
			pointersToText.put(originPointer, textPair);
		}
		displayText(pointersToText);
	}
	
	/**
	 * Creates a window to display text for each pointer.
	 * @param pointersToText map of the origin pointer to a pair containing the text pointer it links to along with its associated string
	 */
	private void displayText(final Map<Integer, Pair<Integer, String>> pointersToText)
	{
        JEditorPane jEditorPane = new JEditorPane();
        jEditorPane.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(jEditorPane);
        
        HTMLEditorKit kit = new HTMLEditorKit();
        jEditorPane.setEditorKit(kit);
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("body {color:#000; font-family:times; margin: 4px; }");

        String html = "<html><body>";
        html += "<table style=\"width:100%\" border=\"1\"><tr><th>Origin Pointer (Dec)</th><th>Origin Pointer (Hex)</th>"
        		+ "<th>Text Pointer (Dec)</th><th>Text Pointer (Hex)</th><th>Text</th></tr>";
        html += createPointerHTMLRow(pointersToText);
        html += "</table></body></html>";
                          
        Document doc = kit.createDefaultDocument();
        
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() { 
        	  public void actionPerformed(ActionEvent e) { 
        	    saveAsCsv(pointersToText);
        	  } 
        	} );
        
        jEditorPane.setDocument(doc);
        jEditorPane.setText(html);
        JFrame j = new JFrame("GNT4 Text Display");
        j.getContentPane().add(scrollPane, BorderLayout.CENTER);
        j.getContentPane().add(saveButton, BorderLayout.SOUTH);
        j.setSize(new Dimension(800,600));
        j.setLocationRelativeTo(null);
        j.setVisible(true);
	}
	
	/**
	 * Saves a CSV file for each pointer and its associated text
	 * @param pointersToText the pointer to text map
	 */
	private void saveAsCsv(Map<Integer, Pair<Integer, String>> pointersToText)
	{
		try
		{
			final JFileChooser fc = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV files", "csv");
			fc.setFileFilter(filter);
			fc.setSelectedFile(new File("shiftjis_text.csv"));
			int returnVal = fc.showSaveDialog(editor);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				PrintWriter pw = new PrintWriter(file);
		        StringBuilder sb = new StringBuilder();
		        sb.append("Origin Pointer (Dec)");
		        sb.append(',');
		        sb.append("Origin Pointer (Hex)");
		        sb.append(',');
		        sb.append("Text Pointer (Dec)");
		        sb.append(',');
		        sb.append("Text Pointer (Hex)");
		        sb.append(',');
		        sb.append("Text");
		        sb.append('\n');
				for (Entry<Integer, Pair<Integer, String>> entry : pointersToText.entrySet()) {
					int originPointer = entry.getKey();
					int textPointer = entry.getValue().getKey();
					String text = new String(entry.getValue().getValue().getBytes("utf-8"));
			        sb.append(originPointer);
			        sb.append(',');
			        sb.append(String.format("0x%08X", originPointer));
			        sb.append(',');
			        sb.append(textPointer);
			        sb.append(',');
			        sb.append(String.format("0x%08X", textPointer));
			        sb.append(',');
			        sb.append(text);
			        sb.append('\n');
				}
		        pw.write(sb.toString());
		        pw.close();
			}
		} catch (Exception e)
		{
			JOptionPane.showMessageDialog(editor, "An error occurred when attempting to save.\n" + e.getLocalizedMessage());
		}
	}

	/**
	 * Creates HTML rows for each origin pointer, containing the origin pointer, text pointer,
	 * and associated text. 
	 * @param pointersToText the pointer to text map
	 * @return HTML rows for the pointer to text map
	 */
	private String createPointerHTMLRow(Map<Integer, Pair<Integer, String>> pointersToText)
	{
		String html = "";
		for (Entry<Integer, Pair<Integer, String>> entry : pointersToText.entrySet()) {
			int originPointer = entry.getKey();
			int textPointer = entry.getValue().getKey();
			String text = entry.getValue().getValue();
			html += "<tr><th>";
			html += originPointer;
			html += "</th><th>";
			html += String.format("0x%08X", originPointer);
			html += "</th><th>";
			html += textPointer;
			html += "</th><th>";
			html += String.format("0x%08X", textPointer);
			html += "</th><th>";
	        html += text.replace("↓", "<br>");
	        html += "</th></tr>";
		}
        return html;
	}
}
