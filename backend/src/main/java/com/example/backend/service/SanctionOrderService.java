package com.example.backend.service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

//import javax.swing.text.Document;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.example.backend.dto.GenerateOrderRequest;
import com.example.backend.dto.GenerateOrderResponse;
import com.example.backend.dto.SanctionOrderDto;
import com.example.backend.entity.GpfSanctionOrder;
import com.example.backend.entity.GpfWithdrawlDetails;
import com.example.backend.entity.GpfWithdrawlMaster;
import com.example.backend.repository.GpfWithdrawlDetailsRepository;
import com.example.backend.repository.GpfWithdrawlMasterRepository;
import com.example.backend.repository.GpfSanctionOrderRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import jakarta.transaction.Transactional;


@Service
@Transactional
public class SanctionOrderService {
    private static final Long COMPLETED_ROLE_ID = 0L; // replace with actual
    
    private final GpfWithdrawlMasterRepository gpfWithdrawlMasterRepository;

    private final GpfWithdrawlDetailsRepository gpfWithdrawlDetailsRepository;
     
    @Autowired
    private final GpfSanctionOrderRepository sanctionOrderRepository;

  
  SanctionOrderService(GpfWithdrawlMasterRepository gpfWithdrawlMasterRepository, GpfWithdrawlDetailsRepository gpfWithdrawlDetailsRepository) {
    this.gpfWithdrawlMasterRepository = gpfWithdrawlMasterRepository;
    this.gpfWithdrawlDetailsRepository = gpfWithdrawlDetailsRepository;
    this.sanctionOrderRepository = null;
  }


    public SanctionOrderDto buildSanctionOrder(Long applicationId) {

    // 1. Fetch master
    GpfWithdrawlMaster master = gpfWithdrawlMasterRepository.findById(applicationId).orElseThrow(() -> new RuntimeException("Master not found"));

    // 2. Fetch details using relationship
    GpfWithdrawlDetails details = gpfWithdrawlDetailsRepository.findByMaster_Id(applicationId).orElseThrow(() -> new RuntimeException("Details not found"));

    // 3. VALIDATION (very important)
    if (!COMPLETED_ROLE_ID.equals(details.getCurrentOwnerRole())) 
        {
             throw new RuntimeException("Order can only be generated for completed applications");
        }

    // 4. Map DTO
    SanctionOrderDto dto = new SanctionOrderDto();

    dto.setRequestedWithdrawlAmount(details.getAmountofwithdrawlrequested() != null ? details.getAmountofwithdrawlrequested().doubleValue() : null  );
    dto.setDesignation(master.getDesignation());
    dto.setEmpCode(master.getEmpcode());
    dto.setGpfAccNo(details.getGpfaccountno());
    dto.setPurposeOfWithdrawl(details.getPurposeofwithdrawl());

    dto.setDateOfJoining(
        master.getDateofjoining() != null
            ? master.getDateofjoining().toString()
            : null
    );

    dto.setDateOfRetirement(
        master.getDateofsuperannuation() != null
            ? master.getDateofsuperannuation().toString()
            : null
    );

    dto.setClosingBalance(details.getOutstandingbalance() != null ? details.getOutstandingbalance().doubleValue() : null);
    dto.setCreditAmount(details.getTotalcreditamount() != null ? details.getTotalcreditamount().doubleValue() : null);
    dto.setRefundAmount(details.getRefundafterdateofoutstandingbalance() != null ? details.getRefundafterdateofoutstandingbalance().doubleValue() : null );

    // ⚠️ confirm this mapping
    dto.setSubsequentWithdrawl(details.getPriorwithdrawlamount() != null ? details.getPriorwithdrawlamount().doubleValue() : null    );

    return dto;
}

    public GenerateOrderResponse generateOrder(GenerateOrderRequest request) {


    Long applicationId = request.getApplicationId();

//System.out.println("test if it has reached service");
//System.out.println(applicationId.toString());
    // 1. Check existing
    
try
{
    Optional<GpfSanctionOrder> existing = sanctionOrderRepository.findByApplicationId(applicationId);

    System.out.println("after repo call");



            System.out.println("test if it has reached service");
    if (existing.isPresent()) {
        GpfSanctionOrder order = existing.get();

        GenerateOrderResponse response = new GenerateOrderResponse();
        response.setOrderId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setStatus("SUCCESS");
        response.setMessage("Order already generated");

        return response;
    }
} catch (Exception e) {
    e.printStackTrace();
}
    // 2. Build DTO
    SanctionOrderDto dto = buildSanctionOrder(applicationId);

    // 3. Generate text
    //String orderText = generateSanctionOrderText(dto);
    String orderText = "Sanction Order Generated";

    // 4. Generate order number
    String orderNumber = generateOrderNumber();

    // 5. Generate PDF (IMPORTANT NEW STEP)
    byte[] pdfBytes = generatePdf(dto);

    // 6. Save everything
    GpfSanctionOrder entity = new GpfSanctionOrder();
    entity.setApplicationId(applicationId);
    entity.setOrderNumber(orderNumber);
    entity.setOrderText(orderText);
    entity.setOrderPdf(pdfBytes);   // ✅ store PDF
    entity.setGeneratedOn(LocalDateTime.now());

    System.out.println("PDF TYPE: " + entity.getOrderPdf().getClass());
    
    // 6. Save everything (REPLACE save())

sanctionOrderRepository.insertSanctionOrder(
    applicationId,
    null,     // make sure this is set (else null)
    entity.getGeneratedOn(),
    entity.getOrderNumber(),
    entity.getOrderPdf(),
    entity.getOrderText()
);
Optional<GpfSanctionOrder> saved =
        sanctionOrderRepository.findByApplicationId(applicationId);

GenerateOrderResponse response = new GenerateOrderResponse();

if (saved.isPresent()) {
    response.setOrderId(saved.get().getId());
} else {
    response.setOrderId(null); // fallback
}

response.setOrderNumber(orderNumber);
response.setStatus("SUCCESS");
response.setMessage("Sanction order generated");

return response;
    
}
private String generateOrderNumber() {
    int year = LocalDate.now().getYear();

    long count = sanctionOrderRepository.count() + 1;

    return String.format("GPF/WDL/%d/%05d", year, count);
}

{/*public byte[] generatePdf(String content) {

    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

        Document document = new Document();
        PdfWriter.getInstance(document, out);

        document.open();
        document.add(new Paragraph(content));
        document.close();

        return out.toByteArray();

    } catch (Exception e) {
        throw new RuntimeException("Error generating PDF", e);
    }
}*/}
public byte[] generatePdf(SanctionOrderDto dto) 
{
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        Document document = new Document();
        PdfWriter.getInstance(document, out);
        document.open();
        // ---------------- HEADER (CENTER) ----------------
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
        Paragraph header = new Paragraph("SANCTION ORDER", boldFont);
        header.setAlignment(Element.ALIGN_CENTER);
        header.setSpacingAfter(10);
        document.add(header);
        // ---------------- DATE (RIGHT) ----------------
        Paragraph date = new Paragraph("Date: " + java.time.LocalDate.now());
        date.setAlignment(Element.ALIGN_RIGHT);
        date.setSpacingAfter(10);
        document.add(date);
        // ---------------- MAIN PARAGRAPH ----------------
        Paragraph para = new Paragraph(
                "Sanction is hereby accorded for withdrawal of Rs. "
                        + dto.getRequestedWithdrawlAmount()
                        + " from GPF Account No. "
                        + dto.getGpfAccNo()
                        + " for the purpose of "
                        + dto.getPurposeOfWithdrawl() + "."
        );
        para.setSpacingAfter(15);
        document.add(para);
        // ---------------- DETAILS TABLE ----------------
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        table.addCell("Designation");
        table.addCell(dto.getDesignation());

        table.addCell("Date of Joining");
        table.addCell(dto.getDateOfJoining());

        table.addCell("Date of Retirement");
        table.addCell(dto.getDateOfRetirement());

        table.addCell("Closing Balance");
        table.addCell(String.valueOf(dto.getClosingBalance()));

        table.addCell("Credit Amount");
        table.addCell(String.valueOf(dto.getCreditAmount()));

        table.addCell("Refund Amount");
        table.addCell(String.valueOf(dto.getRefundAmount()));

        table.setSpacingAfter(20);
        document.add(table);

        // ---------------- SIGNATURE ----------------
        Paragraph sign = new Paragraph("Authorized Signatory");
        sign.setAlignment(Element.ALIGN_RIGHT);
        document.add(sign);

        document.close();

        return out.toByteArray();

    } 
       catch (Exception e)
        {
        throw new RuntimeException("Error generating PDF", e);
        }
}
public byte[] getOrderPdf(Long applicationId) {

    GpfSanctionOrder order = sanctionOrderRepository
            .findByApplicationId(applicationId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

    return order.getOrderPdf();
}



    public String loadTemplate() {
    try {
        ClassPathResource resource = new ClassPathResource("templates/sanction-order.txt");
        byte[] bytes = resource.getInputStream().readAllBytes();
        return new String(bytes, StandardCharsets.UTF_8);
    } catch (Exception e) {
        throw new RuntimeException("Failed to load template", e);
    }
}

    public String generateSanctionOrderText(SanctionOrderDto dto) {
    String template = loadTemplate();
    return template
        .replace("{{amount}}", String.valueOf(dto.getRequestedWithdrawlAmount()))
        .replace("{{gpfAccNo}}", dto.getGpfAccNo())
        .replace("{{designation}}", dto.getDesignation())
        .replace("{{empcode}}", dto.getEmpCode())
        .replace("{{purpose}}", dto.getPurposeOfWithdrawl())
        .replace("{{doj}}", dto.getDateOfJoining())
        .replace("{{dor}}", dto.getDateOfRetirement())
        .replace("{{closing}}", String.valueOf(dto.getClosingBalance()))
        .replace("{{credit}}", String.valueOf(dto.getCreditAmount()))
        .replace("{{refund}}", String.valueOf(dto.getRefundAmount()))
        .replace("{{subsequent}}", String.valueOf(dto.getSubsequentWithdrawl()));
}
}
