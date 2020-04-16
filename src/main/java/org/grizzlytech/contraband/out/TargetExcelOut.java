package org.grizzlytech.contraband.out;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TargetExcelOut extends TargetStdOut {

    private static final String FILE_PATH = "N:";
    private static final String FILE_NAME = "Library-";
    private static final String FILE_EXT = ".xlsx";

    private File outputFile;
    private Workbook wb;
    private Sheet sheet;
    private int rowNum = 0;

    public TargetExcelOut(JSONObject config) {
        open(config);
    }

    @Override
    public void open(JSONObject config) {
        super.open(config);

        String absolutePath = FILE_PATH + File.separator + FILE_NAME + now() + FILE_EXT;
        this.outputFile = new File(absolutePath);
        System.out.println("Output file will be: " + this.outputFile.getAbsolutePath());

        System.out.println("Open");
        this.wb = new XSSFWorkbook();

        CreationHelper createHelper = wb.getCreationHelper();

        sheet = wb.createSheet("new sheet");

        writeHeader();
    }

    protected void writeHeader() {
        if (sheet==null) throw new IllegalStateException("sheet not set");

        // Create a row and put some cells in it. Rows are 0 based.
        Row row = sheet.createRow(rowNum++);

        for (int cellNum = 0; cellNum < outputProperties.size(); cellNum++) {
            Cell cell = row.createCell(cellNum);
            cell.setCellValue(outputProperties.get(cellNum));
        }
    }

    @Override
    public void write(JSONObject document) {
        if (sheet==null) throw new IllegalStateException("sheet not set");

        // Create a row and put some cells in it. Rows are 0 based.
        Row row = sheet.createRow(rowNum++);

        for (int cellNum = 0; cellNum < outputProperties.size(); cellNum++) {
            Cell cell = row.createCell(cellNum);
            cell.setCellValue(document.optString(outputProperties.get(cellNum)));
        }
    }

    @Override
    public void close() throws Exception {
        try (OutputStream fileOut = new FileOutputStream(this.outputFile)) {
            wb.write(fileOut);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static String now()
    {
        LocalDateTime ldt = LocalDateTime.now();
        DateTimeFormatter pattern = DateTimeFormatter
                .ofPattern("yyyyMMdd-HHmmss");
        return pattern.format(ldt);
    }
}
