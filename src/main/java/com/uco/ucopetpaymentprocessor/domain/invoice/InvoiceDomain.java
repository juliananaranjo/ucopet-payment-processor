package com.uco.ucopetpaymentprocessor.domain.invoice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Misma tabla "invoice" que crea la API. Los nombres de tabla y columnas
 * deben coincidir con los de la API: el procesador usa ddl-auto=validate.
 */
@Entity
@Table(name = "invoice")
public class InvoiceDomain implements Persistable<UUID> {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "sale_order_id", nullable = false, updatable = false)
    private UUID saleOrderId;

    @Column(name = "invoice_number", nullable = false, length = 20)
    private String invoiceNumber;

    @Column(name = "headquarter_id", nullable = false)
    private UUID headquarterId;

    @Column(name = "client_id", nullable = false)
    private UUID clientID;

    @Column(name = "pet_id", nullable = false)
    private UUID petId;

    @Column(name = "issue_date", nullable = false)
    private LocalDateTime issueDate;

    @Column(name = "total", nullable = false)
    private Integer total;

    @Column(name = "paid_amount", nullable = false)
    private Integer paidAmount;

    @Column(name = "balance", nullable = false)
    private Integer balance;

    @Column(name = "observations", length = 255)
    private String observations;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 25)
    private InvoiceState state;

    // El id lo asigna la API (no lo genera la base de datos), por eso Spring
    // Data no puede saber solo con el id si la factura es nueva. Este campo se
    // lo indica: true al crearla, false despues de guardarla o leerla.
    @Transient
    private boolean isNew = true;

    public InvoiceDomain() {
        // Constructor vacio requerido por JPA. Los valores se asignan con setters.
    }

    @Override
    public UUID getId() { return id; }
    public UUID getSaleOrderId() { return saleOrderId; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public UUID getHeadquarterId() { return headquarterId; }
    public UUID getClientID() { return clientID; }
    public UUID getPetId() { return petId; }
    public LocalDateTime getIssueDate() { return issueDate; }
    public Integer getTotal() { return total; }
    public Integer getPaidAmount() { return paidAmount; }
    public Integer getBalance() { return balance; }
    public String getObservations() { return observations; }
    public InvoiceState getState() { return state; }

    public void setId(UUID id) { this.id = id; }
    public void setSaleOrderId(UUID saleOrderId) { this.saleOrderId = saleOrderId; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public void setHeadquarterId(UUID headquarterId) { this.headquarterId = headquarterId; }
    public void setClientID(UUID clientID) { this.clientID = clientID; }
    public void setPetId(UUID petId) { this.petId = petId; }
    public void setIssueDate(LocalDateTime issueDate) { this.issueDate = issueDate; }
    public void setTotal(Integer total) { this.total = total; }
    public void setPaidAmount(Integer paidAmount) { this.paidAmount = paidAmount; }
    public void setBalance(Integer balance) { this.balance = balance; }
    public void setObservations(String observations) { this.observations = observations; }
    public void setState(InvoiceState state) { this.state = state; }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad
    @PostPersist
    void markAsNotNew() {
        this.isNew = false;
    }
}