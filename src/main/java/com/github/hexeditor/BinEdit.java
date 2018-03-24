package com.github.hexeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JTextField;
import javax.swing.Timer;

class BinEdit extends JComponent implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener,
		ActionListener, AdjustmentListener
{

	private static final long serialVersionUID = -8556500880720037563L;
	boolean nibArea = true;
	boolean isNibLow = false;
	boolean jSbSource = true;
	boolean isApplet;
	boolean isOled = false;
	int[] xPos;
	int[] xNib = new int[32];
	int[] xTxt = new int[16];
	int[] cShift = new int[256];
	int wChar = -1;
	int wPanel = -1;
	int hMargin = -1;
	int hChar = -1;
	int hPanel = -1;
	int fontSize = 0;
	int caretVisible = 0;
	int maxRow = -1;
	int maxPos;
	int hLimit = -1;
	Font font;
	long virtualSize = 0L;
	long scrPos = 0L;
	long firstPos = 0L;
	long lastPos = 0L;
	long newPos = 0L;
	long clipboardSize;
	long jSBStep = 1L;
	JFileChooser jFC;
	JScrollBar jSB = new JScrollBar(1, 0, 1, 0, 1);
	Object[] InsDelOption = new Object[7];
	JRadioButton[] InsDelB = new JRadioButton[5];
	JTextField InsDelTF = new JTextField();
	Timer timer = new Timer(500, this);
	Clipboard clipboard;
	RandomAccessFile currentFile;
	Stack undoStack = new Stack();
	Vector srcV = new Vector();
	Vector markV = new Vector();
	Vector MarkV = new Vector();
	Vector v1 = new Vector();
	Vector XCV = new Vector();
	Byte byteCtrlY = null;
	EditState eObj = null;
	EditState eObjCtrlY = null;
	File loadedFile = null;
	public BinPanel topPanel;
	SaveModule saveModule;
	FindModule find;
	long longInput = 0L;

	public BinEdit(BinPanel var1, boolean var2)
	{
		this.setLayout(new BorderLayout());
		this.add(this.jSB, "East");
		this.setGrid(14);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addMouseWheelListener(this);
		this.addKeyListener(this);
		this.jSB.setEnabled(true);
		this.jSB.setFocusable(true);
		this.jSB.setUnitIncrement(1);
		this.jSB.addMouseWheelListener(this);
		this.jSB.addAdjustmentListener(this);
		this.timer.addActionListener(this);
		this.topPanel = var1;
		this.isApplet = var2;
		String[] var3 = new String[]
		{ "Delete", "Insert & fill with 0x00", "Insert & fill with 0xFF", "Insert & fill with 0x20 (space)",
				"or Insert clipboard" };
		ButtonGroup var4 = new ButtonGroup();

		for (int var5 = 0; var5 < this.InsDelB.length; ++var5)
		{
			(this.InsDelB[var5] = new JRadioButton(var3[var5])).addActionListener(this);
			var4.add(this.InsDelB[var5]);
			this.InsDelOption[var5 < 4 ? var5 : 6] = this.InsDelB[var5];
		}

		this.InsDelOption[4] = "<html>Bytes to delete or to insert<br>(0x.. for hexa entry):";
		this.InsDelTF.setColumns(9);
		this.InsDelOption[5] = this.InsDelTF;
		if (!var2)
		{
			this.jFC = new JFileChooser(System.getProperty("user.dir"));
			this.jFC.setAcceptAllFileFilterUsed(false);
			this.jFC.setDialogType(0);
			this.jFC.setFileSelectionMode(2);
			this.jFC.setMultiSelectionEnabled(false);
			this.jFC.setDragEnabled(false);
			this.clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		} else
		{
			this.jSbSource = false;
			this.jSB.setValue(0);
			this.pushHObj(new EditState(0L, 0L, 2),
					"\tTry   Hexeditor.jar with this   virtual file.\n  An applet cannot access a real  file nor the    clipboard.\n     The File menu,  Ctrl+X, Ctrl+C, & Ctrl+V are    therefore       inhibited.");
		}

		this.focus();
	}

	public void closeFile()
	{
		if (this.currentFile != null)
		{
			try
			{
				this.currentFile.close();
			} catch (Exception var2)
			{
				System.err.println("Ctrl+OQ " + var2);
			}
		}

		this.loadedFile = null;
		this.undoStack.clear();
		this.doVirtual();
		System.gc();
	}

	public void loadFile(File var1)
	{
		this.loadedFile = var1;
		this.topPanel.JTFile.setText(this.loadedFile.toString() + (this.loadedFile.canWrite() ? "" : " ( ReadOnly ) "));

		try
		{
			this.currentFile = new RandomAccessFile(this.loadedFile, this.loadedFile.canWrite() ? "rw" : "r");
			this.jSbSource = false;
			this.jSB.setValue(0);
			this.undoStack.push(new EditState(0L, this.loadedFile.length(), 0));
			this.doVirtual();
			this.focus();
		} catch (Exception var3)
		{
			System.err.println("loadFile " + var3);
		}

		this.eObjCtrlY = null;
		this.byteCtrlY = null;
	}

	private void focus()
	{
		if (!this.requestFocusInWindow())
		{
			this.requestFocus();
		}

	}

	private void setGrid(int var1)
	{
		byte var3 = 11;
		byte var4 = 35;
		int var5 = -3;
		int var2;
		if (var1 != this.fontSize)
		{
			this.fontSize = var1 < var3 ? var3 : (var4 < var1 ? var4 : var1);
			FontMetrics var6 = this
					.getFontMetrics(this.font = new Font("Monospaced", this.fontSize < 27 ? 0 : 1, this.fontSize));
			this.cShift = var6.getWidths();
			this.wChar = -1;

			for (var2 = 0; var2 < 256; ++var2)
			{
				this.wChar = var2 != 9 && this.cShift[var2] > this.wChar ? this.cShift[var2] : this.wChar;
			}

			this.wChar = var6.charWidth('\u2219') > this.wChar ? var6.charWidth('\u2219') : this.wChar;

			for (var2 = 0; var2 < 256; ++var2)
			{
				this.cShift[var2] = this.wChar - this.cShift[var2] >> 1;
			}

			this.wChar >>= 1;
			this.hChar = var6.getHeight();
			this.hMargin = var6.getLeading() + var6.getAscent();
		}

		this.maxRow = (this.hPanel - this.hMargin) / this.hChar + 1;
		this.maxPos = (this.maxRow << 4) - 1;
		this.xPos = new int[Long.toHexString(this.virtualSize + 32L).length() + 1 >> 1 << 1];

		for (var2 = this.xPos.length - 1; -1 < var2; --var2)
		{
			this.xPos[var2] = var5 += var2 % 2 == 0 ? 2 : 3;
		}

		for (var2 = 0; var2 < this.xNib.length; ++var2)
		{
			int var10003 = (var2 & 1) == 1 ? 2 : ((var2 & 7) == 0 ? 5 : 3);
			int var10002 = var5 + ((var2 & 1) == 1 ? 2 : ((var2 & 7) == 0 ? 5 : 3));
			var5 += var10003;
			this.xNib[var2] = var10002;
		}

		var5 += 4;

		for (var2 = 0; var2 < this.xTxt.length; ++var2)
		{
			var5 += 2;
			this.xTxt[var2] = var5;
		}

		long var8 = (this.virtualSize >> 4) + 2L;
		if (var8 < 1073741824L)
		{
			this.jSbSource = false;
			this.jSB.setMaximum((int) var8);
			this.jSbSource = false;
			this.jSB.setVisibleAmount(this.maxRow);
			this.jSbSource = false;
			this.jSB.setBlockIncrement(this.maxRow - 1);
			this.jSBStep = 16L;
		} else
		{
			var2 = (int) (var8 >> 30);
			this.jSbSource = false;
			this.jSB.setMaximum(1073741824);
			this.jSbSource = false;
			this.jSB.setVisibleAmount(1);
			this.jSbSource = false;
			this.jSB.setBlockIncrement(1048576);
			this.jSBStep = (long) var2 << 4;
		}

	}

	public void adjustmentValueChanged(AdjustmentEvent var1)
	{
		if (var1.getSource() == this.jSB && this.jSbSource)
		{
			this.slideScr((long) this.jSB.getValue() * this.jSBStep, false);
		}

		this.jSbSource = true;
	}

	protected void slideScr(long var1, boolean var3)
	{
		long var4 = this.virtualSize < 9223372036854775792L ? this.virtualSize & -16L : 9223372036854775792L;
		if (var3 || var1 >= 0L)
		{
			if (var3 && (this.lastPos < var1 || var1 < this.lastPos - (long) this.maxPos))
			{
				this.scrPos = this.lastPos - (long) (this.maxPos >> 1) & -16L;
			} else
			{
				this.scrPos = var1 & -16L;
			}
		}

		if (var4 - (long) this.maxPos + 31L <= this.scrPos)
		{
			this.scrPos = var4 - (long) this.maxPos + 31L;
		}

		if (this.scrPos < 0L)
		{
			this.scrPos = 0L;
		}

		if (this.scrPos != (long) this.jSB.getValue() * this.jSBStep)
		{
			this.jSbSource = false;
			this.jSB.setValue((int) (this.scrPos / this.jSBStep));
		}

		this.setSrc();
		this.rePaint();
		this.caretVisible = 0;
		this.timer.restart();
	}

	protected void goTo(String var1)
	{
		if (var1 != null)
		{
			this.String2long(var1);
			if (this.longInput < 0L)
			{
				this.longInput = this.lastPos;
			}

			this.firstPos = this.lastPos = this.longInput < this.virtualSize ? this.longInput : this.virtualSize;
			this.isNibLow = false;
			this.slideScr(0L, true);
		}

	}

	protected void paintComponent(Graphics var1)
	{
		this.paintImg(var1, true);
	}

	protected void paintImg(Graphics gfx, boolean var2)
	{
		char[] hexValues = new char[]
		{ '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		Color[] colors = new Color[]
		{ new Color(188, 188, 188), Color.BLACK, new Color(50, 50, 50, 40), new Color(50, 50, 50, 80), Color.GREEN, Color.RED,
				Color.BLUE, Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.GREEN.darker(), new Color(0, 0, 0, 0) };
		byte[] var5 = new byte[2];
		int[] var10 = new int[2];
		int[] var11 = new int[2];
		long var12 = 0L;
		if (this.hPanel != this.getHeight())
		{
			this.wPanel = this.getWidth();
			this.hPanel = this.getHeight();
			this.setGrid(this.fontSize);
			this.setSrc();
		}

		gfx.setColor(colors[this.isOled ? 1 : 0]);
		gfx.fillRect(0, 0, this.getWidth(), this.getHeight());
		gfx.setFont(this.font);
		gfx.setColor(colors[this.isOled ? 7 : 6]);
		this.hLimit = 0;

		int var8;
		int row;
		// Draws the Offset numbers on the left hand side of the screen
		for (row = 0; row < this.maxRow; ++row)
		{
			var12 = this.scrPos + (long) (row << 4);
			if ((this.virtualSize | 15L) < var12 || var12 < 0L)
			{
				break;
			}

			for (var8 = 0; var8 < this.xPos.length; ++var8)
			{
				char hex = hexValues[(int) (var12 & 15L)];
				gfx.drawString("" + hex, this.cShift[hex] + this.wChar * this.xPos[var8],
						this.hLimit = this.hMargin + this.hChar * row);
				var12 >>= 4;
			}
		}

		if (var12 < 0L)
		{
			gfx.setColor(colors[5]);
			gfx.drawString("-- Limit = 0x7FFFFFFFFFFFFFFE = Long.MAX_VALUE-1 = 2^63-2 = 9223372036854775806 --", 0,
					this.hMargin + this.hChar * row - 3);
		}

		gfx.setColor(colors[9]);
		boolean var16 = this.firstPos < this.lastPos;
		var10 = this.pos2XY(var16 ? this.firstPos : this.lastPos);
		var11 = this.pos2XY(!var16 ? this.firstPos : this.lastPos);
		if (this.lastPos != this.firstPos && this.lastPos >= this.scrPos)
		{
			if (var10[1] == var11[1])
			{
				gfx.fillRect(this.wChar * this.xNib[var10[0] * 2] - 2, this.hChar * var10[1] + 3,
						this.wChar * (this.xNib[var11[0] * 2] - this.xNib[var10[0] * 2]), this.hChar - 4);
				gfx.fillRect(this.wChar * this.xTxt[var10[0]], this.hChar * var10[1] + 3,
						this.wChar * (this.xTxt[var11[0]] - this.xTxt[var10[0]]), this.hChar - 4);
			} else if (var10[1] + 1 == var11[1] && var11[0] == 0)
			{
				gfx.fillRect(this.wChar * this.xNib[var10[0] * 2] - 2, this.hChar * var10[1] + 3,
						this.wChar * (this.xNib[31] + 2 - this.xNib[var10[0] * 2]), this.hChar - 4);
				gfx.fillRect(this.wChar * this.xTxt[var10[0]], this.hChar * var10[1] + 3,
						this.wChar * (this.xTxt[15] + 2 - this.xTxt[var10[0]]), this.hChar - 4);
			} else
			{
				gfx.fillRect(this.wChar * this.xNib[var10[0] * 2] - 2, this.hChar * var10[1] + 3,
						this.wChar * (this.xNib[31] + 2 - this.xNib[var10[0] * 2]) + 4, this.hChar - 4);
				gfx.fillRect(this.wChar * this.xNib[0] - 2, this.hChar * (var10[1] + 1) - 1,
						this.wChar * (this.xNib[31] + 2 - this.xNib[0]) + 4,
						this.hChar * (var11[1] - var10[1] - 1) + 4);
				if (this.xNib[var11[0] * 2] != this.xNib[0])
				{
					gfx.fillRect(this.wChar * this.xNib[0] - 2, this.hChar * var11[1] + 3,
							this.wChar * (this.xNib[var11[0] * 2] - this.xNib[0]), this.hChar - 4);
				}

				gfx.fillRect(this.wChar * this.xTxt[var10[0]], this.hChar * var10[1] + 3,
						this.wChar * (this.xTxt[15] + 2 - this.xTxt[var10[0]]), this.hChar - 4);
				gfx.fillRect(this.wChar * this.xTxt[0], this.hChar * (var10[1] + 1) - 1,
						this.wChar * (this.xTxt[15] + 2 - this.xTxt[0]), this.hChar * (var11[1] - var10[1] - 1) + 4);
				gfx.fillRect(this.wChar * this.xTxt[0], this.hChar * var11[1] + 3,
						this.wChar * (this.xTxt[var11[0]] - this.xTxt[0]), this.hChar - 4);
			}
		}

		if (this.isOled)
		{
			gfx.setXORMode(Color.BLACK);
		}

		// This logic is to handle showing shift-jis on the right-hand side of the screen instead of ASCII
		Charset cs = null;
		boolean secondByte = false;
		byte[] doubleByte = new byte[2];
		if (this.topPanel.viewCBox[1].getSelectedIndex() == 12)
		{
			cs = Charset.forName("UTF-16");
		}
		if (this.topPanel.viewCBox[1].getSelectedIndex() == 13)
		{
			cs = Charset.forName("Shift_JIS");
		}
		
		// Draws the hex numbers left to right then top to bottom with their associated representation on the editor screen
		for (int var7 = 0; var7 < this.srcV.size() && var7 < this.maxRow << 4; ++var7)
		{
			char hex;
			var8 = var7 % 16;
			row = var7 >> 4;
			var5 = (byte[]) ((byte[]) ((byte[]) this.srcV.get(var7)));
			gfx.setColor(colors[var5[1] != 1 ? 11 : (this.isOled ? 2 : 2)]);
			gfx.fillRect(this.wChar * this.xNib[var8 * 2] - 2, this.hChar * row + 3, this.wChar * 5, this.hChar - 4);
			gfx.fillRect(this.wChar * this.xTxt[var8], this.hChar * row + 3, this.wChar * 2, this.hChar - 4);
			gfx.setColor(colors[2 < var5[1] ? (this.isOled ? 0 : 5) : (this.isOled ? 4 : 1)]);
			// First hex value
			hex = hexValues[(255 & var5[0]) >> 4];
			gfx.drawString("" + hex, this.cShift[hex] + this.wChar * this.xNib[var8 * 2],
					this.hMargin + this.hChar * row);
			// Second hex value
			hex = hexValues[(255 & var5[0]) % 16];
			gfx.drawString("" + hex, this.cShift[hex] + this.wChar * this.xNib[var8 * 2 + 1],
					this.hMargin + this.hChar * row);
			// Byte representation
			if ("Shift_JIS".equals(cs.name()))
			{
				if (secondByte)
				{
					doubleByte[0] = var5[0];
					String shiftJisCharacter = new String(doubleByte, cs);
					gfx.drawString(shiftJisCharacter, this.wChar * this.xTxt[var8], this.hMargin + this.hChar * row);
					secondByte = false;
				}
				else
				{
					doubleByte[1] = var5[0];
					secondByte = true;
				}
			}
			else if ("UTF-16".equals(cs.name()))
			{
				if (secondByte)
				{
					doubleByte[1] = var5[0];
					String shiftJisCharacter = new String(doubleByte, cs);
					gfx.drawString(shiftJisCharacter, this.wChar * this.xTxt[var8], this.hMargin + this.hChar * row);
					secondByte = false;
				}
				else
				{
					doubleByte[0] = var5[0];
					secondByte = true;
				}
			}
			else
			{
				hex = (char) (255 & var5[0]);
				if (Character.isISOControl(hex))
				{
					gfx.drawString("∙", this.wChar * this.xTxt[var8], this.hMargin + this.hChar * row);
				} else
				{
					gfx.drawString("" + hex, this.cShift[hex] + this.wChar * this.xTxt[var8],
							this.hMargin + this.hChar * row);
				}
			}
		}

		gfx.setPaintMode();
		gfx.setColor(colors[10]);
		long var14;
		Iterator var17;
		if (this.markV != null && 0 < this.markV.size())
		{
			var17 = this.markV.iterator();

			while (var17.hasNext())
			{
				var14 = ((Long) var17.next()).longValue();
				if (this.virtualSize <= var14)
				{
					var17.remove();
				} else if (this.scrPos <= var14 && var14 - (long) this.maxPos <= this.scrPos)
				{
					var10 = this.pos2XY(var14);
					gfx.fillRect(this.wChar * (this.xNib[0] - 1), this.hChar * (var10[1] + 1) - 2,
							this.wChar * (this.xNib[var10[0] << 1] - this.xNib[0] + 1) - 2, 1);
					gfx.fillRect(this.wChar * this.xNib[var10[0] << 1] - 2, this.hChar * var10[1] + 3, 1,
							this.hChar - 4);
					gfx.fillRect(this.wChar * this.xNib[var10[0] << 1] - 2, this.hChar * var10[1] + 3,
							this.wChar * (this.xNib[31] - this.xNib[var10[0] << 1] + 3), 1);
					gfx.fillRect(this.wChar * (this.xTxt[0] - 1), this.hChar * (var10[1] + 1) - 2,
							this.wChar * (this.xTxt[var10[0]] - this.xTxt[0] + 1), 1);
					gfx.fillRect(this.wChar * this.xTxt[var10[0]] - 1, this.hChar * var10[1] + 3, 1, this.hChar - 4);
					gfx.fillRect(this.wChar * this.xTxt[var10[0]], this.hChar * var10[1] + 3,
							this.wChar * (this.xTxt[15] - this.xTxt[var10[0]] + 3), 1);
				}
			}
		}

		if (this.MarkV != null && 0 < this.MarkV.size())
		{
			var17 = this.MarkV.iterator();

			while (var17.hasNext())
			{
				var14 = ((Long) var17.next()).longValue();
				if (this.virtualSize <= var14)
				{
					var17.remove();
				} else if (this.scrPos <= var14 && var14 - (long) this.maxPos <= this.scrPos)
				{
					var10 = this.pos2XY(var14);
					gfx.fillRect(this.wChar * (this.xNib[0] - 1), this.hChar * (var10[1] + 1) - 2,
							this.wChar * (this.xNib[var10[0] << 1] - this.xNib[0] + 1), 2);
					gfx.fillRect(this.wChar * this.xNib[var10[0] << 1] - 2, this.hChar * var10[1] + 3, 2,
							this.hChar - 3);
					gfx.fillRect(this.wChar * this.xNib[var10[0] << 1] - 2, this.hChar * var10[1] + 3,
							this.wChar * (this.xNib[31] - this.xNib[var10[0] << 1] + 3), 2);
					gfx.fillRect(this.wChar * (this.xTxt[0] - 1), this.hChar * (var10[1] + 1) - 2,
							this.wChar * (this.xTxt[var10[0]] - this.xTxt[0] + 1), 2);
					gfx.fillRect(this.wChar * this.xTxt[var10[0]] - 1, this.hChar * var10[1] + 3, 2, this.hChar - 3);
					gfx.fillRect(this.wChar * this.xTxt[var10[0]], this.hChar * var10[1] + 3,
							this.wChar * (this.xTxt[15] - this.xTxt[var10[0]] + 3), 2);
				}
			}
		}

		if (this.scrPos <= this.lastPos && this.lastPos - (long) this.maxPos <= this.scrPos)
		{
			gfx.setColor(colors[8]);
			var11 = this.pos2XY(this.lastPos);
			if (this.caretVisible < 2 || !var2)
			{
				gfx.fillRect(this.wChar
						* (this.nibArea ? this.xNib[(var11[0] << 1) + (this.isNibLow ? 1 : 0)] : this.xTxt[var11[0]])
						- 1, this.hChar * var11[1] + 3, 2, this.hChar - 4);
			}

			gfx.fillRect(this.wChar * (this.nibArea ? this.xTxt[var11[0]] : this.xNib[var11[0] << 1]),
					this.hChar * (var11[1] + 1) - 2, this.wChar << (this.nibArea ? 1 : 2), 2);
		}

	}

	private int[] pos2XY(long var1)
	{
		var1 -= this.scrPos;
		int var3 = var1 < 0L ? 0 : (var1 < (long) (this.maxPos + 1) ? (int) var1 : this.maxPos + 1);
		int[] var4 = new int[]
		{ var3 % 16, var3 >> 4 };
		return var4;
	}

	protected void rePaint()
	{
		this.repaint();
		this.setStatus();
	}

	protected void setStatus()
	{
		int encodingSelection = this.topPanel.viewCBox[1].getSelectedIndex();
		String displayText = "";
		if (this.firstPos != this.lastPos)
		{
			if (this.lastPos - this.firstPos < 2147483647L && this.firstPos - this.lastPos < 2147483647L)
			{
				this.topPanel.JTsizes.setText(this.lastPos - this.firstPos + " bytes selected.");
			} else
			{
				this.topPanel.JTsizes.setForeground(Color.red);
				this.topPanel.JTsizes.setText("Don\'t select more than 2^31-1 bytes!");
			}
		} else
		{
			StringBuffer var6 = new StringBuffer(this.isApplet ? "Offset: " : "<html>Offset:&nbsp;<b>");
			var6.append(this.coloredLong(this.lastPos)).append("/-")
					.append(this.coloredLong(this.virtualSize - this.lastPos));
			this.topPanel.JTsizes.setForeground(Color.black);
			this.topPanel.JTsizes.setText(var6.toString());
		}

		this.topPanel.JTView.setText("");
		if (this.lastPos >= this.scrPos && this.scrPos + (long) this.srcV.size() >= this.lastPos)
		{
			if (this.lastPos <= this.virtualSize)
			{
				int var5 = encodingSelection != 0 && encodingSelection != 1
						? (encodingSelection != 2 && encodingSelection != 3
								? (encodingSelection != 4 && encodingSelection != 5 && encodingSelection != 8
										? (encodingSelection != 6 && encodingSelection != 7 && encodingSelection != 9 ? (encodingSelection != 10 && encodingSelection != 11 ? 128 : 64)
												: 8)
										: 4)
								: 2)
						: 1;
				int var4 = (int) ((this.virtualSize < this.scrPos + (long) this.srcV.size() ? this.virtualSize
						: this.scrPos + (long) this.srcV.size()) - this.lastPos);
				if (var4 == 0 || var4 < var5 && var5 < 9)
				{
					return;
				}

				var4 = var5 < var4 ? var5 : (encodingSelection != 12 ? var4 : var4 >> 1 << 1);
				byte[] textBytes = new byte[var4];

				for (var4 = 0; var4 < textBytes.length; ++var4)
				{
					textBytes[var4] = ((byte[]) ((byte[]) this.srcV
							.get((int) (this.lastPos - this.scrPos + (long) var4))))[0];
				}

				var5 = textBytes[0];

				try
				{
					if (encodingSelection == 0)
					{
						for (var4 = 0; var4 < 8; ++var4)
						{
							displayText = displayText + ((var5 & 128) == 128 ? '1' : '0');
							if (var4 == 3)
							{
								displayText = displayText + ' ';
							}

							var5 <<= 1;
						}
					} else if (encodingSelection == 1)
					{
						displayText = Integer.toString(var5) + " / " + Integer.toString(var5 & 255);
					} else if (encodingSelection == 8)
					{
						displayText = this.topPanel.fForm
								.format((double) Float.intBitsToFloat((new BigInteger(textBytes)).intValue()));
					} else if (encodingSelection == 9)
					{
						displayText = this.topPanel.dForm.format(Double.longBitsToDouble((new BigInteger(textBytes)).longValue()));
					} else if (encodingSelection == 10)
					{
						displayText = new String(textBytes, this.topPanel.cp437Available ? "cp437" : "ISO-8859-1");
					} else if (encodingSelection == 11)
					{
						displayText = new String(textBytes, "UTF-8");
					} else if (encodingSelection == 12)
					{
						displayText = new String(textBytes,
								this.topPanel.viewCBox[0].getSelectedIndex() < 1 ? "UTF-16BE" : "UTF-16LE");
					} else if (encodingSelection == 13)
					{
						displayText = BinUtil.readShiftJisText(textBytes, 0);
					} else
					{
						byte[] var2 = new byte[encodingSelection < 6 ? encodingSelection : (encodingSelection == 6 ? 8 : 9)];
						var2[0] = 0;
						if (this.topPanel.viewCBox[0].getSelectedIndex() < 1)
						{
							if ((var2.length & 1) == 0)
							{
								System.arraycopy(textBytes, 0, var2, 0, var2.length);
							} else
							{
								System.arraycopy(textBytes, 0, var2, 1, var2.length - 1);
							}
						} else
						{
							for (var4 = var2.length & 1; var4 < var2.length; ++var4)
							{
								var2[var4] = textBytes[var2.length - var4 - 1];
							}
						}

						displayText = (new BigInteger(var2)).toString();
					}
				} catch (Exception e)
				{
					System.err.println("setStatus " + e);
				}

				this.topPanel.JTView.setText(displayText.replaceAll("\t", "  ").replaceAll("\n", "  "));
				this.topPanel.JTView.setCaretPosition(0);
			}

		}
	}

	private String coloredLong(long var1)
	{
		boolean var3 = this.topPanel.hexOffset;
		StringBuffer var4 = new StringBuffer(var3 ? "0x" : "");
		String var5 = var3 ? Long.toHexString(var1).toUpperCase() : Long.toString(var1);
		int var7 = var5.length();
		if (var3 && var7 % 2 == 1)
		{
			var5 = "0" + var5;
		}

		var7 = var3 ? 0 : var7 % 3;
		if (this.isApplet)
		{
			var4.append(var5);
		} else
		{
			int var6;
			for (var6 = 0; var6 < var5.length(); ++var6)
			{
				if (var6 % (var3 ? 4 : 6) == var7)
				{
					var4.append("<FONT color=blue>");
				}

				var4.append(var5.charAt(var6));
				if (var6 % (var3 ? 4 : 6) == (var3 ? 1 : 2 + var7))
				{
					var4.append("</FONT>");
				}
			}

			if (!var3 && var6 % 6 < 3 + var7)
			{
				var4.append("</FONT>");
			}
		}

		return var4.toString();
	}

	public void actionPerformed(ActionEvent var1)
	{
		if (var1.getSource() == this.timer)
		{
			int[] var2 = this.pos2XY(this.lastPos);
			this.caretVisible = ++this.caretVisible % 4;
			if ((this.caretVisible & 1) == 0)
			{
				this.paintImmediately(this.wChar * (this.nibArea ? this.xNib[var2[0] << 1] : this.xTxt[var2[0]]) - 1,
						this.hChar * var2[1] + 3, this.wChar + 1 << 1, this.hChar - 3);
			}
		} else if (var1.getSource() != this.InsDelB[4])
		{
			this.InsDelTF.setEnabled(true);
		} else
		{
			this.InsDelTF.setEnabled(true);
			boolean var4 = this.topPanel.hexOffset;
			String var3 = var4 ? Long.toHexString(this.clipboardSize).toUpperCase() : Long.toString(this.clipboardSize);
			if (var4 && var3.length() % 2 == 1)
			{
				var3 = "0" + var3;
			}

			this.InsDelTF.setText((var4 ? "0x" : "") + var3);
			this.fromClipboard(true);
		}

	}

	public void mouseClicked(MouseEvent var1)
	{
	}

	public void mouseReleased(MouseEvent var1)
	{
	}

	public void mouseEntered(MouseEvent var1)
	{
		this.setCursor(new Cursor(2));
	}

	public void mouseExited(MouseEvent var1)
	{
		this.setCursor((Cursor) null);
	}

	public void mousePressed(MouseEvent var1)
	{
		this.focus();
		this.lastPos = this.getCaretPos(var1);
		this.caretVisible = 0;
		if (var1.isShiftDown())
		{
			this.isNibLow = false;
			this.rePaint();
		} else
		{
			this.firstPos = this.lastPos;
			this.rePaint();
			this.timer.restart();
		}

	}

	public void mouseMoved(MouseEvent var1)
	{
	}

	public void mouseDragged(MouseEvent var1)
	{
		if (this.lastPos != (this.newPos = this.getCaretPos(var1)))
		{
			this.lastPos = this.newPos;
			this.isNibLow = false;
			this.rePaint();
		}

	}

	protected long getCaretPos(MouseEvent var1)
	{
		int var3 = -2;
		int var4 = (var1.getX() < this.getWidth() ? (0 < var1.getX() ? var1.getX() - 3 * this.wChar / 2 : 0)
				: this.getWidth()) / this.wChar;

		int var2;
		for (var2 = 0; var2 < this.xNib.length && var3 < 0; ++var2)
		{
			if (var4 < this.xNib[var2])
			{
				var3 = var2;
			}
		}

		if (var3 < 0 && var4 < this.xNib[this.xNib.length - 1] + 3)
		{
			var3 = this.xNib.length - 1;
		}

		this.nibArea = -2 < var3;
		this.isNibLow = 1 == (var3 & 1);
		var3 >>= 1;

		for (var2 = 0; var2 < this.xTxt.length && var3 < 0; ++var2)
		{
			if (var4 < this.xTxt[var2])
			{
				var3 = var2;
			}
		}

		this.newPos = this.scrPos
				+ (long) ((var1.getY() < this.getHeight() ? (0 < var1.getY() ? var1.getY() / this.hChar : 0)
						: this.maxRow - 1) << 4)
				+ (long) (var3 < 0 ? 15 : var3);
		if (this.virtualSize <= this.newPos || this.newPos < 0L)
		{
			this.newPos = this.virtualSize;
			this.isNibLow = false;
		}

		if ((this.lastPos != this.newPos || this.lastPos + 1L != this.newPos) && !this.undoStack.isEmpty())
		{
			((EditState) this.undoStack.lastElement()).isEditing = false;
		}

		return this.newPos;
	}

	public void mouseWheelMoved(MouseWheelEvent var1)
	{
		if (!var1.isControlDown())
		{
			this.slideScr(this.scrPos
					+ (long) var1.getWheelRotation() * (var1.getScrollType() == 0 ? 32L : (long) this.maxPos + 1L),
					false);
		} else
		{
			this.setGrid(this.fontSize - 3 * var1.getWheelRotation());
			this.slideScr(this.scrPos, true);
			this.rePaint();
		}

	}

	public void keyReleased(KeyEvent var1)
	{
	}

	public void keyPressed(KeyEvent var1)
	{
		switch (var1.getKeyCode())
		{
		case 8:
			this.KeyFromMenu(90);
		case 33:
			this.newPos = this.lastPos - ((long) this.maxPos - 15L);
			break;
		case 34:
			this.newPos = this.lastPos + ((long) this.maxPos - 15L);
			break;
		case 35:
			if (!var1.isControlDown() && this.lastPos + 15L <= this.virtualSize)
			{
				this.newPos = this.lastPos | 15L;
			} else
			{
				this.newPos = this.virtualSize - 1L;
			}
			break;
		case 36:
			if (!var1.isControlDown())
			{
				this.newPos = this.lastPos & -16L;
			} else
			{
				this.newPos = 0L;
			}
			break;
		case 37:
		case 226:
			this.newPos = this.lastPos - (this.isNibLow ? 0L : 1L);
			break;
		case 38:
		case 224:
			this.newPos = this.lastPos - 16L;
			break;
		case 39:
		case 227:
			this.newPos = this.lastPos + 1L;
			break;
		case 40:
		case 225:
			this.newPos = this.lastPos + 16L;
			break;
		default:
			return;
		}

		this.isNibLow = false;
		long var2 = this.newPos - this.lastPos;
		if (this.newPos == -1L && this.lastPos < 99999L)
		{
			this.lastPos = 0L;
		} else if (this.newPos <= 0L && this.lastPos < 99999L)
		{
			this.lastPos = this.newPos & 15L;
		} else if ((this.newPos == this.virtualSize || this.newPos == this.virtualSize + 1L) && var2 == 1L)
		{
			this.lastPos = this.virtualSize;
		} else if (this.virtualSize > this.newPos && this.newPos >= 0L)
		{
			this.lastPos = this.newPos;
		} else
		{
			this.lastPos = (this.virtualSize - 1L & -16L) + (this.newPos & 15L)
					- ((this.newPos & 15L) <= (this.virtualSize - 1L & 15L) ? 0L : 16L);
		}

		if (!var1.isShiftDown())
		{
			this.firstPos = this.lastPos;
		}

		if (!this.undoStack.empty() && ((EditState) this.undoStack.lastElement()).isEditing)
		{
			((EditState) this.undoStack.lastElement()).isEditing = false;
		}

		if (9223372036854775792L - (long) this.maxPos + 16L < this.scrPos)
		{
			this.slideScr(this.scrPos = Long.MAX_VALUE & -16L - (long) this.maxPos + 15L, true);
		} else if (this.newPos >= this.scrPos && var2 != -((long) this.maxPos - 15L))
		{
			if (this.scrPos >= this.newPos - (long) this.maxPos && var2 != (long) this.maxPos - 15L)
			{
				this.timer.stop();
				this.rePaint();
				this.caretVisible = 0;
				this.timer.restart();
			} else
			{
				this.slideScr(this.scrPos += var2 + 15L & -16L, true);
			}
		} else
		{
			this.slideScr(this.scrPos = this.scrPos + var2 & -16L, true);
		}

	}

	public void KeyFromMenu(int keyValue)
	{
		char[] hexValues = new char[]
		{ '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		boolean var4 = this.saveModule != null || this.find != null;
		Object[] var8 = new Object[]
		{ "<html>Valid entry are:<br>decimal, hexa (0x..) or percent (..%)" };
		int var5;
		String var6;
		long var10;
		long var12;
		switch (keyValue)
		{
		case KeyEvent.VK_MINUS:
		case KeyEvent.VK_ADD:
		case KeyEvent.VK_SUBTRACT:
		case KeyEvent.VK_PLUS:
			this.setGrid(this.fontSize + (keyValue != 107 && keyValue != 521 ? -3 : 3));
			this.slideScr(this.scrPos, true);
			this.rePaint();
			break;
		case KeyEvent.VK_A:
			if (this.virtualSize < 2147483648L)
			{
				this.firstPos = 0L;
				this.lastPos = this.virtualSize;
				this.rePaint();
			} else
			{
				JOptionPane.showMessageDialog(this, "Selection cannot be greater than 2GiB");
			}
			break;
		case KeyEvent.VK_C:
		case KeyEvent.VK_X:
			if (this.firstPos != this.lastPos && !var4 && !this.isApplet)
			{
				boolean var21 = this.firstPos < this.lastPos;
				var10 = var21 ? this.firstPos : this.lastPos;
				var12 = !var21 ? this.firstPos : this.lastPos;

				try
				{
					this.XCV = this.virtualStack(var10, var12);
					char[] var22;
					if (!this.nibArea)
					{
						var22 = new char[this.XCV.size()];

						for (var5 = 0; var5 < this.XCV.size(); ++var5)
						{
							var22[var5] = (char) (255 & ((byte[]) ((byte[]) this.XCV.get(var5)))[0]);
							if (Character.isISOControl(var22[var5]) && "\t\\u000A\f\\u000D".indexOf(var22[var5]) < 0)
							{
								throw new Exception("\'" + (var22[var5] & 255) + "\' isIsoControl");
							}
						}
					} else
					{
						var22 = new char[this.XCV.size() << 1];

						for (var5 = 0; var5 < this.XCV.size(); ++var5)
						{
							var22[2 * var5] = hexValues[(255 & ((byte[]) ((byte[]) this.XCV.get(var5)))[0]) >> 4];
							var22[2 * var5 + 1] = hexValues[(255 & ((byte[]) ((byte[]) this.XCV.get(var5)))[0]) % 16];
						}
					}

					this.clipboard.setContents(new StringSelection(new String(var22)), (ClipboardOwner) null);
				} catch (Exception var20)
				{
					JOptionPane.showMessageDialog(this, "Can\'t copy text into the clipboard:\n" + var20);
				}

				if (keyValue == 88)
				{
					this.pushHObj(new EditState(var10, var12 - var10, 8), (String) null);
				}
			}
			break;
		case KeyEvent.VK_D:
		case KeyEvent.VK_U:
			var12 = 0L;
			long var14 = this.virtualSize;
			this.isNibLow = false;
			Iterator var23;
			if (this.markV != null && 0 < this.markV.size())
			{
				for (var23 = this.markV.iterator(); var23
						.hasNext(); var14 = this.lastPos < var10 && var10 < var14 ? var10 : var14)
				{
					var10 = ((Long) var23.next()).longValue();
					var12 = var12 < var10 && var10 < this.lastPos ? var10 : var12;
				}
			}

			if (this.MarkV != null && 0 < this.MarkV.size())
			{
				for (var23 = this.MarkV.iterator(); var23
						.hasNext(); var14 = this.lastPos < var10 && var10 < var14 ? var10 : var14)
				{
					var10 = ((Long) var23.next()).longValue();
					var12 = var12 < var10 && var10 < this.lastPos ? var10 : var12;
				}
			}

			this.firstPos = this.lastPos = keyValue == 85 ? var12 : var14;
			this.slideScr(0L, true);
			break;
		case KeyEvent.VK_F:
			if (this.find == null)
			{
				this.topPanel.find();
			}
			break;
		case KeyEvent.VK_G:
			this.goTo(JOptionPane.showInputDialog(this, var8, "Hexeditor.jar: GoTo", JOptionPane.PLAIN_MESSAGE));
			break;
		case KeyEvent.VK_J:
			if (loadedFile != null)
			{
				GNT4TextDisplay gnt4TextDisplay = new GNT4TextDisplay(this);
				gnt4TextDisplay.displayTextGNT4();
			}
			else
			{
				JOptionPane.showMessageDialog(this, "Please open a file in the hex editor.");
			}
			break;
		case KeyEvent.VK_1:
			if (loadedFile != null)
			{
				GNTSPTranslator gntSPTranslator = new GNTSPTranslator(this);
				gntSPTranslator.translateGNTSP();
			}
			else
			{
				JOptionPane.showMessageDialog(this, "Please open a file in the hex editor.");
			}
			break;
		case KeyEvent.VK_K:
			if (loadedFile != null)
			{
				GNT4Translator gnt4Translator = new GNT4Translator(this);
				gnt4Translator.translateGNT4();
			}
			else
			{
				JOptionPane.showMessageDialog(this, "Please open a file in the hex editor.");
			}
			break;
		case KeyEvent.VK_M:
			Long var9 = new Long(this.lastPos);
			if (this.markV.remove(var9))
			{
				this.MarkV.add(var9);
			} else if (!this.MarkV.remove(var9))
			{
				this.markV.add(var9);
			}

			this.rePaint();
			break;
		case KeyEvent.VK_O:
		case KeyEvent.VK_Q:
			if (!var4 && !this.isApplet)
			{
				if (this.currentFile == null && !this.undoStack.empty() || this.currentFile != null && 1 < this.undoStack.size())
				{
					var5 = JOptionPane.showConfirmDialog(this, "Save the current modified file?");
					if (var5 == 2)
					{
						break;
					}

					if (var5 == 0)
					{
						this.save();
						break;
					}
				}

				this.closeFile();
				if (keyValue == 79 && this.jFC.showOpenDialog(this) == 0)
				{
					this.loadFile(this.jFC.getSelectedFile());
				}
			} else
			{
				JOptionPane.showMessageDialog(this, "Busy, save or find running.");
			}
			break;
		case KeyEvent.VK_P:
			var5 = this.wChar * (this.xTxt[15] + 9);
			var5 = var5 < this.getWidth() ? var5 : this.getWidth();
			BufferedImage var16 = new BufferedImage(var5, this.hLimit + 10, 8);
			this.paintImg(var16.getGraphics(), false);
			var6 = this.loadedFile.getPath();
			var5 = 1;

			File var17;
			do
			{
				var17 = new File(var6 + var5 + ".png");
				++var5;
			} while (var17.exists());

			try
			{
				ImageIO.write(var16, "png", var17);
			} catch (IOException var19)
			{
				var19.printStackTrace();
			}
			break;
		case KeyEvent.VK_S:
			if (this.saveModule == null && !this.isApplet)
			{
				this.save();
			}
			break;
		case KeyEvent.VK_T:
			this.nibArea = !this.nibArea;
			this.isNibLow = false;
			this.timer.stop();
			this.repaint();
			this.caretVisible = 0;
			this.timer.restart();
			break;
		case KeyEvent.VK_V:
			if (!var4 && !this.isApplet && (var6 = fromClipboard(true)) != null)
			{
				pushHObj(new EditState(this.lastPos, (long) var6.length(), 4), var6);
			}
			break;
		case KeyEvent.VK_W:
			this.isOled = !this.isOled;
			this.rePaint();
			break;
		case KeyEvent.VK_Y:
			if (this.eObjCtrlY != null)
			{
				this.pushHObj(this.eObjCtrlY, (String) null);
				this.eObjCtrlY = null;
			} else if (this.byteCtrlY != null)
			{
				this.eObj.stack.push(this.byteCtrlY);
				this.firstPos = ++this.lastPos;
				if (this.scrPos < this.lastPos - (long) this.maxPos)
				{
					this.scrPos += 16L;
				}

				this.doVirtual();
				this.byteCtrlY = null;
			}
			break;
		case KeyEvent.VK_Z:
			if (!var4 && !this.undoStack.empty() && (1 < this.undoStack.size()
					|| 1 == this.undoStack.size() && 2 < ((EditState) this.undoStack.lastElement()).a1))
			{
				this.eObj = this.eObjCtrlY = (EditState) this.undoStack.lastElement();
				if (this.eObj.isEditing && !this.eObj.stack.empty())
				{
					this.byteCtrlY = this.eObj.stack.pop();
					if (!this.isNibLow)
					{
						this.firstPos = --this.lastPos;
					} else
					{
						this.isNibLow = false;
					}

					if ((this.eObj.size = (long) this.eObj.stack.size()) == 0L)
					{
						this.undoStack.pop();
						this.byteCtrlY = null;
					} else
					{
						this.eObjCtrlY = null;
					}
				} else
				{
					this.firstPos = this.lastPos = this.eObj.p1;
					this.undoStack.pop();
				}

				this.doVirtual();
				this.slideScr(this.scrPos, true);
			}
			break;
		case KeyEvent.VK_DELETE:
		case KeyEvent.VK_INSERT:
			if (!var4)
			{
				this.InsDelB[keyValue == 127 ? 0 : 1].setSelected(true);
				this.InsDelTF.setEnabled(true);
				String var7 = fromClipboard(false);
				if (JOptionPane.showConfirmDialog(this, this.InsDelOption, "Hexeditor.jar: DEL/INS", 2) != 2)
				{
					label298:
					{
						if (this.InsDelB[4].isSelected())
						{
							if (!this.isApplet && var7 != null
									&& this.virtualSize + (long) var7.length() < Long.MAX_VALUE)
							{
								this.pushHObj(new EditState(this.lastPos, (long) var7.length(), 6), var7);
							}
						} else
						{
							if ((var6 = this.InsDelTF.getText()) == null)
							{
								break label298;
							}

							this.String2long(var6);
							if (this.longInput < 1L)
							{
								break label298;
							}

							if (this.InsDelB[0].isSelected())
							{
								this.longInput = this.longInput + this.lastPos < this.virtualSize ? this.longInput
										: this.virtualSize - this.lastPos;
							} else if (Long.MAX_VALUE < this.virtualSize + this.longInput)
							{
								this.longInput = Long.MAX_VALUE - this.virtualSize;
							}

							this.pushHObj(
									new EditState(this.lastPos, this.longInput, this.InsDelB[0].isSelected() ? 8 : 6),
									this.InsDelB[0].isSelected() ? null
											: (this.InsDelB[1].isSelected() ? " "
													: (this.InsDelB[2].isSelected() ? "ÿ" : " ")));
						}

						this.isNibLow = false;
						this.eObjCtrlY = null;
						this.byteCtrlY = null;
						this.InsDelTF.setText("");
					}
				}
			}
		}

		this.focus();
	}

	public void pushHObj(EditState editedState, String newCharacters)
	{
		if (!undoStack.isEmpty())
		{
			(eObj = (EditState) undoStack.lastElement()).isEditing = false;
		}

		if (newCharacters != null)
		{
			for (byte charByte : newCharacters.getBytes())
			{
				editedState.stack.push(charByte);
			}
		}

		undoStack.push(editedState);
		firstPos = lastPos;
		doVirtual();
	}
	
	public void pushHObjBytes(EditState editedState, byte[] charBytes)
	{
		if (!undoStack.isEmpty())
		{
			(eObj = (EditState) undoStack.lastElement()).isEditing = false;
		}

		if (charBytes != null)
		{
			for (byte charByte : charBytes)
			{
				editedState.stack.push(charByte);
			}
		}

		undoStack.push(editedState);
		firstPos = lastPos;
		doVirtual();
	}

	private String fromClipboard(boolean showErrorMessages)
	{
		String text = null;
		StringBuffer var5 = new StringBuffer();

		try
		{
			text = (String) clipboard.getContents((Object) null).getTransferData(DataFlavor.stringFlavor);
			if (text == null || text.length() < 1)
			{
				throw new Exception("nothing to paste");
			}

			if (Long.MAX_VALUE < lastPos + (long) text.length())
			{
				throw new Exception("file cannot exceed Long.MAX_VALUE");
			}

			if (nibArea)
			{
				if (text.length() % 2 != 0)
				{
					throw new Exception("Nibble area, String must be an hexa string with odd characters.");
				}

				text = text.toUpperCase();

				for (int charIndex = 0; charIndex < text.length(); charIndex += 2)
				{
					if ("0123456789ABCDEFabcdef".indexOf(text.charAt(charIndex)) < 0
							|| "0123456789ABCDEFabcdef".indexOf(text.charAt(charIndex + 1)) < 0)
					{
						throw new Exception("Nibble area, String must be an hexa string.");
					}
					// This will get the character representations of the byte
					// Example: 4 for the first and 8 for the second will append the char H
					var5.append((char) (("0123456789ABCDEFabcdef".indexOf(text.charAt(charIndex)) << 4)
							+ "0123456789ABCDEFabcdef".indexOf(text.charAt(charIndex + 1))));
				}

				text = var5.toString();
			}

			clipboardSize = (long) text.length();
		} catch (Exception e)
		{
			text = null;
			clipboardSize = 0L;
			if (showErrorMessages)
			{
				JOptionPane.showMessageDialog(this, "Can\'t paste text from the clipboard:\n" + e);
			}
		}

		return text;
	}

	protected void String2long(String inputText)
	{
		this.longInput = -1L;
		int var5 = -1;
		int var6 = 0;
		int var2;
		if ((var2 = inputText.length()) != 0 && !inputText.equals("-") && !inputText.equals("+"))
		{
			BigDecimal var7 = null;
			BigInteger var8 = null;
			boolean var9 = false;
			boolean isHex = inputText.startsWith("0x") || inputText.startsWith("Ox") || inputText.startsWith("ox");
			String var11 = "yzafpnµm kMGTPEZY";
			String var12 = "KMGTPE";
			inputText.replaceAll(" ", "");
			if (1 < inputText.length() && !isHex)
			{
				if (var9 = 105 == inputText.charAt(inputText.length() - 1))
				{
					var5 = var12.indexOf(inputText.charAt(inputText.length() - 2));
				} else
				{
					int var18 = inputText.endsWith("c") ? -2
							: (inputText.endsWith("d") ? -1
									: (inputText.endsWith("da") ? 1
											: (inputText.endsWith("h") ? 2 : (inputText.endsWith("%") ? -2 : 0))));
					int var19 = var11.indexOf(inputText.charAt(inputText.length() - 1));
					var6 = var18 != 0 ? var18 : (-1 < var19 ? var19 * 3 - 24 : 0);
				}
			}

			if (!var9 || inputText.length() >= 3 && var5 >= 0)
			{
				if (isHex)
				{
					if (inputText.length() < 3)
					{
						return;
					}

					try
					{
						var8 = new BigInteger(inputText.substring(2, var2), 16);
					} catch (Exception var16)
					{
						return;
					}
				} else
				{
					while (0 < var2)
					{
						try
						{
							var7 = new BigDecimal(inputText.substring(0, var2));
							break;
						} catch (Exception var17)
						{
							--var2;
						}
					}

					if (var2 == 0 || var7 == null)
					{
						return;
					}

					var7 = var7.scaleByPowerOfTen(var6).multiply(BigDecimal.valueOf(1L << 10 * (var5 + 1)));
					var8 = var7.toBigInteger();
				}

				long var14 = var8.longValue();
				if (var8.signum() < 0)
				{
					this.longInput = -1L;
				} else if (BigInteger.valueOf(Long.MAX_VALUE).compareTo(var8) < 0)
				{
					this.longInput = Long.MAX_VALUE;
				} else
				{
					this.longInput = var14;
				}

			}
		}
	}

	public void keyTyped(KeyEvent keyEvent)
	{
		boolean var3 = saveModule != null || find != null;
		char keyChar = keyEvent.getKeyChar();
		int var7 = "0123456789ABCDEFabcdef".indexOf(Character.toUpperCase(keyChar));
		if (!keyEvent.isAltDown() && !keyEvent.isControlDown() && !Character.isISOControl(keyChar) && keyChar < 256
				&& keyEvent.getSource() == this && 0L < virtualSize && !var3 && (!nibArea || -1 < var7))
		{
			if (!undoStack.empty() && ((EditState) undoStack.lastElement()).isEditing)
			{
				eObj = (EditState) this.undoStack.lastElement();
			} else
			{
				eObj = new EditState(this.lastPos, 0L, 4);
				undoStack.push(eObj);
				eObj.isEditing = true;
			}

			if (!nibArea)
			{
				eObj.stack.push(Byte.valueOf((byte) keyChar));
				if (lastPos < Long.MAX_VALUE)
				{
					firstPos = ++lastPos;
				}
			} else if (-1 < "0123456789ABCDEFabcdef".indexOf(keyChar))
			{
				byte var4;
				if ((int) (lastPos - scrPos) < srcV.size())
				{
					var4 = ((byte[]) ((byte[]) srcV.get((int) (lastPos - scrPos))))[0];
				} else
				{
					var4 = 0;
				}

				var4 = (byte) (isNibLow ? (var4 & 240) + var7 : (var7 << 4) + (var4 & 15));
				if (isNibLow && !eObj.stack.empty()
						&& eObj.p1 + (long) eObj.stack.size() == lastPos + 1L)
				{
					eObj.stack.pop();
				}

				eObj.stack.push(Byte.valueOf(var4));
				if (!(isNibLow = !isNibLow) && lastPos < Long.MAX_VALUE)
				{
					firstPos = ++lastPos;
				}
			}

			if (scrPos < lastPos - (long) maxPos)
			{
				scrPos += 16L;
			}

			eObjCtrlY = null;
			byteCtrlY = null;
			doVirtual();
		}

	}

	protected void doVirtual()
	{
		this.v1.clear();
		if (this.undoStack.isEmpty())
		{
			this.scrPos = this.firstPos = this.lastPos = this.virtualSize = 0L;
			this.markV.clear();
			this.MarkV.clear();
			this.jSbSource = false;
			this.jSB.setValue(0);
			this.topPanel.JTFile.setText("");
			this.topPanel.fJTF[1].setText("");
			this.setGrid(this.fontSize);
			this.setSrc();
			this.rePaint();
			this.caretVisible = 0;
			this.timer.restart();
		} else
		{
			EditState var5 = (EditState) this.undoStack.lastElement();
			if (var5.a1 != 6 && var5.a1 != 8)
			{
				var5.size = var5.stack.size() != 0 ? (long) var5.stack.size() : var5.size;
				var5.virtualSize = var5.p1 + var5.size;
			}

			if (!this.isApplet)
			{
				String var10 = this.topPanel.JTFile.getText();
				if (var10.endsWith(" *"))
				{
					var10 = var10.substring(0, var10.length() - 2);
				}

				this.topPanel.JTFile.setText(var10 + (1 < this.undoStack.size() ? " *" : ""));
			}

			var5 = (EditState) this.undoStack.firstElement();
			this.v1.add(new EditState(0L, var5.virtualSize, var5.offset, var5));

			for (int var1 = 1; var1 < this.undoStack.size(); ++var1)
			{
				var5 = (EditState) this.undoStack.get(var1);
				EditState var9 = var5.a1 == 8 ? null : new EditState(var5.p1, var5.virtualSize, var5.offset, var5);
				long var3 = var5.a1 == 6 ? var5.size : (var5.a1 == 8 ? -var5.size : 0L);
				int var2 = this.v1.size() - 1;
				if (var5 != null && var5.p1 != var5.virtualSize)
				{
					for (; -1 < var2; --var2)
					{
						EditState var6 = (EditState) this.v1.get(var2);
						if (var2 == this.v1.size() - 1 && var6.virtualSize == var5.p1)
						{
							this.v1AddNoNull(var2 + 1, var9);
							break;
						}

						if (var5.virtualSize <= var6.p1)
						{
							var6.p1 += var3;
							var6.virtualSize += var3;
						} else
						{
							if (var5.a1 == 6 && var6.p1 == var5.p1)
							{
								var6.p1 += var3;
								var6.virtualSize += var3;
								this.v1AddNoNull(var2, var9);
								break;
							}

							if (var5.a1 != 6 && var5.p1 <= var6.p1 && var6.virtualSize <= var5.virtualSize)
							{
								this.v1.remove(var2);
								if (var6.virtualSize == var5.virtualSize)
								{
									this.v1AddNoNull(var2, var9);
								}
							} else
							{
								if (var5.a1 != 6 && var5.p1 < var6.virtualSize && var6.virtualSize <= var5.virtualSize)
								{
									if (var6.virtualSize == var5.virtualSize)
									{
										this.v1AddNoNull(var2 + 1, var9);
									}

									var6.virtualSize = var5.p1;
									break;
								}

								if (var5.a1 == 6 || var5.p1 > var6.p1 || var6.p1 >= var5.virtualSize)
								{
									if (var6.p1 < var5.p1 && (var5.virtualSize < var6.virtualSize || var5.a1 == 6))
									{
										EditState var7 = this.v1Clone(var6);
										var7.virtualSize = var5.p1;
										var6.offset += (var5.a1 == 6 ? var5.p1 : var5.virtualSize) - var6.p1;
										var6.p1 = var5.a1 == 8 ? var5.p1 : var5.virtualSize;
										var6.virtualSize += var3;
										if (var6.p1 == var6.virtualSize)
										{
											this.v1.remove(var2);
										}

										this.v1AddNoNull(var2, var9);
										this.v1AddNoNull(var2, var7);
									}
									break;
								}

								var6.offset += var5.virtualSize - var6.p1;
								var6.p1 = var5.virtualSize;
								this.v1AddNoNull(var2, var9);
							}
						}
					}
				}
			}

			long newSize = this.v1 != null && this.v1.size() != 0 ? ((EditState) this.v1.lastElement()).virtualSize : 0L;
			if (this.virtualSize != newSize)
			{
				this.virtualSize = newSize;
				this.setGrid(this.fontSize);
			}

			this.setSrc();
			this.rePaint();
			this.caretVisible = 0;
			this.timer.restart();
		}
	}

	protected void v1AddNoNull(int var1, EditState var2)
	{
		if (var2 != null && -1 < var1 && var1 <= this.v1.size() && var2.p1 != var2.virtualSize)
		{
			this.v1.add(var1, var2);
		}

	}

	protected EditState v1Clone(EditState var1)
	{
		if (var1 != null && var1.p1 != var1.virtualSize)
		{
			EditState var2 = new EditState(var1.p1, var1.virtualSize, var1.offset, var1.o);
			return var2;
		} else
		{
			return null;
		}
	}

	protected Vector virtualStack(long var1, long var3)
	{
		byte[] var5 = new byte[2];
		int var8 = 0;
		int var10 = 0;
		long var17 = var1;
		EditState var19 = null;

		Vector var20;
		for (var20 = new Vector(); var8 < this.v1.size(); ++var8)
		{
			var19 = (EditState) this.v1.get(var8);
			if (var1 < var19.virtualSize)
			{
				break;
			}
		}

		while (var8 < this.v1.size())
		{
			var19 = (EditState) this.v1.get(var8);
			long var11 = var19.p1 - var19.offset;
			long var13 = var19.virtualSize < var3 ? var19.virtualSize : var3;
			if (var19.o.a1 != 4 && var19.o.a1 != 2 && (var19.o.a1 != 6 || 1 >= var19.o.stack.size()))
			{
				if (var19.o.a1 == 6)
				{
					var5[1] = (byte) var19.o.a1;

					for (var5[0] = (var19.o.stack.get(0)).byteValue(); var17 < var13; ++var17)
					{
						var20.add(var5.clone());
					}
				} else
				{
					try
					{
						var5[1] = (byte) (var19.p1 != var19.offset ? 1 : var19.o.a1);
						byte[] var6 = new byte[(int) (var13 - var17)];
						this.currentFile.seek(var17 - var11);
						int var9 = 0;

						int var7;
						while (var9 < var6.length)
						{
							var7 = this.currentFile.read(var6, var9, var6.length - var9);
							if (var7 < 0)
							{
								throw new IOException("EOF");
							}

							var9 += var7;
							if (var7 == 0)
							{
								++var10;
								if (var10 == 9)
								{
									var17 = var13;
									JOptionPane.showMessageDialog(this, "Unable to access file");
								}
							}
						}

						for (var7 = 0; var17 < var13 || var7 < var10; ++var7)
						{
							var5[0] = var6[var7];
							var20.add(var5.clone());
							++var17;
						}
					} catch (Exception var22)
					{
						System.err.println("virtualStack " + var22);
					}
				}
			} else
			{
				for (var5[1] = (byte) (var19.a1 == 2 && var19.p1 != var19.offset ? 1
						: var19.o.a1); var17 < var13; ++var17)
				{
					var5[0] = (var19.o.stack.get((int) (var17 - var11))).byteValue();
					var20.add(var5.clone());
				}
			}

			if (var3 < var19.virtualSize)
			{
				break;
			}

			++var8;
		}

		return var20;
	}

	protected void setSrc()
	{
		this.srcV = this.virtualStack(this.scrPos,
				this.scrPos + (long) this.maxPos + 1L < 0L ? Long.MAX_VALUE : this.scrPos + (long) this.maxPos + 1L);
	}

	protected boolean save()
	{
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(0);
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setDialogTitle("Save as...");
		fileChooser.setDialogType(1);
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setDragEnabled(false);
		fileChooser.setFileFilter(new BinFileFilter());
		if (loadedFile != null && loadedFile.canWrite())
		{
			fileChooser.setSelectedFile(this.loadedFile);
		} else
		{
			fileChooser.setCurrentDirectory(
					loadedFile == null ? new File(System.getProperty("user.dir")) : loadedFile.getParentFile());
		}

		if (fileChooser.showSaveDialog(this) != 0)
		{
			return false;
		} else
		{
			this.topPanel.saveRunning(true);
			if (saveModule != null)
			{
				saveModule.interrupt();
			}

			(saveModule = new SaveModule()).setDaemon(true);
			saveModule.f1 = loadedFile;
			saveModule.f2 = fileChooser.getSelectedFile();
			saveModule.virtual = v1;
			saveModule.hexV = this;
			saveModule.jPBar = topPanel.savePBar;
			saveModule.start();
			return true;
		}
	}

	protected void saveCleanUp(File var1)
	{
		if (var1 != null)
		{
			this.loadedFile = new File(var1, "");
			this.topPanel.JTFile.setText(this.loadedFile.toString() + (this.loadedFile.canWrite() ? "" : " (ReadOnly)"));

			try
			{
				this.currentFile = new RandomAccessFile(this.loadedFile, "rw");
			} catch (Exception var3)
			{
				System.err.println(var3);
			}

			this.undoStack.clear();
			this.undoStack.push(new EditState(0L, this.loadedFile.length(), 0));
			this.doVirtual();
		}

		this.topPanel.saveRunning(false);
		this.saveModule = null;
		this.eObjCtrlY = null;
		this.byteCtrlY = null;
	}

	protected void find1()
	{
		if (this.virtualSize != 0L && (this.topPanel.finByte != null || this.topPanel.findChar != null))
		{
			this.String2long(this.topPanel.fJTF[1].getText());
			if (this.find != null)
			{
				this.find.interrupt();
			}

			(this.find = new FindModule()).setDaemon(true);
			this.find.f1 = this.loadedFile;
			this.find.v1 = this.v1;
			this.find.isApplet = this.isApplet;
			this.find.ignoreCase = this.topPanel.useFindChar;
			this.find.pos = this.longInput < 0L ? (this.firstPos == this.lastPos ? this.lastPos : this.lastPos + 1L)
					: (this.virtualSize - 1L < this.longInput ? this.virtualSize - 1L
							: (this.virtualSize - 1L == this.longInput ? 0L : this.longInput + 1L));
			this.find.inBytes = this.topPanel.finByte;
			this.find.inChars = this.topPanel.findChar;
			this.find.wordSize = 1 << this.topPanel.fJCB[3].getSelectedIndex();
			this.find.hexV = this;
			this.find.jPBar = this.topPanel.findPBar;
			this.topPanel.findRunning(true);
			this.find.start();
		}
	}

	protected void find2(long var1, long var3)
	{
		this.slideScr(0L, true);
		StringBuffer var5 = new StringBuffer("0x");
		String var6 = Long.toHexString(var3).toUpperCase();
		if (var6.length() % 2 == 1)
		{
			var5.append("0");
		}

		this.topPanel.findRunning(false);
		this.topPanel.fJTF[1].setText(var5.append(var6).toString());
		this.find = null;
		this.lastPos = var3;
		this.firstPos = var1;
		this.isNibLow = false;
		this.slideScr(0L, true);
	}
	
	/**
	 * Gets the byte from the list of bytes at a specific location.
	 * Each byte includes a color; this color value is not included in the return value.
	 * 
	 * @param offset the location of the byte to retrieve from the current file
	 * @return the byte at the specified location
	 */
	public byte getByte(int offset)
	{
		byte[] byteColorCombo = (byte[]) srcV.get(offset);
		return byteColorCombo[0];
	}
}
