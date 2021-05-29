package com.example.poyraz.cse476project;

public class Product {

    private String productName;
    private Double productPrice;
    private String productCurrency;
    private String imageURL;

    public void setProductName(String productName) {

        this.productName = productName;
    }

    public void setProductPrice(Double productPrice) {

        this.productPrice = productPrice;
    }

    public void setProductCurrency(String productCurrency) {

        this.productCurrency = productCurrency;
    }

    public void setImageURL(String imageURL) {

        this.imageURL = imageURL;
    }

    public String getProductName() {

        return productName;
    }

    public Double getProductPrice() {

        return productPrice;
    }

    public String getProductCurrency() {

        return productCurrency;
    }

    public String getImageURL() {

        return imageURL;
    }

}
