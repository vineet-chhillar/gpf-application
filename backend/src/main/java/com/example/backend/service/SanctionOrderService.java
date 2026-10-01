package com.example.backend.service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

//import javax.swing.text.Document;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.example.backend.dto.GenerateOrderRequest;
import com.example.backend.dto.GenerateOrderResponse;
import com.example.backend.dto.SanctionOrderDto;
import com.example.backend.entity.GpfAdvanceDetails;
import com.example.backend.entity.GpfAdvanceMaster;
import com.example.backend.entity.GpfSanctionOrder;
import com.example.backend.entity.GpfWithdrawlDetails;
import com.example.backend.entity.GpfWithdrawlMaster;
import com.example.backend.repository.GpfWithdrawlDetailsRepository;
import com.example.backend.repository.GpfWithdrawlMasterRepository;
import com.example.backend.repository.GpfAdvanceDetailsRepo;
import com.example.backend.repository.GpfAdvanceMasterRepo;
import com.example.backend.repository.GpfSanctionOrderRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import jakarta.transaction.Transactional;


@Service
@Transactional
public class SanctionOrderService {
    private static final Long COMPLETED_ROLE_ID = 0L; // replace with actual
    
    private final GpfWithdrawlMasterRepository gpfWithdrawlMasterRepository;

    private final GpfWithdrawlDetailsRepository gpfWithdrawlDetailsRepository;

    private final GpfAdvanceMasterRepo advanceMasterRepo;

    private final GpfAdvanceDetailsRepo advanceDetailsRepo;
     
    @Autowired
    private final GpfSanctionOrderRepository sanctionOrderRepository;

   @Autowired
    private JdbcTemplate jdbcTemplate;

  SanctionOrderService(GpfWithdrawlMasterRepository gpfWithdrawlMasterRepository, GpfWithdrawlDetailsRepository gpfWithdrawlDetailsRepository, GpfAdvanceMasterRepo advanceMasterRepo, GpfAdvanceDetailsRepo advanceDetailsRepo, GpfSanctionOrderRepository sanctionOrderRepository) {
    this.gpfWithdrawlMasterRepository = gpfWithdrawlMasterRepository;
    this.gpfWithdrawlDetailsRepository = gpfWithdrawlDetailsRepository;
    this.sanctionOrderRepository = sanctionOrderRepository;
    this.advanceMasterRepo = advanceMasterRepo;
    this.advanceDetailsRepo = advanceDetailsRepo;
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
    String type = request.getType(); // 🔥 NEW
    try {
        Optional<GpfSanctionOrder> existing =  sanctionOrderRepository.findByApplicationId(applicationId);
                
        if (existing.isPresent()) {
//System.out.println("application id is:" + existing.isPresent());
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

    /* ===============================
       🔥 BUILD DTO BASED ON TYPE
    =============================== */

    SanctionOrderDto dto;
    String orderNumber;

    if ("withdrawl".equalsIgnoreCase(type)) {

        dto = buildSanctionOrder(applicationId);
        orderNumber = generateOrderNumber("WDL");

    } else if ("advance".equalsIgnoreCase(type)) {

        dto = buildAdvanceSanction(applicationId);
        
        orderNumber = generateOrderNumber("ADV");

    } else {
        throw new RuntimeException("Invalid type");
    }

    /* ===============================
       GENERATE ORDER
    =============================== */

    


    String orderText;

//if ("withdrawl".equalsIgnoreCase(type)) {
  //  orderText = generateSanctionOrderText(dto);
//} else {
 // orderText = generateSanctionOrderText(dto);
//}
String baseText = generateSanctionOrderText(dto);

String heading = "withdrawl".equalsIgnoreCase(type)
        ? "Sanction Order (Withdrawal) for Application ID: "
        : "Sanction Order (Advance) for Application ID: ";

orderText = heading + applicationId + "\n\n" + baseText;

   byte[] pdfBytes;

try {

    if ("withdrawl".equalsIgnoreCase(type)) {

        pdfBytes = generatePdf(dto);

    } else {

        pdfBytes = generateAdvancePdf(dto);
        System.out.println("Moved to advance sanction");
    }

} catch (Exception e) {

    e.printStackTrace();    
    throw new RuntimeException("PDF generation failed: " + e.getMessage());
}
String orderType = "withdrawl".equalsIgnoreCase(type) ? "WDL" : "ADV";

try
{
    GpfSanctionOrder entity = new GpfSanctionOrder();
    entity.setApplicationId(applicationId);
    entity.setOrderNumber(orderNumber);
    entity.setOrderText(orderText);
    entity.setOrderPdf(pdfBytes);
    entity.setGeneratedOn(LocalDateTime.now());
    entity.setType(orderType);
       

    sanctionOrderRepository.insertSanctionOrder(
            applicationId,
            "System", // generatedBy
            entity.getGeneratedOn(),
            entity.getOrderNumber(),
            entity.getOrderPdf(),
            entity.getOrderText(),
            orderType
    );
}
catch (Exception e) {
e.printStackTrace();    
    throw new RuntimeException("Failed While inserting sanction order: " + e.getMessage());
}
    Optional<GpfSanctionOrder> saved =
            sanctionOrderRepository.findByApplicationId(applicationId);

    GenerateOrderResponse response = new GenerateOrderResponse();

    response.setOrderId(saved.map(GpfSanctionOrder::getId).orElse(null));
    response.setOrderNumber(orderNumber);
    response.setStatus("SUCCESS");
    response.setMessage("Sanction order generated");

    return response;
}

private SanctionOrderDto buildAdvanceSanction(Long applicationId) {

    // 1. Fetch master
    GpfAdvanceMaster master = advanceMasterRepo.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Advance Master not found"));

    // 2. Fetch details
    GpfAdvanceDetails details = advanceDetailsRepo.findByMaster_Id(applicationId)
            .orElseThrow(() -> new RuntimeException("Advance Details not found"));

    // 3. VALIDATION (same rule as withdrawal)
    if (!COMPLETED_ROLE_ID.equals(details.getCurrentOwnerRole())) {
        throw new RuntimeException("Order can only be generated for completed applications");
    }

    // 4. Map DTO
    SanctionOrderDto dto = new SanctionOrderDto();

    dto.setRequestedWithdrawlAmount(
        details.getAmountofadvancerequested() != null
            ? details.getAmountofadvancerequested().doubleValue()
            : null
    );

    dto.setDesignation(master.getDesignation());
    dto.setEmpName(master.getEmpname());
    dto.setEmpCode(master.getEmpcode());

    dto.setGpfAccNo(details.getGpfaccountno());

    // ⚠️ reuse same DTO field (no need new field)
    dto.setPurposeOfWithdrawl(details.getPurposeofadvance());

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

    
    dto.setClosingBalance(
        details.getOutstandingbalance() != null
            ? details.getOutstandingbalance().doubleValue()
            : null
    );

    dto.setCreditAmount(
        details.getTotalcreditamount() != null
            ? details.getTotalcreditamount().doubleValue()
            : null
    );

    dto.setRefundAmount(
        details.getRefundafterdateofoutstandingbalance() != null
            ? details.getRefundafterdateofoutstandingbalance().doubleValue()
            : null
    );

   Double requestedAmount = details.getAmountofadvancerequested() != null
        ? details.getAmountofadvancerequested().doubleValue()
        : null;

Double installments = details.getNoofmonthlyinstallmentsforpaymentofconsolidatedadvance() != null
        ? details.getNoofmonthlyinstallmentsforpaymentofconsolidatedadvance().doubleValue()
        : null;

dto.setRequestedAdvanceAmount(requestedAmount);
dto.setNoOfInstallments(installments);

dto.setgetPurposeOfAdvance(details.getPurposeofadvance());


Double installmentAmount = null;

if (requestedAmount != null && installments != null && installments > 0) {
    installmentAmount = requestedAmount / installments;


    installmentAmount = Math.round(installmentAmount * 100.0) / 100.0;
}

dto.setInstallmentAmount(installmentAmount);


     
    
    // Advance may not have this — safe fallback
    //dto.setSubsequentWithdrawl(
      //  details.get.getPriorwithdrawlamount() != null
        //    ? details.getPriorwithdrawlamount().doubleValue()
         //   : null
    //);

    return dto;
}

private String generateOrderNumber(String type) {

    int year = LocalDate.now().getYear();

    String prefix = "withdrawl".equalsIgnoreCase(type) ? "WDL" : "ADV";

    long seq = getNextSequence(type);

    return String.format("GPF/%s/%d/%05d", prefix, year, seq);
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
public byte[] generateAdvancePdf(SanctionOrderDto dto) 
{
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        Document document = new Document();
        PdfWriter.getInstance(document, out);
        document.open();
        
        Set<String> scientistGroup = Set.of("Scientist-D", "Scientist-E", "Scientist-F", "Scientist-G");
        String adminCode = scientistGroup.contains(
        dto.getDesignation().toUpperCase()
        ) ? "ADMN.I" : "ADMN.II";

        String adminCodeHindi = scientistGroup.contains(
        dto.getDesignation().toUpperCase()
        ) ? "I" : "II";


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

        //BaseFont baseFont = BaseFont.createFont("fonts/NotoSansDevanagari-Regular.ttf",BaseFont.IDENTITY_H,BaseFont.EMBEDDED);
        //Font hindiFont = new Font(baseFont, 10, Font.BOLD);

        // ---------------- HEADER (CENTER) ----------------
        // Hindi font
BaseFont hindiBase = BaseFont.createFont(
        "fonts/NotoSerifDevanagari-Regular.ttf",
        BaseFont.IDENTITY_H,
        BaseFont.EMBEDDED
);
Font hindiBold = new Font(hindiBase, 10, Font.BOLD);

// English font
Font engBold = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);

// Create paragraph
Paragraph header = new Paragraph();
header.setAlignment(Element.ALIGN_CENTER);
header.setSpacingAfter(10);

// ---- Line 1 ----
header.add(new Chunk("संख्या. " + dto.getEmpCode() + " रा. सू.वि. कें./जीपीएफ/", hindiBold));
header.add(new Chunk(java.time.LocalDate.now().getYear() + "-प्रशासन-",hindiBold));
header.add(new Chunk(adminCodeHindi + "/",engBold));

header.add(new Chunk(
        "No. " + dto.getEmpCode() + "/NIC/GPF/" +
        java.time.LocalDate.now().getYear() + "-" + adminCode + "\n",
        engBold
));

// ---- Line 2 ----
header.add(new Chunk("भारत सरकार / ", hindiBold));
header.add(new Chunk("Government of India\n", engBold));

// ---- Line 3 ----
header.add(new Chunk("इलेक्ट्रॉनिक्स और सूचना प्रौद्योगिकी मंत्रालय / ", hindiBold));
header.add(new Chunk("Ministry of Electronics and Information Technology\n", engBold));

// ---- Line 4 ----
header.add(new Chunk("राष्ट्रीय सूचना-विज्ञान केंद्र / ", hindiBold));
header.add(new Chunk("National Informatics Centre\n", engBold));

// ---- Line 5 ----
header.add(new Chunk("प्रशासन विभाग-" , hindiBold));
header.add(new Chunk(adminCodeHindi + " / ", engBold));
header.add(new Chunk("[" + adminCodeNew + "]", engBold));

// Add to document
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
        //Font boldFontCenter = new Font(Font.FontFamily.HELVETICA, 10,Font.BOLD | Font.UNDERLINE);
        // Hindi font

// Create paragraph
Paragraph headercenter = new Paragraph();
headercenter.setAlignment(Element.ALIGN_CENTER);
headercenter.setSpacingAfter(10);

// Add mixed content using chunks
headercenter.add(new Chunk("आदेश संख्या /", hindiBold));
headercenter.add(new Chunk("ORDER No: ", engBold));
headercenter.add(new Chunk(generateOrderNumber("ADV"), engBold));

// Line break (cleaner than \n)
headercenter.add(Chunk.NEWLINE);

// Add to document
document.add(headercenter);
        // ---------------- MAIN PARAGRAPH ----------------
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
        
        Paragraph para = new Paragraph();
        para.add(new Chunk(
                "Under the Powers delegated in NIC vide NIC-HQ Office order No. 1(6)/2014-Pers dated 19.01.2016, I am directed "
                + "to convey the sanction of the competent authority under rules 12(1)(f) read with 12(2) of GPF rules to the grant of an advance of "
                ,normalFont)); 
                
       para.add(new Chunk("Rs." + dto.getRequestedAdvanceAmount() + "/-" + "(" + convertToWords(dto.getRequestedAdvanceAmount()) + ") "  
       +"to " + dto.getEmpName() + ", " + dto.getDesignation() + " (Employee Code " + dto.getEmpCode() + ") " 
       +"from his GPF Account No. " + dto.getGpfAccNo() + " to enable him to defray the expenses to be incurred by him in connection with "
       +dto.getPurposeOfAdvance()
       ,boldFontRight));
       para.add(Chunk.NEWLINE);

       para.add(new Chunk(
        "2.The sum of Rs. "
        ,normalFont
       ));
       para.add(new Chunk(
    dto.getRequestedAdvanceAmount() + "/-" + "(" + convertToWords(dto.getRequestedAdvanceAmount()) + ") "
    ,boldFontRight));

    para.add(new Chunk(
    "will be recovered in "
    ,normalFont
     ));
     para.add(new Chunk(
     dto.getNoOfInstallments() + "(" + convertToWords(dto.getNoOfInstallments()) + ") "
     ,boldFontRight
     ));

     para.add(new Chunk(
    "monthly instalments of "
     ,normalFont
    ));
 
     para.add(new Chunk(
       "Rs. " + dto.getInstallmentAmount() + "/-(" + convertToWords(dto.getInstallmentAmount()) + ") "
        ,boldFontRight
     ));
   
     para.add(new Chunk(
    "each commencing from the salary for the month of "
     ,normalFont
    ));

LocalDate nextMonth = LocalDate.now().plusMonths(1);
DateTimeFormatter formatterAdvance =
        DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);
String monthYear = nextMonth.format(formatterAdvance);
       
para.add(new Chunk(
    monthYear
     ,boldFontRight
    ));
       para.add(Chunk.NEWLINE);

       para.add(new Chunk(
        "3. The details of balance at the credit of "
        ,normalFont
       ));

       para.add(new Chunk(
dto.getEmpName()
    ,boldFontRight
    ));

    para.add(new Chunk(
" as on date are given below:-"
    ,normalFont
));

                
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

        String adminCodeHindi = scientistGroup.contains(
        dto.getDesignation().toUpperCase()
        ) ? "I" : "II";


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

        //BaseFont baseFont = BaseFont.createFont("fonts/NotoSansDevanagari-Regular.ttf",BaseFont.IDENTITY_H,BaseFont.EMBEDDED);
        //Font hindiFont = new Font(baseFont, 10, Font.BOLD);

        // ---------------- HEADER (CENTER) ----------------
        // Hindi font
BaseFont hindiBase = BaseFont.createFont(
        "fonts/NotoSerifDevanagari-Regular.ttf",
        BaseFont.IDENTITY_H,
        BaseFont.EMBEDDED
);
Font hindiBold = new Font(hindiBase, 10, Font.BOLD);

// English font
Font engBold = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);

// Create paragraph
Paragraph header = new Paragraph();
header.setAlignment(Element.ALIGN_CENTER);
header.setSpacingAfter(10);

// ---- Line 1 ----
header.add(new Chunk("संख्या. " + dto.getEmpCode() + " रा. सू.वि. कें./जीपीएफ/", hindiBold));
header.add(new Chunk(java.time.LocalDate.now().getYear() + "-प्रशासन-",hindiBold));
header.add(new Chunk(adminCodeHindi + "/",engBold));

header.add(new Chunk(
        "No. " + dto.getEmpCode() + "/NIC/GPF/" +
        java.time.LocalDate.now().getYear() + "-" + adminCode + "\n",
        engBold
));

// ---- Line 2 ----
header.add(new Chunk("भारत सरकार / ", hindiBold));
header.add(new Chunk("Government of India\n", engBold));

// ---- Line 3 ----
header.add(new Chunk("इलेक्ट्रॉनिक्स और सूचना प्रौद्योगिकी मंत्रालय / ", hindiBold));
header.add(new Chunk("Ministry of Electronics and Information Technology\n", engBold));

// ---- Line 4 ----
header.add(new Chunk("राष्ट्रीय सूचना-विज्ञान केंद्र / ", hindiBold));
header.add(new Chunk("National Informatics Centre\n", engBold));

// ---- Line 5 ----
header.add(new Chunk("प्रशासन विभाग-" , hindiBold));
header.add(new Chunk(adminCodeHindi + " / ", engBold));
header.add(new Chunk("[" + adminCodeNew + "]", engBold));

// Add to document
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
        //Font boldFontCenter = new Font(Font.FontFamily.HELVETICA, 10,Font.BOLD | Font.UNDERLINE);
        // Hindi font

// Create paragraph
Paragraph headercenter = new Paragraph();
headercenter.setAlignment(Element.ALIGN_CENTER);
headercenter.setSpacingAfter(10);

// Add mixed content using chunks
headercenter.add(new Chunk("आदेश संख्या /", hindiBold));
headercenter.add(new Chunk("ORDER No: ", engBold));
headercenter.add(new Chunk(generateOrderNumber("WDL"), engBold));

// Line break (cleaner than \n)
headercenter.add(Chunk.NEWLINE);

// Add to document
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
                "(" + "Date of Retirement: " + formattedDate + ")" +"\n"  ,boldFontRight));

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
private long getNextSequence(String type) {
    if ("withdrawl".equalsIgnoreCase(type)) {
        return jdbcTemplate.queryForObject("SELECT nextval('wdl_seq')", Long.class);
    } else {
        return jdbcTemplate.queryForObject("SELECT nextval('adv_seq')", Long.class);
    }
}
}