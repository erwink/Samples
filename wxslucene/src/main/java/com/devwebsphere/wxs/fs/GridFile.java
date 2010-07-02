//
//This sample program is provided AS IS and may be used, executed, copied and
//modified without royalty payment by customer (a) for its own instruction and 
//study, (b) in order to develop applications designed to run with an IBM 
//WebSphere product, either for customer's own internal use or for redistribution 
//by customer, as part of such an application, in customer's own products. "
//
//5724-J34 (C) COPYRIGHT International Business Machines Corp. 2005
//All Rights Reserved * Licensed Materials - Property of IBM
//
package com.devwebsphere.wxs.fs;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.WXSMap;
import com.devwebsphere.wxsutils.WXSUtils;

public class GridFile
{
	static final Logger logger = Logger.getLogger(GridFile.class.getName());
	/**
	 * 
	 */
	private static final long serialVersionUID = 7484187837407547799L;
	WXSUtils client;
	String fullPath;
	FileMetaData md;
	boolean exists;
	WXSMap<String, FileMetaData> mdMap;

	public GridFile(WXSUtils client, String pathname) 
	{
		this.client = client;
		fullPath = pathname;
		mdMap = client.getCache(MapNames.MD_MAP);
		md = mdMap.get(fullPath);
		exists = (md != null);
		if(!exists)
		{
			md = new FileMetaData();
			md.setName(fullPath);
		}
	}

	public String getName() {
		if(logger.isLoggable(Level.FINEST))
		{
			logger.log(Level.FINEST, this.toString() + ":getName");
		}
		StringTokenizer tok = new StringTokenizer(fullPath, "" + File.separatorChar);
		String name = null;
		while(tok.hasMoreTokens())
		{
			name = tok.nextToken();
		}
		return name;
	}

	public String getPath() {
		if(logger.isLoggable(Level.FINEST))
		{
			logger.log(Level.FINEST, this.toString() + ":getPath");
		}
		return "";
	}

	public boolean isAbsolute() {
		if(logger.isLoggable(Level.FINEST))
		{
			logger.log(Level.FINEST, this.toString() + ":isAbsolute");
		}
		return true;
	}

	public String getAbsolutePath() {
		if(logger.isLoggable(Level.FINEST))
		{
			logger.log(Level.FINEST, this.toString() + ":getAbsolutePath");
		}
		return fullPath;
	}

	public GridFile getAbsoluteFile() {
		if(logger.isLoggable(Level.FINEST))
		{
			logger.log(Level.FINEST, this.toString() + ":getAbsoluteFile");
		}
		return this;
	}

	public String getCanonicalPath() throws IOException {
		if(logger.isLoggable(Level.FINEST))
		{
			logger.log(Level.FINEST, this.toString() + ":getCanonicalPath");
		}
		return fullPath;
	}

	public GridFile getCanonicalFile() throws IOException {
		if(logger.isLoggable(Level.FINEST))
		{
			logger.log(Level.FINEST, this.toString() + ":getCanonicalFile");
		}
		return this;
	}

	public boolean canRead() {
		if(logger.isLoggable(Level.FINEST))
		{
			logger.log(Level.FINEST, this.toString() + ":canRead");
		}
		return md.canRead();
	}

	public boolean canWrite() {
		if(logger.isLoggable(Level.FINEST))
		{
			logger.log(Level.FINEST, this.toString() + ":canWrite");
		}
		return md.canWrite();
	}

	public boolean exists() 
	{
		if(logger.isLoggable(Level.FINEST))
		{
			logger.log(Level.FINEST, this.toString() + ":exists");
		}
		return exists;
	}

	public boolean isDirectory() 
	{
		if(logger.isLoggable(Level.FINEST))
		{
			logger.log(Level.FINEST, this.toString() + ":isDirectory");
		}
		return md.isDirectory();
	}

	public boolean isFile() 
	{
		if(logger.isLoggable(Level.FINEST))
		{
			logger.log(Level.FINEST, this.toString() + ":isFile");
		}
		return !md.isDirectory();
	}

	public boolean isHidden() 
	{
		if(logger.isLoggable(Level.FINEST))
		{
			logger.log(Level.FINEST, this.toString() + ":isHidden");
		}
		return false;
	}

	public long lastModified() 
	{
		if(logger.isLoggable(Level.FINEST))
		{
			logger.log(Level.FINEST, this.toString() + ":getLastModified");
		}
		return md.getLastModifiedTime();
	}

	public long length() 
	{
		if(logger.isLoggable(Level.FINEST))
		{
			logger.log(Level.FINEST, this.toString() + ":length");
		}
		return md.getActualSize();
	}

	public boolean delete() {
		if(logger.isLoggable(Level.FINEST))
		{
			logger.log(Level.FINEST, this.toString() + ":delete");
		}
		if(!exists)
			return false;
		WXSMap<String, byte[]> chunkMap = client.getCache(MapNames.CHUNK_MAP);
		long numChunks = (md.getActualSize() / GridOutputStream.BLOCK_SIZE) + 1;
		ArrayList<String> chunks = new ArrayList<String>();
		for(long i = 0; i < numChunks; ++i)
		{
			chunks.add(GridInputStream.generateKey(fullPath, i));
		}
		chunkMap.removeAll(chunks);
		mdMap.remove(fullPath);
		return true;
	}

	void flush()
	{
		if(logger.isLoggable(Level.FINEST))
		{
			logger.log(Level.FINEST, this.toString() + ":flush");
		}
		mdMap.put(fullPath, md);
	}
	
	public boolean setLastModified(long time) 
	{
		if(logger.isLoggable(Level.FINEST))
		{
			logger.log(Level.FINEST, this.toString() + ":setLastModified");
		}
		md.setLastModifiedTime(time);
		flush();
		return true;
	}

	public boolean setReadOnly() 
	{
		if(logger.isLoggable(Level.FINEST))
		{
			logger.log(Level.FINEST, this.toString() + ":setReadOnly");
		}
		md.setReadOnly();
		flush();
		return true;
	}

	public boolean setWritable(boolean writable, boolean ownerOnly) 
	{
		if(logger.isLoggable(Level.FINEST))
		{
			logger.log(Level.FINEST, this.toString() + ":setWritable/2");
		}
		md.setWriteable();
		flush();
		return true;
	}

	public boolean setWritable(boolean writable) 
	{
		if(logger.isLoggable(Level.FINEST))
		{
			logger.log(Level.FINEST, this.toString() + ":setWriteable/1");
		}
		md.setWriteable();
		flush();
		return true;
	}

	public boolean setReadable(boolean readable, boolean ownerOnly) 
	{
		if(logger.isLoggable(Level.FINEST))
		{
			logger.log(Level.FINEST, this.toString() + ":setReadable/2");
		}
		md.setReadOnly();
		flush();
		return true;
	}

	public boolean setReadable(boolean readable) {
		if(logger.isLoggable(Level.FINEST))
		{
			logger.log(Level.FINEST, this.toString() + ":setReadable/1");
		}
		md.setReadOnly();
		flush();
		return true;
	}

	public boolean setExecutable(boolean executable, boolean ownerOnly) 
	{
		if(logger.isLoggable(Level.FINEST))
		{
			logger.log(Level.FINEST, this.toString() + ":setExecutable/2");
		}
		md.setExecutable();
		flush();
		return true;
	}

	public boolean setExecutable(boolean executable) {
		if(logger.isLoggable(Level.FINEST))
		{
			logger.log(Level.FINEST, this.toString() + ":setExecutable/1");
		}
		md.setExecutable();
		flush();
		return true;
	}

	public boolean canExecute() 
	{
		if(logger.isLoggable(Level.FINEST))
		{
			logger.log(Level.FINEST, this.toString() + ":canExecute");
		}
		return md.isExecutable();
	}

	public long getTotalSpace() 
	{
		if(logger.isLoggable(Level.FINEST))
		{
			logger.log(Level.FINEST, this.toString() + ":getTotalSpace");
		}
		return md.getActualSize();
	}

	public long getFreeSpace() 
	{
		if(logger.isLoggable(Level.FINEST))
		{
			logger.log(Level.FINEST, this.toString() + ":getFreeSpace");
		}
		return 0L;
	}

	public long getUsableSpace() 
	{
		if(logger.isLoggable(Level.FINEST))
		{
			logger.log(Level.FINEST, this.toString() + ":getUsableSpace");
		}
		return md.getActualSize();
	}

	public boolean equals(Object obj) {
		if(logger.isLoggable(Level.FINEST))
		{
			logger.log(Level.FINEST, this.toString() + ":equals");
		}
		return super.equals(obj);
	}

	public int hashCode() {
		if(logger.isLoggable(Level.FINEST))
		{
			logger.log(Level.FINEST, this.toString() + ":hashCode");
		}
		return super.hashCode();
	}

	public String toString() 
	{
		return "GridFile(" + md + ")";
	}

}