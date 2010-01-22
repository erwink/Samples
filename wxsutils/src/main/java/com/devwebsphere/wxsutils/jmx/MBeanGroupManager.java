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
package com.devwebsphere.wxsutils.jmx;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;

/**
 * This is an abstract base class to create a SummaryMBean for a collection of homogenous MBeans. It has
 * two methods to retrieve a TabularData of EVERY attribute of all the mbeans as well as one with only
 * the attributes for an external monitor. Typically, the monitor only attributes exclude things like
 * calculated statistics which a 'real' JMX monitor will calculate it self.
 *
 * @param <M> The MBean Impl class
 */
public abstract class MBeanGroupManager <M>
{
	public ConcurrentMap<String, M> beans = new ConcurrentHashMap<String, M>();

	final static public String UNKNOWN_MAP = "_UNDEFINED";
	
	/**
	 * The MBeanImpl class to use
	 */
	Class<M> mbeanClass;
	/**
	 * The JMX MBean interface to use, M must implement this
	 */
	Class mbeanInterface;
	/**
	 * The type of the MBeans grouped here.
	 */
	String typeName;
	/**
	 * The name of the grid owning these MBeans
	 */
	String gridName;
	/**
	 * The name of the MBean attribute thats the unique key for this group.
	 * Example: Map MBeans then MapName would be the key
	 */
	String keyAttributeName;
	
	volatile MBeanServer mbeanServer;
	
	public ObjectName makeObjectName(String grid, String keyValue)
		throws MalformedObjectNameException
	{
		Hashtable<String, String> props = new Hashtable<String, String>();
		props.put("grid", grid);
		props.put(keyAttributeName, keyValue);
		props.put("type", typeName);
		return new ObjectName("com.devwebsphere.wxs", props);
	}

	/**
	 * Construct a manager.
	 * @param clazz The MBeanImpl class file
	 * @param clazzI The JMX interface implemented by clazz
	 * @param gridName The name of the grid
	 * @param typeName The name of the attribute (agentClass or mapName...)
	 * @param keyName The attribute value attribute
	 */
	public MBeanGroupManager(Class<M> clazz, Class clazzI, String gridName, String typeName, String keyName)
	{
		mbeanClass = clazz;
		mbeanInterface = clazzI;
		this.typeName = typeName;
		this.gridName = gridName;
		this.keyAttributeName = keyName;
	}
	
	public List<String> getCurrentBeanNames()
	{
		return new ArrayList<String>(beans.keySet());
	}
	
	/**
	 * This returns the JMXServer to use for registering mbeans. The first time that it's called
	 * it also registers the Summary MBean for this group
	 * @return
	 */
	public MBeanServer getServer()
	{
		if(mbeanServer == null)
		{
			ArrayList<MBeanServer> mBeanServers = MBeanServerFactory.findMBeanServer(null);
	        mbeanServer = (MBeanServer) mBeanServers.get(0);
	        try
	        {
		        SummaryMBeanImpl<M> mb = new SummaryMBeanImpl<M>(this, mbeanClass, keyAttributeName, typeName);
		        
				Hashtable<String, String> props = new Hashtable<String, String>();
				props.put("grid", gridName);
				props.put("type", typeName + "Summary");
				ObjectName on = new ObjectName("com.devwebsphere.wxs", props);
				StandardMBean realMBean = new StandardMBean(mb, SummaryMBean.class);
				mbeanServer.registerMBean(realMBean, on);
	        }
	        catch(Exception e)
	        {
	        	System.out.println(e.toString());
	        	e.printStackTrace();
	        }
		}
		return mbeanServer;
	}
	
	/**
	 * This must be implemented and return an MBeanImpl for the parameters.
	 * @param gridName
	 * @param mapName
	 * @return
	 */
	abstract public M createMBean(String gridName, String keyName);

	/**
	 * This is called to fetch the MBeanImpl for a given key. The bean
	 * is created if it didn't already exist.
	 */
	public M getBean(String keyValue)
	{
		M bean = beans.get(keyValue);
		if(bean == null)
		{
			synchronized(MBeanGroupManager.class)
			{
				bean = beans.get(keyValue);
				if(bean == null)
				{
					bean = createMBean(gridName, keyValue);
					try
					{
						MBeanServer server = getServer();
						if(server != null && !keyValue.equals(UNKNOWN_MAP));
						{
							StandardMBean mbean = new StandardMBean(bean, mbeanInterface);
							MBeanInfo info = mbean.getMBeanInfo();
							ObjectName on = makeObjectName(gridName, keyValue);
							server.registerMBean(mbean, on);
						}
					}
					catch(Exception e)
					{
						throw new ObjectGridRuntimeException(e);
					}
					beans.put(keyValue, bean);
				}
			}
		}
		return bean;
	}
}