/**
 * 
 */
package hep.crest.server.security;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;

/**
 * This class configure the authentication of the application.
 *
 * @version %I%, %G%
 * @author formica
 *
 */
@Configuration
@PropertySource("classpath:/ldap.properties")
public class AuthenticationProviderConfig {

    /**
     * Provide a user details service to load user from specific DB tables.
     *
     * @param ds
     *            the DataSource
     * @return UserDetailsService
     */
    @Bean(name = "dbUserDetailsService")
    public UserDetailsService userDetails(DataSource ds) {
        final JdbcDaoImpl jdbc = new JdbcDaoImpl();
        jdbc.setDataSource(ds);
        jdbc.setUsersByUsernameQuery(
                "select crest_usrname as username, crest_usrpss as password, 1 from CREST_USERS "
                        + " where crest_usrname=?");
        jdbc.setAuthoritiesByUsernameQuery(
                "select u.crest_usrname as username, r.crest_usrrole as authority from "
                        + "CREST_USERS u, CREST_ROLES r where u.crest_usrid=r.crest_usrid "
                        + "and u.crest_usrname=?");
        return jdbc;
    }

}
