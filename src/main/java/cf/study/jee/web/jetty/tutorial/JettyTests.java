package cf.study.jee.web.jetty.tutorial;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import misc.MiscUtils;

import org.apache.commons.lang3.SystemUtils;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.Test;

import com.google.common.net.MediaType;

public class JettyTests {

	@Test
	public void creatServer() throws Exception {
		Server server = new Server(8080);
		server.start();
		server.dumpStdErr();
		
		Executors.newScheduledThreadPool(1).schedule(this::testServer, 1, TimeUnit.SECONDS);
		MiscUtils.easySleep(4000);
	}
	
	public void testServer() {
		testAtUrl("http://localhost:8080/");
	}

	private void testAtUrl(String url) {
		HttpResponse httpResponse = MiscUtils.easyGet(url);
		if (httpResponse == null) {
			System.out.println("response is null");
		} else {
			System.out.println(httpResponse.toString());
			try {
				System.out.println(EntityUtils.toString(httpResponse.getEntity()));
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static class HelloHandler extends AbstractHandler {
		final String respStr;
		
		public HelloHandler(String respStr) {
			super();
			this.respStr = respStr;
		}

		@Override
		public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
			System.out.println("target: \t" + target);
			System.out.println("baseRequest: \t" + baseRequest);
			System.out.println("request: \t" + request);
			System.out.println("response: \t" + response);
			
			response.setContentType(MediaType.HTML_UTF_8.toString());
			response.setStatus(HttpServletResponse.SC_OK);
			
			response.getWriter().println(respStr);
			
			baseRequest.setHandled(true);
		}
	}
	
	public static class LogHandler extends AbstractHandler {
		@Override
		public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
			System.out.println("target: \t" + target);
			System.out.println("baseRequest: \t" + baseRequest);
			System.out.println("request: \t" + request);
			System.out.println("response: \t" + response);
		}
	}
	
	@Test
	public void helloHandler() throws Exception {
		Server server = new Server(8080);
		server.setHandler(new HelloHandler(MiscUtils.invocInfo()));
		server.start();
		
		Executors.newScheduledThreadPool(1).schedule(this::testServer, 1, TimeUnit.SECONDS);
		MiscUtils.easySleep(4000);
	}
	
	@Test
	public void testResourceHandler() throws Exception {
		//create a basic Jetty server object that will listen on port 8080.
		//Note that if you set this to port 0 then a randomly available port will be 
		//assigned that you can either look in the logs for the port,
		//or programmatically obtain it for use in test cases.
		Server server = new Server(8080);
		
		//Create the resourceHandler. It is the object that will actually handle the request for a given file.
		//It is a jetty handler object so it is suitable for chaining with other handlers as you will see in other examples.
		ResourceHandler resHandler = new ResourceHandler();
		
		//Configure the ResourceHandler. Setting the resource base indicates where the files should be served out of.
		//In this example it is the current directory but it can be configured to anything that the jvm has access to.
		resHandler.setDirectoriesListed(true);
		resHandler.setResourceBase(".");
		
		//Add the ResourceHandler to the server.
		HandlerList handlerList = new HandlerList();
		handlerList.setHandlers(new Handler[] {new LogHandler(), resHandler, new DefaultHandler(), new LogHandler()});
		server.setHandler(handlerList);
		
		//Start things up! By using the server.join() the server thread will join with the current thread.
		server.start();
		server.join();
	}
	
	@Test
	public void testConnector() throws Exception {
		//The server
		Server server = new Server();
		
		//HTTP connector
		ServerConnector http = new ServerConnector(server) {
			@Override
			protected void doStart() throws Exception {
				super.doStart();
				System.out.println(MiscUtils.invocationInfo());
			}
			
			@Override
			protected void doStop() throws Exception {
				super.doStop();
				System.out.println(MiscUtils.invocationInfo());
			}
		};
		http.setHost("localhost");
		http.setPort(8080);
		http.setIdleTimeout(30000);
		
		//Set the connector
		server.addConnector(http);
		
		server.start();
		Executors.newScheduledThreadPool(1).schedule(this::testServer, 1, TimeUnit.SECONDS);
		Executors.newScheduledThreadPool(1).schedule(this::testServer, 1, TimeUnit.SECONDS);
		
		MiscUtils.easySleep(4000);
	}
	
	public static class HelloServlet extends HttpServlet {
		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//			super.doGet(req, resp);
			resp.setContentType(ContentType.TEXT_HTML.getMimeType());
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getWriter().println("<h1>Hello from <br>\n" + MiscUtils.invocationInfo() + "</h1>");
		}
	}
	
	@Test
	public void minimalServlet() throws Exception {
		//create a basic jetty server object that will listen on port 8080
		//Note that if you set this to port 0 then a randomly available port
		//will be assigned that you can either look in the logs for the port,
		//or programmatically obtain it for use in test cases.
		Server server = new Server(8080);
		
		//The servletHandler is a dead simple way to create a context handler
		//That is backed by an instance of Servelt
		//This handler then needs to be registered with the Server Object.
		ServletHandler handler = new ServletHandler();
		server.setHandler(handler);
		
		//Passing in the class for the servlet allows jetty to instantiate an
		//instance of that Servlet and mount it on a given context path.
		handler.addServletWithMapping(HelloServlet.class, "/*");
		
		//start things up
		server.start();
		Executors.newScheduledThreadPool(1).schedule(this::testServer, 1, TimeUnit.SECONDS);
		MiscUtils.easySleep(4000);
	}
	
	@Test
	public void oneContext() throws Exception {
		Server server = new Server(8080);
		//Add a single handler on context "/hello"
		ContextHandler context = new ContextHandler();
		context.setContextPath("/hello");
		context.setHandler(new HelloHandler("context"));
		
		server.setHandler(context);
		
		//Can be accessed using http://localhost:8080/hello
		server.start();
		Executors.newScheduledThreadPool(1).schedule(this::testHelloContext, 1, TimeUnit.SECONDS);
		MiscUtils.easySleep(4000);
	}
	
	public void testHelloContext() {
		testAtUrl("http://localhost:8080/hello");
	}
	
	@Test
	public void multipleContexts() throws Exception {
		Server server = new Server(8080);
		
		ContextHandler root = new ContextHandler("/");
		root.setContextPath("/");
		root.setHandler(new HelloHandler("Root Hello"));
		
		ContextHandler ctxFr = new ContextHandler("/fr");
		ctxFr.setHandler(new HelloHandler("Bonjour"));
		
		ContextHandler ctxIT = new ContextHandler("it");
		ctxIT.setHandler(new HelloHandler("Bongiorno"));
		
		ContextHandler ctxV = new ContextHandler("/");
		ctxV.setVirtualHosts(new String[] {"127.0.0.2"});
		ctxV.setHandler(new HelloHandler("Virtual Hello"));
		
		ContextHandlerCollection ctxs = new ContextHandlerCollection();
		ctxs.setHandlers(new Handler[] {root, ctxFr, ctxIT, ctxV});
		
		server.setHandler(ctxs);
		
		server.start();
		ScheduledExecutorService threads = Executors.newScheduledThreadPool(4);
		
		threads.schedule(()->testAtUrl("http://localhost:8080/"), 1, TimeUnit.SECONDS);
		threads.schedule(()->testAtUrl("http://localhost:8080/fr"), 1, TimeUnit.SECONDS);
		threads.schedule(()->testAtUrl("http://localhost:8080/it"), 1, TimeUnit.SECONDS);
		threads.schedule(()->testAtUrl("http://127.0.0.2:8080/"), 1, TimeUnit.SECONDS);
		MiscUtils.easySleep(4000);
	}
	
	@Test
	public void oneServletContext() throws Exception {
		Server server = new Server(8080);
		
		ServletContextHandler ctx = new ServletContextHandler(ServletContextHandler.SESSIONS);
		ctx.setContextPath("/");
		ctx.setResourceBase(SystemUtils.JAVA_IO_TMPDIR);
		
		server.setHandler(ctx);
		
		ctx.addServlet(HelloServlet.class, "/*");
		ctx.addServlet(HelloServlet.class, "/dump/");
		
		server.start();
		
		ScheduledExecutorService threads = Executors.newScheduledThreadPool(4);
		
		threads.schedule(()->testAtUrl("http://localhost:8080/"), 1, TimeUnit.SECONDS);
		threads.schedule(()->testAtUrl("http://localhost:8080/dump/fr"), 1, TimeUnit.SECONDS);
		MiscUtils.easySleep(4000);
	}
	
	public void war() throws Exception {
        // Create a basic jetty server object that will listen on port 8080.
        // Note that if you set this to port 0 then a randomly available port
        // will be assigned that you can either look in the logs for the port,
        // or programmatically obtain it for use in test cases.
        Server server = new Server(8080);
 
        // Setup JMX
        MBeanContainer mbContainer = new MBeanContainer(
                ManagementFactory.getPlatformMBeanServer());
        server.addBean(mbContainer);
 
        // The WebAppContext is the entity that controls the environment in
        // which a web application lives and breathes. In this example the
        // context path is being set to "/" so it is suitable for serving root
        // context requests and then we see it setting the location of the war.
        // A whole host of other configurations are available, ranging from
        // configuring to support annotation scanning in the webapp (through
        // PlusConfiguration) to choosing where the webapp will unpack itself.
        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        File warFile = new File(
                "../../jetty-distribution/target/distribution/demo-base/webapps/test.war");
        webapp.setWar(warFile.getAbsolutePath());
 
        // A WebAppContext is a ContextHandler as well so it needs to be set to
        // the server so it is aware of where to send the appropriate requests.
        server.setHandler(webapp);
 
        // Configure a LoginService
        // Since this example is for our test webapp, we need to setup a
        // LoginService so this shows how to create a very simple hashmap based
        // one. The name of the LoginService needs to correspond to what is
        // configured in the webapp's web.xml and since it has a lifecycle of
        // its own we register it as a bean with the Jetty server object so it
        // can be started and stopped according to the lifecycle of the server
        // itself.
        HashLoginService loginService = new HashLoginService();
        loginService.setName("Test Realm");
        loginService.setConfig("src/test/resources/realm.properties");
        server.addBean(loginService);
 
        // Start things up!
        server.start();
 
        // The use of server.join() the will make the current thread join and
        // wait until the server is done executing.
        // See http://docs.oracle.com/javase/7/docs/api/java/lang/Thread.html#join()
        server.join();
    }
}