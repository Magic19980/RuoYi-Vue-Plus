package org.dromara.department.service.impl;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.department.domain.vo.ScoreProposalVo;
import org.dromara.system.domain.vo.SysOssVo;
import org.dromara.system.service.ISysDictDataService;
import org.dromara.system.service.ISysOssService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Date;

/** 根据SCORE模板生成XLSX。 */
@Service
@RequiredArgsConstructor
public class ScoreProposalXlsxService {

    private final ISysOssService ossService;
    private final ISysDictDataService dictDataService;

    @Value("${department.score-proposal.template-path:classpath:templates/department/score-proposal-template.xlsx}")
    private String templatePath;

    public void export(ScoreProposalVo record, HttpServletResponse response) throws IOException {
        if (record == null) throw new ServiceException("SCORE提案不存在");
        try (XSSFWorkbook workbook = openTemplate()) {
            XSSFSheet sheet = workbook.getSheet("EIP企业改进提案表");
            if (sheet == null) throw new ServiceException("SCORE模板缺少EIP企业改进提案表工作表");
            fillSheet(workbook, sheet, record);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            workbook.write(output);
            byte[] bytes = output.toByteArray();
            String filename = "SCORE提案-" + text(record.getProposerName(), "未命名") + "-" + dateText(record.getStartDate()) + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''"
                + URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20"));
            response.setContentLength(bytes.length);
            response.getOutputStream().write(bytes);
        }
    }

    private XSSFWorkbook openTemplate() throws IOException {
        Resource resource = new DefaultResourceLoader().getResource(templatePath);
        if (!resource.exists()) throw new ServiceException("SCORE模板不存在：" + templatePath);
        return new XSSFWorkbook(resource.getInputStream());
    }

    private void fillSheet(XSSFWorkbook workbook, XSSFSheet sheet, ScoreProposalVo record) throws IOException {
        setText(sheet, 2, 5, text(record.getCompanyName(), "PT. Tsingyao Electric Indonesia\n印尼青耀电气有限公司"));
        setText(sheet, 3, 5, text(record.getTeamMembers(), ""));

        setText(sheet, 6, 0, "1");
        setText(sheet, 6, 1, proposerLevelText(record.getProposerLevel()));
        setText(sheet, 6, 2, text(record.getMainCategory(), ""));
        setText(sheet, 6, 3, text(record.getSubCategory(), ""));
        setText(sheet, 6, 4, text(record.getProblemDescription(), ""));
        setText(sheet, 6, 5, text(record.getImprovementMeasure(), ""));
        setText(sheet, 6, 6, joinNameRole(record));
        setText(sheet, 6, 7, text(record.getEmployeeNo(), ""));
        setText(sheet, 6, 8, text(record.getDeptName(), ""));
        setText(sheet, 6, 9, text(record.getImplementerSupervisor(), ""));
        setImage(workbook, sheet, record.getBeforeOssId(), 10, 6, "score-before");
        setImage(workbook, sheet, record.getAfterOssId(), 11, 6, "score-after");
        setDate(sheet, 6, 12, record.getStartDate());
        setDate(sheet, 6, 13, record.getPlannedCompletionDate());
        setDate(sheet, 6, 14, record.getActualCompletionDate());
        setText(sheet, 6, 15, text(record.getCompletionStatus(), "进行中"));
        setText(sheet, 6, 16, text(record.getRemark(), ""));
        // 按业务要求不收集固化清单和推广清单，模板对应列保持空白。
        setText(sheet, 6, 17, "");
        setText(sheet, 6, 18, "");

        String summary = "本周提出改进项：1项，完成改进项：" + (record.getActualCompletionDate() == null ? "0" : "1")
            + "项，完成率" + (record.getActualCompletionDate() == null ? "0%" : "100%");
        setText(sheet, 7, 4, summary);
    }

    private void setImage(XSSFWorkbook workbook, XSSFSheet sheet, Long ossId, int col, int row, String name) throws IOException {
        setText(sheet, row, col, "");
        if (ossId == null) return;
        try {
            ResponseEntity<byte[]> response = ossService.download(ossId);
            byte[] bytes = response == null ? null : response.getBody();
            if (bytes == null || bytes.length == 0) return;
            int pictureType = pictureType(ossService.getById(ossId));
            int pictureId = workbook.addPicture(bytes, pictureType);
            XSSFDrawing drawing = sheet.createDrawingPatriarch();
            XSSFClientAnchor anchor = new XSSFClientAnchor(0, 0, 0, 0, col, row, col + 1, row + 1);
            XSSFPicture picture = drawing.createPicture(anchor, pictureId);
            picture.resize(1.0, 1.0);
        } catch (Exception ignored) {
            // 图片为可选内容，图片服务异常时保留文字表格输出。
        }
    }

    private int pictureType(SysOssVo oss) {
        String suffix = oss == null ? "" : text(oss.getFileSuffix(), "").toLowerCase();
        if (suffix.endsWith("jpg") || suffix.endsWith("jpeg")) return Workbook.PICTURE_TYPE_JPEG;
        // POI 的 XSSF 图片常量不包含 GIF，GIF按 PNG 图片类型写入表格。
        if (suffix.endsWith("bmp")) return Workbook.PICTURE_TYPE_DIB;
        return Workbook.PICTURE_TYPE_PNG;
    }

    private void setDate(XSSFSheet sheet, int row, int col, LocalDate value) {
        Cell cell = sheet.getRow(row).getCell(col, org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        if (value == null) cell.setBlank();
        else cell.setCellValue(Date.from(value.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
    }

    private void setText(XSSFSheet sheet, int row, int col, String value) {
        Cell cell = sheet.getRow(row).getCell(col, org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        cell.setCellValue(value == null ? "" : value);
    }

    private String joinNameRole(ScoreProposalVo record) {
        String name = text(record.getProposerName(), "");
        String role = text(record.getProposerRole(), "");
        return role.isEmpty() ? name : name + "\n" + role;
    }

    private String dateText(LocalDate value) {
        return value == null ? LocalDate.now().toString() : value.toString();
    }

    private String proposerLevelText(String value) {
        String label = StringUtils.isBlank(value) ? "" : dictDataService.selectDictLabel("dm_score_job", value);
        return text(label, text(value, "基层员工"));
    }

    private String text(String value, String fallback) {
        return StringUtils.isBlank(value) ? fallback : value.trim();
    }
}
