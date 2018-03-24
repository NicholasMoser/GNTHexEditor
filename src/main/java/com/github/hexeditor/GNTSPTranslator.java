package com.github.hexeditor;

import java.awt.Font;
import java.awt.GridLayout;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A GNT module for text translation. Given one or more pointers, allows the user to translate the text associated.
 */
public class GNTSPTranslator extends GNTModule
{
	Charset cs;
	
	public enum TranslateOption
	{
		REPLACE("Replace"),
		APPEND("Append"),
		SKIP("Skip"),
		UNKNOWN("Unknown");
		
		private final String text;
		
		TranslateOption(final String text)
		{
			this.text = text;
		}
		
		@Override
		public String toString()
		{
			return text;
		}
	}
	
	/**
	 * Constructor for GNTSPTranslator
	 * @param editor the hex editor window
	 */
	public GNTSPTranslator(BinEdit editor)
	{
		super(editor);
		cs = Charset.forName("UTF-16BE");
	}
	
	/**
	 * Opens a dialog that allows you to translate text for Naruto GNTSP seq files.
	 */
	public void translateGNTSP()
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
		int appendPosition = fileBytes.length;
		for (int originPointer = pointerTableStart; originPointer <= pointerTableEnd; originPointer += 4)
		{
			originPointers.add(originPointer);
			textPointers.add(BinUtil.getUint32Big(fileBytes, originPointer));
		}
		
		// For each text pointer, prompt the user to translate the text at its location
		for (int i = 0; i < textPointers.size(); i++)
		{
			int originPointer = originPointers.get(i);
			int textPointer = textPointers.get(i);
			String utf16Text = BinUtil.readUtf16Text(fileBytes, textPointer);
			TextEdit textEdit = getTranslatedText(utf16Text, i + 1, textPointers.size());
			if (textEdit == null)
			{
				editor.goTo(Integer.toString(textPointers.get(0)));
				return;
			}
			byte[] translatedBytes = textEdit.getTextBytes();
			TranslateOption translateOption = textEdit.getTranslateOption();
			
			if (translateOption == TranslateOption.REPLACE)
			{
				BinUtil.modifyEditor(editor, translatedBytes, textPointer);
			}
			else if (translateOption == TranslateOption.APPEND)
			{
				BinUtil.modifyEditor(editor, translatedBytes, appendPosition);
				byte[] appendPositionBytes = ByteBuffer.allocate(4).putInt(appendPosition).array();
				BinUtil.modifyEditor(editor, appendPositionBytes, originPointer);
				appendPosition += translatedBytes.length;
			}
		}
		
		editor.goTo(Integer.toString(textPointers.get(0)));
	}

	/**
	 * Asks the user for the text to translate the utf-16 text with.
	 * Returns the utf-16 bytes for the translated string or null if exit.
	 * 
	 * @param utf16Text the utf-16 text to be translated
	 * @param currentPointer the current pointer for display purposes
	 * @param numPointers the total number of pointers
	 * @return the translated text
	 */
	private TextEdit getTranslatedText(String utf16Text, int currentPointer, int numPointers)
	{
		boolean validValue = false;
		byte[] utf16Bytes = null;
		TranslateOption translateOption = TranslateOption.UNKNOWN;
		while(!validValue)
		{
			JPanel textPanel = new JPanel(new GridLayout(4,1));
			String messageLine1 = "Please edit the following UTF-16 text. Newlines are two characters.";
			JLabel messageLabel1 = new JLabel(messageLine1);
			messageLabel1.setFont(messageLabel1.getFont().deriveFont(Font.PLAIN, 16));
			String messageLine2 = "You have " + utf16Text.length() + " characters you can use for replacement.";
			JLabel messageLabel2 = new JLabel(messageLine2);
			messageLabel2.setFont(messageLabel2.getFont().deriveFont(Font.PLAIN, 16));
			String messageLine3 = "You are at pointer " + currentPointer + " of " + numPointers;
			JLabel messageLabel3 = new JLabel(messageLine3);
			messageLabel3.setFont(messageLabel3.getFont().deriveFont(Font.PLAIN, 16));
			textPanel.add(messageLabel1);
			textPanel.add(messageLabel2);
			textPanel.add(messageLabel3);
			String title = "Translate Shift-Jis Text";
	        JTextField editableField = new JTextField(utf16Text);
	        editableField.setMaximumSize( editableField.getPreferredSize() );
	        editableField.setFont(editableField.getFont().deriveFont(Font.PLAIN, 24));
			textPanel.add(editableField);
	        String[] options = {TranslateOption.REPLACE.toString(), TranslateOption.APPEND.toString(), TranslateOption.SKIP.toString(), "Exit"};
	        int inputValue = JOptionPane.showOptionDialog(editor, textPanel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, 0);	
			switch(inputValue)
			{
			case 0:
				translateOption = TranslateOption.REPLACE;
				break;
			case 1:
				translateOption = TranslateOption.APPEND;
				break;
			case 2:
				return new TextEdit(new byte[0], TranslateOption.SKIP);
			case 3:
			case -1:
				return null;
			}
			// Need to use \\b as word boundaries for regex
			String inputText = editableField.getText().replaceAll(Pattern.quote("[LINE]"), "\r\n").replaceAll(Pattern.quote("→↓"), "\r\n").replaceAll(Pattern.quote("[END]"), "");
			if (translateOption != TranslateOption.REPLACE || inputText.length() == utf16Text.length())
			{
				utf16Bytes = inputText.getBytes(cs);
				// We need to add 0x0000 to the end to terminate the String.
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				try
				{
					output.write(utf16Bytes);
					output.write(0x00);
					output.write(0x00);
					utf16Bytes = output.toByteArray();
					validValue = true;
				}
				catch (IOException e)
				{
					JOptionPane.showMessageDialog(editor, "Please try again; IOException: " + e.getMessage());
				}
			}
			else
			{
				JOptionPane.showMessageDialog(editor, "For replacement, make sure you match the same number of characters.");
			}
		}
		return new TextEdit(utf16Bytes, translateOption);
	}
	
	public class TextEdit
	{
		private byte[] textBytes;
		private TranslateOption translateOption;

		public TextEdit(byte[] textBytes, TranslateOption translateOption)
		{
			this.textBytes = textBytes;
			this.translateOption = translateOption;
		}
		
		public byte[] getTextBytes()
		{
			return textBytes;
		}
		
		public TranslateOption getTranslateOption()
		{
			return translateOption;
		}
	}
}
