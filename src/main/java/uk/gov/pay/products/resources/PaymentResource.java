package uk.gov.pay.products.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.products.model.Payment;
import uk.gov.pay.products.service.PaymentFactory;
import uk.gov.pay.products.validations.PaymentRequestValidator;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.*;

@Path("/v1/api")
public class PaymentResource {

    private static Logger logger = LoggerFactory.getLogger(PaymentResource.class);

    private final PaymentFactory paymentFactory;
    private final PaymentRequestValidator requestValidator;

    @Inject
    public PaymentResource(PaymentFactory paymentFactory, PaymentRequestValidator requestValidator) {
        this.paymentFactory = paymentFactory;
        this.requestValidator = requestValidator;
    }

    @Path("/payments/{paymentExternalId}")
    @GET
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public Response findPaymentByExternalId(@PathParam("paymentExternalId") String paymentExternalId) {
        logger.info("Find a payment with externalId - [ {} ]", paymentExternalId);
        return paymentFactory.paymentFinder().findByExternalId(paymentExternalId)
                .map(payment ->
                        Response.status(OK).entity(payment).build())
                .orElseGet(() ->
                        Response.status(NOT_FOUND).build());
    }

    @Path("/products/{productExternalId}/payments")
    @POST
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public Response createPayment(@PathParam("productExternalId") String productExternalId, JsonNode priceOverride) {
        logger.info("Create a payment for product id - [ {} ]", productExternalId);
        return requestValidator.validatePriceOverrideRequest(priceOverride)
                .map(errors -> Response.status(Response.Status.BAD_REQUEST).entity(errors).build())
                .orElseGet(() -> {
                    Payment payment = paymentFactory.paymentCreator().doCreate(productExternalId, extractAmountIfAvailable(priceOverride));
                    return Response.status(CREATED).entity(payment).build();
                });
    }

    private Long extractAmountIfAvailable(JsonNode priceOverride) {
        if (priceOverride == null || priceOverride.get("price") == null) {
            return null;
        } else {
            return priceOverride.get("price").asLong();
        }
    }

    @Path("/products/{productExternalId}/payments")
    @GET
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public Response findPaymentsByProductExternalId(@PathParam("productExternalId") String productExternalId) {
        logger.info("Find a list of payments for product id - [ {} ]", productExternalId);
        List<Payment> payments = paymentFactory.paymentFinder().findByProductExternalId(productExternalId);
        return payments.size() > 0 ? Response.status(OK).entity(payments).build() : Response.status(NOT_FOUND).build();
    }
}
