package top.zenyoung.boot.util;

import cn.afterturn.easypoi.entity.vo.TemplateExcelConstants;
import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.TemplateExportParams;
import cn.afterturn.easypoi.handler.inter.IExcelModel;
import cn.afterturn.easypoi.view.PoiBaseView;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.multipart.MultipartFile;
import top.zenyoung.boot.exception.ServiceException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Excel 工具类
 *
 * @author young
 */
@Slf4j
public class ExcelUtils {
    private static final String EXCEL_EXT = "xlsx";

    private static <T> Map<String, Object> dataHandler(@Nonnull final List<T> items,
                                                       @Nonnull final Function<Optional<T>, Map<String, Object>> convert) {
        final Map<String, Object> map = Maps.newHashMap();
        final List<Map<String, Object>> mapList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(items)) {
            mapList.add(convert.apply(Optional.empty()));
        } else {
            items.stream()
                    .filter(Objects::nonNull)
                    .forEach(item -> mapList.add(convert.apply(Optional.of(item))));
        }
        map.put("mapList", mapList);
        return map;
    }

    /**
     * Excel导出处理
     *
     * @param exportTemplate 导出模板名称
     * @param exportFilename 导出文件名称
     * @param items          导出数据集合
     * @param convertHandler 导出数据处理
     * @param <T>            导出数据类型
     */
    public static <T> void export(@Nonnull final String exportTemplate, @Nonnull final String exportFilename,
                                  @Nonnull final List<T> items,
                                  @Nonnull final Function<Optional<T>, Map<String, Object>> convertHandler) {
        Assert.hasText(exportTemplate, "'exportTemplate'不能为空");
        HttpUtils.servlet((req, res) -> {
            //导出模板
            final TemplateExportParams params = new TemplateExportParams(exportTemplate);
            //导出数据处理
            final Map<String, Object> map = dataHandler(items, convertHandler);
            //poi导出
            final ModelMap modelMap = new ModelMap();
            modelMap.put(TemplateExcelConstants.FILE_NAME, exportFilename);
            modelMap.put(TemplateExcelConstants.PARAMS, params);
            modelMap.put(TemplateExcelConstants.MAP_DATA, map);
            //导出处理
            PoiBaseView.render(modelMap, req, res, TemplateExcelConstants.EASYPOI_TEMPLATE_EXCEL_VIEW);
        });
    }

    private static XSSFWorkbook getDefaultWb() {
        return new XSSFWorkbook();
    }

    private static XSSFSheet getDefaultSheet(@Nonnull final XSSFWorkbook wb) {
        final XSSFSheet sheet = wb.createSheet("sheet1");
        sheet.setDefaultRowHeight((short) (2 * 256));
        sheet.setDefaultColumnWidth(15);
        //设置默认样式
        setDefaultStyle(wb, sheet);
        //
        return sheet;
    }

    private static void setDefaultStyle(@Nonnull final XSSFWorkbook wb, @Nonnull final XSSFSheet sheet) {
        final CellStyle style = wb.createCellStyle();
        final XSSFDataFormat dataFormat = wb.createDataFormat();
        style.setDataFormat(dataFormat.getFormat("@"));
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(HorizontalAlignment.CENTER);
        final org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCol col = sheet.getCTWorksheet().getColsArray(0).addNewCol();
        col.setMin(1);
        col.setMax(16384);
        col.setWidth(15);
        col.setStyle(style.getIndex());
    }

    /**
     * 创建行
     *
     * @param sheet Sheet
     * @param row   行号
     * @return 行对象
     */
    public static Row createRow(@Nonnull final Sheet sheet, final int row) {
        return sheet.createRow(row);
    }

    /**
     * 添加单元格
     *
     * @param wb              WorkBook
     * @param row             Row
     * @param col             col
     * @param val             单元格数据
     * @param fontColor       字体颜色
     * @param foregroundColor 前背景色
     * @param bold            字体是否加粗
     */
    public static void addCell(@Nonnull final Workbook wb, @Nonnull final Row row, final int col, @Nonnull final String val,
                               @Nullable final IndexedColors fontColor, @Nullable final IndexedColors foregroundColor, final boolean bold) {
        //创建单元格
        final Cell cell = row.createCell(col);
        cell.setCellValue(val);
        //单元格样式
        final CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        //字体
        final Font font = wb.createFont();
        //字体颜色
        Optional.ofNullable(fontColor)
                .map(IndexedColors::getIndex)
                .ifPresent(font::setColor);
        font.setBold(bold);
        cellStyle.setFont(font);
        //背景色
        Optional.ofNullable(foregroundColor)
                .map(IndexedColors::getIndex)
                .ifPresent(fc -> {
                    cellStyle.setFillForegroundColor(fc);
                    cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                });
        //添加单元格样式
        cell.setCellStyle(cellStyle);
    }

    /**
     * 添加数据有效性检查.
     *
     * @param sheet              要添加此检查的Sheet
     * @param firstRow           开始行
     * @param lastRow            结束行
     * @param firstCol           开始列
     * @param lastCol            结束列
     * @param explicitListValues 有效性检查的下拉列表
     * @throws IllegalArgumentException 如果传入的行或者列小于0(< 0)或者结束行/列比开始行/列小
     */
    public static void setValidationData(@Nonnull final Sheet sheet, final int firstRow, final int lastRow,
                                         final int firstCol, final int lastCol, final String[] explicitListValues) throws IllegalArgumentException {
        if (firstRow < 0 || lastRow < 0 || firstCol < 0 || lastCol < 0 || lastRow < firstRow || lastCol < firstCol) {
            throw new IllegalArgumentException("Wrong Row or Column index : " + firstRow + ":" + lastRow + ":" + firstCol + ":" + lastCol);
        }
        //
        final Consumer<Function<DataValidationHelper, DataValidationConstraint>> buildValidation = handler -> {
            final DataValidationHelper helper = new XSSFDataValidationHelper((XSSFSheet) sheet);
            final DataValidationConstraint constraint = handler.apply(helper);
            final CellRangeAddressList regions = new CellRangeAddressList(firstRow, lastRow, firstCol, lastCol);
            final DataValidation validation = helper.createValidation(constraint, regions);
            validation.setSuppressDropDownArrow(true);
            validation.setShowErrorBox(true);
            validation.setEmptyCellAllowed(false);
            sheet.addValidationData(validation);
        };
        //
        final int maxLen = 255;
        //检查叠加长度,隐藏Sheet方式处理
        if (Joiner.on(",").join(explicitListValues).length() >= maxLen) {
            final Workbook wb = sheet.getWorkbook();
            final int sheetTotal = wb.getNumberOfSheets();
            final String hiddenSheetName = "hiddenSheet" + sheetTotal;
            final Sheet hiddenSheet = wb.createSheet(hiddenSheetName);
            Row row;
            for (int i = 0; i < explicitListValues.length; i++) {
                row = createRow(hiddenSheet, i);
                final Cell cell = row.createCell(0);
                cell.setCellValue(explicitListValues[i]);
            }
            final String strFormula = hiddenSheetName + "!$A1:$A65535";
            buildValidation.accept(helper -> helper.createFormulaListConstraint(strFormula));
            wb.setSheetHidden(sheetTotal, true);
            return;
        }
        //少量数据下拉校验处理
        buildValidation.accept(helper -> helper.createExplicitListConstraint(explicitListValues));
    }

    /**
     * 创建导入模板
     *
     * @param renderTemplate 绘制模板处理
     */
    public static void createImportTemplate(@Nonnull final String templateName, @Nonnull final BiConsumer<Workbook, Sheet> renderTemplate) {
        HttpUtils.servlet((req, res) -> {
            final boolean isIe = HttpUtils.isIE(req);
            final XSSFWorkbook wb = getDefaultWb();
            final XSSFSheet sheet = getDefaultSheet(wb);
            renderTemplate.accept(wb, sheet);
            //文件名
            final String filename = EXCEL_EXT.equalsIgnoreCase(FilenameUtils.getExtension(templateName)) ? templateName : templateName + "." + EXCEL_EXT;
            final String utf8 = "UTF-8", iso88591 = "ISO-8859-1";
            try (final OutputStream output = res.getOutputStream()) {
                final String outFileName = isIe ? URLEncoder.encode(filename, utf8) : new String(filename.getBytes(utf8), iso88591);
                res.setHeader("content-disposition", "attachment;filename=" + outFileName);
                wb.write(output);
                output.flush();
            } catch (Throwable e) {
                throw new ServiceException(e.getMessage());
            }
        });
    }

    /**
     * 解析Excel
     *
     * @param file      Excel文件
     * @param titleRows 标题行数量
     * @param cls       数据类型
     * @param handler   解析结果处理
     * @param <T>       解析数据类型
     */
    public static <T, R extends IExcelModel> List<R> importParse(@Nonnull final MultipartFile file,
                                                                 final int titleRows, @Nonnull final Class<T> cls,
                                                                 @Nonnull final Function<List<T>, List<R>> handler) {
        if (file.isEmpty()) {
            throw new ServiceException("上传文件不能为空");
        }
        final String fileName;
        if (!Strings.isNullOrEmpty(fileName = file.getOriginalFilename()) && !EXCEL_EXT.equalsIgnoreCase(FilenameUtils.getExtension(fileName))) {
            throw new ServiceException("请导入正确的模板文件");
        }
        try {
            final ImportParams params = new ImportParams();
            params.setTitleRows(titleRows);
            params.setNeedCheckOrder(true);
            //数据字段反射
            final List<Field> fields = Lists.newArrayList();
            ReflectionUtils.doWithFields(cls, field -> {
                final Class<?> type = field.getType();
                if (type == String.class || type == Number.class || type == Date.class || type == Boolean.class) {
                    field.setAccessible(true);
                    fields.add(field);
                }
            });
            //解析Excel
            final List<T> rows = removeBlankRowHandler(ExcelImportUtil.importExcel(file.getInputStream(), cls, params), fields);
            //检查数据
            if (CollectionUtils.isEmpty(rows)) {
                throw new ServiceException("没有导入数据或模板不正确!");
            }
            //有数据处理
            return handler.apply(rows);
        } catch (ServiceException e) {
            throw e;
        } catch (Throwable e) {
            log.error("importParse(file: {},titleRows: {},cls: {})-exp: {}", file.getName(), titleRows, cls, e.getMessage());
            throw new ServiceException(e.getMessage());
        }
    }

    private static <T> List<T> removeBlankRowHandler(@Nullable final List<T> rows, @Nonnull final List<Field> fields) {
        if (CollectionUtils.isEmpty(rows) || CollectionUtils.isEmpty(fields)) {
            return rows;
        }
        return rows.stream()
                .filter(row -> {
                    for (final Field field : fields) {
                        try {
                            final Object val = field.get(row);
                            if (Objects.nonNull(val) && !Strings.isNullOrEmpty(val.toString())) {
                                return true;
                            }
                        } catch (Throwable e) {
                            log.warn("removeBlankRowHandler(row: {})-exp: {}", row, e.getMessage());
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());
    }

}
