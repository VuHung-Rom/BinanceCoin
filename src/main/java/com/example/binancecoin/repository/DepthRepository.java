package com.example.binancecoin.repository;


import com.example.binancecoin.entity.DethEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepthRepository extends JpaRepository<DethEntity,Long> {
}
