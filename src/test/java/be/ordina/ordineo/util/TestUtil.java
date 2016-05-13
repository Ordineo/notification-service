package be.ordina.ordineo.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by PhDa on 10/05/2016.
 */

public final class TestUtil {

    static String authToken;

    private static String getToken() throws Exception {

        String url = "https://gateway-ordineo.cfapps.io/auth";
        URL object = new URL(url);

        HttpURLConnection con = (HttpURLConnection) object.openConnection();
        con.setDoOutput(true);
        con.setDoInput(true);
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestMethod("POST");

        JSONObject cred = new JSONObject();

        cred.put("username", "Nivek");
        cred.put("password", "password");

        OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
        wr.write(cred.toString());
        wr.flush();

        //display what returns the POST request

        StringBuilder sb = new StringBuilder();
        int HttpResult = con.getResponseCode();
        if (HttpResult == HttpURLConnection.HTTP_OK) {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "utf-8"));
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();
            return "Bearer " +sb.substring(10,sb.length()-3);
        } else {
            return con.getResponseMessage();
        }
    }

    public static void setAuthorities() throws Exception {
        if(authToken==null) try {
            System.out.println("Generating authToken");
            authToken = getToken();
        } catch (Exception e) {
            e.printStackTrace();
        }
        final String token = authToken.substring(7);
        Claims claims = Jwts.parser()
                .setSigningKey("360t00l")
                .parseClaimsJws(token)
                .getBody();
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

        String s = (String)claims.get("role");
        s = s.replace("[", "").replace("]", "");
        String[] split = s.split(", ");
        for (String string : split) {
            authorities.add(new SimpleGrantedAuthority(string));
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(claims.getSubject(), "", authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public static String getAuthToken() {
        if(authToken==null) try {
            System.out.println("Generating authToken");
            authToken = getToken();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return authToken;
    }
}
