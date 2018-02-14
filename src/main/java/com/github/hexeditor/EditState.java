package com.github.hexeditor;

import java.util.Stack;

class EditState
{

	int a1;
	public long p1 = -1L;
	public long p2 = -1L;
	public long offset = 0L;
	public long size = 0L;
	public boolean isEditing = false;
	EditState o = null;
	public Stack<Byte> stack = new Stack<Byte>();

	public EditState(long var1, long var3, int var5)
	{
		this.p1 = var1;
		this.a1 = var5;
		this.size = var3;
		this.p2 = this.p1 + var3;
	}

	public EditState(long var1, long var3, long var5, EditState var7)
	{
		this.p1 = var1;
		this.p2 = var3;
		this.offset = var5;
		this.o = var7;
	}

	public String toString()
	{
		return this.o != null
				? "p//offset: " + this.p1 + "/" + this.p2 + "//" + this.offset + "   \to.a1: " + this.o.a1
						+ "   \to.B.size/o.size: " + this.o.stack.size() + "/" + this.o.size
				: "p: " + this.p1 + "/" + this.p2 + "   \ta1: " + this.a1 + "   \tB.size/size: " + this.stack.size()
						+ "/" + this.size;
	}
}
