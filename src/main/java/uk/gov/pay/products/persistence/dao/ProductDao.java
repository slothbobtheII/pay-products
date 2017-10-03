package uk.gov.pay.products.persistence.dao;

import com.google.inject.Inject;
import com.google.inject.Provider;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.persistence.entity.ProductEntity;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProductDao extends JpaDao<ProductEntity> {

    @Inject
    protected ProductDao(Provider<EntityManager> entityManager) {
        super(entityManager, ProductEntity.class);
    }

    public Optional<ProductEntity> findByExternalId(String externalId) {
        String query = "SELECT product FROM ProductEntity product " +
                "WHERE product.externalId = :externalId";

        return entityManager.get()
                .createQuery(query, ProductEntity.class)
                .setParameter("externalId", externalId)
                .getResultList().stream().findFirst();
    }

    public List<Product> findByExternalServiceId(String externalServiceId) {
        String query = "SELECT product FROM ProductEntity product " +
                "WHERE product.catalogueEntity.externalServiceId = :externalServiceId";

        return entityManager.get()
                .createQuery(query, ProductEntity.class)
                .setParameter("externalServiceId", externalServiceId)
                .getResultList().stream()
                .map(ProductEntity::toProduct)
                .collect(Collectors.toList());
    }
}
