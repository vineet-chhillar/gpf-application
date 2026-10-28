package com.example.backend.commons;

import org.slf4j.Logger;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

//import com.itextpdf.xmp.impl.Utils;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.*;
import javax.net.ssl.HttpsURLConnection;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class LoginHelper {
    public static Map<String, String> authenticate(String ldapURL, String bindDN, String bindPASS, String baseDN,
                                            String username, String password) {
        /*
         * System.out.println("CALL LDAP AUTHENTICATE FUCNTION");
         * System.out.println("LDAP CONNECT CREDENTIALS:");
         * System.out.println("LDAP URL : " + ldapURL); System.out.println("BindDN : " +
         * bindDN); System.out.println("BindPassword : " + bindPASS);
         * System.out.println("BaseDN : " + baseDN);
         * System.out.println("USER CREDENTIALS:"); System.out.println("USERNAME : " +
         * username); System.out.println("PASSWORD : " + password);
         */
        try {
            Hashtable connectLDAPHashtable = new Hashtable();
            connectLDAPHashtable.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            connectLDAPHashtable.put(Context.PROVIDER_URL, ldapURL);
            connectLDAPHashtable.put(Context.SECURITY_AUTHENTICATION, "simple");
            connectLDAPHashtable.put(Context.SECURITY_PRINCIPAL, bindDN);
            connectLDAPHashtable.put(Context.SECURITY_CREDENTIALS, bindPASS);
            String[] attribute = {"dn", "mobile", "cn"};
            SearchControls searchControls = new SearchControls();
            searchControls.setReturningAttributes(attribute);
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            /*
             * String searchFilter = "(|(mail=" + username +
             * "@nic.in)(mailequivalentaddress=" + username + "@nic.in)" + "(mail=" +
             * username + "@ias.nic.in)(mailequivalentaddress=" + username +
             * "@ias.nic.in))";
             */
            StringBuilder searchFilter = new StringBuilder();
            searchFilter.append("(|");
            searchFilter.append("(|(mail=" + username + "@nic.in)(mailequivalentaddress=" + username + "@nic.in))");
            searchFilter.append("(|(mail=" + username + "@gov.in)(mailequivalentaddress=" + username + "@gov.in))");
            searchFilter
                    .append("(|(mail=" + username + "@ias.nic.in)(mailequivalentaddress=" + username + "@ias.nic.in))");
            searchFilter.append("(|(mail=" + username + ")(mailequivalentaddress=" + username + "))");
            searchFilter.append("(|(mail=" + username + ")(uid=" + username + "))");
            searchFilter.append(")");

            DirContext connectLDAPContext = new InitialDirContext(connectLDAPHashtable);
            NamingEnumeration ne = connectLDAPContext.search(baseDN, searchFilter.toString(), searchControls);
            if (ne.hasMoreElements()) {
                String userID = null;
                try {
                    while (ne.hasMoreElements()) {
                        SearchResult sr = (SearchResult) ne.nextElement();
                        String searchResultStr = sr.toString();

                        /* System.out.println("SEARCH RESULT STRING : " + searchResultStr); */

                        int index = searchResultStr.indexOf(':');
                        userID = searchResultStr.substring(0, index);

                        /* System.out.println("UID (USER ID) : " + userID); */
                    }
                    connectLDAPContext.close();
                    connectLDAPHashtable.clear();
                } catch (Exception ex1) {
                    System.err.println("EXCEPTION WHILE EXTRACTING UID (USER ID).");
                    System.err.println("UID EXCEPTION : " + ex1);
                    System.err.println("UID EXCEPTION : " + ex1.getMessage());
                }
                Hashtable ht1 = new Hashtable();
                ht1.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
                ht1.put(Context.PROVIDER_URL, ldapURL);
                ht1.put(Context.SECURITY_PRINCIPAL, userID + "," + baseDN);
                ht1.put(Context.SECURITY_CREDENTIALS, password);

                try {
                    DirContext authenUserContext = new InitialDirContext(ht1);
                    NamingEnumeration searchAuthenUserContext = authenUserContext.search(baseDN,
                            searchFilter.toString(), searchControls);
                    Map<String, String> ldapDataMap = new HashMap<String, String>();
                    if (searchAuthenUserContext.hasMoreElements()) {
                        SearchResult sr = (SearchResult) searchAuthenUserContext.next();
                        String dn = sr.getName() + ", " + baseDN;
                        System.out.println("Distinguished Name is : " + dn);
                        String MY_ATTRS[] = {"cn", "mobile", "employeenumber"};
                        Attributes attributes = authenUserContext.getAttributes(dn, MY_ATTRS);
                        ldapDataMap.put("cn",
                                attributes.get("cn") != null && !attributes.get("cn").get().equals("")
                                        ? attributes.get("cn").get().toString()
                                        : "");
                        ldapDataMap.put("mobile",
                                attributes.get("mobile") != null && !attributes.get("mobile").get().equals("")
                                        ? attributes.get("mobile").get().toString()
                                        : "");
                        ldapDataMap.put("employeenumber",
                                attributes.get("employeenumber") != null
                                        && !attributes.get("employeenumber").get().equals("")
                                        ? attributes.get("employeenumber").get().toString()
                                        : "");

                        /*
                         * System.out.println("CN : " + (attributes.get("cn") != null &&
                         * !attributes.get("cn").get().equals("") ?
                         * attributes.get("cn").get().toString() : "")); System.out.println("MOBILE : "
                         * + (attributes.get("mobile") != null &&
                         * !attributes.get("mobile").get().equals("") ?
                         * attributes.get("mobile").get().toString() : ""));
                         * System.out.println("ECODE : " + (attributes.get("employeenumber") != null &&
                         * !attributes.get("employeenumber").get().equals("") ?
                         * attributes.get("employeenumber").get().toString() : ""));
                         */
                    }
                    System.out.println("USER " + username + " AUTHENTICATED WITH CORRECT USERNAME AND PASSWORD.");
                    return ldapDataMap;
                } catch (Exception ex) {
                    System.err.println("EXCEPTION FOR WRONG PASSWORD");
                    System.err.println("WRONG PASSWORD EXCEPTION : " + ex);
                    System.err.println("WRONG PASSWORD EXCEPTION MESSAGE : " + ex.getMessage());
                    return null;
                }
            } else {
                System.out.println("USER " + username + "   IS NOT Valid");
                return null;
            }
        } catch (Exception e) {
            System.err.println("EXCPETION WHILE CONNECTING TO LDAP SERVER.");
            System.err.println("LDAP EXCEPTION : " + e);
            e.printStackTrace();
            System.err.println("LDAP EXCEPTION MESSAGE: " + e.getMessage());
            return null;
        }
    }


    public static String checkLoginDigNic(Logger logger, String processid, String sessionid, String clientIp) throws IOException {
        URL obj = new URL("https://digital.nic.in/webservice/rest/validatesession.php");
        HttpsURLConnection con = null;
        DataOutputStream wr = null;
        BoundedBufferedReader bin = null;
        StringBuffer response = null;
        InputStreamReader isr = null;
        try {
            //trustCustomCertificate();
            Utils.trustAllHosts();
            con = (HttpsURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setConnectTimeout(2000);
            wr = new DataOutputStream(con.getOutputStream());
            MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
            requestParams.add("empcode", processid);
            requestParams.add("sessionid", sessionid);
            requestParams.add("clientip", clientIp);
            wr.writeBytes(Utils.getQuery(requestParams));
            wr.flush();


            isr = new InputStreamReader(con.getInputStream());
            bin = new BoundedBufferedReader(isr);
            String inputLine;
            response = new StringBuffer();

            int i = 0;
            while ((inputLine = bin.readLine()) != null) {
                //if (i == 0) {
                response.append(inputLine);
                /*} else {
                    response.append("\n");
                    response.append(inputLine);
                }
                i++;*/
            }

        } catch (IOException e) {
            e.printStackTrace();
            logger.error(Arrays.toString(e.getStackTrace()));
        } finally {
            if (wr != null) {
                wr.close();
            }
            if (isr != null) {
                isr.close();
            }
            if (bin != null) {
                bin.close();
            }
            if (con != null) {
                con.disconnect();
            }
        }
        if (response != null) {
            return response.toString();
        } else {
            return null;
        }


    }
}
