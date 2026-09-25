package com.example.backend.service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;

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
    dto.setEmpName(master.getEmpname());
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
    String orderText = "Sanction Order for Application ID: " + applicationId;
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
        
        Set<String> scientistGroup = Set.of("Scientist-D", "Scientist-E", "Scientist-F", "Scientist-G");
        String adminCode = scientistGroup.contains(
        dto.getDesignation().toUpperCase()
        ) ? "ADMN.I" : "ADMN.II";


        String adminCodeNew = scientistGroup.contains(
        dto.getDesignation().toUpperCase()
        ) ? "Administration Section-I" : "Administration Section-II";

        String prevFY = getPreviousFinancialYear();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate date = LocalDate.parse(dto.getDateOfRetirement(), inputFormat);
        String formattedDate = date.format(outputFormat);

        String currentFinYearStartDate = getCurrentFYStartDate().format(formatter);
        String currentFinYearEndDateTillDecember = getCurrentFYEndTillDecember().format(formatter);

        // ---------------- HEADER (CENTER) ----------------
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
        Paragraph header = new Paragraph("No." + dto.getEmpCode()+"/NIC/GPF/" + java.time.LocalDate.now().getYear()+"-"+ adminCode + "\n"
         + "Government of India" + "\n"
         + "Ministry of Electronics and Information Technology" + "\n"
         + "National Informatics Centre" + "\n"
         + "[" + adminCodeNew + "]"
         , boldFont);
        header.setAlignment(Element.ALIGN_CENTER);
        header.setSpacingAfter(10);
        document.add(header);
        //-------------------HEADER (RIGHT)----------------
        Font boldFontRight = new Font(Font.FontFamily.HELVETICA,10,Font.BOLD );
        Paragraph headerright = new Paragraph("A-Block, CGO Complex," + "\n"
        + "Lodhi Road, New Delhi-110003" + "\n"
        + "Dated: " + java.time.LocalDate.now().format(formatter) 
         , boldFontRight);
        headerright.setAlignment(Element.ALIGN_RIGHT);
        headerright.setSpacingAfter(10);
        document.add(headerright);
        //------------------HEADER (Center)----------------
        Font boldFontCenter = new Font(Font.FontFamily.HELVETICA, 10,Font.BOLD | Font.UNDERLINE);
        Paragraph headercenter = new Paragraph("ORDER No:- " + generateOrderNumber() + "\n" 
        ,boldFontCenter);
        headercenter.setAlignment(Element.ALIGN_CENTER);
        headercenter.setSpacingAfter(10);
        document.add(headercenter);
        // ---------------- MAIN PARAGRAPH ----------------
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
        
        Paragraph para = new Paragraph();
        para.add(new Chunk(
                "Under the Powers delegated National Informatics Centre vide Office order No. M-11017/1/2014-" 
                + "MS(O&M) dated 17.07.2014 and 19.01.2016, sanction is hereby accorded under Rule 15(1)(C) read" 
                + "with Rule 16(1) & 16(2) of GPF Rules 1960 to the withdrawal of Rs. " + dto.getRequestedWithdrawlAmount() + "(" + convertToWords(dto.getRequestedWithdrawlAmount()) + ") "
                +"by " + dto.getEmpName() + ", " + dto.getDesignation() + ", Employee Code:" + dto.getEmpCode() + " from his/her GPF A/C No " 
                + dto.getGpfAccNo() + " for the purpose of " + dto.getPurposeOfWithdrawl() + "\n" 
                + "2. " + dto.getEmpName() +" has rendered more than "+ getNumberOfYearsOfService(dto.getDateOfJoining(), dto.getDateOfRetirement())
                + " years service.", normalFont));

                para.add(new Chunk(
                "(" + "Date of Retirement: " + formattedDate + ")" +"\n"  ,boldFont));

                para.add(new Chunk(
                 "3. The anount of withdrawal is Less Than 50% of balance in his/her GPF A/C." + "\n"
                + "\n"
                + "4. The balance at the credit of individual is detailed below: -" + "\n"
                ,normalFont));


                //Sanction is hereby accorded for withdrawal of Rs. "
                  //      + dto.getRequestedWithdrawlAmount()
                    //    + " from GPF Account No. "
                      //  + dto.getGpfAccNo()
                        //+ " for the purpose of "
                        //+ dto.getPurposeOfWithdrawl() + "."
        
        para.setSpacingAfter(15);
        document.add(para);
        // ---------------- DETAILS TABLE ----------------
        double totalOneToThree=dto.getClosingBalance()+dto.getCreditAmount()+dto.getRefundAmount();

        Font smallBoldFont = new Font(Font.FontFamily.HELVETICA,9,Font.BOLD);

       PdfPTable table = new PdfPTable(3);
       table.setWidthPercentage(100);
       float[] columnWidths = {1f, 6f, 3f};
       table.setWidths(columnWidths);

        table.addCell(new Phrase("i)", smallBoldFont));
        table.addCell(new Phrase("Closing balance as per statement for the year " + prevFY));
        table.addCell(dto.getClosingBalance() != null ? String.valueOf(dto.getClosingBalance()) : "N/A");

        table.addCell(new Phrase("ii)", smallBoldFont));
        table.addCell(new Phrase("Credit from" + currentFinYearStartDate + " to " + currentFinYearEndDateTillDecember));
        table.addCell(dto.getCreditAmount() != null ? String.valueOf(dto.getCreditAmount()) : "N/A");

        table.addCell(new Phrase("iii)", smallBoldFont));
        table.addCell(new Phrase("Refund of advance from" + currentFinYearStartDate + " to " + currentFinYearEndDateTillDecember));
        table.addCell(dto.getRefundAmount() != null ? String.valueOf(dto.getRefundAmount()) : "N/A");

        table.addCell(new Phrase("iv)", smallBoldFont));
        table.addCell(new Phrase("Total of Col(i) to (iii)"));
        table.addCell(String.valueOf(totalOneToThree));

        table.addCell(new Phrase("v)", smallBoldFont));
        table.addCell(new Phrase("Subsequent withdrawal"));
        table.addCell(String.valueOf(dto.getSubsequentWithdrawl()));

        table.addCell(new Phrase("vi)", smallBoldFont));
        table.addCell(new Phrase("Balance as on date of Sanction"));
        table.addCell(String.valueOf(totalOneToThree - dto.getSubsequentWithdrawl()));

        table.setSpacingAfter(100);
        document.add(table);

        // ---------------- SIGNATURE ----------------
        Paragraph sign = new Paragraph("Authorized Signatory");
        sign.setSpacingAfter(10);
        sign.setAlignment(Element.ALIGN_RIGHT);
        document.add(sign);


       Paragraph paraFooterParagraph = new Paragraph(
                "1. The Senior Accounts Officer, Pay & Accounts Office, NICHQ, New Delhi-110003" + "\n"
               +"2. DDO, NICHQ, New Delhi-110003" + "\n"
               +"3. Individual Concerned, with the instruction that within one month of the drawal of the amount, he/she should produce certificate to the effect that the withdrawal sanctioned above has been utilised for the purpose for which it was drawn."+"\n"
               +"4. Personal file " + dto.getEmpCode() + "\n"
                , normalFont);

                
        paraFooterParagraph.setSpacingAfter(80);
        document.add(paraFooterParagraph);



        Paragraph signFooter = new Paragraph("Authorized Signatory");
        signFooter.setAlignment(Element.ALIGN_RIGHT);
        document.add(signFooter);

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
private static final String[] units = {
    "", "One", "Two", "Three", "Four", "Five", "Six", "Seven",
    "Eight", "Nine", "Ten", "Eleven", "Twelve", "Thirteen",
    "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"
};

private static final String[] tens = {
    "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
};

public String convertToWords(Double number) {
    if (number == 0) return "Zero";

    return convert(number.longValue()).trim() + " Only";
}

private String convert(long n) {
    if (n < 20) return units[(int) n];

    if (n < 100)
        return tens[(int) (n / 10)] + " " + units[(int) (n % 10)];

    if (n < 1000)
        return units[(int) (n / 100)] + " Hundred " + convert(n % 100);

    if (n < 100000)
        return convert(n / 1000) + " Thousand " + convert(n % 1000);

    if (n < 10000000)
        return convert(n / 100000) + " Lakh " + convert(n % 100000);

    return convert(n / 10000000) + " Crore " + convert(n % 10000000);
}
public String getNumberOfYearsOfService(String dateOfJoining, String dateOfRetirement) {
    LocalDate doj = LocalDate.parse(dateOfJoining);
    LocalDate dor = LocalDate.parse(dateOfRetirement);
    long years = java.time.temporal.ChronoUnit.YEARS.between(doj, dor);
   return String.valueOf(years);

}
public String getPreviousFinancialYear() {
    java.time.LocalDate today = java.time.LocalDate.now();

    int year = today.getYear();
    int month = today.getMonthValue();

    int startYear, endYear;

    if (month < 4) {
        // Jan–Mar → current FY is (year-1)-(year)
        startYear = year - 2;
        endYear = year - 1;
    } else {
        // Apr–Dec → current FY is (year)-(year+1)
        startYear = year - 1;
        endYear = year;
    }

    return startYear + "-" + endYear;
}
public LocalDate getCurrentFYStartDate() {
    LocalDate today = LocalDate.now();

    int year = today.getYear();
    int month = today.getMonthValue();

    if (month < 4) {
        // Jan–Mar → FY started last year
        return LocalDate.of(year - 1, 4, 1);
    } else {
        // Apr–Dec → FY started this year
        return LocalDate.of(year, 4, 1);
    }
}

public LocalDate getCurrentFYEndTillDecember() {
    LocalDate today = LocalDate.now();

    int year = today.getYear();
    int month = today.getMonthValue();

    if (month < 4) {
        // Jan–Mar → December belongs to previous calendar year
        return LocalDate.of(year - 1, 12, 1);
    } else {
        // Apr–Dec → December of same year
        return LocalDate.of(year, 12, 1);
    }
}
}