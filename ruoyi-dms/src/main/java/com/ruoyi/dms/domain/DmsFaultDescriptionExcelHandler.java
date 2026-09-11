package com.ruoyi.dms.domain;

import com.ruoyi.common.utils.poi.ExcelHandlerAdapter;
import com.ruoyi.common.utils.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 工单故障描述导出格式化处理器
 *
 * 将 PM 计划生成的工单描述中的任务清单原始 JSON
 * （如：任务清单：[{"name":"检查主轴润滑系统","duration":30}, ...]）
 * 渲染为可读的编号清单，改善导出 Excel 的展示效果。
 */
public class DmsFaultDescriptionExcelHandler implements ExcelHandlerAdapter
{
    /** 任务清单 JSON 块匹配 */
    private static final Pattern TASK_LIST_PATTERN = Pattern.compile("任务清单：\\[.*?](?=$|\\n)", Pattern.DOTALL);
    /** 单个任务项提取 */
    private static final Pattern TASK_ITEM_PATTERN = Pattern.compile("\\{[^}]*\"name\"\\s*:\\s*\"([^\"]+)\"[^}]*}");

    @Override
    public Object format(Object value, String[] args, Cell cell, Workbook wb)
    {
        if (value == null)
        {
            return "";
        }
        String text = value.toString();
        if (StringUtils.isEmpty(text) || !text.contains("任务清单：["))
        {
            return text;
        }

        Matcher block = TASK_LIST_PATTERN.matcher(text);
        if (!block.find())
        {
            return text;
        }

        // 将 JSON 任务清单渲染为「任务清单：①xxx；②xxx」编号清单
        StringBuilder listText = new StringBuilder("任务清单：");
        Matcher item = TASK_ITEM_PATTERN.matcher(block.group());
        int index = 1;
        while (item.find())
        {
            if (index > 1)
            {
                listText.append("；");
            }
            listText.append("第").append(index).append("项 ").append(item.group(1));
            index++;
        }

        return TASK_LIST_PATTERN.matcher(text).replaceFirst(Matcher.quoteReplacement(listText.toString()));
    }
}
