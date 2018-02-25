package com.github.hexeditor;

import javax.swing.JOptionPane;

/**
 * Parent class for GNT4 modules. Contains common functionality across different GNT4 modules.
 */
public class GNT4Module
{
	BinEdit editor;

	/**
	 * Constructor for GNT4Module
	 * @param editor the hex editor window
	 */
	public GNT4Module(BinEdit editor)
	{
		this.editor = editor;
	}

	/**
	 * Asks the user for the pointer table start. Both decimal and hexadecimal are allowed.
	 * @return the pointer table start value
	 */
	public int getPointerTableStart()
	{
		boolean validValue = false;
		int value = 0;
		while(!validValue)
		{
			String message = "Please enter the start of the pointer table (inclusive).\n";
			message += "Note: By default assumes the number is in decimal (base-10).\n";
			message += "Add 0x to the start of your number if you wish to instead use hexadecimal (base-16).\n";
			message += "Example: 258 or 0x102";
			String title = "Pointer Table Start";
			String inputValue = JOptionPane.showInputDialog(editor, message, title, JOptionPane.PLAIN_MESSAGE);
			if (inputValue == null)
			{
				return -1;
			}
			try
			{
				if (inputValue.startsWith("0x") || inputValue.startsWith("Ox") || inputValue.startsWith("ox"))
				{
					value = Integer.valueOf(inputValue.substring(2), 16);
				}
				else
				{
					value = Integer.valueOf(inputValue);
				}
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
	 * Asks the user for the pointer table end. Both decimal and hexadecimal are allowed.
	 * @return the pointer table end value
	 */
	public int getPointerTableEnd()
	{
		boolean validValue = false;
		int value = 0;
		while(!validValue)
		{
			String message = "Please enter the end of the pointer table (inclusive).\n";
			message += "Note: By default assumes the number is in decimal (base-10).\n";
			message += "Add 0x to the start of your number if you wish to instead use hexadecimal (base-16).\n";
			message += "Example: 258 or 0x102";
			String title = "Pointer Table End";
			String inputValue = JOptionPane.showInputDialog(editor, message, title, JOptionPane.PLAIN_MESSAGE);
			if (inputValue == null)
			{
				return -1;
			}
			try
			{
				if (inputValue.startsWith("0x") || inputValue.startsWith("Ox") || inputValue.startsWith("ox"))
				{
					value = Integer.valueOf(inputValue.substring(2), 16);
				}
				else
				{
					value = Integer.valueOf(inputValue);
				}
				validValue = true;
			}
			catch (NumberFormatException e)
			{
				JOptionPane.showMessageDialog(editor, "Please enter a valid number.");
			}
		}
		return value;
	}
}
