package com.inn.cafe.serviceImpl;

import com.inn.cafe.JWT.CustomerUserDetailsService;
import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.POJO.Bill;
import com.inn.cafe.constents.AgriStockConstants;
import com.inn.cafe.dao.BillDao;
import com.inn.cafe.service.BillService;
import com.inn.cafe.utils.AgriStockUtils;
import com.inn.cafe.utils.EmailUtil;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Service
public class BillServiceImpl implements BillService {
    @Autowired
    BillDao billDao;

    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    com.inn.cafe.JWT.jwtUtil jwtUtil;

    @Autowired
    JwtFilter jwtFilter;
    @Autowired
    CustomerUserDetailsService customerUserDetailsService;

    @Autowired
    EmailUtil emailUtil;

    @Override
    public ResponseEntity<String> generateReport(Map<String, Object> requestMap) {
        log.info("Insert generateReport");
        try {
            String filename;
            if (validateResquestMap(requestMap)) {
                if (requestMap.containsKey("isGenerate") && !(Boolean) requestMap.get("isGenerate")) {
                    filename = (String) requestMap.get("uuid");
                } else {
                    filename = AgriStockUtils.getUUID();
                    requestMap.put("uuid", filename);
                    insertBill(requestMap);
                }
                // print user data (name , email m contactNumber , ...)
                String data = "Name: " + requestMap.get("name") + "\n" + "Contact Number: " + requestMap.get("contactNumber") +
                        "\n" + "Email: " + requestMap.get("email") + "\n" + "Payment Method: " + requestMap.get("paymentMethod");
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(AgriStockConstants.STORE_LOCATION + "\\" + filename + ".pdf"));
                document.open();
                setRectaangleInPdf(document);

                // print pdf Header
                Paragraph chunk = new Paragraph("Laksh Krushi Seva Kendra Talsande.", getFont("Header"));
                chunk.setAlignment(Element.ALIGN_CENTER);
                document.add(chunk);


                Paragraph paragraph = new Paragraph(data + "\n \n", getFont("Data"));
                document.add(paragraph);

                // Create table in pdf to print data
                PdfPTable table = new PdfPTable(5);
                table.setWidthPercentage(100);
                addTableHeader(table);


                // Print table data
                JSONArray jsonArray = AgriStockUtils.getJsonArrayFromString((String) requestMap.get("productDetails"));
                for (int i = 0; i < jsonArray.length(); i++) {
                    addRows(table, AgriStockUtils.getMapFromJson(jsonArray.getString(i)));
                }

                document.add(table);

                Paragraph spacer = new Paragraph("\n"); // Add two line breaks
                document.add(spacer);

                // print pdf Footer with increased font size for Total Amount
                Font totalAmountFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK); // Define font with larger size
                Paragraph footer = new Paragraph("Total Amount : " + requestMap.get("totalAmount"), totalAmountFont);
                document.add(footer);

// Add a thank-you message with default font size
                Paragraph thankYou = new Paragraph("\nThank you for visiting Laksh Krushi Seva Kendra Talsande.", getFont("Data"));
                document.add(thankYou);

                Paragraph spacer1 = new Paragraph("\n\n\n\n"); // Add two line breaks
                document.add(spacer1);

                // Add developer credit at the end
                Paragraph developerCredit = new Paragraph("Developed by Pravin Softwares - 7028040641", getFont("Data"));
                developerCredit.setAlignment(Element.ALIGN_CENTER); // Align text to the center
                document.add(developerCredit);

                // WhatsApp Sending (New Code)
                String recipientNumber = requestMap.get("contactNumber").toString(); // WhatsApp number
                String message = "Thank you for your order. Here is your bill.";
                String pdfFilePath = AgriStockConstants.STORE_LOCATION + "\\" + filename + ".pdf";
                sendPdfToWhatsApp(pdfFilePath, recipientNumber, message);

                document.close();
                return new ResponseEntity<>("{\"uuid\":\"" + filename + "\"}", HttpStatus.OK);
            }
            return AgriStockUtils.getResponeEntity("Required data not found", HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return AgriStockUtils.getResponeEntity(AgriStockConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Bill>> getBills() {
        List<Bill> list = new ArrayList<>();
        if (jwtFilter.isAdmin()) {
            list = billDao.getAllBills();
        } else {
            list = billDao.getBillByUserName(jwtFilter.getCurrentUsername());
        }
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<byte[]> getPdf(Map<String, Object> requestMap) {
        log.info("Inside getPdf : requestMap {}", requestMap);
        try {
            byte[] byteArray = new byte[0];
            if (!requestMap.containsKey("uuid") && validateResquestMap(requestMap)) {
                return new ResponseEntity<>(byteArray, HttpStatus.BAD_REQUEST);
            }
            String filepath = AgriStockConstants.STORE_LOCATION + "\\" + (String) requestMap.get("uuid") + ".pdf";

            if (AgriStockUtils.isFileExist(filepath)) {
                byteArray = getByteArray(filepath);
                return new ResponseEntity<>(byteArray, HttpStatus.OK);
            } else {
                requestMap.put("isGenerate", false);
                generateReport(requestMap);
                byteArray = getByteArray(filepath);
                return new ResponseEntity<>(byteArray, HttpStatus.OK);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public ResponseEntity<String> delete(Integer id) {
        try {
            if (jwtFilter.isAdmin()) {
                Optional optional = billDao.findById(id);
                if (!optional.isEmpty()) {
                    billDao.deleteById(id);
                    //System.out.println("Product is deleted successfully");
                    return AgriStockUtils.getResponeEntity("Bill is deleted successfully", HttpStatus.OK);
                }
                //System.out.println("Product id doesn't exist");
                return AgriStockUtils.getResponeEntity("Bill id doesn't exist", HttpStatus.OK);
            } else {
                return AgriStockUtils.getResponeEntity(AgriStockConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return AgriStockUtils.getResponeEntity(AgriStockConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void insertBill(Map<String, Object> requestMap) {
        try {
            Bill bill = new Bill();
            bill.setUuid((String) requestMap.get("uuid"));
            bill.setName((String) requestMap.get("name"));
            bill.setEmail((String) requestMap.get("email"));
            bill.setContactNumber((String) requestMap.get("contactNumber"));
            bill.setPaymentMethod((String) requestMap.get("paymentMethod"));
            bill.setTotal(Integer.parseInt((String) requestMap.get("totalAmount")));
            bill.setProductDetails((String) requestMap.get("productDetails"));
            bill.setCreatedBy(jwtFilter.getCurrentUsername());
            billDao.save(bill);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean validateResquestMap(Map<String, Object> requestMap) {
        return requestMap.containsKey("name") &&
                requestMap.containsKey("contactNumber") &&
                requestMap.containsKey("email") &&
                requestMap.containsKey("paymentMethod") &&
                requestMap.containsKey("productDetails") &&
                requestMap.containsKey("totalAmount");
    }

    private void setRectaangleInPdf(Document document) throws DocumentException {
        log.info("Inside setRectaangleInPdf.");
        Rectangle rectangle = new Rectangle(577, 825, 18, 15);
        rectangle.enableBorderSide(1);
        rectangle.enableBorderSide(2);
        rectangle.enableBorderSide(4);
        rectangle.enableBorderSide(8);
        rectangle.setBorderColor(BaseColor.BLACK);
        rectangle.setBorderWidth(1);
        document.add(rectangle);
    }

    private Font getFont(String type) {
        log.info("Inside getFont");
        switch (type) {
            case "Header":
                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE, 18, BaseColor.BLACK);
                headerFont.setStyle(Font.BOLD);
                return headerFont;
            case "Data":
                Font dareFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 11, BaseColor.BLACK);
                dareFont.setStyle(Font.BOLD);
                return dareFont;
            default:
                return new Font();
        }
    }

    private void addTableHeader(PdfPTable table) {
        log.info("Inside addTableHeader");
        Stream.of("Product Name", "Company", "Quantity", "Price", "Sub Total")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(2);
                    header.setPhrase(new Phrase(columnTitle));
                    header.setBackgroundColor(BaseColor.YELLOW);
                    header.setHorizontalAlignment(Element.ALIGN_CENTER);
                    header.setVerticalAlignment(Element.ALIGN_CENTER);
                    table.addCell(header);
                });
    }

    private void addRows(PdfPTable table, Map<String, Object> data) {
        log.info("Inside addRows");
        table.addCell((String) data.get("name"));
        table.addCell((String) data.get("category"));
        table.addCell((String) data.get("quantity"));
        table.addCell(Double.toString((Double) data.get("price")));
        table.addCell(Double.toString((Double) data.get("total")));
    }

    private byte[] getByteArray(String filepath) throws Exception {
        File initalFile = new File(filepath);
        InputStream targetStream = new FileInputStream(initalFile);
        byte[] byteArray = IOUtils.toByteArray(targetStream);
        targetStream.close();
        return byteArray;
    }


    // Helper Method to Send PDF to WhatsApp
    private void sendPdfToWhatsApp(String filePath, String recipientNumber, String message) {
        try {
            String apiUrl = "https://graph.facebook.com/v16.0/7028040641/messages"; // Replace with actual phone-number-id
            String accessToken = "YOUR_ACCESS_TOKEN"; // Replace with your WhatsApp API Access Token

            // Build JSON payload
            JSONObject payload = new JSONObject();
            payload.put("messaging_product", "whatsapp");
            payload.put("to", recipientNumber);
            payload.put("type", "document");

            JSONObject document = new JSONObject();
            document.put("\"C:\\\\Users\\\\PravinChavan\\\\Desktop\\\\AgriStock\\\\Backend\\\\AgriStockStoredFiles\";", uploadPdfToStorage(filePath)); // Upload file to server or cloud storage to generate public URL
            document.put("caption", message);
            payload.put("document", document);

            // Send POST request
            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            OutputStream os = connection.getOutputStream();
            os.write(payload.toString().getBytes());
            os.flush();
            os.close();

            // Handle Response
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                log.info("PDF sent successfully via WhatsApp!");
            } else {
                log.error("Failed to send PDF via WhatsApp. Response Code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error sending PDF via WhatsApp: " + e.getMessage());
        }
    }


    private String uploadPdfToStorage(String filePath) throws Exception {
        // Placeholder for uploading the file to a cloud storage (e.g., AWS S3, Google Cloud Storage)
        log.info("Uploading PDF to cloud storage...");
        // For now, return a dummy URL for testing purposes
        return "C:\\Users\\PravinChavan\\Desktop\\AgriStock\\Backend\\AgriStockStoredFiles";
    }
}

