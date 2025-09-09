package com.example.application.service;

import com.example.application.entity.MemeConfiguration;
import com.example.application.repository.MemeConfigurationRepository;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;

/**
 * Service for generating debt collection posters as PDF
 */
@Service
public class PosterGeneratorService {

    @Autowired
    private MemeConfigurationRepository memeConfigurationRepository;

    private final Random random = new Random();

    /**
     * Generate a debt collection poster PDF
     */
    public byte[] generatePoster(String buildingNumber, String entranceNumber,
                                String floorNumber, String doorNumber,
                                String rankCategory, BigDecimal debtAmount) {

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // Create PDF document
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Create font that supports Unicode/Cyrillic/Mongolian
            PdfFont font;
            PdfFont boldFont;

            try {
                // Try to use system fonts that support Mongolian
                font = PdfFontFactory.createFont("c:/windows/fonts/arial.ttf", PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                boldFont = PdfFontFactory.createFont("c:/windows/fonts/arialbd.ttf", PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            } catch (Exception e) {
                try {
                    // Fallback to standard fonts with UTF-8 encoding
                    font = PdfFontFactory.createFont(StandardFonts.HELVETICA, PdfEncodings.UTF8);
                    boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD, PdfEncodings.UTF8);
                } catch (Exception e2) {
                    // Final fallback to standard fonts
                    font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
                    boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
                }
            }

            // Format debt amount
            String formattedAmount = String.format("%,.2f", debtAmount);

            // Get random meme content
            MemeConfiguration memeImage = memeConfigurationRepository.findRandomMemeImage();
            MemeConfiguration memeText = memeConfigurationRepository.findRandomMemeText();

            String imageUrl = memeImage != null ? memeImage.getContent() : "";
            String funnyText = memeText != null ? memeText.getContent() : "Өрөө төлөөрэй! 😄";

            // Debug logging
            System.out.println("DEBUG: Meme image found: " + (memeImage != null ? memeImage.getContent() : "null"));
            System.out.println("DEBUG: Meme text found: " + (memeText != null ? memeText.getContent() : "null"));
            System.out.println("DEBUG: Final funny text: " + funnyText);

            // Header section with apartment info
            Paragraph header = new Paragraph()
                .add(new Text(buildingNumber + "-" + entranceNumber + "-" + floorNumber + "-" + doorNumber + " тоот")
                    .setFont(boldFont).setFontSize(18))
                .setTextAlignment(TextAlignment.CENTER)
                .setBackgroundColor(new DeviceRgb(102, 102, 255))
                .setFontColor(ColorConstants.WHITE)
                .setPadding(10);
            document.add(header);

            // Rank category
            Paragraph rankParagraph = new Paragraph()
                .add(new Text(rankCategory).setFont(boldFont).setFontSize(14))
                .setTextAlignment(TextAlignment.CENTER)
                .setBackgroundColor(new DeviceRgb(255, 102, 102))
                .setFontColor(ColorConstants.WHITE)
                .setPadding(8);
            document.add(rankParagraph);

            // Main message
            Paragraph mainMessage = new Paragraph()
                .add(new Text("Шимэгчлэхээ болиод, " + formattedAmount + " төгрөгний өрөө төлнө үү")
                    .setFont(font).setFontSize(16))
                .setTextAlignment(TextAlignment.CENTER)
                .setBackgroundColor(new DeviceRgb(255, 255, 204))
                .setPadding(15)
                .setMarginTop(10);
            document.add(mainMessage);

            // Debt amount (large)
            Paragraph debtAmountParagraph = new Paragraph()
                .add(new Text(formattedAmount).setFont(boldFont).setFontSize(24))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(new DeviceRgb(255, 0, 0))
                .setMarginTop(20);
            document.add(debtAmountParagraph);

            // Meme section with image and text
            if (!funnyText.isEmpty() && !funnyText.equals("Өрөө төлөөрэй! 😄")) {
                System.out.println("DEBUG: Adding meme section with text: " + funnyText);

                // Add meme image if available
                if (!imageUrl.isEmpty()) {
                    try {
                        System.out.println("DEBUG: Adding meme image from URL: " + imageUrl);
                        addMemeImage(document, imageUrl, font);
                    } catch (Exception e) {
                        System.out.println("DEBUG: Failed to add meme image: " + e.getMessage());
                        // Add placeholder if image fails
                        Paragraph imagePlaceholder = new Paragraph()
                            .add(new Text("[Meme зураг татагдсангүй]").setFont(font).setFontSize(14))
                            .setTextAlignment(TextAlignment.CENTER)
                            .setMarginTop(20)
                            .setPadding(10)
                            .setBackgroundColor(new DeviceRgb(240, 240, 240));
                        document.add(imagePlaceholder);
                    }
                } else {
                    // Add placeholder if no image URL
                    Paragraph imagePlaceholder = new Paragraph()
                        .add(new Text("[Meme зураг байхгүй]").setFont(font).setFontSize(14))
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginTop(20)
                        .setPadding(10)
                        .setBackgroundColor(new DeviceRgb(240, 240, 240));
                    document.add(imagePlaceholder);
                }

                // Meme text with emoji support
                String processedText = processEmojis(funnyText);
                System.out.println("DEBUG: Processed meme text: " + processedText);

                Paragraph memeTextParagraph = new Paragraph()
                    .add(new Text(processedText).setFont(font).setFontSize(16))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(10)
                    .setPadding(15)
                    .setBackgroundColor(new DeviceRgb(248, 248, 248));
                document.add(memeTextParagraph);
            } else {
                System.out.println("DEBUG: No meme text to add or using default text");
            }

            // Footer
            Paragraph footer = new Paragraph()
                .add(new Text("Энэхүү мэдэгдэл нь автоматаар үүсгэгдсэн болно.\nАсуудал байвал удирдлагатай холбогдоно уу.")
                    .setFont(font).setFontSize(10))
                .setTextAlignment(TextAlignment.CENTER)
                .setBackgroundColor(new DeviceRgb(70, 130, 180))
                .setFontColor(ColorConstants.WHITE)
                .setPadding(10)
                .setMarginTop(30);
            document.add(footer);

            // Close document
            document.close();

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate poster PDF", e);
        }
    }





    private String formatCurrency(BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("mn", "MN"));
        return formatter.format(amount);
    }

    private String processEmojis(String text) {
        if (text == null) return "";

        System.out.println("DEBUG: Processing emojis in text: " + text);

        // Replace common emojis with text representations for better PDF compatibility
        String processed = text
            .replace("😄", " [инээмсэглэл] ")
            .replace("😂", " [инээд] ")
            .replace("🤣", " [инээд] ")
            .replace("😭", " [уйлах] ")
            .replace("😱", " [айх] ")
            .replace("💰", " [мөнгө] ")
            .replace("💸", " [мөнгө алдах] ")
            .replace("🏠", " [байшин] ")
            .replace("🔥", " [гал] ")
            .replace("⚡", " [цахилгаан] ")
            .replace("⏰", " [цаг] ")
            .replace("📈", " [өсөлт] ")
            .replace("📉", " [бууралт] ")
            .replace("❗", "!")
            .replace("❓", "?")
            .replace("👍", " [сайн] ")
            .replace("👎", " [муу] ")
            .replace("🤔", " [бодох] ")
            .replace("😅", " [ичих] ")
            .replace("😏", " [муу санаа] ")
            .replace("😌", " [тайван] ")
            .replace("💪", " [хүчтэй] ")
            .replace("💙", " [сэтгэл] ");

        System.out.println("DEBUG: Processed text: " + processed);
        return processed;
    }

    private void addMemeImage(Document document, String imageUrl, PdfFont font) throws Exception {
        try {
            // Download image from URL
            java.net.URL url = new java.net.URL(imageUrl);
            java.io.InputStream inputStream = url.openStream();
            byte[] imageBytes = inputStream.readAllBytes();
            inputStream.close();

            // Create image data
            ImageData imageData = ImageDataFactory.create(imageBytes);
            Image image = new Image(imageData);

            // Set image properties
            image.setWidth(300); // Max width 300px
            image.setHeight(200); // Max height 200px
            image.setAutoScale(true); // Maintain aspect ratio
            image.setHorizontalAlignment(HorizontalAlignment.CENTER);
            image.setMarginTop(20);
            image.setMarginBottom(10);

            // Add image to document
            document.add(image);

            System.out.println("DEBUG: Meme image added successfully");

        } catch (Exception e) {
            System.out.println("DEBUG: Error adding meme image: " + e.getMessage());
            throw e;
        }
    }
}
