package com.vehiclerental.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterForm {

    @NotBlank(message = "Enter your full name")
    @Size(max = 100, message = "Name must be 100 characters or fewer")
    private String fullName;

    @NotBlank(message = "Enter your email")
    @Email(message = "Enter a valid email address")
    @Size(max = 150, message = "Email must be 150 characters or fewer")
    private String email;

    @NotBlank(message = "Enter your phone number")
    @Pattern(regexp = "[0-9+\\- ]{7,20}", message = "Enter a valid phone number")
    private String phone;

    @NotBlank(message = "Enter your driving licence number")
    @Size(max = 30, message = "Licence number must be 30 characters or fewer")
    private String licenseNo;

    @NotBlank(message = "Choose a password")
    @Size(min = 8, max = 72, message = "Password must be 8 to 72 characters")
    private String password;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getLicenseNo() { return licenseNo; }
    public void setLicenseNo(String licenseNo) { this.licenseNo = licenseNo; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
