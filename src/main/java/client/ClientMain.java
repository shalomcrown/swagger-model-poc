package client;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.glassfish.jersey.logging.LoggingFeature;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import io.swagger.client.ApiClient;
import io.swagger.client.Configuration;
import io.swagger.client.api.SiteInformationApi;


public class ClientMain {

	static TrustManager manager =  new X509TrustManager() {
		@Override
		public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
		}

		@Override
		public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[] {};
		}
	};

	//===================================================================


	public static void main(String[] args) throws Exception {
		ApiClient client = Configuration.getDefaultApiClient();
		client.setBasePath("https://localhost:8080/api");


		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, new TrustManager[] {manager}, new java.security.SecureRandom());

//		HttpAuthenticationFeature authFeature = HttpAuthenticationFeature.basic("Guy", "Levin");

		ObjectMapper mapper = new ObjectMapper()
				.enable(SerializationFeature.INDENT_OUTPUT)
				.registerModule(new JavaTimeModule())
				.findAndRegisterModules()
				.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
				.disable(SerializationFeature.CLOSE_CLOSEABLE)
				.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
				.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
				.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

		JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
		provider.setMapper(mapper);

		Client httpClient = ClientBuilder.newBuilder()
				.sslContext(sslContext)
				.hostnameVerifier((s1, s2) -> true)
//				.register(authFeature)
				.register(provider)
				.register(new LoggingFeature(Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME),
						Level.INFO, LoggingFeature.Verbosity.PAYLOAD_ANY, 10000))
				.build();

		client.setHttpClient(httpClient);

		SiteInformationApi api = new SiteInformationApi(client);

		api.getPets().forEach(System.out::println);
	}
}
