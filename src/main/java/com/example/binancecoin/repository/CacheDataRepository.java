package com.example.binancecoin.repository;


import com.example.binancecoin.entity.CacheDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface CacheDataRepository extends JpaRepository<CacheDataEntity,Long> {

}
