package korastudy.be.service;

import korastudy.be.dto.request.quiz.OptionCreateRequest;
import korastudy.be.dto.request.quiz.QuestionCreateRequest;
import korastudy.be.entity.Enum.QuestionType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class ExcelImportService {

    public List<QuestionCreateRequest> parseExcelFile(MultipartFile file) throws IOException {
        List<QuestionCreateRequest> questions = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = sheet.iterator();

            // Skip header row
            if (iterator.hasNext()) {
                iterator.next();
            }

            while (iterator.hasNext()) {
                Row row = iterator.next();
                if (isRowEmpty(row)) continue;

                try {
                    questions.add(parseRow(row));
                } catch (Exception e) {
                    // Log error or throw specific exception for this row
                    throw new RuntimeException("Lỗi tại dòng " + (row.getRowNum() + 1) + ": " + e.getMessage());
                }
            }
        }

        return questions;
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK)
                return false;
        }
        return true;
    }

    private QuestionCreateRequest parseRow(Row row) {
        QuestionCreateRequest.QuestionCreateRequestBuilder builder = QuestionCreateRequest.builder();

        // Column 0: Question Text
        String questionText = getCellValueAsString(row.getCell(0));
        if (questionText == null || questionText.trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung câu hỏi không được để trống");
        }
        builder.questionText(questionText);

        // Column 1: Question Type
        String typeStr = getCellValueAsString(row.getCell(1));
        if (typeStr == null || typeStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Loại câu hỏi không được để trống");
        }
        QuestionType type;
        try {
            type = QuestionType.valueOf(typeStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
             throw new IllegalArgumentException("Loại câu hỏi không hợp lệ: " + typeStr);
        }
        builder.questionType(type);

        // Column 2: Score
        Double score = getCellValueAsNumeric(row.getCell(2));
        builder.score(score != null ? score : 1.0);

        // Column 3: Order
        Double orderVal = getCellValueAsNumeric(row.getCell(3));
        builder.orderIndex(orderVal != null ? orderVal.intValue() : 1);

        // Column 4: Explanation
        builder.explanation(getCellValueAsString(row.getCell(4)));

        // Parsing Options (from Column 5 onwards)
        // Structure: [Answer 1] [Is Correct 1] [Answer 2] [Is Correct 2] ...
        List<OptionCreateRequest> options = new ArrayList<>();
        int cellIndex = 5;
        
        // Assume max 10 options to avoid infinite loops if something is wrong
        for (int i = 0; i < 10; i++) {
            Cell answerCell = row.getCell(cellIndex);
            Cell correctCell = row.getCell(cellIndex + 1);

            String answerText = getCellValueAsString(answerCell);
            
            if (answerText != null && !answerText.trim().isEmpty()) {
                boolean isCorrect = false;
                
                // Handle various boolean formats
                if (correctCell != null) {
                    if (correctCell.getCellType() == CellType.BOOLEAN) {
                        isCorrect = correctCell.getBooleanCellValue();
                    } else {
                        String correctStr = getCellValueAsString(correctCell); // Convert to string first
                        if (correctStr != null) {
                            String s = correctStr.trim().toLowerCase();
                            isCorrect = s.equals("true") || s.equals("1") || s.equals("yes") || s.equals("đúng");
                        }
                    }
                }

                options.add(OptionCreateRequest.builder()
                        .optionText(answerText)
                        .isCorrect(isCorrect)
                        .orderIndex(i + 1)
                        .build());
            }

            cellIndex += 2;
             // Stop if we run out of cells in the row
            if (cellIndex >= row.getLastCellNum()) break;
        }

        builder.options(options);
        
        return builder.build();
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                // Handle cases where numbers are entered as text
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                 try {
                     return cell.getStringCellValue();
                 } catch (Exception e) {
                      return String.valueOf(cell.getNumericCellValue());
                 }
            default:
                return null;
        }
    }

    private Double getCellValueAsNumeric(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return Double.parseDouble(cell.getStringCellValue());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
