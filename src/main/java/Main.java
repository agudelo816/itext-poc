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
    

    public static void main(String[] args) throws Exception {
        String dest = "table_example.pdf";
        List<String> columnNames = Arrays.asList("Column 1", "Column 2", "Column 3");
        float[] columnWidths = new float[columnNames.size()];
        Arrays.fill(columnWidths, 25);

        PdfWriter writer = createPdfWriter(dest);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        com.itextpdf.kernel.font.PdfFont font = com.itextpdf.kernel.font.PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);
        Table table = createTable(columnWidths);
        addHeaderCells(table, columnNames, font);

        document.add(table);
        document.close();
    }

    private static PdfWriter createPdfWriter(String dest) throws Exception {
        return new PdfWriter(dest);
    }

    private static Table createTable(float[] columnWidths) {
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));
        return table;
    }

    private static void addHeaderCells(Table table, List<String> columnNames, com.itextpdf.kernel.font.PdfFont font) {
        for (String columnName : columnNames) {
            Paragraph paragraphCell = new Paragraph(columnName);
            paragraphCell.setFont(font);
            paragraphCell.setFontSize(10);
            paragraphCell.setTextAlignment(TextAlignment.CENTER);
            Cell cell = new Cell();
            cell.add(paragraphCell);
            table.addHeaderCell(cell);
        }
    }
}
