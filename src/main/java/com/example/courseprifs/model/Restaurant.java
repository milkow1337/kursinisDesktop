package com.example.courseprifs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Restaurant extends BasicUser {
    private String restaurantName;
    private LocalTime openingTime;
    private LocalTime closingTime;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Cuisine> menu;
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FoodOrder> foodOrders;

    public Restaurant(String login, String password, String name, String surname, String phoneNumber, String address, String restaurantName, LocalTime openingTime, LocalTime closingTime) {
        super(login, password, name, surname, phoneNumber, address);
        this.restaurantName = restaurantName;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
    }
    
    //old constructor
    //public Restaurant(String login, String password, String name, String surname, String phoneNumber, String address) {
    //    super(login, password, name, surname, phoneNumber, address);
    //}

    @Override
    public String toString() {
        return restaurantName != null ? restaurantName : super.toString();
    }
}
