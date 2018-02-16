package com.github.hexeditor;

import javax.swing.JOptionPane;

public class GNT4Translator
{
	BinEdit editor;
	
	public GNT4Translator(BinEdit editor)
	{
		this.editor = editor;
	}
	
	/**
	 * Opens a dialog that allows you to translate text for Naruto GNT4.
	 */
	public void translateGNT4()
	{
		// TODO: translate GNT4 text
		
		// I don't think these two are actually needed.
		// int endianFlag = getEndianness();
		// int pointerSizeFlag = getPointerSize();
		
		int pointerTableStart = getPointerTableStart();
		int pointerTableEnd = getPointerTableEnd();
		boolean appendText = appendText();
		int newTextLocationOffset = -1;
		if (appendText)
		{
			newTextLocationOffset = getNewTextLocationOffset();
		}
		
		byte pointerTableStartByte = getByte(pointerTableStart);
		byte pointerTableEndByte = getByte(pointerTableEnd);
		byte[] bytes = new byte[2];
		bytes[0] = pointerTableStartByte;
		bytes[1] = pointerTableEndByte;
		System.out.println("Hex at pointer start: " + firstHex(pointerTableStartByte) + secondHex(pointerTableStartByte));
		System.out.println("Hex at pointer end: " + firstHex(pointerTableEndByte) + secondHex(pointerTableEndByte));
				
		int position = pointerTableEnd;
		editor.pushHObjBytes(new EditState(position, (long) bytes.length, 4), bytes);
	}
	
	/**
	 * Helper function to get the byte from the list of bytes at a specific location.
	 * Each byte includes a color which is discarded. 
	 * 
	 * @param offset the location of the byte to retrieve from the current file
	 * @return the byte at the specified location
	 */
	private byte getByte(int offset)
	{
		byte[] byteColorCombo = (byte[]) editor.srcV.get(offset);
		return byteColorCombo[0];
	}
	
	private String firstHex(byte fullByte)
	{
		return Integer.toHexString((255 & fullByte) >> 4).toUpperCase();
	}
	
	private String secondHex(byte fullByte)
	{
		return Integer.toHexString((255 & fullByte) % 16).toUpperCase();
	}
	
	/**
	 * Asks the user for the endianness. The two choices are little and big endian.
	 * @return 0 for little endian, 1 for big endian
	 */
	private int getEndianness()
	{
		String[] options = new String[] {"LittleEndian", "BigEndian"};
		String endianMsg = "Which endian are you working with?\n";
		endianMsg += "Little endian is for ASCII (main.dol, gameplay assets)\n";
		endianMsg += "Big endian is for Shift-JIS (.seq files, menu assets)\n";
		String endianTitle = "Endianness";
		int endianFlag = JOptionPane.showOptionDialog(editor, endianMsg, endianTitle, JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
		        null, options, options[0]);
		return endianFlag;
	}
	
	/**
	 * Asks the user for the pointer size. The two choices are 4 bytes and 8 bytes.
	 * @return 0 for 4 bytes, 1 for 8 bytes
	 */
	private int getPointerSize()
	{
		String[] options = new String[] {"4-byte Pointer", "8-byte Pointer"};
		String pointerSizeMsg = "Which pointer size are you working with?\n";
		pointerSizeMsg += "4-byte: (xx xx)\n";
		pointerSizeMsg += "8-byte: (xx xx xx xx)\n";
		String pointerSizeTitle = "Pointer Size";
		int pointerSize = JOptionPane.showOptionDialog(editor, pointerSizeMsg, pointerSizeTitle, JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
		        null, options, options[0]);
		return pointerSize;
	}

	/**
	 * Asks the user for the pointer table start. Only decimal (base-10) values are allowed).
	 * @return the pointer table start value
	 */
	private int getPointerTableStart()
	{
		boolean validValue = false;
		int value = 0;
		while(!validValue)
		{
			String pointerTableStartMsg = "Please enter the start of the pointer table.\n";
			pointerTableStartMsg += "Note: Input it as a decimal (base-10) value.\n";
			pointerTableStartMsg += "Example: 258";
			String pointerTableStartTitle = "Pointer Table Start";
			String inputValue = JOptionPane.showInputDialog(editor, pointerTableStartMsg, pointerTableStartTitle, JOptionPane.PLAIN_MESSAGE);
			try
			{
				value = Integer.valueOf(inputValue);
				validValue = true;
			}
			catch (NumberFormatException e)
			{
				JOptionPane.showMessageDialog(editor, "Please enter a valid decimal (base-10) value.");
			}
		}
		return value;
	}

	/**
	 * Asks the user for the pointer table end. Only decimal (base-10) values are allowed).
	 * @return the pointer table end value
	 */
	private int getPointerTableEnd()
	{
		boolean validValue = false;
		int value = 0;
		while(!validValue)
		{
			String pointerTableEndMsg = "Please enter the end of the pointer table.\n";
			pointerTableEndMsg += "Note: Input it as a decimal (base-10) value.\n";
			pointerTableEndMsg += "Example: 258";
			String pointerTableEndTitle = "Pointer Table End";
			String inputValue = JOptionPane.showInputDialog(editor, pointerTableEndMsg, pointerTableEndTitle, JOptionPane.PLAIN_MESSAGE);
			try
			{
				value = Integer.valueOf(inputValue);
				validValue = true;
			}
			catch (NumberFormatException e)
			{
				JOptionPane.showMessageDialog(editor, "Please enter a valid decimal (base-10) value.");
			}
		}
		return value;
	}
	
	/**
	 * Asks the user if they wish to insert text at the end of the document.
	 * @return if the user wishes to insert text at the end of the document.
	 */
	private boolean appendText()
	{
		String[] options = new String[] {"Yes", "No"};
		String appendTextMsg = "Do you wish to insert text at the end of the file?\n";
		appendTextMsg += "Note: This is primarily used for .seq editing.\n";
		String appendTextTitle = "Append Text";
		int appendTextFlag = JOptionPane.showOptionDialog(editor, appendTextMsg, appendTextTitle, JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
		        null, options, options[0]);
		return appendTextFlag == 0 ? true : false;
	}

	/**
	 * Asks the user for the new text location offset. Only decimal (base-10) values are allowed).
	 * @return the pointer table start value
	 */
	private int getNewTextLocationOffset()
	{
		boolean validValue = false;
		int value = 0;
		while(!validValue)
		{
			String newTextLocationOffsetMsg = "Please enter the new text location offset.\n";
			newTextLocationOffsetMsg += "Note: Input it as a decimal (base-10) value.\n";
			newTextLocationOffsetMsg += "Example: 258";
			String newTextLocationOffsetTitle = "Pointer Table Start";
			String inputValue = JOptionPane.showInputDialog(editor, newTextLocationOffsetMsg, newTextLocationOffsetTitle, JOptionPane.PLAIN_MESSAGE);
			try
			{
				value = Integer.valueOf(inputValue);
				validValue = true;
			}
			catch (NumberFormatException e)
			{
				JOptionPane.showMessageDialog(editor, "Please enter a valid decimal (base-10) value.");
			}
		}
		return value;
	}
}
