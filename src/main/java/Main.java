import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.TextAlignment;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println("MAIN MAIN");
        // Path to the PDF file
        String dest = "table_example.pdf";

        // Creating a PdfWriter
        PdfWriter writer = new PdfWriter(dest);

        // Creating a PdfDocument
        PdfDocument pdf = new PdfDocument(writer);

        // Creating a Document
        Document document = new Document(pdf);

        // List of column names
        List<String> columnNames = Arrays.asList("Column 1f", "Column 2f", "Column 3", "Column 4");
        int numColumns = columnNames.size();

        // Define column widths (each column width set to 25)
        float[] columnWidths = new float[numColumns];
        Arrays.fill(columnWidths, 25);

        // Define column widths (you can adjust the widths as per your requirements)
//        float[] columnWidths = {25F, 25F, 25F, 25F};

        // Creating a table
//        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        Table table = new Table(columnWidths);
        table.setWidth(UnitValue.createPercentValue(100));

        // Setting font and font size (example)
        com.itextpdf.kernel.font.PdfFont font = com.itextpdf.kernel.font.PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);

        // Adding header cells to the table
        for (String columnName : columnNames) {
            Paragraph paragraphCell = new Paragraph(columnName);
            paragraphCell.setFont(font);
            paragraphCell.setFontSize(10);
            paragraphCell.setTextAlignment(TextAlignment.CENTER);
            Cell celda1 = new Cell();
            celda1.add(paragraphCell);
            table.addHeaderCell(celda1);
        }

        // Adding the table to the document
        document.add(table);

        // Closing the document
        document.close();
    }
}
