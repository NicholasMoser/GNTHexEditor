package com.github.hexeditor;

import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

final class UI$1 implements Runnable
{

	private final String[] val$args;

	UI$1(String[] var1)
	{
		this.val$args = var1;
	}

	public void run()
	{
		JFrame frame = new JFrame(
				1 < this.val$args.length && this.val$args[0].equals("-slave")
						? "hexeditor.jar currently linked to  " + this.val$args[1]
						: "GNT Hex Editor",
				GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration());
		List<Image> icons = new ArrayList<Image>();
		icons.add((new ImageIcon(getClass().getClassLoader().getResource("com/github/hexeditor/naru16.gif"))).getImage());
		icons.add((new ImageIcon(getClass().getClassLoader().getResource("com/github/hexeditor/naru32.gif"))).getImage());
		icons.add((new ImageIcon(getClass().getClassLoader().getResource("com/github/hexeditor/naru64.gif"))).getImage());
		icons.add((new ImageIcon(getClass().getClassLoader().getResource("com/github/hexeditor/naru128.gif"))).getImage());
		frame.setIconImages(icons);
		frame.setDefaultCloseOperation(3);
		Rectangle graphicsBounds = frame.getGraphicsConfiguration().getBounds();
		Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(frame.getGraphicsConfiguration());
		int var4 = graphicsBounds.width - screenInsets.left - screenInsets.right;
		int var5 = graphicsBounds.height - screenInsets.top - screenInsets.bottom;
		var4 = 700 < var4 ? 700 : var4;
		var5 = 999 < var5 ? var5 : var5;
		if (1 < this.val$args.length && this.val$args[0].equals("-slave"))
		{
			frame.setBounds(graphicsBounds.x + graphicsBounds.width - var4, graphicsBounds.y + (graphicsBounds.height + screenInsets.top - screenInsets.bottom - var5) >> 1,
					var4, var5);
		} else
		{
			frame.setBounds(graphicsBounds.x + (graphicsBounds.width + screenInsets.left - screenInsets.right - var4) >> 1,
					graphicsBounds.y + (graphicsBounds.height + screenInsets.top - screenInsets.bottom - var5) >> 1, var4, var5);
		}

		UI.rootPane = frame.getRootPane();
		UI.all();
		JFrame.setDefaultLookAndFeelDecorated(true);
		frame.getContentPane().add(UI.access$000(), "Center");
		frame.setVisible(true);
	}
}
