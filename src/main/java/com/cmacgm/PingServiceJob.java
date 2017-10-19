package com.cmacgm;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.IntervalTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import com.cmacgm.model.Users;
import com.cmacgm.repository.ApplicationRepository;
import com.cmacgm.repository.ApplicationUrlRepository;

@Configuration
@EnableScheduling
public class PingServiceJob implements SchedulingConfigurer {

	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PingServiceJob.class);

	@Autowired
	ApplicationRepository applicationRepository;

	@Autowired
	ApplicationUrlRepository applicationUrlRepository;
	
	static {
        disableSSLVerification();
    }

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		LOGGER.debug("adding applicationRepository job");
		List<com.cmacgm.model.Application> application = applicationRepository.findAll();

		if (application != null) {
			for (com.cmacgm.model.Application configApplication : application) {
				long delay = configApplication.getSyncJobInitialDelay();
				long rate = configApplication.getSyncJobRate();
				
				taskRegistrar.addFixedRateTask(new IntervalTask(new Runnable() {

					public void run() {
						LOGGER.debug("running applicationRepository job");
						applicationRepository.updateLastSyncTime(new Date(), configApplication.getId());
						
						try {
							StringBuilder buf = new StringBuilder();
							boolean trigger = false;
							buf.append("<html>" + "<body>" + "<table>" + "<tr>"
									+ "<th>Application Name</th>" + "<th>Server Type</th>"
									+ "<th>Status Code</th>" + "<th>Description :</th>"
									+ "<th>Application Url</th>" + "<th>Last Sync Time Url</th>" + "</tr>");
							for (com.cmacgm.model.ApplicationUrl applicationUrl : configApplication
									.getApplicationUrl()) {
								if (applicationUrl != null
										&& applicationUrl.getServerType().getName().equalsIgnoreCase("web")
										|| applicationUrl.getServerType().getName().equalsIgnoreCase("webservices")) {
									String statusCode = "404";
									HashMap<String, String> hMap = new HashMap<>();
									hMap = pingUrl(applicationUrl.getApplicationUrl());
									if (hMap != null && !hMap.isEmpty()) {
										statusCode = hMap.get("responseCode");
									}
									if (hMap != null && !hMap.isEmpty() && statusCode != null
											&& !applicationUrl.getStatusCode().equalsIgnoreCase(statusCode)) {
										applicationUrl.setStatusCode(statusCode);
										applicationUrl.setDescription(hMap.get("description"));
									
										if (statusCode.equalsIgnoreCase("200")) {
											applicationUrl.setStatus(true);
											buf.append("<tr><td>").append(configApplication.getApplicationName())
													.append("</td><td>")
													.append(applicationUrl.getServerType().getName())
													.append("</td><td>").append(applicationUrl.getStatusCode())
													.append("</td><td>").append(applicationUrl.getDescription())
													.append("</td><td>").append(applicationUrl.getApplicationUrl())
													.append("</td><td>").append(configApplication.getLastSyncTime())
													.append("</td></tr>");

										} else {
											   if(hMap.get("description").equalsIgnoreCase("connect timed out")  ||
													   hMap.get("description").equalsIgnoreCase("Read timed out"))
												applicationUrl.setStatus(true);
												else
												applicationUrl.setStatus(false);
											buf.append("<tr><td>").append(configApplication.getApplicationName())
													.append("</td><td>")
													.append(applicationUrl.getServerType().getName())
													.append("</td><td>").append(applicationUrl.getStatusCode())
													.append("</td><td>").append(applicationUrl.getDescription())
													.append("</td><td>").append(applicationUrl.getApplicationUrl())
													.append("</td><td>").append(configApplication.getLastSyncTime())
													.append("</td></tr>");

											}
										
									//	buf.append("</table>" + "</body>" + "</html>");
									
										applicationUrlRepository.update(applicationUrl.isStatus(),
												applicationUrl.getStatusCode(), applicationUrl.getDescription(),
												new Date(), applicationUrl.getId());
										if(!hMap.get("description").equalsIgnoreCase("connect timed out") ||
												   !hMap.get("description").equalsIgnoreCase("Read timed out")){
										trigger = true;
										}
									}

								} else if (applicationUrl != null
										&& (applicationUrl.getServerType().getName().equalsIgnoreCase("db")
												|| applicationUrl.getServerType().getName()
														.equalsIgnoreCase("server"))
										&& applicationUrl.getIpAddress() != null
										&& applicationUrl.getHostPortNo() != null) {
									String statusCode = "404";
									HashMap<String, String> hMapSocket = new HashMap<>();

									hMapSocket = isSocketAliveUtility(applicationUrl.getIpAddress(),
											applicationUrl.getHostPortNo());

									if (hMapSocket != null && !hMapSocket.isEmpty()) {
										statusCode = hMapSocket.get("responseCode");
									}

									if (hMapSocket != null && !hMapSocket.isEmpty() && statusCode != null
											&& !applicationUrl.getStatusCode().equalsIgnoreCase(statusCode)) {
									
										applicationUrl.setStatusCode(hMapSocket.get("responseCode"));
										applicationUrl.setDescription(hMapSocket.get("description"));

										if (statusCode.equalsIgnoreCase("200")) {
											applicationUrl.setStatus(true);
											buf.append("<tr><td>").append(configApplication.getApplicationName())
													.append("</td><td>")
													.append(applicationUrl.getServerType().getName())
													.append("</td><td>").append(applicationUrl.getStatusCode())
													.append("</td><td>").append(applicationUrl.getDescription())
													.append("</td><td>").append(applicationUrl.getApplicationUrl())
													.append("</td><td>").append(configApplication.getLastSyncTime())
													.append("</td></tr>");

										} else {
											 if(hMapSocket.get("description").equalsIgnoreCase("connect timed out") ||
													 hMapSocket.get("description").equalsIgnoreCase("Read timed out"))
											applicationUrl.setStatus(true);
											else
											applicationUrl.setStatus(false);
											buf.append("<tr><td>").append(configApplication.getApplicationName())
													.append("</td><td>")
													.append(applicationUrl.getServerType().getName())
													.append("</td><td>").append(applicationUrl.getStatusCode())
													.append("</td><td>").append(applicationUrl.getDescription())
													.append("</td><td>").append(applicationUrl.getApplicationUrl())
													.append("</td><td>").append(configApplication.getLastSyncTime())
													.append("</td></tr>");
											

										}
										
										applicationUrlRepository.update(applicationUrl.isStatus(),
												applicationUrl.getStatusCode(), applicationUrl.getDescription(),
												new Date(), applicationUrl.getId());	
										if(!hMapSocket.get("description").equalsIgnoreCase("connect timed out") ||
												   !hMapSocket.get("description").equalsIgnoreCase("Read timed out")){
										trigger = true;
										}
									}
								}

							}
							if(configApplication
									.getApplicationUrl()==null || configApplication
									.getApplicationUrl().size()==0){
								buf.append("<tr><td>").append("No Url Configured").append("</td></tr>");
							}								
							buf.append("</table>" + "</body>" + "</html>");
							
							if (trigger)
								sendEmail(configApplication, buf.toString());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}, rate, delay));
			}
		}
	}

	
	private static HashMap<String, String> pingUrl(String address) {
		HashMap<String, String> map = new HashMap<>();
		int responseCode = 404;
		map.put("responseCode", " ");
		map.put("description", " ");
		try {
			URL url = new URL(address);

			URLConnection urlConnection = url.openConnection();
			urlConnection.setConnectTimeout(7000);
			urlConnection.setReadTimeout(7000);
			if (address.startsWith("http")) {
				responseCode = ((HttpURLConnection) urlConnection).getResponseCode();
				map.put("responseCode", String.valueOf(responseCode));
			} else {				
				responseCode = ((HttpsURLConnection) urlConnection).getResponseCode();
				map.put("responseCode", String.valueOf(responseCode));
			}
			if (responseCode==200)
				map.put("description", "SUCCESS");
			else
				map.put("description", "FAILURE");

		} catch (ConnectException e) {
			map.put("responseCode", String.valueOf(responseCode));
			map.put("description", e.getMessage());
			return map;
		} catch (SocketTimeoutException e) {
			map.put("responseCode", "400");
			map.put("description", e.getMessage());
			return map;
		} catch (IOException e) {
			map.put("responseCode", String.valueOf(responseCode));
			map.put("description", e.getMessage());
			return map;
		} catch (Exception e) {
			map.put("responseCode", String.valueOf(responseCode));
			map.put("description", e.getMessage());
			return map;
		}

		return map;

	}

	public static HashMap<String, String> isSocketAliveUtility(String hostName, String hostPortNumber) throws IOException {
		HashMap<String, String> map = new HashMap<>();
		// Creates a socket address from a hostname and a port number
		SocketAddress socketAddress = new InetSocketAddress(hostName, Integer.parseInt(hostPortNumber));
		Socket socket = new Socket();

		map.put("responseCode", " ");
		map.put("description", " ");
		try {
			socket.connect(socketAddress, 7000);
			
			map.put("responseCode", "200");
			map.put("description", "SUCCESS");
			return map;
		} catch (ConnectException e) {
			map.put("responseCode", "404");
			map.put("description", e.getMessage());
			return map;
		} catch (SocketTimeoutException e) {
			map.put("responseCode", "400");
			map.put("description", e.getMessage());
			return map;
		} catch (IOException e) {
			map.put("responseCode", "404");
			map.put("description", e.getMessage());
			return map;
		} catch (Exception e) {
			map.put("responseCode", "404");
			map.put("description", e.getMessage());
			return map;
		}
		finally{
	        if(socket != null){
	        	socket.close();
	        }
	    }

	}
	
	 //Method used for bypassing SSL verification
    public static void disableSSLVerification() {

        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

           

			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
					throws CertificateException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
					throws CertificateException {
				// TODO Auto-generated method stub
				
			}

        } };

        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };      
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);           
    }

	public void sendEmail(com.cmacgm.model.Application application, String htmlContent) {
		SendMail sendMail = new SendMail();

		if (application.getUsers() != null) {
			for (Users userData : application.getUsers()) {
				//System.out.println(userData.getEmail() + " " + htmlContent);
				sendMail.SendMail(application.getApplicationName() + " Development Team", userData.getEmail(),
						htmlContent);
			}

		}
	}

/*	public static void main(String args[]) {
		HashMap<String, String> map = new HashMap<>();
		map = pingUrl("http://10.13.68.167/success.html");
		System.out.println(map.get("responseCode"));
		System.out.println(map.get("description"));

	}*/

}
