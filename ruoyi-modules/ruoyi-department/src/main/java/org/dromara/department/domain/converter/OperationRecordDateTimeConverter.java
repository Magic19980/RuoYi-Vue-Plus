package org.dromara.department.domain.converter;

import org.apache.fesod.sheet.converters.Converter;
import org.apache.fesod.sheet.enums.CellDataTypeEnum;
import org.apache.fesod.sheet.metadata.GlobalConfiguration;
import org.apache.fesod.sheet.metadata.data.ReadCellData;
import org.apache.fesod.sheet.metadata.data.WriteCellData;
import org.apache.fesod.sheet.metadata.property.ExcelContentProperty;
import org.apache.fesod.sheet.util.DateUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 运维工作记录日期时间兼容转换器。
 *
 * <p>兼容 Excel 数字日期、标准日期时间，以及历史台账中的点号日期、全角时间分隔符、
 * 仅日期和缺少日期/时间分隔符等格式。</p>
 */
public class OperationRecordDateTimeConverter implements Converter<LocalDateTime> {

    private static final DateTimeFormatter FLEXIBLE_FORMATTER = new DateTimeFormatterBuilder()
        .appendPattern("uuuu/M/d")
        .optionalStart()
        .appendPattern(" H:mm")
        .optionalStart()
        .appendPattern(":ss")
        .optionalEnd()
        .optionalEnd()
        .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
        .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
        .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
        .toFormatter(Locale.ROOT);

    private static final Pattern JOINED_DATE_TIME_PATTERN = Pattern.compile(
        "^(\\d{4}/\\d{1,2}/\\d{1,4}:\\d{2}(?::\\d{2})?)$");

    @Override
    public Class<?> supportJavaTypeKey() {
        return LocalDateTime.class;
    }

    /**
     * 字段使用注解转换器时由 Fesod 直接调用，需要覆盖所有可能的单元格类型。
     */
    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return null;
    }

    @Override
    public LocalDateTime convertToJavaData(ReadCellData<?> cellData,
                                           ExcelContentProperty contentProperty,
                                           GlobalConfiguration globalConfiguration) {
        if (cellData == null || cellData.getType() == CellDataTypeEnum.EMPTY) {
            return null;
        }
        if (cellData.getType() == CellDataTypeEnum.NUMBER || cellData.getType() == CellDataTypeEnum.DATE) {
            BigDecimal numberValue = cellData.getNumberValue();
            if (numberValue != null) {
                boolean use1904Windowing = globalConfiguration != null
                    && Boolean.TRUE.equals(globalConfiguration.getUse1904windowing());
                return DateUtils.getLocalDateTime(numberValue.doubleValue(), use1904Windowing);
            }
        }

        String value = cellData.getStringValue();
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = normalize(value);
        try {
            return LocalDateTime.parse(normalized, FLEXIBLE_FORMATTER);
        } catch (DateTimeParseException exception) {
            String splitJoinedValue = splitJoinedDateTime(normalized);
            if (splitJoinedValue != null) {
                try {
                    return LocalDateTime.parse(splitJoinedValue, FLEXIBLE_FORMATTER);
                } catch (DateTimeParseException ignored) {
                    // 继续抛出包含原始单元格值的统一错误。
                }
            }
            throw new IllegalArgumentException("无法解析日期时间：" + value
                + "，支持 yyyy-MM-dd HH:mm:ss、yyyy/M/d H:mm 和 yyyy.M.d 格式", exception);
        }
    }

    @Override
    public WriteCellData<?> convertToExcelData(LocalDateTime value,
                                               ExcelContentProperty contentProperty,
                                               GlobalConfiguration globalConfiguration) {
        return value == null ? new WriteCellData<>("") : new WriteCellData<>(value);
    }

    private String normalize(String value) {
        String normalized = value.trim()
            .replace('\u00A0', ' ')
            .replace('\u3000', ' ')
            .replace('：', ':')
            .replace('；', ':')
            .replace('／', '/')
            .replace('年', '/')
            .replace('月', '/')
            .replace("日", "")
            .replace('T', ' ')
            .replaceAll("\\s+", " ");

        int separatorIndex = normalized.indexOf(' ');
        String datePart = separatorIndex < 0 ? normalized : normalized.substring(0, separatorIndex);
        String timePart = separatorIndex < 0 ? "" : normalized.substring(separatorIndex + 1);
        datePart = datePart.replace('.', '/').replace('-', '/');
        timePart = timePart.replace('.', ':');

        // 历史台账中存在 2026/5/1613:30:00、2025.5.611:16 这种日期与时间黏连的写法，
        // 这里保留原始黏连串，在解析阶段按有效日期拆分，避免把 6 日误判为 61 日。
        if (timePart.isEmpty() && JOINED_DATE_TIME_PATTERN.matcher(datePart).matches()) {
            return datePart;
        }
        return timePart.isEmpty() ? datePart : datePart + " " + timePart;
    }

    /**
     * 尝试拆分日期和时间黏连值。日期日部分按 1 位和 2 位依次尝试，并由格式化器校验真实日期。
     */
    private String splitJoinedDateTime(String value) {
        if (!JOINED_DATE_TIME_PATTERN.matcher(value).matches()) {
            return null;
        }
        int datePrefixEnd = value.indexOf('/', value.indexOf('/') + 1) + 1;
        String datePrefix = value.substring(0, datePrefixEnd);
        String dayAndTime = value.substring(datePrefixEnd);
        for (int dayLength = 1; dayLength <= 2 && dayLength < dayAndTime.length(); dayLength++) {
            String day = dayAndTime.substring(0, dayLength);
            String time = dayAndTime.substring(dayLength);
            String candidate = datePrefix + day + " " + time;
            try {
                LocalDateTime.parse(candidate, FLEXIBLE_FORMATTER);
                return candidate;
            } catch (DateTimeParseException ignored) {
                // 尝试另一种日期日长度。
            }
        }
        return null;
    }
}
