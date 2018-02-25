package com.github.hexeditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

/**
 * A GNT4 module for text display. Given one or more pointers, displays the associated text for each pointer
 * to the user.
 */
public class GNT4TextDisplay extends GNT4Module
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
		
		// Get list of text pointers
		List<Integer> textPointers = new ArrayList<Integer>();
		byte[] fileBytes = BinUtil.getFileBytes(editor);
		for (int i = pointerTableStart; i <= pointerTableEnd; i += 4)
		{
			textPointers.add(BinUtil.getUint32Big(fileBytes, i));
		}
		
		// For each text pointer, get the text and display it to the user
		Map<Integer, String> pointersToText = new HashMap<Integer, String>();
		for (int i = 0; i < textPointers.size(); i++)
		{
			int pointer = textPointers.get(i);
			String sjisText = BinUtil.readShiftJisText(fileBytes, pointer);
			pointersToText.put(pointer, sjisText);
		}
		displayText(pointersToText);
	}
	
	/**
	 * Creates a window to display text for each pointer.
	 * @param pointersToText a map of text for each pointer
	 */
	private void displayText(final Map<Integer, String> pointersToText)
	{
        JEditorPane jEditorPane = new JEditorPane();
        jEditorPane.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(jEditorPane);
        
        HTMLEditorKit kit = new HTMLEditorKit();
        jEditorPane.setEditorKit(kit);
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("body {color:#000; font-family:times; margin: 4px; }");
        styleSheet.addRule("h1 {color: blue;}");
        styleSheet.addRule("h2 {color: #ff0000;}");

        String html = "<html><body>";
        html += "<table style=\"width:100%\"><tr><th>Base 10 Pointer</th><th>Base 16 Pointer</th><th>Text</th></tr>";
        html = addPointerHTMLRow(html, pointersToText);
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
        j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        j.setSize(new Dimension(800,600));
        j.setLocationRelativeTo(null);
        j.setVisible(true);
	}
	
	private void saveAsCsv(Map<Integer, String> pointersToText)
	{
		try
		{
			final JFileChooser fc = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV files", "csv");
			fc.setFileFilter(filter);
			fc.setSelectedFile(new File("shiftjis_text.csv"));
			int returnVal = fc.showOpenDialog(editor);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				PrintWriter pw = new PrintWriter(file);
		        StringBuilder sb = new StringBuilder();
		        sb.append("Base 10 Pointer");
		        sb.append(',');
		        sb.append("Base 16 Pointer");
		        sb.append(',');
		        sb.append("Text");
		        sb.append('\n');
				for (Map.Entry<Integer, String> entry : pointersToText.entrySet()) {
					int pointer = entry.getKey();
					String text = new String( entry.getValue().getBytes("utf-8") );
			        sb.append(pointer);
			        sb.append(',');
			        sb.append(String.format("0x%08X", pointer));
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
	 * 
	 * @param html
	 * @param pointer
	 * @param text
	 * @return
	 */
	private String addPointerHTMLRow(String html, Map<Integer, String> pointersToText)
	{
		for (Map.Entry<Integer, String> entry : pointersToText.entrySet()) {
			int pointer = entry.getKey();
			String text = entry.getValue();
			html += "<tr><th>";
			html += pointer;
			html += "</th><th>";
			html += String.format("0x%08X", pointer);
			html += "</th><th>";
	        html += text.replace("↓", "<br>");
	        html += "</th></tr>";
		}
        return html;
	}
}
