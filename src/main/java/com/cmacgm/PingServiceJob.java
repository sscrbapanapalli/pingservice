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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.cmacgm.model.Application;
import com.cmacgm.model.ApplicationUrl;
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
		// Initial Method used for bypassing SSL verification for https URLS
		disableSSLVerification();
	}

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		LOGGER.debug("adding applicationRepository job");
		List<com.cmacgm.model.Application> application = applicationRepository.findAll();

		if (stringNotEmptyOrNull(application)) {
			long rate = 0l, delay = 0l;

			/*
			 * Iterating the Application to check the ApplicationUrl
			 * availability
			 */
			for (com.cmacgm.model.Application configApplication : application) {
				/* Application SyncJob Initial Delay time in milliseconds */
				if (stringNotEmptyOrNull(configApplication.getSyncJobInitialDelay()))
					delay = configApplication.getSyncJobInitialDelay();
				/*
				 * Application Sync Job Rate to run again and again in
				 * milliseconds
				 */
				if (stringNotEmptyOrNull(configApplication.getSyncJobRate()))
					rate = configApplication.getSyncJobRate();

				taskRegistrar.addFixedRateTask(new IntervalTask(new Runnable() {
					Application configAppl = new Application();

					public void run() {
						LOGGER.debug("running applicationRepository job");

						/*
						 * update Last Sync Time when the job is executed for
						 * these application
						 */
						if (stringNotEmptyOrNull(configApplication.getId()))
							applicationRepository.updateLastSyncTime(new Date(), configApplication.getId());
						/*
						 * get the Last Sync Time when the job is executed for
						 * these application to send the email with
						 * corresponding updated time
						 */
						if (stringNotEmptyOrNull(configApplication.getId()))
							configAppl = applicationRepository.findByIdApplication(configApplication.getId());

						try {
							StringBuilder buf = new StringBuilder();
							boolean trigger = false;
							/* appending the table header to send an email */

							buf.append("<html>" + "<body>" + "<table border='1'>" + "<tr>" + "<th>Application Name</th>"
									+ "<th>Server Type</th>" + "<th>Status Code</th>" + "<th>Description </th>"
									+ "<th>Application Url</th>" + "<th>Last Sync Time </th>" + "</tr>");

							/*
							 * Iterating the ApplicationUrl to check the
							 * availability
							 */
							for (com.cmacgm.model.ApplicationUrl applicationUrl : configAppl.getApplicationUrl()) {
								ApplicationUrl configWebApplUrl = new ApplicationUrl();

								String statusCode = "", prevStatusCode = "", serverTypeName = "", tempStatusCode = "";
								/*Retry count rate must be atleast 1 or greater than 1*/
								Integer retryCount = null, retryCountRate = null;
								Long appUrlId = null;
								String error400 = "400", status200 = "200", serverTypeWeb = "web",
										serverTypeWebService = "webservices", serverTypeDb = "db",
										serverTypeServer = "server";

								if (stringNotEmptyOrNull(applicationUrl)
										&& stringNotEmptyOrNull(applicationUrl.getId()))
									appUrlId = applicationUrl.getId();

								if (stringNotEmptyOrNull(applicationUrl)
										&& stringNotEmptyOrNull(applicationUrl.getServerType().getName()))
									serverTypeName = applicationUrl.getServerType().getName();

								if (stringNotEmptyOrNull(appUrlId) && stringNotEmptyOrNull(applicationUrl)
										&& (serverTypeName.equalsIgnoreCase(serverTypeWeb)
												|| serverTypeName.equalsIgnoreCase(serverTypeWebService)
												|| serverTypeName.equalsIgnoreCase(serverTypeServer)
												|| serverTypeName.equalsIgnoreCase(serverTypeDb))) {

									HashMap<String, String> hMap = new HashMap<>();
									/*
									 * if serverTypeName is web and
									 * serverTypeName is webservices execute the
									 * pingUrl method based on application url
									 */
									if (stringNotEmptyOrNull(applicationUrl.getApplicationUrl())
											&& (serverTypeName.equalsIgnoreCase(serverTypeWeb)
													|| serverTypeName.equalsIgnoreCase(serverTypeWebService)))
										hMap = pingUrl(applicationUrl.getApplicationUrl());
									/*
									 * if serverTypeName is server and
									 * serverTypeName is db execute the
									 * isSocketAliveUtility method parameters
									 * host ip and port number
									 */
									else if (stringNotEmptyOrNull(applicationUrl)
											&& stringNotEmptyOrNull(applicationUrl.getIpAddress())
											&& stringNotEmptyOrNull(applicationUrl.getHostPortNo())
											&& (serverTypeName.equalsIgnoreCase(serverTypeServer)
													|| serverTypeName.equalsIgnoreCase(serverTypeDb)))
										hMap = isSocketAliveUtility(applicationUrl.getIpAddress(),
												applicationUrl.getHostPortNo());

									if (stringNotEmptyOrNull(applicationUrl)) {
										if (stringNotEmptyOrNull(applicationUrl.getRetryCount()))
											retryCount = applicationUrl.getRetryCount();

										if (stringNotEmptyOrNull(applicationUrl.getRetryCountRate()))
											retryCountRate = applicationUrl.getRetryCountRate();

									}

									if (stringNotEmptyOrNull(applicationUrl)
											&& stringNotEmptyOrNull(applicationUrl.getStatusCode()))
										prevStatusCode = applicationUrl.getStatusCode();

									if (!isEmpty(hMap)) {
										statusCode = hMap.get("responseCode");

										if (statusCode != error400 && stringNotEmptyOrNull(retryCount)
												&& stringNotEmptyOrNull(retryCountRate) && retryCount > 0
												&& retryCount <= retryCountRate - 1) {
											/*
											 * In between retry count Interval
											 * service may be be up or down
											 * retry count is greater than zero
											 * increment the retry count to 1
											 */

											applicationUrlRepository.updateRetryCount(retryCount + 1, appUrlId);
											configWebApplUrl = applicationUrlRepository
													.findByIdApplicationUrl(appUrlId);
											if (stringNotEmptyOrNull(configWebApplUrl.getRetryCount()))
												retryCount = configWebApplUrl.getRetryCount();

											if (stringNotEmptyOrNull(configWebApplUrl.getRetryCountRate()))
												retryCountRate = configWebApplUrl.getRetryCountRate();
										}
									}
									configWebApplUrl = applicationUrlRepository
											.findByIdApplicationUrl(appUrlId);
									
									if (stringNotEmptyOrNull(configWebApplUrl)
											&& stringNotEmptyOrNull(configWebApplUrl.getTempStatus()))
										tempStatusCode = configWebApplUrl.getTempStatus();
									/*
									 * In between retry Interval service may be
									 * be up or down again email trigger retry
									 * count update to 0
									 */
									if (stringNotEmptyOrNull(configWebApplUrl) && stringNotEmptyOrNull(statusCode)
											&& stringNotEmptyOrNull(tempStatusCode) && stringNotEmptyOrNull(prevStatusCode)
											&& ((!tempStatusCode.equalsIgnoreCase(statusCode)
											&& !tempStatusCode.equalsIgnoreCase(prevStatusCode)))
											&& stringNotEmptyOrNull(retryCount) && retryCount > 0) {
										applicationUrlRepository.updateTempStatus(statusCode, appUrlId);
										applicationUrlRepository.updateRetryCount(0, appUrlId);
										configWebApplUrl = applicationUrlRepository.findByIdApplicationUrl(appUrlId);
									}else if (stringNotEmptyOrNull(configWebApplUrl) && stringNotEmptyOrNull(statusCode)
											&& stringNotEmptyOrNull(tempStatusCode) && stringNotEmptyOrNull(prevStatusCode)
											&& (statusCode.equalsIgnoreCase(tempStatusCode) && tempStatusCode.equalsIgnoreCase(prevStatusCode) && prevStatusCode.equalsIgnoreCase(statusCode))
											&& stringNotEmptyOrNull(retryCount) && retryCount > 0) {										
										applicationUrlRepository.updateRetryCount(0, appUrlId);
										configWebApplUrl = applicationUrlRepository.findByIdApplicationUrl(appUrlId);
									}

									if (stringNotEmptyOrNull(configWebApplUrl.getRetryCountRate()))
										retryCountRate = configWebApplUrl.getRetryCountRate();

									/*
									 * In between retry Interval web service may
									 * be be up or down status code change
									 */
									if (!isEmpty(hMap) && stringNotEmptyOrNull(statusCode)
											&& stringNotEmptyOrNull(retryCount)
											&& !prevStatusCode.equalsIgnoreCase(statusCode)) {
										/*
										 * status code change and not equal to
										 * 400 then only retry count update to 1
										 */
										if (statusCode != error400 && retryCount == 0) {
											applicationUrlRepository.updateRetryCount(1, appUrlId);
										}

										if (stringNotEmptyOrNull(configWebApplUrl)) {
											/*
											 * getting an updated record for
											 * retry count
											 */
											configWebApplUrl = applicationUrlRepository
													.findByIdApplicationUrl(appUrlId);
											if (stringNotEmptyOrNull(configWebApplUrl.getRetryCount()))
												retryCount = configWebApplUrl.getRetryCount();

											if (stringNotEmptyOrNull(configWebApplUrl.getRetryCountRate()))
												retryCountRate = configWebApplUrl.getRetryCountRate();
										}
										applicationUrl.setStatusCode(statusCode);
										applicationUrl.setDescription(hMap.get("description"));
										/*
										 * if retryCount and retryCountRate
										 * equals email content attached to
										 * string builder
										 */
										if (retryCount == retryCountRate) {
											if (statusCode.equalsIgnoreCase(status200)) {
												/*
												 * status code equals to 200
												 * append the green color
												 */
												applicationUrl.setStatus(true);
												buf.append("<tr><td>").append(applicationUrl.getAppName())
														.append("</td><td>").append(serverTypeName);
												buf.append("</td><td style='color:green'>").append(statusCode)
														.append("</td><td style='color:green'>")
														.append(applicationUrl.getDescription());

												buf.append("</td><td>").append(applicationUrl.getApplicationUrl())
														.append("</td><td>")
														.append(getFormatDate(configAppl.getLastSyncTime()))
														.append("</td></tr>");

											} else {
												/*
												 * status code equals to 400 it
												 * is consider as Read timeout
												 * so not set status as down
												 */
												if (statusCode == error400)
													applicationUrl.setStatus(true);
												else
													applicationUrl.setStatus(false);
												/*
												 * status count equals to 200
												 * append the red color
												 */
												buf.append("<tr><td>").append(applicationUrl.getAppName())
														.append("</td><td>").append(serverTypeName);

												buf.append("</td><td style='color:red'>").append(statusCode)
														.append("</td><td  style='color:red'>")
														.append(applicationUrl.getDescription());

												if (stringNotEmptyOrNull(configAppl.getLastSyncTime()))
													buf.append("</td><td>").append(applicationUrl.getApplicationUrl())
															.append("</td><td>")
															.append(getFormatDate(configAppl.getLastSyncTime()))
															.append("</td></tr>");

											}
										}
										/*
										 * status code not equals to 400 it is
										 * consider as Read timeout so not send
										 * email and retryCount ==
										 * retryCountRate send email using
										 * boolean value trigger equals to true
										 */
										if (statusCode != error400 && stringNotEmptyOrNull(retryCount)
												&& stringNotEmptyOrNull(retryCountRate)
												&& retryCount == retryCountRate) {
											applicationUrlRepository.update(0, applicationUrl.isStatus(),
													applicationUrl.getStatusCode(), applicationUrl.getDescription(),
													new Date(), appUrlId);
											trigger = true;

										}
									}

								}
							}
							/* finally closing the table */
							buf.append("</table>" + "</body>" + "</html>");

							/*
							 * send email if trigger boolean value equals to
							 * true
							 */
							if (trigger) {
								sendEmail(configAppl, buf.toString());

							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}, rate, delay));
			}
		}
	}

	/* string NotEmptyOrNull */
	private boolean stringNotEmptyOrNull(String st) {
		return st != null && !st.isEmpty();
	}

	/* Object is NotEmptyOrNull */
	private boolean stringNotEmptyOrNull(Object obj) {
		return obj != null;
	}

	/* Collection is NotEmptyOrNull */
	public static boolean isEmpty(Collection<?> value) {
		return value == null || value.isEmpty();
	}

	/* Map is NotEmptyOrNull */
	public static boolean isEmpty(Map<?, ?> value) {
		return value == null || value.isEmpty();
	}

	/* FormatDate for LastSyncTime Format: dd-MM-yyyy hh:mm:ss aa */
	public String getFormatDate(Date lastSyncTime) {
		DateFormat outputformat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss aa");
		String output = null;
		output = outputformat.format(lastSyncTime);
		return output;
	}

	/*
	 * To check rest url for application url return HashMap with responseCode
	 * and description
	 */
	private static HashMap<String, String> pingUrl(String address) {
		HashMap<String, String> map = new HashMap<>();
		int responseCode = 404;
		map.put("responseCode", " ");
		map.put("description", " ");
		try {
			URL url = new URL(address);

			URLConnection urlConnection = url.openConnection();

			urlConnection.setConnectTimeout(15000);
			urlConnection.setReadTimeout(15000);
			if (address.startsWith("http")) {
				/* To check http rest url for application url */
				responseCode = ((HttpURLConnection) urlConnection).getResponseCode();
				map.put("responseCode", String.valueOf(responseCode));
			} else {
				/* To check https rest url for application url */
				responseCode = ((HttpsURLConnection) urlConnection).getResponseCode();
				map.put("responseCode", String.valueOf(responseCode));
			}
			if (responseCode == 200)
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

	public static HashMap<String, String> isSocketAliveUtility(String hostName, String hostPortNumber)
			throws IOException {
		HashMap<String, String> map = new HashMap<>();
		// Creates a socket address from a hostname and a port number
		SocketAddress socketAddress = new InetSocketAddress(hostName, Integer.parseInt(hostPortNumber));
		Socket socket = new Socket();

		map.put("responseCode", " ");
		map.put("description", " ");
		try {
			socket.connect(socketAddress, 15000);

			map.put("responseCode", "200");
			map.put("description", "SUCCESS");
			return map;
		} catch (ConnectException e) {
			map.put("responseCode", "404");
			map.put("description", e.getMessage());
			return map;
		} catch (SocketTimeoutException e) {
			map.put("responseCode", "404");
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
		} finally {
			if (socket != null) {
				socket.close();
			}
		}

	}

	// Method used for bypassing SSL verification for https URLS
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

	// Method used for sending email for configured user for applications
	public void sendEmail(com.cmacgm.model.Application application, String htmlContent) {
		SendMail sendMail = new SendMail();

		if (application.getUsers() != null) {
			for (Users userData : application.getUsers()) {
				// System.out.println(userData.getEmail() + " " + htmlContent);
				sendMail.SendMail(application.getApplicationName() + " Development Team", userData.getEmail(),
						htmlContent);
			}

		}
	}

	/*
	 * public static void main(String args[]) { HashMap<String, String> map =
	 * new HashMap<>(); map = pingUrl("http://10.13.68.167/success.html");
	 * System.out.println(map.get("responseCode"));
	 * System.out.println(map.get("description"));
	 * 
	 * }
	 */

}
