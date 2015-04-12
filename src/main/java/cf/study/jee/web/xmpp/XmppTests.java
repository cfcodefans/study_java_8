package cf.study.jee.web.xmpp;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.proxy.ProxyServlet;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.Test;

import cf.study.jee.web.jetty.res.WebResources;

public class XmppTests {
	public static final String DEFAULT_XMPP_HTTP_BIND_ADDR = "http://localhost:7070";

	public static class HttpBindProxyServlet extends ProxyServlet {
		private static final long serialVersionUID = 1L;

		private URL xmppHttpBindAddr = null;
		public static final String XMPP_HTTP_BIND_ADDR = "xmpp.httpbind.addr";

		public void init() throws ServletException {
			super.init();
			String addrStr = getServletConfig().getInitParameter(XMPP_HTTP_BIND_ADDR);
			try {
				xmppHttpBindAddr = new URL(StringUtils.defaultIfBlank(addrStr, DEFAULT_XMPP_HTTP_BIND_ADDR));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}

		protected String rewriteTarget(HttpServletRequest clientRequest) {
			String target = xmppHttpBindAddr.toExternalForm();
			String query = clientRequest.getQueryString();
			if (query != null)
				target = target + "?" + query;
			return target;
		}
	}

	@Test
	public void testJwchatPath() throws Exception {
		String jwchatPathStr = "jwchat-1.0";
		Path jwchatPath = Paths.get(XmppTests.class.getResource(".").toURI()).resolve(jwchatPathStr).toAbsolutePath()
				.normalize();
		System.out.println(jwchatPath);
	}

	@Test
	public void testJwchatWithOpenfire() throws Exception {
		Server proxySrv = new Server(8080);

		ServletContextHandler sch = new ServletContextHandler();
		sch.setContextPath("/xmpp/httpbind");
		sch.addServlet(HttpBindProxyServlet.class, "/xmpp/httpbind/*");


		String jwchatPathStr = "jwchat-1.0";
		Path jwchatPath = Paths.get(XmppTests.class.getResource(".").toURI()).resolve(jwchatPathStr).toAbsolutePath()
				.normalize();

		ContextHandler jwchat = WebResources.res(jwchatPathStr, jwchatPath.toString());

		// proxySrv.setHandler(sch);

		HandlerList hls = new HandlerList();
		hls.setHandlers(new Handler[] { sch, jwchat });
		proxySrv.setHandler(hls);

		proxySrv.start();
//		proxySrv.dumpStdErr();
		proxySrv.join();
	}
}
