package com.uco.ucopetpaymentprocessor.service.invoice;

import com.uco.ucopetpaymentprocessor.dto.invoice.InvoiceCancelMessage;
import com.uco.ucopetpaymentprocessor.dto.invoice.InvoiceCreateMessage;
import com.uco.ucopetpaymentprocessor.dto.invoice.InvoicePaymentMessage;

public interface InvoiceService {

    void registerNewInvoice(InvoiceCreateMessage message);

    void registerPayment(InvoicePaymentMessage message);

    void cancelInvoice(InvoiceCancelMessage message);
}