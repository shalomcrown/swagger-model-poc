package com.airobotics.customer_api.swagger_model_poc;

import java.io.File;
import java.net.*;
import java.time.Duration;
import java.util.*;
import java.util.logging.*;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import com.airobotics.customer_api.swagger_model_poc.model.Cat;
import com.airobotics.customer_api.swagger_model_poc.model.Dog;
import com.airobotics.customer_api.swagger_model_poc.model.Pet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.Lists;

import io.swagger.v3.jaxrs2.SwaggerSerializers;
import io.swagger.v3.jaxrs2.integration.SwaggerLoader;
import io.swagger.v3.jaxrs2.integration.XmlWebOpenApiContext;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.servers.ServerVariable;
import io.swagger.v3.oas.models.servers.ServerVariables;

@OpenAPIDefinition(  info = @Info(title = "Airobotics Customer API", version = "1.1",
contact = @Contact(email = "shalomc@airoboticsDrones.com"))
)
@SecurityScheme(name = "basic", type = SecuritySchemeType.HTTP, scheme = "basic")
@ApplicationPath("/")
@Path("/")
public class ApiServer extends ResourceConfig {

	public static Logger logger = configLogging();
	public static Duration SESSION_TIMEOUT = Duration.ofHours(2);
	public static final String USER = "USER";

	HttpServer server;
	List<Pet> inventory = Lists.newArrayList(
			Dog.builder().withCoatColor("Green").build(),
			Cat.builder().withSiamese(true).build());

	static ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
			.findAndRegisterModules()
			.registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
			.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);

	//===================================================================

	@GET
	@Path("alive")
	@Operation(summary = "Poll API to see that it is responding", hidden = true)
	@Produces(MediaType.TEXT_PLAIN)
	public String alive() {
		return "OK";
	}

	@GET
	@Path("swagger.json")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get OpenAPI model JSON", hidden = true)
	public String getOpenAPIJson(@Context ContainerRequest request) {
		return OpenAPIHolder.getInstance().getOpenApiJson();
	}


	@GET
	@Path("swagger.yaml")
	@Produces(MediaType.TEXT_PLAIN)
	@Operation(summary = "Get OpenAPI model YAML", hidden = true)
	public String getOpenAPIYaml(@Context ContainerRequest request) {
		return OpenAPIHolder.getInstance().getOpenApiYaml();
	}

	//===================================================================

	@GET
	@Path("pets")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Get overall site status with lists of stations and UAVs", tags = {"Site Information"})
//	@ApiResponses(value = {@ApiResponse(responseCode = "200", description =  "Successful operation",
//			                            content = @Content(mediaType = "application/json",
//										                   schema = @Schema(implementation = GetSiteStatusResponse.class))),
//			               @ApiResponse(responseCode = "403", description =  "Forbidden")})
	public List<Pet> getPets() {
		return inventory;
	}
	//===================================================================

	public void swaggerConfig() {
		try {
			OpenAPI openAPI = new OpenAPI();

			List<String> netAddrs = new ArrayList<String>();

			if (netAddrs.isEmpty() == true) {
				netAddrs = getAllLocalIpAddresses();
			}

			ServerVariables sv = new ServerVariables();

			ServerVariable ipAddress = new ServerVariable();
			for (String netIpAddr : netAddrs) {
				ipAddress.addEnumItem(netIpAddr);
			}
			ipAddress.setDefault(netAddrs.get(0));
			sv.addServerVariable("address", ipAddress);

			Server externalServer = (new Server())
					.url("https://{address}/api")
					.description("Use this server for external access")
					.variables(sv);


			Server localServer = (new Server())
					.url("https://{address}:8080/api")
					.description("Use this server for local access")
					.variables(sv);

			openAPI.setServers(Lists.newArrayList(externalServer, localServer));

			SwaggerLoader loader = new SwaggerLoader();
			loader.setResourceClasses(this.getClass().getName());
			loader.setPrettyPrint(true);
			loader.setOutputFormat("JSON");
			loader.setContextId("CustomerAPI");
//			loader.setFilterClass(CustomerAPISchemaFilter.class.getName());

			SwaggerConfiguration oasConfig = new SwaggerConfiguration()
					                                .openAPI(openAPI)
					                                .prettyPrint(loader.getPrettyPrint())
					                                .resourceClasses(new HashSet<String>(Arrays.asList(loader.getResourceClasses().split(","))))
//													.objectMapperProcessorClass(CustomerAPIObjectMapperProcessor.class.getName())
					                                .readAllResources(true);

			XmlWebOpenApiContext<?> openApiContext = new XmlWebOpenApiContext<>();
			openApiContext.app(this);
			openApiContext.id(loader.getContextId());
			openApiContext.setOpenApiConfiguration(oasConfig);
			openApiContext.init();

			loader.setOutputFormat("JSON");
			OpenAPIHolder.getInstance().setOpenApiJson(loader.resolve().get("JSON"));

			loader.setOutputFormat("YAML");
			OpenAPIHolder.getInstance().setOpenApiYaml(loader.resolve().get("YAML"));

			openApiContext.read();

		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to start Swagger. ", e);
		}
	}

	//===================================================================

	private List<String> getAllLocalIpAddresses() {
		List<String> res = Lists.newArrayList();
		try {
			Enumeration<?> en = NetworkInterface.getNetworkInterfaces();
			while (en.hasMoreElements()) {
				NetworkInterface i = (NetworkInterface) en.nextElement();
				for (Enumeration<?> en2 = i.getInetAddresses(); en2.hasMoreElements(); ) {
					InetAddress addr = (InetAddress) en2.nextElement();
					if ((addr.isLoopbackAddress() == false) && (addr instanceof  Inet4Address)) {
						res.add(addr.getHostAddress());
					}

				}
			}
		} catch (SocketException e) {
			logger.log(Level.SEVERE, "Unable to get interface addresses", e);
		}
		return res;

	}

	// ================================================================
	/**
	 * @see com.airobotics.hqc.plugins.IHqcPlugin#start()
	 */
	public void start() {
		try {
			logger.log(Level.FINE,"Starting customer API service");
//			registerBinders();

			register(new AbstractBinder() {
				@Override
				protected void configure() {
					bindFactory(new Factory<OpenAPIHolder>() {

						@Override
						public OpenAPIHolder provide() {
							return OpenAPIHolder.getInstance();
						}

						@Override
						public void dispose(OpenAPIHolder instance) {
							//Nothing;
						}

					}).to(OpenAPIHolder.class);
				}
			});

			register(RolesAllowedDynamicFeature.class);
			register(LoggingFeature.class);
			register(this);
			register(SwaggerSerializers.class);
			register(JacksonFeature.class);
			register(CorsFilter.class);


	     	SSLContextConfigurator sslCon = new SSLContextConfigurator();
	        sslCon.setKeyStoreFile("/home/shalomc/Documents/Customer API/tomcat.pkcs12");
	        sslCon.setKeyStorePass("airobotics");

	        SSLEngineConfigurator ssl = new SSLEngineConfigurator(sslCon, false, false, false);

			URI baseUri = UriBuilder.fromUri("https://0.0.0.0/api").port(8080).build();

			server = GrizzlyHttpServerFactory.createHttpServer(baseUri, this, false);

			server.getListener("grizzly").setSSLEngineConfig(ssl);
			server.getListener("grizzly").setSecure(true);


			HttpHandler staticHttpHandler;
			if (isRunningInEclipse(2)) {
				staticHttpHandler = new StaticHttpHandler("/usr/local/lib/airobotics/plugins/hqc_customer_api/static");
				logger.info("Document Root: /usr/local/lib/airobotics/plugins/hqc_customer_api/static");
			} else {
				URLClassLoader staticLoader = new URLClassLoader(new URL[] {this.getClass().getProtectionDomain().getCodeSource().getLocation()}, this.getClassLoader());

				staticHttpHandler = new CLStaticHttpHandler(staticLoader, "/static/");
				logger.info("Document Root: " +  this.getClass().getProtectionDomain().getCodeSource().getLocation() + "/static");
			}

			server.getServerConfiguration().addHttpHandler(staticHttpHandler, "/");

			WebSocketAddOn webSocketAddOn = new WebSocketAddOn();
			server.getListeners().forEach(p -> p.registerAddOn(webSocketAddOn));


			swaggerConfig();

			server.start();

		} catch (Exception e) {
			logger.log(Level.SEVERE, "Couldn't start customer API", e);
		}
	}

	//===================================================================

	public static Logger configLogging() {
        try {
    		Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);

            System.setProperty("java.util.logging.SimpleFormatter.format",
                    "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %4$-7s [%3$s] (%2$s) %5$s %6$s%n");

            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.FINEST);
            consoleHandler.setFormatter(new SimpleFormatter());

            new File("/usr/local/lib/airobotics/logs/customer-api-test/").mkdirs();

            String log= String.format("/usr/local/lib/airobotics/logs/customer-api-test/log-%1tF-%1$tH%1$tM%1$tS-%%u.log",
            		new Date());

            FileHandler fileHandler = new FileHandler(log, 1_000_000, 100);
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.FINEST);

            logger.setLevel(Level.FINEST);
            logger.addHandler(consoleHandler);
            logger.addHandler(fileHandler);

            return logger;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

	public static boolean isRunningInEclipse(int folderDepth) {
	    File projectRoot = new File(ApiServer.class.getProtectionDomain().getCodeSource().getLocation().getFile());
	    for (int i = 0; i < folderDepth; i++) {
	        projectRoot = projectRoot.getParentFile();
	        if (projectRoot == null || !projectRoot.isDirectory()) {
	            return false;
	        }
	    }
	    return new File(projectRoot, ".project").isFile();
	}

	//===================================================================

	public static void main(String[] args) throws Exception {
		ApiServer server = new ApiServer();

		server.start();

		synchronized (server) {
			server.wait();
		}
	}
}
