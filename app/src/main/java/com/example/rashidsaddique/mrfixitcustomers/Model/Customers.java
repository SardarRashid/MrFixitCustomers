package com.example.rashidsaddique.mrfixitcustomers.Model;

public class Customers {
    private String name,phone,avtarUrl,rates,employeeType;

    public Customers() {
    }

    public Customers(String name, String phone, String avtarUrl, String rates, String employeeType) {
        this.name = name;
        this.phone = phone;
        this.avtarUrl = avtarUrl;
        this.rates = rates;
        this.employeeType = employeeType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvtarUrl() {
        return avtarUrl;
    }

    public void setAvtarUrl(String avtarUrl) {
        this.avtarUrl = avtarUrl;
    }

    public String getRates() {
        return rates;
    }

    public void setRates(String rates) {
        this.rates = rates;
    }

    public String getEmployeeType() {
        return employeeType;
    }

    public void setEmployeeType(String employeeType) {
        this.employeeType = employeeType;
    }
}
