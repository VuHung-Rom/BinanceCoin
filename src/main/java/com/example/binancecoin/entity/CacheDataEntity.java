package com.example.binancecoin.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;
@Table(name="cachedata")
@Getter
@Setter
@Entity
public class CacheDataEntity {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date openTime;
    private Date closeTime;
    private Double volume;
    private String token;


}
