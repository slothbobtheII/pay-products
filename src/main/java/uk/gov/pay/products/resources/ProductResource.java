package uk.gov.pay.products.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import io.dropwizard.jersey.PATCH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.products.model.Product;
import uk.gov.pay.products.service.ProductFactory;
import uk.gov.pay.products.validations.ProductRequestValidator;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.*;

@Path("/")
public class ProductResource {
    private static Logger logger = LoggerFactory.getLogger(ProductResource.class);

    private static final String API_VERSION_PATH = "v1";
    public static final String PRODUCTS_RESOURCE_PATH = API_VERSION_PATH + "/api/products";
    public static final String PRODUCT_RESOURCE_PATH = PRODUCTS_RESOURCE_PATH + "/{productExternalId}";
    public static final String DISABLE_PRODUCT_RESOURCE_PATH = PRODUCTS_RESOURCE_PATH + "/{productExternalId}/disable";

    private final ProductRequestValidator requestValidator;
    private final ProductFactory productFactory;


    @Inject
    public ProductResource(ProductRequestValidator requestValidator, ProductFactory productFactory) {
        this.requestValidator = requestValidator;
        this.productFactory = productFactory;
    }

    @POST
    @Path(PRODUCTS_RESOURCE_PATH)
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @PermitAll
    public Response createProduct(JsonNode payload) {
        logger.info("Create Service POST request - [ {} ]", payload);
        return requestValidator.validateCreateRequest(payload)
                .map(errors -> Response.status(Status.BAD_REQUEST).entity(errors).build())
                .orElseGet(() -> {
                    Product product = productFactory.productCreator().doCreate(Product.from(payload));
                    return Response.status(Status.CREATED).entity(product).build();
                });

    }

    @GET
    @Path(PRODUCT_RESOURCE_PATH)
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @PermitAll
    public Response findProduct(@PathParam("productExternalId") String productExternalId) {
        logger.info("Find a product with externalId - [ {} ]", productExternalId);
        return productFactory.productFinder().findByExternalId(productExternalId)
                .map(product ->
                        Response.status(OK).entity(product).build())
                .orElseGet(() ->
                        Response.status(NOT_FOUND).build());
    }

    @PATCH
    @Path(DISABLE_PRODUCT_RESOURCE_PATH)
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @PermitAll
    public Response disableProduct(@PathParam("productExternalId") String productExternalId) {
        logger.info("Disabling a product with externalId - [ {} ]", productExternalId);
        return productFactory.productFinder().disableProduct(productExternalId)
                .map(product -> Response.status(NO_CONTENT).build())
                .orElseGet(() -> Response.status(NOT_FOUND).build());
    }

    @GET
    @Path(PRODUCTS_RESOURCE_PATH)
    @Produces(APPLICATION_JSON)
    @PermitAll
    public Response findProducts(@QueryParam("gatewayAccountId") Integer gatewayAccountId) {
        logger.info("Searching for products with gatewayAccountId - [ {} ]", gatewayAccountId);
        List<Product> products = productFactory.productFinder().findByGatewayAccountId(gatewayAccountId);
        return products.size() > 0 ? Response.status(OK).entity(products).build() : Response.status(NOT_FOUND).build();
    }
}
