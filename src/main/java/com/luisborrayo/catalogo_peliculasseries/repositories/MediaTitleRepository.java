package com.luisborrayo.catalogo_peliculasseries.repositories;

import com.luisborrayo.catalogo_peliculasseries.model.MediaTitle;
import com.luisborrayo.catalogo_peliculasseries.model.MediaTitle.TitleType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Transactional
public class MediaTitleRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public MediaTitle save(MediaTitle mediaTitle) {
        if (mediaTitle.getMediaTitleId() == null) {
            entityManager.persist(mediaTitle);
            return mediaTitle;
        } else {
            return entityManager.merge(mediaTitle);
        }
    }

    public Optional<MediaTitle> findById(Long id) {
        return Optional.ofNullable(entityManager.find(MediaTitle.class, id));
    }

    public List<MediaTitle> findAll() {
        return entityManager.createQuery("SELECT mt FROM MediaTitle mt", MediaTitle.class)
                .getResultList();
    }

    public long count() {
        return entityManager.createQuery("SELECT COUNT(mt) FROM MediaTitle mt", Long.class)
                .getSingleResult();
    }

    public long countByTitleType(TitleType titleType) {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(mt) FROM MediaTitle mt WHERE mt.titleType = :titleType",
                Long.class
        );
        query.setParameter("titleType", titleType);
        return query.getSingleResult();
    }

    public long countTitlesWithoutPoster() {
        return entityManager.createQuery(
                "SELECT COUNT(mt) FROM MediaTitle mt WHERE NOT EXISTS " +
                        "(SELECT mf FROM MediaFile mf WHERE mf.mediaTitle = mt " +
                        "AND mf.fileType = 'POSTER' AND mf.isActive = true)",
                Long.class
        ).getSingleResult();
    }

    public long countTitlesWithPoster() {
        return entityManager.createQuery(
                "SELECT COUNT(DISTINCT mt) FROM MediaTitle mt JOIN mt.mediaFiles mf " +
                        "WHERE mf.fileType = 'POSTER' AND mf.isActive = true",
                Long.class
        ).getSingleResult();
    }

    public long countByCreatedAtAfter(LocalDateTime date) {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(mt) FROM MediaTitle mt WHERE mt.createdAt > :date",
                Long.class
        );
        query.setParameter("date", date);
        return query.getSingleResult();
    }

    public void delete(MediaTitle mediaTitle) {
        if (entityManager.contains(mediaTitle)) {
            entityManager.remove(mediaTitle);
        } else {
            entityManager.remove(entityManager.merge(mediaTitle));
        }
    }
}