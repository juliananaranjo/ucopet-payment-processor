package com.uco.ucopetpaymentprocessor.domain.sale;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "saleOrder")
public class SaleOrderDomain {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "headquarter_id", nullable = false)
    private UUID headquarterId;

    @Column(name = "order_number", nullable = false, length = 20)
    private String orderNumber;

    @Column(name = "client_id", nullable = false)
    private UUID clientID;

    @Column(name = "pet_id", nullable = false)
    private UUID petId;

    @Column(name = "total", nullable = false)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 20)
    private SaleOrderState state;

    protected SaleOrderDomain() {
        // Requerido por JPA para leer filas de la base de datos.
    }

    public UUID getId() { return id; }
    public UUID getHeadquarterId() { return headquarterId; }
    public String getOrderNumber() { return orderNumber; }
    public UUID getClientID() { return clientID; }
    public UUID getPetId() { return petId; }
    public BigDecimal getTotal() { return total; }
    public SaleOrderState getState() { return state; }

    public void setState(SaleOrderState state) { this.state = state; }
}