/*-
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.feedback;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.btr.proxy.search.ProxySearch;
import com.btr.proxy.search.ProxySearch.Strategy;
import com.btr.proxy.util.PlatformUtil;
import com.btr.proxy.util.PlatformUtil.Platform;

/**
 * Class used to get the eventual proxy host/port of the System
 * If set manually, it uses the java get Property,
 * if set automatically, it uses the Proxy Vole API 
 */
public class FeedbackProxy {

	private static Logger logger = LoggerFactory.getLogger(FeedbackProxy.class);

	private static String host = null;
	private static int port = 0;

	public static void init() {
		System.setProperty("java.net.useSystemProxies", "true");
		Proxy proxy = getProxy();
		if (proxy != null) {
			InetSocketAddress addr = (InetSocketAddress) proxy.address();
			if (addr == null) {
				logger.debug("No proxy found.");
				System.setProperty("java.net.useSystemProxies", "false");
			} else {
				host = addr.getHostName();
				port = addr.getPort();

				System.setProperty("java.net.useSystemProxies", "false");
				System.setProperty("http.proxyHost", host);
				System.setProperty("http.proxyPort", "" + port);
			}
		}
		System.setProperty("java.net.useSystemProxies", "false");
	}

	public static String getHost() {
		return host;
	}

	public static int getPort() {
		return port;
	}

	private static Proxy getProxy() {
		List<Proxy> l = null;
		try {
			
			String host = System.getProperty("http.proxyHost");
			String port = System.getProperty("http.proxyPort");
			// if no proxy has been set manually
			if(host==null&&port==null){
				// We set the default proxy selector using Proxy Vole (to get the eventual auto proxy settings)
				logger.debug("Checking for an automatically configured proxy.");
				setProxySelector();
			}
			ProxySelector def = ProxySelector.getDefault();

			l = def.select(new URI("http://www.dawnsci.org/"));
			ProxySelector.setDefault(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (l != null) {
			for (Iterator<Proxy> iter = l.iterator(); iter.hasNext();) {
				java.net.Proxy proxy = iter.next();
				return proxy;
			}
		}
		return null;
	}

	private static void setProxySelector() {
		ProxySearch proxySearch = new ProxySearch();

		if (PlatformUtil.getCurrentPlattform() == Platform.WIN) {
			proxySearch.addStrategy(Strategy.IE);
			proxySearch.addStrategy(Strategy.FIREFOX);
			proxySearch.addStrategy(Strategy.JAVA);
		} else if (PlatformUtil.getCurrentPlattform() == Platform.LINUX) {
			proxySearch.addStrategy(Strategy.GNOME);
			proxySearch.addStrategy(Strategy.KDE);
			proxySearch.addStrategy(Strategy.FIREFOX);
		} else {
			proxySearch.addStrategy(Strategy.OS_DEFAULT);
		}
		
		ProxySelector myProxySelector = proxySearch.getProxySelector();

		ProxySelector.setDefault(myProxySelector);
	}
}
