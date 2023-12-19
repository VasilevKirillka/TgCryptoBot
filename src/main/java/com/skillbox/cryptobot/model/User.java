package com.skillbox.cryptobot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User {
    @Id
    @Column(name = "id", unique = true, nullable = false)
    private Long telegramId;
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "user_id", unique = true)
    private UUID userId;

    @Column(name = "price", nullable = true)
    private Double price;

    @Column(name = "last_notification")
    private Timestamp lastPriceNotification;

}
