package com.example.courseprifs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Driver extends BasicUser{
    private String licence;
    private LocalDate bDate;
    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;

    @OneToMany(mappedBy = "driver", fetch = FetchType.LAZY)
    private List<FoodOrder> deliveries;

    public Driver(String login, String password, String name, String surname, String phoneNumber, String address, String licence, LocalDate bDate, VehicleType vehicleType) {
        super(login, password, name, surname, phoneNumber, address);
        this.licence = licence;
        this.bDate = bDate;
        this.vehicleType = vehicleType;
    }

    @PreRemove
    private void preRemove() {
        if (deliveries != null) {
            for (FoodOrder order : deliveries) {
                order.setDriver(null);
            }
        }
    }
}
