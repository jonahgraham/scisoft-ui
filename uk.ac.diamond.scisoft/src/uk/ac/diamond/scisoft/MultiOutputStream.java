/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package uk.ac.diamond.scisoft;

import java.io.IOException;
import java.io.OutputStream;

class MultiOutputStream extends OutputStream
{
	OutputStream[] outputStreams;
	
	public MultiOutputStream(OutputStream... outputStreams)
	{
		this.outputStreams= outputStreams; 
	}
	
	@Override
	public void write(int b) throws IOException
	{
		for (OutputStream out: outputStreams)
			out.write(b);			
	}
	
	@Override
	public void write(byte[] b) throws IOException
	{
		for (OutputStream out: outputStreams)
			out.write(b);
	}
 
	@Override
	public void write(byte[] b, int off, int len) throws IOException
	{
		for (OutputStream out: outputStreams)
			out.write(b, off, len);
	}
 
	@Override
	public void flush() throws IOException
	{
		for (OutputStream out: outputStreams)
			out.flush();
	}
 
	@Override
	public void close() throws IOException
	{
		for (OutputStream out: outputStreams)
			out.close();
	}
}