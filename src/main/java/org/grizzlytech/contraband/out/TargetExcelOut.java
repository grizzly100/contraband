package org.grizzlytech.contraband.out;

import com.google.common.flogger.FluentLogger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.grizzlytech.contraband.Configuration;
import org.json.JSONObject;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Target which write to EXCEL .xlsx using Apache POI
 */
public class TargetExcelOut extends TargetStdOut {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

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
    public void open(JSONObject jsonConfig) {
        super.open(jsonConfig);

        // Build output filename
        File targetDir = Configuration.getDir(jsonConfig, "targetDir");
        String absolutePath = targetDir.getAbsolutePath() + File.separator + FILE_NAME + now() + FILE_EXT;
        this.outputFile = new File(absolutePath);
        logger.atInfo().log("Output file will be: %s", this.outputFile.getAbsolutePath());

        // Open Excel workbook
        this.wb = new XSSFWorkbook();

        CreationHelper createHelper = wb.getCreationHelper();

        sheet = wb.createSheet("Recordings");

        writeHeader();
    }

    protected void writeHeader() {
        if (sheet == null) throw new IllegalStateException("sheet not set");

        // Create a row and put some cells in it. Rows are 0 based.
        Row row = sheet.createRow(rowNum++);

        for (int cellNum = 0; cellNum < outputProperties.size(); cellNum++) {
            Cell cell = row.createCell(cellNum);
            cell.setCellValue(outputProperties.get(cellNum));
        }
    }

    @Override
    public void write(JSONObject document) {
        super.write(document); // write to STDOUT to help observe progress

        if (sheet == null) throw new IllegalStateException("sheet not set");

        // Create a row and put some cells in it. Rows are 0 based.
        Row row = sheet.createRow(rowNum++);

        for (int cellNum = 0; cellNum < outputProperties.size(); cellNum++) {
            Cell cell = row.createCell(cellNum);
            cell.setCellValue(document.optString(outputProperties.get(cellNum)));
        }
    }

    @Override
    public void close() {
        try (OutputStream fileOut = new FileOutputStream(this.outputFile)) {
            wb.write(fileOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static String now() {
        LocalDateTime ldt = LocalDateTime.now();
        DateTimeFormatter pattern = DateTimeFormatter
                .ofPattern("yyyyMMdd-HHmmss");
        return pattern.format(ldt);
    }
}
