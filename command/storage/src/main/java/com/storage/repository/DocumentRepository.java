package com.storage.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.storage.model.Document;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {

}
