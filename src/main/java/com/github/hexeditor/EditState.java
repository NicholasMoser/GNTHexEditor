package com.github.hexeditor;

import java.util.Stack;

class EditState
{

	int a1;
	public long p1 = -1L;
	public long virtualSize = -1L;
	public long offset = 0L;
	public long size = 0L;
	public boolean isEditing = false;
	EditState o = null;
	public Stack<Byte> stack = new Stack<Byte>();

	public EditState(long var1, long size, int var5)
	{
		this.p1 = var1;
		this.a1 = var5;
		this.size = size;
		this.virtualSize = this.p1 + size;
	}

	public EditState(long p1, long virtualSize, long offset, EditState o)
	{
		this.p1 = p1;
		this.virtualSize = virtualSize;
		this.offset = offset;
		this.o = o;
	}

	public String toString()
	{
		return this.o != null
				? "p//offset: " + this.p1 + "/" + this.virtualSize + "//" + this.offset + "   \to.a1: " + this.o.a1
						+ "   \to.B.size/o.size: " + this.o.stack.size() + "/" + this.o.size
				: "p: " + this.p1 + "/" + this.virtualSize + "   \ta1: " + this.a1 + "   \tB.size/size: " + this.stack.size()
						+ "/" + this.size;
	}
}
