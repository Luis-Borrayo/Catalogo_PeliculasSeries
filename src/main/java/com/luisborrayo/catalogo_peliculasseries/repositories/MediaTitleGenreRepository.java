package com.luisborrayo.catalogo_peliculasseries.repositories;

import com.luisborrayo.catalogo_peliculasseries.model.MediaTitleGenre;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Transactional
public class MediaTitleGenreRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public MediaTitleGenre save(MediaTitleGenre mediaTitleGenre) {
        if (mediaTitleGenre.getId() == null) {
            entityManager.persist(mediaTitleGenre);
            return mediaTitleGenre;
        } else {
            return entityManager.merge(mediaTitleGenre);
        }
    }

    public Optional<MediaTitleGenre> findById(Long id) {
        return Optional.ofNullable(entityManager.find(MediaTitleGenre.class, id));
    }

    public List<MediaTitleGenre> findAll() {
        return entityManager.createQuery("SELECT mtg FROM MediaTitleGenre mtg", MediaTitleGenre.class)
                .getResultList();
    }

    public long countByMediaTitleId(Long mediaTitleId) {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(mtg) FROM MediaTitleGenre mtg WHERE mtg.mediaTitle.mediaTitleId = :mediaTitleId",
                Long.class
        );
        query.setParameter("mediaTitleId", mediaTitleId);
        return query.getSingleResult();
    }

    public void delete(MediaTitleGenre mediaTitleGenre) {
        if (entityManager.contains(mediaTitleGenre)) {
            entityManager.remove(mediaTitleGenre);
        } else {
            entityManager.remove(entityManager.merge(mediaTitleGenre));
        }
    }
}