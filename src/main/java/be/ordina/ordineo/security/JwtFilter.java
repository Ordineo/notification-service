package be.ordina.ordineo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JwtFilter extends GenericFilterBean {
    @Override
    public void doFilter(final ServletRequest req,
                         final ServletResponse res,
                         final FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) req;

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ServletException("Missing or invalid Authorization header.");
        }

        final String token = authHeader.substring(7); // The part after "Bearer "

        try {
            Claims claims = Jwts.parser()
                        .setSigningKey("360t00l")
                        .parseClaimsJws(token)
                        .getBody();
            request.setAttribute("claims", claims);


            List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

            String s = (String)claims.get("role");
            //String s = request.getHeader("roles");
            s = s.replace("[", "").replace("]", "");
            String[] split = s.split(",");
            for (String string : split) {
                authorities.add(new SimpleGrantedAuthority(string));
            }

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(claims.getSubject(), "");
            authToken.setDetails(new WebAuthenticationDetails((HttpServletRequest) request));
            Authentication authentication = new UsernamePasswordAuthenticationToken(claims.getSubject(), "", authorities); //this.authenticationProvider.authenticate(token);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        catch (final SignatureException e) {
            throw new ServletException("Invalid token.");
        }

        chain.doFilter(req, res);
    }


}
