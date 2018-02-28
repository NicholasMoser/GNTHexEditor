package com.github.hexeditor;

import java.awt.Font;
import java.awt.GridLayout;
import java.nio.ByteBuffer;
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
 * A GNT4 module for text translation. Given one or more pointers, allows the user to translate the text associated.
 */
public class GNT4Translator extends GNT4Module
{
	Map<Character, byte[]> sjisMap;
	
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
	 * Constructor for GNT4Translator
	 * @param editor the hex editor window
	 */
	public GNT4Translator(BinEdit editor)
	{
		super(editor);
		sjisMap = new HashMap<Character, byte[]>();
		sjisMap.put('~', new byte[] {0x00, 0x0A} );
		sjisMap.put(' ', new byte[] {0x40, (byte) 129} );
		sjisMap.put(',', new byte[] {0x43, (byte) 129} );
		sjisMap.put('.', new byte[] {0x44, (byte) 129} );
		sjisMap.put('·', new byte[] {0x45, (byte) 129} );
		sjisMap.put(':', new byte[] {0x46, (byte) 129} );
		sjisMap.put(';', new byte[] {0x47, (byte) 129} );
		sjisMap.put('?', new byte[] {0x48, (byte) 129} );
		sjisMap.put('!', new byte[] {0x49, (byte) 129} );
		sjisMap.put('"', new byte[] {0x4e, (byte) 129} );
		sjisMap.put('`', new byte[] {0x66, (byte) 129} );
		sjisMap.put('\'', new byte[] {0x66, (byte) 129} );
		sjisMap.put('(', new byte[] {0x69, (byte) 129} );
		sjisMap.put(')', new byte[] {0x6a, (byte) 129} );
		sjisMap.put('[', new byte[] {0x6d, (byte) 129} );
		sjisMap.put(']', new byte[] {0x6e, (byte) 129} );
		sjisMap.put('{', new byte[] {0x6f, (byte) 129} );
		sjisMap.put('}', new byte[] {0x70, (byte) 129} );
		sjisMap.put('+', new byte[] {0x7b, (byte) 129} );
		sjisMap.put('-', new byte[] {0x7c, (byte) 129} );
		sjisMap.put('<', new byte[] {(byte) 131, (byte) 129} );
		sjisMap.put('>', new byte[] {(byte) 132, (byte) 129} );
		sjisMap.put('%', new byte[] {(byte) 147, (byte) 129} );
		sjisMap.put('#', new byte[] {(byte) 148, (byte) 129} );
		sjisMap.put('&', new byte[] {(byte) 149, (byte) 129} );
		sjisMap.put('*', new byte[] {(byte) 150, (byte) 129} );
		sjisMap.put('@', new byte[] {(byte) 151, (byte) 129} );
		sjisMap.put('0', new byte[] {0x4f, (byte) 130} );
		sjisMap.put('1', new byte[] {0x50, (byte) 130} );
		sjisMap.put('2', new byte[] {0x51, (byte) 130} );
		sjisMap.put('3', new byte[] {0x52, (byte) 130} );
		sjisMap.put('4', new byte[] {0x53, (byte) 130} );
		sjisMap.put('5', new byte[] {0x54, (byte) 130} );
		sjisMap.put('6', new byte[] {0x55, (byte) 130} );
		sjisMap.put('7', new byte[] {0x56, (byte) 130} );
		sjisMap.put('8', new byte[] {0x57, (byte) 130} );
		sjisMap.put('9', new byte[] {0x58, (byte) 130} );
		sjisMap.put('A', new byte[] {0x60, (byte) 130} );
		sjisMap.put('B', new byte[] {0x61, (byte) 130} );
		sjisMap.put('C', new byte[] {0x62, (byte) 130} );
		sjisMap.put('D', new byte[] {0x63, (byte) 130} );
		sjisMap.put('E', new byte[] {0x64, (byte) 130} );
		sjisMap.put('F', new byte[] {0x65, (byte) 130} );
		sjisMap.put('G', new byte[] {0x66, (byte) 130} );
		sjisMap.put('H', new byte[] {0x67, (byte) 130} );
		sjisMap.put('I', new byte[] {0x68, (byte) 130} );
		sjisMap.put('J', new byte[] {0x69, (byte) 130} );
		sjisMap.put('K', new byte[] {0x6A, (byte) 130} );
		sjisMap.put('L', new byte[] {0x6B, (byte) 130} );
		sjisMap.put('M', new byte[] {0x6C, (byte) 130} );
		sjisMap.put('N', new byte[] {0x6D, (byte) 130} );
		sjisMap.put('O', new byte[] {0x6E, (byte) 130} );
		sjisMap.put('P', new byte[] {0x6F, (byte) 130} );
		sjisMap.put('Q', new byte[] {0x70, (byte) 130} );
		sjisMap.put('R', new byte[] {0x71, (byte) 130} );
		sjisMap.put('S', new byte[] {0x72, (byte) 130} );
		sjisMap.put('T', new byte[] {0x73, (byte) 130} );
		sjisMap.put('U', new byte[] {0x74, (byte) 130} );
		sjisMap.put('V', new byte[] {0x75, (byte) 130} );
		sjisMap.put('W', new byte[] {0x76, (byte) 130} );
		sjisMap.put('X', new byte[] {0x77, (byte) 130} );
		sjisMap.put('Y', new byte[] {0x78, (byte) 130} );
		sjisMap.put('Z', new byte[] {0x79, (byte) 130} );
		sjisMap.put('a', new byte[] {(byte) 129, (byte) 130} );
		sjisMap.put('b', new byte[] {(byte) 130, (byte) 130} );
		sjisMap.put('c', new byte[] {(byte) 131, (byte) 130} );
		sjisMap.put('d', new byte[] {(byte) 132, (byte) 130} );
		sjisMap.put('e', new byte[] {(byte) 133, (byte) 130} );
		sjisMap.put('f', new byte[] {(byte) 134, (byte) 130} );
		sjisMap.put('g', new byte[] {(byte) 135, (byte) 130} );
		sjisMap.put('h', new byte[] {(byte) 136, (byte) 130} );
		sjisMap.put('i', new byte[] {(byte) 137, (byte) 130} );
		sjisMap.put('j', new byte[] {(byte) 138, (byte) 130} );
		sjisMap.put('k', new byte[] {(byte) 139, (byte) 130} );
		sjisMap.put('l', new byte[] {(byte) 140, (byte) 130} );
		sjisMap.put('m', new byte[] {(byte) 141, (byte) 130} );
		sjisMap.put('n', new byte[] {(byte) 142, (byte) 130} );
		sjisMap.put('o', new byte[] {(byte) 143, (byte) 130} );
		sjisMap.put('p', new byte[] {(byte) 144, (byte) 130} );
		sjisMap.put('q', new byte[] {(byte) 145, (byte) 130} );
		sjisMap.put('r', new byte[] {(byte) 146, (byte) 130} );
		sjisMap.put('s', new byte[] {(byte) 147, (byte) 130} );
		sjisMap.put('t', new byte[] {(byte) 148, (byte) 130} );
		sjisMap.put('u', new byte[] {(byte) 149, (byte) 130} );
		sjisMap.put('v', new byte[] {(byte) 150, (byte) 130} );
		sjisMap.put('w', new byte[] {(byte) 151, (byte) 130} );
		sjisMap.put('x', new byte[] {(byte) 152, (byte) 130} );
		sjisMap.put('y', new byte[] {(byte) 153, (byte) 130} );
		sjisMap.put('z', new byte[] {(byte) 154, (byte) 130} );
	}
	
	/**
	 * Opens a dialog that allows you to translate text for Naruto GNT4 seq files.
	 */
	public void translateGNT4()
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
			String sjisText = BinUtil.readShiftJisText(fileBytes, textPointer);
			TextEdit textEdit = getTranslatedText(sjisText, i + 1, textPointers.size());
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
				//fileBytes = BinUtil.getFileBytes(editor);
				appendPosition += translatedBytes.length;
			}
		}
		
		editor.goTo(Integer.toString(textPointers.get(0)));
	}
	
	/**
	 * Takes a string of ascii English text and returns the most appropriate shift-jis byte array
	 * representing that string. Returns null if there is a character not found in the map.
	 * There are four times as many bytes as characters, since there are two 0x00 bytes and then
	 * the two bytes for the character itself. The last four bytes are for the terminator.
	 * 
	 * @param text the ascii English text
	 * @return the shift-jis bytes for the provided text
	 */
	public byte[] asciiToShiftJisBytes(String text)
	{
		byte[] bytes = new byte[(text.length() * 4) + 4];
		for (int i = 0; i < text.length(); i++){
		    char c = text.charAt(i);
		    byte[] byteCouple = sjisMap.get(c);
		    if (byteCouple == null)
		    {
		    	return null;
		    }
		    bytes[i * 4] = 0x00;
		    bytes[(i * 4) + 1] = 0x00;
		    bytes[(i * 4) + 2] = byteCouple[0];
		    bytes[(i * 4) + 3] = byteCouple[1];
		}
	    bytes[bytes.length - 4] = 0x00;
	    bytes[bytes.length - 3] = 0x00;
	    bytes[bytes.length - 2] = 0x00;
	    bytes[bytes.length - 1] = 0x1A;
		return bytes;
	}

	/**
	 * Asks the user for the text to translate the shift-jis text with.
	 * Returns the shift-jis bytes for the translated string or null if exit.
	 * 
	 * @param sjisText the shift-jis text to be translated
	 * @param currentPointer the current pointer
	 * @param numPointers the total number of pointers
	 * @return the translated text
	 */
	private TextEdit getTranslatedText(String sjisText, int currentPointer, int numPointers)
	{
		boolean validValue = false;
		byte[] shiftJisBytes = null;
		TranslateOption translateOption = TranslateOption.UNKNOWN;
		while(!validValue)
		{
			JPanel textPanel = new JPanel(new GridLayout(4,1));
			String messageLine1 = "Please edit the following shift-jis text.";
			JLabel messageLabel1 = new JLabel(messageLine1);
			messageLabel1.setFont(messageLabel1.getFont().deriveFont(Font.PLAIN, 16));
			String messageLine2 = "You have " + sjisText.length() + " characters you can use for replacement.";
			JLabel messageLabel2 = new JLabel(messageLine2);
			messageLabel2.setFont(messageLabel2.getFont().deriveFont(Font.PLAIN, 16));
			String messageLine3 = "You are at pointer " + currentPointer + " of " + numPointers;
			JLabel messageLabel3 = new JLabel(messageLine3);
			messageLabel3.setFont(messageLabel3.getFont().deriveFont(Font.PLAIN, 16));
			textPanel.add(messageLabel1);
			textPanel.add(messageLabel2);
			textPanel.add(messageLabel3);
			String title = "Translate Shift-Jis Text";
	        JTextField editableField = new JTextField(sjisText);
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
			String inputText = editableField.getText().replaceAll(Pattern.quote("[LINE]"), "~").replaceAll(Pattern.quote("[END]"), "");
			if (translateOption != TranslateOption.REPLACE || inputText.length() == sjisText.length())
			{
				shiftJisBytes = asciiToShiftJisBytes(inputText);
				if (shiftJisBytes != null)
				{
					validValue = true;
				}
				else
				{
					JOptionPane.showMessageDialog(editor, "Your new text contains an invalid character.");
				}
			}
			else
			{
				JOptionPane.showMessageDialog(editor, "For replacement, make sure you match the same number of characters.");
			}
		}
		return new TextEdit(shiftJisBytes, translateOption);
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
