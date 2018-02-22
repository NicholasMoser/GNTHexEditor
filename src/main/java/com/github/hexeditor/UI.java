package com.github.hexeditor;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Properties;
import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class UI extends JApplet
{

	private static final long serialVersionUID = -3041749654374521418L;
	private static boolean applet = false;
	private static JPanel panel = new JPanel(new BorderLayout());
	private static Runtime runtime = Runtime.getRuntime();
	private static String[] arg = null;
	public static JRootPane rootPane = null;
	public static String browse = null;
	public static final String htmlBase = null;
	public static final String htmlReport = null;
	public static final String htmlEnd = null;

	static void all()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception var1)
		{
			;
		}

		panel.add(new BinPanel(applet, arg));
	}

	public void init()
	{
		applet = true;
		rootPane = this.getRootPane();
		all();
		String var1 = this.getParameter("JAVAMINVERSION");
		if (System.getProperty("java.specification.version").compareToIgnoreCase(var1) < 0)
		{
			this.getContentPane().add(new JLabel(
					"<html><h1>Java version found: " + System.getProperty("java.version") + ", needed: " + var1),
					"Center");
		} else
		{
			this.getContentPane().add(panel, "Center");
		}

	}

	public static void main(String[] var0)
	{
		arg = (String[]) ((String[]) var0.clone());
		if (!applet && var0 != null && 0 < var0.length)
		{
			for (int var1 = 0; var1 < var0.length; ++var1)
			{
				if (var0[var1].toLowerCase().equals("-bug"))
				{
					bugReport();
				}
			}
		}

		SwingUtilities.invokeLater(new UI$1(var0));
	}

	private static void bugReport()
	{
		try
		{
			System.setErr(new PrintStream(
					new FileOutputStream(new File(System.getProperty("user.dir"), "Hexeditor.jar_BugReport.txt"))));
			StringBuffer var1 = new StringBuffer(
					"if you find errors, feel free to send me a mail with a short explaination and this file at: @T \r\n\r\nHexeditor.jar 2014-07-29\r\n");
			Properties var2 = System.getProperties();
			Enumeration var3 = var2.propertyNames();

			while (var3.hasMoreElements())
			{
				String var0 = (String) var3.nextElement();
				if (" user.name user.home ".indexOf(var0) == -1)
				{
					try
					{
						var1.append(var0 + " " + var2.getProperty(var0) + "\n");
					} catch (Exception var5)
					{
						var1.append(var0 + " SECURITY EXCEPTION!\n");
					}
				}
			}

			var1.append("\r\nµP\t" + runtime.availableProcessors());
			var1.append("\r\nMem(MiB), free/total/max: " + (runtime.freeMemory() >> 20) + "/" + (runtime.totalMemory() >> 20)
					+ "/" + (runtime.maxMemory() >> 20));
			var1.append("\r\n\r\nError messages:");
			System.err.println(var1);
		} catch (Exception var6)
		{
			;
		}

	}

	static JPanel access$000()
	{
		return panel;
	}

}
