package com.example.backend.commons;

import org.springframework.util.MultiValueMap;

import javax.net.ssl.*;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

	public static String getQuery(MultiValueMap<String,String> params) {
		StringBuilder result = new StringBuilder();
		boolean first = true;

		for (String key : params.keySet()) {
			for(String value : params.get(key)) {
				if (first)
					first = false;
				else
					result.append("&");
				result.append(URLEncoder.encode(key, StandardCharsets.UTF_8));
				result.append("=");
				result.append(URLEncoder.encode(value, StandardCharsets.UTF_8));
			}
		}

		return result.toString();
	}

	public static void trustAllHosts() {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[]{};
			}

			public void checkClientTrusted(X509Certificate[] chain,
										   String authType) throws CertificateException {
			}

			public void checkServerTrusted(X509Certificate[] chain,
										   String authType) throws CertificateException {
			}
		}};

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection
					.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}

	public String getTxnID(){

		//SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmssSSS");
		//DateFormat txn = new SimpleDateFormat("yyyyMMdd'T'hhmmss");
		DateFormat txn = new SimpleDateFormat("yyyyMMddhhmmssSSS");
		String txnId = txn.format(new Date());
		return txnId;
	}

	public static void safeClose(InputStream inputStream){
		try{
			if(inputStream!=null){
				inputStream.close();
			}
		} catch (IOException ignored){

		}
	}

	public static void safeClose(OutputStream outputStream){
		try{
			if(outputStream!=null){
				outputStream.close();
			}
		} catch (IOException ignored){

		}
	}

	public static void safeClose(InputStreamReader inputStreamReader){
		try{
			if(inputStreamReader!=null){
				inputStreamReader.close();
			}
		} catch (IOException ignored){

		}
	}

	public static void safeClose(BufferedReader bufferedReader){
		try{
			if(bufferedReader!=null){
				bufferedReader.close();
			}
		} catch (IOException ignored){

		}
	}

	final public static HostnameVerifier DO_NOT_VERIFY = (hostname, session) -> true;



}
