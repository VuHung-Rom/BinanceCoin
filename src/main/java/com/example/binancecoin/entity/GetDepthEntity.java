package com.example.binancecoin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
@Entity

public class GetDepthEntity {
    @Id
    @Column
    private Long id;

}
