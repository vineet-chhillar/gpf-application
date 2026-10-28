
package com.example.backend.controller;

import com.example.backend.commons.LoginHelper;
import com.example.backend.dto.LoginForm;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.backend.dto.GpfEmployeeDetailsDTO;
import com.example.backend.service.GpfEmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.IOException;

@Controller
public class LoginController {

    @Autowired
private GpfEmployeeService employeeService;

    private static final Logger logger =
            LoggerFactory.getLogger(LoginController.class);

    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    @Value("${loginType:ldap}")
    private String loginType;

    @Value("${gpf.ldap.url:ldaps://auths.nic.in:636}")
    private String ldapURL;

    @Value("${gpf.ldap.bind-dn:}")
private String bindDN;

@Value("${gpf.ldap.bind-password:}")
private String bindPASS;

@Value("${gpf.ldap.base-dn:}")
private String baseDN;

    @GetMapping("/")
    public String root() {

        System.out.println("Root path accessed.");
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {

            // Serve the compiled React application's index.html
            return "forward:/index.html";
        }

        return "redirect:/index";
    }

@GetMapping("/logout")
public void logout(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication) throws IOException {

    // Invalidate the HTTP session and clear Spring Security authentication
    SecurityContextLogoutHandler logoutHandler =
            new SecurityContextLogoutHandler();

    logoutHandler.setInvalidateHttpSession(true);
    logoutHandler.setClearAuthentication(true);

    logoutHandler.logout(request, response, authentication);

    // Clear the security context for this request
    SecurityContextHolder.clearContext();

    // Redirect to the login page, not the application root
    response.sendRedirect(request.getContextPath() + "/index?loggedOut=true");
}

    @GetMapping("/index")
public String index(Model model) {
System.out.println("Index path accessed.");
    Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();
System.out.println("Index path accessed1.");
    if (authentication != null
            && authentication.isAuthenticated()
            && !(authentication instanceof AnonymousAuthenticationToken)) {
        return "redirect:/";
    }
    System.out.println("Index path accessed2.");

    model.addAttribute("user", new LoginForm());
    System.out.println("Index path accessed3.");
    return "login";
}

    @PostMapping("/loginprocess")
    public String login(
            @ModelAttribute("user") @Valid LoginForm user,
            BindingResult result,
            HttpServletRequest request,
            HttpServletResponse response) {


                System.out.println("login process started.");
        if (result.hasErrors()) {
            return "login";
        }

      String loginId = user.getLoginId();

if (loginId == null || loginId.isBlank()) {
    return loginFailure(result);
}

loginId = loginId.trim();

String empCode;
System.out.println(loginId);
        try {

    if ("debug".equalsIgnoreCase(loginType.trim())) {

        // STAGING ONLY: login ID and password must match.
        String password = user.getPassword();

        if (password == null
                || !loginId.equals(password)
                || !loginId.matches("\\d+")) {

            return loginFailure(result);
        }

        // Use login ID as employee code in staging.
        empCode = Long.toString(Long.parseLong(loginId));

        System.out.println("Staging authentication successful.");

    } else if ("ldap".equalsIgnoreCase(loginType.trim())) {

        // Production: authenticate using LDAP.
        Map<String, String> responseMap =
                LoginHelper.authenticate(
                        ldapURL,
                        bindDN,
                        bindPASS,
                        baseDN,
                        loginId,
                        user.getPassword()
                );

        if (responseMap == null || responseMap.isEmpty()) {
            return loginFailure(result);
        }

        responseMap = responseMap.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().trim().toLowerCase(),
                        e -> e.getValue() == null
                                ? ""
                                : e.getValue().trim(),
                        (first, second) -> first
                ));

        empCode = responseMap.get("employeenumber");

        if (empCode == null || empCode.isBlank()) {
            logger.warn("LDAP employee number is missing");
            return loginFailure(result);
        }

        empCode = Long.toString(Long.parseLong(empCode));

    } else {

        logger.error("Unsupported login type configured");
        return loginFailure(result);
    }

} catch (NumberFormatException ex) {

    logger.warn("Invalid employee code during authentication");
    return loginFailure(result);

} catch (Exception ex) {

    logger.error("Authentication service failed", ex);
    return loginFailure(result);
}

System.out.println("Authentication block completed");
System.out.println("Employee code: " + empCode);


       try {
    System.out.println("Starting employee code conversion");

    Long employeeCode = Long.parseLong(empCode);

    System.out.println("Employee code converted: " + employeeCode);
    System.out.println("Fetching employee details from database");

    GpfEmployeeDetailsDTO employeeDetails =
            employeeService.getEmployeeDetails(employeeCode)
                    .orElse(null);

    System.out.println(
            "Employee details found: " + (employeeDetails != null)
    );

    if (employeeDetails == null) {
        logger.warn("No active GPF employee record found");

        result.addError(
                new ObjectError(
                        "user",
                        "Employee record not found or not authorized for GPF"
                )
        );

        return "login";
    }

    System.out.println("Creating Spring Security authentication");

    Authentication authentication =
            new UsernamePasswordAuthenticationToken(
                    empCode,
                    null,
                    List.of(
                            new SimpleGrantedAuthority("ROLE_GPF_USER")
                    )
            );

    System.out.println("Creating session");

    request.getSession(true);
    request.changeSessionId();

    SecurityContext context =
            SecurityContextHolder.createEmptyContext();

    context.setAuthentication(authentication);
    SecurityContextHolder.setContext(context);

    System.out.println("Saving security context");

    securityContextRepository.saveContext(
            context,
            request,
            response
    );

    System.out.println("Saving employee details in session");

    request.getSession(false).setAttribute(
            "GPF_EMPLOYEE_DETAILS",
            employeeDetails
    );

    logger.info("GPF login successful");
    System.out.println("Login completed successfully");

    return "redirect:/";

} catch (Exception ex) {

    logger.error(
            "Error after authentication while loading employee or creating session",
            ex
    );

    result.addError(
            new ObjectError(
                    "user",
                    "Login could not be completed. Please check the application logs."
            )
    );

    return "login";
}
    }

    private String loginFailure(BindingResult result) {

        result.addError(
                new ObjectError("user", "Invalid Login ID or Password")
        );

        return "login";
    }
}
