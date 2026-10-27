package com.uco.ucopetpaymentprocessor.service.invoice;

import com.uco.ucopetpaymentprocessor.dto.invoice.InvoiceCancelMessageDTO;
import com.uco.ucopetpaymentprocessor.dto.invoice.InvoiceCreateMessageDTO;
import com.uco.ucopetpaymentprocessor.dto.invoice.InvoicePaymentMessageDTO;

public interface InvoiceService {

    void registerNewInvoice(InvoiceCreateMessageDTO message);

    void registerPayment(InvoicePaymentMessageDTO message);

    void cancelInvoice(InvoiceCancelMessageDTO message);
}