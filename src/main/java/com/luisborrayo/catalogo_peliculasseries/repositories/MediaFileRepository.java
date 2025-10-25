package com.luisborrayo.catalogo_peliculasseries.repositories;

import com.luisborrayo.catalogo_peliculasseries.model.MediaFile;
import com.luisborrayo.catalogo_peliculasseries.model.MediaFile.FileType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Transactional
public class MediaFileRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Optional<MediaFile> findActiveFileByTitleAndType(Long titleId, FileType fileType) {
        TypedQuery<MediaFile> query = entityManager.createQuery(
                "SELECT mf FROM MediaFile mf WHERE mf.mediaTitle.mediaTitleId = :titleId " +
                        "AND mf.fileType = :fileType AND mf.isActive = true",
                MediaFile.class
        );
        query.setParameter("titleId", titleId);
        query.setParameter("fileType", fileType);

        List<MediaFile> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public long countByIsActive(Boolean isActive) {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(mf) FROM MediaFile mf WHERE mf.isActive = :isActive",
                Long.class
        );
        query.setParameter("isActive", isActive);
        return query.getSingleResult();
    }

    public MediaFile save(MediaFile mediaFile) {
        if (mediaFile.getMediaFileId() == null) {
            entityManager.persist(mediaFile);
            return mediaFile;
        } else {
            return entityManager.merge(mediaFile);
        }
    }

    public Optional<MediaFile> findById(Long id) {
        return Optional.ofNullable(entityManager.find(MediaFile.class, id));
    }

    public List<MediaFile> findAll() {
        return entityManager.createQuery("SELECT mf FROM MediaFile mf", MediaFile.class)
                .getResultList();
    }

    public void delete(MediaFile mediaFile) {
        if (entityManager.contains(mediaFile)) {
            entityManager.remove(mediaFile);
        } else {
            entityManager.remove(entityManager.merge(mediaFile));
        }
    }
}