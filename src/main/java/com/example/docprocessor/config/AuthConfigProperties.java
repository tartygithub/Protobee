package com.example.docprocessor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.auth")
public class AuthConfigProperties {

    private boolean ldapEnabled = false;
    private String ldapUrl = "ldap://localhost:8389";
    private String ldapBase = "dc=example,dc=com";
    private String ldapUserDnPattern = "uid={0},ou=people";

    // Getters & Setters
    public boolean isLdapEnabled() { return ldapEnabled; }
    public void setLdapEnabled(boolean ldapEnabled) { this.ldapEnabled = ldapEnabled; }

    public String getLdapUrl() { return ldapUrl; }
    public void setLdapUrl(String ldapUrl) { this.ldapUrl = ldapUrl; }

    public String getLdapBase() { return ldapBase; }
    public void setLdapBase(String ldapBase) { this.ldapBase = ldapBase; }

    public String getLdapUserDnPattern() { return ldapUserDnPattern; }
    public void setLdapUserDnPattern(String ldapUserDnPattern) { this.ldapUserDnPattern = ldapUserDnPattern; }
}
